package de.rpg.game.controller;

import de.rpg.game.model.Gegenstand;
import de.rpg.game.repository.GegenstandRepository;
import org.hibernate.generator.internal.GeneratedGeneration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gegenstaende")
public class GegenstandController {

    private final GegenstandRepository gegenstandRepository;

    public GegenstandController(GegenstandRepository gegenstandRepository) {
        this.gegenstandRepository = gegenstandRepository;
    }

    @PostMapping
    public ResponseEntity<Gegenstand> erstellenGegenstand(@RequestBody Gegenstand gegenstand) {
        Gegenstand neuerGegenstand = gegenstandRepository.save(gegenstand);
        return new ResponseEntity<>(neuerGegenstand, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Gegenstand>> alleGegenstaendeHolen() {
        List<Gegenstand> Liste = gegenstandRepository.findAll();
        return ResponseEntity.ok(Liste);
    }
}
