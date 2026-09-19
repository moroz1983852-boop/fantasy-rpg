package de.rpg.game.service;

import de.rpg.game.model.Gegenstand;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.GegenstandRepository;
import de.rpg.game.repository.SpielerRepository;
import org.springframework.stereotype.Service;
import de.rpg.game.dto.SpielerErstellenDto;

import java.util.List;

@Service
public class SpielerService {

    private final SpielerRepository spielerRepository;
    private final GegenstandRepository gegenstandRepository;

    public SpielerService(SpielerRepository spielerRepository, GegenstandRepository gegenstandRepository) {
        this.spielerRepository = spielerRepository;
        this.gegenstandRepository = gegenstandRepository;
    }

    public Spieler erstellenSpieler(SpielerErstellenDto dto) {
        if (spielerRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Ein Spieler mit diesem Namen existiert bereits!");
        }

        Spieler neuerSpieler = new Spieler();
        neuerSpieler.setName(dto.getName());
        neuerSpieler.setKlasse(dto.getKlasse());

        return spielerRepository.save(neuerSpieler);
    }

    public List<Spieler> alleSpielerHolen() {
        return spielerRepository.findAll();
    }

    public Spieler spielerHolenNachId(Long id) {
        return spielerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spieler mit ID " + id + " wurde nicht gefunden!"));
    }

    public Spieler levelUp(Long id) {
        Spieler spieler = spielerHolenNachId(id);

        spieler.setLevel(spieler.getLevel() + 1);
        spieler.setGesundheit(spieler.getGesundheit() + 20);
        spieler.setStaerke(spieler.getStaerke() +  5);

        return spielerRepository.save(spieler);
    }

    public Spieler updateSpieler(Long id, Spieler spielerDetails) {
        Spieler spieler = spielerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        spieler.setName(spielerDetails.getName());
        spieler.setKlasse(spielerDetails.getKlasse());

        return spielerRepository.save(spieler);
    }

    public void deleteSpieler(Long id) {
        Spieler spieler = spielerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        spielerRepository.delete(spieler);
    }

    public Spieler gegenstandHinzufügen(Long spielerId, Long gegestandId) {
        Spieler spieler = spielerHolenNachId(spielerId);
        Gegenstand gegenstand = gegenstandRepository.findById(gegestandId)
                .orElseThrow(() -> new RuntimeException("Gegenstand nicht gefunden"));

        spieler.getInventar().add(gegenstand);

        if (gegenstand.getBonusStaerke() != null) {
            spieler.setStaerke(spieler.getStaerke() + gegenstand.getBonusStaerke());
        }
        if (gegenstand.getBonusGesundheit() != null) {
            spieler.setGesundheit(spieler.getGesundheit() + gegenstand.getBonusGesundheit());
        }

        return spielerRepository.save(spieler);
    }

    public Spieler trankBenutzen(Long spielerId, Long gegenstandId) {
        Spieler spieler = spielerRepository.findById(spielerId)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        Gegenstand trank = gegenstandRepository.findById(gegenstandId)
                .orElseThrow(() -> new RuntimeException("Gegenstand nicht gefunden"));

        if (!spieler.getInventar().contains(trank)) {
            throw new RuntimeException("Gegenstand ist nicht im Inventar");
        }

        if (!"Trank".equalsIgnoreCase(trank.getTyp())) {
            throw new RuntimeException("Dieser Gegenstand ist kein Trank");
        }

        int neuesGesundheit = spieler.getGesundheit() + trank.getBonusGesundheit();
        spieler.setGesundheit(neuesGesundheit);

        spieler.getInventar().remove(trank);

        return spielerRepository.save(spieler);
    }
}