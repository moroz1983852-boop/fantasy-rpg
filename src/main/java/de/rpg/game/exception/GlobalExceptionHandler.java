package de.rpg.game.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RessourceNichtGefundenException.class)
    public ResponseEntity<Map<String, Object>> behandleNichtGefunden(
            RessourceNichtGefundenException ex, HttpServletRequest anfrage
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(baueKoerper(HttpStatus.NOT_FOUND, ex.getMessage(), anfrage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> behandleUngueltigeAnfrage(
            IllegalArgumentException ex, HttpServletRequest anfrage
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(baueKoerper(HttpStatus.BAD_REQUEST, ex.getMessage(), anfrage));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> behandleValidierungsfehler(
            MethodArgumentNotValidException ex, HttpServletRequest anfrage
    ) {
        Map<String, String> feldFehler = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fehler ->
                feldFehler.put(fehler.getField(), fehler.getDefaultMessage()));

        Map<String, Object> koerper =
                baueKoerper(HttpStatus.BAD_REQUEST, "Validierung fehlgeschlagen", anfrage);
        koerper.put("felder", feldFehler);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(koerper);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> behandleFehlerhafteParameter(
            Exception ex, HttpServletRequest anfrage
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(baueKoerper(HttpStatus.BAD_REQUEST,
                        "Ein Parameter fehlt oder hat ein falsches Format.", anfrage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> behandleAlleAnderenFehler(
            Exception ex, HttpServletRequest anfrage
    ) {
        if (ex instanceof ErrorResponse springFehler) {
            HttpStatus status = HttpStatus.valueOf(springFehler.getStatusCode().value());
            return ResponseEntity
                    .status(status)
                    .body(baueKoerper(status, status.getReasonPhrase(), anfrage));
        }

        log.error("Unerwarteter Fehler bei {}", anfrage.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baueKoerper(HttpStatus.INTERNAL_SERVER_ERROR, "Ein unerwarteter Fehler ist aufgetreren.", anfrage));
    }

    private Map<String, Object> baueKoerper(HttpStatus status, String nachricht, HttpServletRequest anfrage) {
        Map<String, Object> koerper = new HashMap<>();
        koerper.put("zeitstempel", LocalDateTime.now());
        koerper.put("status", status.value());
        koerper.put("fehler", status.getReasonPhrase());
        koerper.put("nachricht", nachricht);
        koerper.put("pfad", anfrage.getRequestURI());
        return koerper;
    }
}