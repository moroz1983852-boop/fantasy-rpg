package de.rpg.game.service;

import de.rpg.game.model.Monster;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.MonsterRepository;
import de.rpg.game.repository.SpielerRepository;
import org.springframework.stereotype.Service;

@Service
public class KampfService {

    private final SpielerRepository spielerRepository;
    private final MonsterRepository monsterRepository;

    public KampfService(
            SpielerRepository spielerRepository,
            MonsterRepository monsterRepository) {
        this.spielerRepository = spielerRepository;
        this.monsterRepository = monsterRepository;
    }

    public String angreifen(Long spielerId, Long monsterId) {
        Spieler spieler = spielerRepository.findById(spielerId)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new RuntimeException("Monster nicht gefunden"));

        monster.setGesundheit(monster.getGesundheit() - spieler.getStaerke());

        if (monster.getGesundheit() <= 0) {
            monsterRepository.delete(monster);
            spieler.setErfahrungspunkte(spieler.getErfahrungspunkte() + monster.getErfahrungspunkteBelohnung());
            spielerRepository.save(spieler);
            return "Sieg! Monster " + monster.getName() + " wurde besiegt! Spieler erhält "
                    + monster.getErfahrungspunkteBelohnung() + " XP.";
        }

        monsterRepository.save(monster);
        return "Angriff erfolgreich! Monster " + monster.getName() + " hat noch "
                + monster.getGesundheit() + " HP.";
    }
}
