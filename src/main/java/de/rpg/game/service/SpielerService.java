package de.rpg.game.service;

import de.rpg.game.dto.SpielerErstellenDto;
import de.rpg.game.exception.RessourceNichtGefundenException;
import de.rpg.game.model.Gegenstand;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.GegenstandRepository;
import de.rpg.game.repository.SpielerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class SpielerService {

    private static final int MAX_INVENTAR_GROESSE = 5;
    private static final int XP_PRO_LEVEL = 100;
    private static final int STAERKE_PRO_LEVEL = 5;
    private static final int GESUNDHEIT_PRO_LEVEL = 20;

    private static final String TYP_TRANK = "TRANK";
    private static final String TYP_WAFFE = "WAFFE";
    private static final String TYP_RUESTUNG = "RUESTUNG";

    private final SpielerRepository spielerRepository;
    private final GegenstandRepository gegenstandRepository;

    public SpielerService(SpielerRepository spielerRepository, GegenstandRepository gegenstandRepository) {
        this.spielerRepository = spielerRepository;
        this.gegenstandRepository = gegenstandRepository;
    }

    public Spieler erstellenSpieler(SpielerErstellenDto dto) {
        pruefeNameFrei(dto.getName());

        Spieler neuerSpieler = new Spieler();
        neuerSpieler.setName(dto.getName());
        neuerSpieler.setKlasse(dto.getKlasse());

        return spielerRepository.save(neuerSpieler);
    }

    @Transactional(readOnly = true)
    public List<Spieler> alleSpielerHolen() {
        return spielerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Spieler spielerHolenNachId(Long id) {
        return spielerRepository.findById(id)
                .orElseThrow(() -> new RessourceNichtGefundenException("Spieler", id));
    }

    public Spieler updateSpieler(Long id, Spieler spielerDetails) {
        Spieler spieler = spielerHolenNachId(id);

        if (!Objects.equals(spieler.getName(), spielerDetails.getName())) {
            pruefeNameFrei(spielerDetails.getName());
        }

        spieler.setName(spielerDetails.getName());
        spieler.setKlasse(spielerDetails.getKlasse());

        return spielerRepository.save(spieler);
    }

    public void deleteSpieler(Long id) {
        Spieler spieler = spielerHolenNachId(id);
        spielerRepository.delete(spieler);
    }

    public Spieler levelUp(Long id) {
        Spieler spieler = spielerHolenNachId(id);
        stufeAufsteigen(spieler);
        return spielerRepository.save(spieler);
    }

    public Spieler xpHinzufuegen(Long spielerId, int xp){
        if (xp < 0) {
            throw new IllegalArgumentException("Erfahrungspunkte dürfen nicht negativ sein!");
        }

        Spieler spieler = spielerHolenNachId(spielerId);
        int aktuelleXp = spieler.getErfahrungspunkte() + xp;

        while (aktuelleXp >= XP_PRO_LEVEL) {
            aktuelleXp -= XP_PRO_LEVEL;
            stufeAufsteigen(spieler);
        }

        spieler.setErfahrungspunkte(aktuelleXp);
        return spielerRepository.save(spieler);
    }

    public Spieler gegenstandHinzufuegen(Long spielerId, Long gegenstandId) {
        Spieler spieler = spielerHolenNachId(spielerId);
        Gegenstand gegenstand = gegenstandHolenNachId(gegenstandId);

        if (spieler.getInventar().size() >= MAX_INVENTAR_GROESSE) {
            throw new IllegalArgumentException(
                    "Das Inventar ist voll! Maximal " + MAX_INVENTAR_GROESSE + " Gegenstände erlaubt.");
        }

        spieler.getInventar().add(gegenstand);
        return spielerRepository.save(spieler);
    }

    public Spieler trankBenutzen(Long spielerId, Long gegenstandId) {
        Spieler spieler = spielerHolenNachId(spielerId);
        Gegenstand trank = gegenstandHolenNachId(gegenstandId);

        pruefeImInventar(spieler, trank);

        if (!TYP_TRANK.equalsIgnoreCase(trank.getTyp())) {
            throw new IllegalArgumentException("Dieser Gegenstand ist kein Trank!");
        }

        spieler.setGesundheit(spieler.getGesundheit() + nullSicher(trank.getBonusGesundheit()));
        spieler.getInventar().remove(trank);

        return spielerRepository.save(spieler);
    }

    public Spieler ausruestungAnlegen(Long spielerId, Long gegenstandId) {
        Spieler spieler = spielerHolenNachId(spielerId);
        Gegenstand gegenstand = gegenstandHolenNachId(gegenstandId);

        pruefeImInventar(spieler, gegenstand);

        String typ = gegenstand.getTyp();

        if (TYP_WAFFE.equalsIgnoreCase(typ)) {
            spieler.setStaerke(spieler.getStaerke() + nullSicher(gegenstand.getBonusStaerke()));
        } else if (TYP_RUESTUNG.equalsIgnoreCase(typ)) {
            spieler.setGesundheit(spieler.getGesundheit() + nullSicher(gegenstand.getBonusGesundheit()));
        } else {
            throw new IllegalArgumentException(
                    "Dieser Gegenstand kann nicht ausgerüstet werden! Nur WAFFE oder RUESTUNG.");
        }

        spieler.getInventar().remove(gegenstand);
        return spielerRepository.save(spieler);
    }

    private Gegenstand gegenstandHolenNachId(Long id) {
        return gegenstandRepository.findById(id)
                .orElseThrow(() -> new RessourceNichtGefundenException("Gegenstand", id));
    }

    private void pruefeNameFrei(String name) {
        if (spielerRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Ein Spieler mit dem Namen '" + name + "' existiert bereits!");
        }
    }

    private void pruefeImInventar(Spieler spieler, Gegenstand gegenstand) {
        if (!spieler.getInventar().contains(gegenstand)) {
            throw new IllegalArgumentException("Dieser Gegenstand befindet sich nicht im Inventar!");
        }
    }

    private void stufeAufsteigen(Spieler spieler) {
        spieler.setLevel(spieler.getLevel() + 1);
        spieler.setStaerke(spieler.getStaerke() + STAERKE_PRO_LEVEL);
        spieler.setGesundheit(spieler.getGesundheit() + GESUNDHEIT_PRO_LEVEL);
    }

    private int nullSicher(Integer wert) {
        return Objects.requireNonNullElse(wert, 0);
    }
}