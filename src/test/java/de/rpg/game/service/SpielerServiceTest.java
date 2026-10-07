package de.rpg.game.service;

import de.rpg.game.dto.SpielerErstellenDto;
import de.rpg.game.exception.RessourceNichtGefundenException;
import de.rpg.game.model.Gegenstand;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.GegenstandRepository;
import de.rpg.game.repository.SpielerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpielerServiceTest {

    private static final Long SPIELER_ID = 1L;
    private static final Long GEGENSTAND_ID = 1L;

    @Mock
    private SpielerRepository spielerRepository;

    @Mock
    private GegenstandRepository gegenstandRepository;

    @InjectMocks
    private SpielerService spielerService;

    // Spieler erstellen

    @Test
    @DisplayName("Neuer Spieler wird mit Startwerten gespeichert, wenn der Name frei ist")
    void erstellenSpieler_sollteSpielerSpeichernWennNameFrei() {
        SpielerErstellenDto dto = new SpielerErstellenDto();
        dto.setName("Siegfried");
        dto.setKlasse("Krieger");

        when(spielerRepository.findByName("Siegfried")).thenReturn(Optional.empty());
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.erstellenSpieler(dto);

        assertEquals("Siegfried", ergebnis.getName());
        assertEquals("Krieger", ergebnis.getKlasse());
        assertEquals(1, ergebnis.getLevel());
        assertEquals(100, ergebnis.getGesundheit());

        verify(spielerRepository).save(any(Spieler.class));
    }

    @Test
    @DisplayName("Spieler wird nicht erstellt, wenn der Name schon vergeben ist")
    void erstellenSpieler_sollteFehlerWerfenWennNameVergeben() {
        SpielerErstellenDto dto = new SpielerErstellenDto();
        dto.setName("Siegfried");
        dto.setKlasse("Krieger");

        when(spielerRepository.findByName("Siegfried")).thenReturn(Optional.of(erstelleSpieler()));

        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class,
                () -> spielerService.erstellenSpieler(dto));

        assertTrue(fehler.getMessage().contains("Siegfried"));
        verify(spielerRepository, never()).save(any());
    }

    // Spieler suchen und ändern

    @Test
    @DisplayName("Unbekannte Spieler-ID führt zu RessourceNichtGefundenException")
    void spielerHolenNachId_sollteFehlerWerfenWennSpielerNichtExistiert() {

        when(spielerRepository.findById(99L)).thenReturn(Optional.empty());

        RessourceNichtGefundenException fehler = assertThrows(RessourceNichtGefundenException.class,
                () -> spielerService.spielerHolenNachId(99L));

        assertEquals("Spieler mit ID 99 wurde nicht gefunden!", fehler.getMessage());
    }

    @Test
    @DisplayName("Umbenennen auf einen vergebenen Namen ist verboten")
    void updateSpieler_sollteFehlerWerfenWennNeuerNameVergeben() {
        Spieler spieler = erstelleSpieler();
        Spieler neueDaten = new Spieler();
        neueDaten.setName("Brunhild");
        neueDaten.setKlasse("Magierin");

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(spielerRepository.findByName("Brunhild")).thenReturn(Optional.of(new Spieler()));

        assertThrows(IllegalArgumentException.class,
                () -> spielerService.updateSpieler(SPIELER_ID, neueDaten));

        verify(spielerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nur die Klasse ändern ist erlaubt, auch wenn der Name gleich bleibt")
    void updateSpieler_sollteGleichenNamenErlauben() {
        Spieler spieler = erstelleSpieler();
        Spieler neueDaten = new Spieler();
        neueDaten.setName("Siegfried");
        neueDaten.setKlasse("Magier");

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.updateSpieler(SPIELER_ID, neueDaten);

        assertEquals("Magier", ergebnis.getKlasse());
        verify(spielerRepository, never()).findByName(anyString());
    }

    // Inventar

    @Test
    @DisplayName("Gegenstand landet im Inventar, die Stärke bleibt aber unverändert")
    void gegenstandHinzufuegen_sollteGegenstandInsInvetarLegenOhneStaerkeZuAendern() {
        Spieler spieler = erstelleSpieler();
        Gegenstand schwert = erstelleGegenstand("Flammenschwert", "WAFFE", 15, null);

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(GEGENSTAND_ID)).thenReturn(Optional.of(schwert));
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.gegenstandHinzufuegen(SPIELER_ID, GEGENSTAND_ID);

        assertEquals(10, ergebnis.getStaerke());
        assertEquals(1, ergebnis.getInventar().size());
        assertEquals("Flammenschwert", ergebnis.getInventar().get(0).getName());
        verify(spielerRepository).save(spieler);
    }

    @Test
    @DisplayName("Ein sechster Gegenstand passt nicht in ein volles Inventar")
    void gegenstandHinzufuegen_sollteFehlerWerfenWennInventarVoll() {
        Spieler spieler = erstelleSpieler();

        for (int i = 1; i <= 5; i++) {
            spieler.getInventar().add(erstelleGegenstand("Stein " + i, "SONSTIGES", 0, 0));
        }

        Gegenstand neuerGegenstand = erstelleGegenstand("Heiltrank", "TRANK", null, 20);

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(GEGENSTAND_ID)).thenReturn(Optional.of(neuerGegenstand));

        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class,
        () -> spielerService.gegenstandHinzufuegen(SPIELER_ID, GEGENSTAND_ID));

        assertTrue(fehler.getMessage().contains("Inventar ist voll"));
        assertEquals(5, spieler.getInventar().size());
        verify(spielerRepository, never()).save(any());
    }

    // Tränke

    @Test
    @DisplayName("Heiltrank erhört, die Gesundheit und verschwindet aus den Inventar")
    void trankBenutzen_sollteGesundgeutErhoehenUndTrankEntfernen() {
        Spieler spieler = erstelleSpieler();
        spieler.setGesundheit(80);
        Gegenstand trank = erstelleGegenstand("Heiltrank", "TRANK", null, 20);
        spieler.getInventar().add(trank);

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(GEGENSTAND_ID)).thenReturn(Optional.of(trank));
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.trankBenutzen(SPIELER_ID, GEGENSTAND_ID);
        assertEquals(100, ergebnis.getGesundheit());
        assertTrue(ergebnis.getInventar().isEmpty());
        verify(spielerRepository).save(spieler);
    }

    @Test
    @DisplayName("Eine Waffe kann nicht als Trank getrunken werden!")
    void trankBenutzen_sollteFehlerWerfenWennGegenstandKeinTrank() {
        Spieler spieler = erstelleSpieler();
        Gegenstand schwert = erstelleGegenstand("Flammenschwert", "WAFFE", 15, null);
        spieler.getInventar().add(schwert);

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(GEGENSTAND_ID)).thenReturn(Optional.of(schwert));

        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class,
                () -> spielerService.trankBenutzen(SPIELER_ID, GEGENSTAND_ID));

        assertEquals("Dieser Gegenstand ist kein Trank!", fehler.getMessage());
        assertEquals(1, spieler.getInventar().size());
        verify(spielerRepository, never()).save(any());
    }

    @Test
    @DisplayName("250 XP lassen einen level-1-Spieler direkt auf Level 3 steigen")
    void xpHinzufuegen_sollteMehrereLevelAufsteigen() {
        Spieler spieler = erstelleSpieler();

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.xpHinzufuegen(SPIELER_ID, 250);

        assertEquals(3, ergebnis.getLevel());
        assertEquals(50, ergebnis.getErfahrungspunkte());
        assertEquals(20, ergebnis.getStaerke());
        assertEquals(140, ergebnis.getGesundheit());
        verify(spielerRepository, times(1)).save(spieler);
    }

    @Test
    @DisplayName("Negative XP werden abgelehnt, ohne die Datenbank zu fragen")
    void xpHinzufuegen_sollteFehlerWerfenBeiNegativenXp() {
        assertThrows(IllegalArgumentException.class,
                () -> spielerService.xpHinzufuegen(SPIELER_ID, -50));

        verifyNoInteractions(spielerRepository);
    }

    // Ausrüstung

    @Test
    @DisplayName("Rüstung erhört die Gesundheit und verlässt das Inventar")
    void ausruestungAnlegen_sollteRuestungGesundheitErhoehen() {
        Spieler spieler = erstelleSpieler();
        Gegenstand ruestung = erstelleGegenstand("Drachenpanzer", "RUESTUNG", null, 30);
        spieler.getInventar().add(ruestung);

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(GEGENSTAND_ID)).thenReturn(Optional.of(ruestung));
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.ausruestungAnlegen(SPIELER_ID, GEGENSTAND_ID);

        assertEquals(130, ergebnis.getGesundheit());
        assertTrue(ergebnis.getInventar().isEmpty());
    }

    @Test
    @DisplayName("Waffe ohne Stärkebonus (null) führt nicht zum Absturz")
    void ausruestungAnlegen_sollteNullBonusAlsNullBehandeln() {
        Spieler spieler = erstelleSpieler();
        Gegenstand stock = erstelleGegenstand("Holzstock", "WAFFE", null, null);
        spieler.getInventar().add(stock);

        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(GEGENSTAND_ID)).thenReturn(Optional.of(stock));
        speichernGibtArgumentZurueck();

        Spieler ergebnis = spielerService.ausruestungAnlegen(SPIELER_ID, GEGENSTAND_ID);

        assertEquals(10, ergebnis.getStaerke());
    }

    // Hilfsmethoden

    private Spieler erstelleSpieler() {
        Spieler spieler = new Spieler();
        spieler.setId(SPIELER_ID);
        spieler.setName("Siegfried");
        spieler.setKlasse("Krieger");
        return spieler;
    }

    private Gegenstand erstelleGegenstand(String name, String typ, Integer bonusStaerke, Integer bonusGesundheit) {
        Gegenstand gegenstand = new Gegenstand();
        gegenstand.setId(GEGENSTAND_ID);
        gegenstand.setName(name);
        gegenstand.setTyp(typ);
        gegenstand.setBonusStaerke(bonusStaerke);
        gegenstand.setBonusGesundheit(bonusGesundheit);
        return gegenstand;
    }

    private void speichernGibtArgumentZurueck() {
        when(spielerRepository.save(any(Spieler.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));
    }
}