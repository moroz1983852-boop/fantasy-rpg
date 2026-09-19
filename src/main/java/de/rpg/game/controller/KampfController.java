package de.rpg.game.controller;

import de.rpg.game.model.Monster;
import de.rpg.game.repository.MonsterRepository;
import de.rpg.game.service.KampfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kampf")
public class KampfController {

    private final KampfService kampfService;
    private final MonsterRepository monsterRepository;

    public KampfController(KampfService kampfService,  MonsterRepository monsterRepository) {
        this.kampfService = kampfService;
        this.monsterRepository = monsterRepository;
    }

    @PostMapping("/monster")
    public ResponseEntity<Monster> monsterErstellen(@RequestBody Monster monster) {
        Monster neuerMonster = monsterRepository.save(monster);
        return ResponseEntity.ok(neuerMonster);
    }

    @PostMapping("/angriff")
    public ResponseEntity<String> angreifen(@RequestParam Long spielerId, @RequestParam Long monsterId) {
        String ergebnis = kampfService.angreifen(spielerId, monsterId);
        return ResponseEntity.ok(ergebnis);
    }
}
