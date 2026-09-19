package de.rpg.game.controller;

import de.rpg.game.model.Spieler;
import de.rpg.game.dto.SpielerErstellenDto;
import de.rpg.game.service.SpielerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spieler")
public class SpielerController {

    private final SpielerService spielerService;

    public SpielerController(SpielerService spielerService) {
        this.spielerService = spielerService;
    }

    @PostMapping
    public ResponseEntity<Spieler> erstellenSpieler(@Valid @RequestBody SpielerErstellenDto dto) {
        Spieler neuerSpieler = spielerService.erstellenSpieler(dto);
        return new ResponseEntity<>(neuerSpieler, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Spieler>> alleSpielerHolen() {
        List<Spieler> spielerListe = spielerService.alleSpielerHolen();
        return ResponseEntity.ok(spielerListe);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Spieler> spielerHolenNachId(@PathVariable Long id) {
        Spieler spieler = spielerService.spielerHolenNachId(id);
        return ResponseEntity.ok(spieler);
    }

    @PostMapping("/{id}/level-up")
    public ResponseEntity<Spieler> levelUp(@PathVariable Long id) {
        Spieler aktualisierterSpieler = spielerService.levelUp(id);
        return ResponseEntity.ok(aktualisierterSpieler);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Spieler> updateSpieler(@PathVariable Long id, @RequestBody Spieler spielerDetails) {
        Spieler updatedSpieler = spielerService.updateSpieler(id, spielerDetails);
        return ResponseEntity.ok(updatedSpieler);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSpieler(@PathVariable Long id) {
        spielerService.deleteSpieler(id);
        return ResponseEntity.ok("Spieler mit ID " + id + " wurde erfolgreich gelöscht.");
    }

    @PostMapping("/{spielerId}/inventar/{gegenstandId}")
    public ResponseEntity<Spieler> gegenstandHinzufügen(
            @PathVariable Long spielerId,
            @PathVariable Long gegenstandId) {

        Spieler aktualisierterSpieler = spielerService.gegenstandHinzufügen(spielerId, gegenstandId);
        return ResponseEntity.ok(aktualisierterSpieler);
    }

    @PostMapping("/{spielerId}/trank/{gegenstandId}")
    public ResponseEntity<Spieler> trankBenutzen(
            @PathVariable Long spielerId,
            @PathVariable Long gegenstandId) {
        Spieler aktualisierterSpieler = spielerService.trankBenutzen(spielerId, gegenstandId);
        return ResponseEntity.ok(aktualisierterSpieler);
    }
}