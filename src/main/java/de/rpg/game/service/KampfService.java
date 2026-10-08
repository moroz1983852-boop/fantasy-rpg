package de.rpg.game.service;

import de.rpg.game.exception.RessourceNichtGefundenException;
import de.rpg.game.model.Monster;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.MonsterRepository;
import de.rpg.game.repository.SpielerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class KampfService {

    private SpielerRepository spielerRepository;
    private MonsterRepository monsterRepository;
    private SpielerService spielerService;

    public KampfService(
            SpielerRepository spielerRepository,
            MonsterRepository monsterRepository,
            SpielerService spielerService) {
        this.spielerRepository = spielerRepository;
        this.monsterRepository = monsterRepository;
        this.spielerService = spielerService;
    }

    public String angreifen(Long spielerId, Long monsterId) {
        Spieler spieler = spielerRepository.findById(spielerId)
                .orElseThrow(() -> new RessourceNichtGefundenException("Spieler", spielerId));
        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new RessourceNichtGefundenException("Monster", monsterId));

        if (spieler.getGesundheit() <= 0) {
            throw new IllegalArgumentException(
                    "Du bist zu schwach Kämpfen! Bitte heile dich zuerst mit einem Trank.");
        }

        StringBuilder kampfLog = new StringBuilder();

        int schadenAmMonster = nullSicher(spieler.getStaerke());
        monster.setGesundheit(nullSicher(monster.getGesundheit()) - schadenAmMonster);
        kampfLog.append(String.format("Du greifst %s  an und verursachst %d Schaden.",
                monster.getName(), schadenAmMonster));

        if (monster.getGesundheit() <= 0) {
            int belohnung = nullSicher(monster.getErfahrungspunkteBelohnung());
            monsterRepository.delete(monster);
            spielerService.xpHinzufuegen(spielerId, belohnung);
            kampfLog.append(String.format("Sieg! %s wurde besiegt! Du erhälst %d XP",
                    monster.getName(), belohnung));
            return kampfLog.toString();
        }
        monsterRepository.save(monster);
        kampfLog.append(String.format("%s hat noch %d HP.", monster.getName(), monster.getGesundheit()));

        int schadenAmSpieler = nullSicher(monster.getStaerke());
        spieler.setGesundheit(Math.max(0, spieler.getGesundheit() - schadenAmSpieler));
        kampfLog.append(String.format("%s greifst dich an und verursacht %d Schaden.",
                monster.getName(), schadenAmSpieler));
        spielerRepository.save(spieler);

        if (spieler.getGesundheit() <= 0) {
            kampfLog.append("Niederlage! Du wurdest im Kampf besiegt!");
        } else {
            kampfLog.append(String.format("Du hast noch %d HP.", spieler.getGesundheit()));
        }
        return kampfLog.toString();
    }

    private int nullSicher(Integer wert) {
        return Objects.requireNonNullElse(wert, 0);
    }
}
