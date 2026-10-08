package de.rpg.game.service;

import de.rpg.game.exception.RessourceNichtGefundenException;
import de.rpg.game.model.Monster;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.MonsterRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KampfServiceTest {
    private static final Long SPIELER_ID = 1L;
    private static final Long MONSTER_ID = 1L;

    @Mock
    private SpielerRepository spielerRepository;

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private SpielerService spielerService;

    @InjectMocks
    private KampfService kampfService;

    // Normale Kampfrunden

    @Test
    @DisplayName("Beide überleben: Kampflog enthält die komplette Runde")
    void angreifen_sollteVollstaendigeRundeProtokollieren() {
        Spieler spieler = erstelleSpieler(10, 100);
        Monster goblin = erstelleMonster("Goblin", 25, 15, 50);
        kampfteilnehmerVorbereiten(spieler, goblin);

        String ergebnis = kampfService.angreifen(SPIELER_ID, MONSTER_ID);

        assertEquals("Du greifst Goblin an und verursachst 10 Schaden. "
                        + "Goblin hat noch 15 HP. "
                        + "Goblin greift dich an und verursacht 15 Schaden! "
                        + "Du hast noch 85 HP.",
                ergebnis);

        assertEquals(15, goblin.getGesundheit());
        assertEquals(85, spieler.getGesundheit());
        verify(monsterRepository).save(goblin);
        verify(spielerRepository).save(spieler);
        verifyNoInteractions(spielerService);
    }

    @Test
    @DisplayName("Monster stribt: wird gelöscht, Spieler bekommt XP und wird nicht angreifen")
    void angreifen_sollteMonsterBesiegenUndXpVergeben() {
        Spieler spieler = erstelleSpieler(20, 100);
        Monster goblin = erstelleMonster("Goblin", 20, 15, 50);
        kampfteilnehmerVorbereiten(spieler, goblin);

        String ergebnis = kampfService.angreifen(SPIELER_ID, MONSTER_ID);

        assertEquals("Du greifst Goblin an und verursachst 20 Schaden. "
                        + "Sieg! Goblin wurde besiegt! Du erhälst 50 XP.",
                ergebnis);

        assertEquals(100, spieler.getGesundheit());
        verify(monsterRepository).delete(goblin);
        verify(spielerRepository, never()).save(any());
        verify(spielerService).xpHinzufuegen(SPIELER_ID, 50);
    }

    @Test
    @DisplayName("Spieler stribt: Gesundheit fällt auf 0, nicht ins Negative")
    void angreifen_sollteSpielerBesiegenUndGesundheitAufNullSetzen() {
        Spieler spieler = erstelleSpieler(5, 10);
        Monster drache = erstelleMonster("Drache", 100, 50, 500);
        kampfteilnehmerVorbereiten(spieler, drache);

        String ergebnis = kampfService.angreifen(SPIELER_ID, MONSTER_ID);

        assertEquals("Du greifst Drache an und verursachst 5 Schaden. "
                        + "Drache hat noch 95 HP. "
                        + "Drache greift dich an und verursacht 50 Schaden! "
                        + "Niederlage! Du wurdest im Kampf besiegt!",
                ergebnis);

        assertEquals(95, drache.getGesundheit());
        assertEquals(0, spieler.getGesundheit());
        verify(spielerRepository).save(spieler);
    }

    // Fehlerfälle

    @Test
    @DisplayName("Spieler mit 0 HP darf nicht kämpfen")
    void angreifen_sollteFehlerWerfenWennSpielerZuSchwach() {
        Spieler spieler = erstelleSpieler(10, 0);
        Monster goblin = erstelleMonster("Goblin", 25, 15, 50);
        kampfteilnehmerVorbereiten(spieler, goblin);

        IllegalArgumentException fehler = assertThrows(IllegalArgumentException.class,
                () -> kampfService.angreifen(SPIELER_ID, MONSTER_ID));

        assertEquals("Du bist zu schwach zum Kämpfen! Bitte heile dich zuerst mit einem Trank.",
                fehler.getMessage());
        assertEquals(25, goblin.getGesundheit());
        verify(monsterRepository, never()).save(any());
        verify(spielerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unbekannter Spieler: Moncster wird gar nicht erst gesucht")
    void angreifen_sollteFehlerWerfenWennSpielerNichtExistiert() {
        when(spielerRepository.findById(99L)).thenReturn(Optional.empty());

        RessourceNichtGefundenException fehler = assertThrows(RessourceNichtGefundenException.class,
                () -> kampfService.angreifen(99L, MONSTER_ID));

        assertEquals("Spieler mit ID 99 wurde nicht gefunden!", fehler.getMessage());
        verifyNoInteractions(monsterRepository);
    }

    @Test
    @DisplayName("Unbekanntes Monster führt zu RessourceNichtGefundenException")
    void angreifen_sollteFehlerWerfenWennMonsterNichtExistiert() {
        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(erstelleSpieler(10, 100)));
        when(monsterRepository.findById(99L)).thenReturn(Optional.empty());

        RessourceNichtGefundenException fehler = assertThrows(RessourceNichtGefundenException.class,
                () -> kampfService.angreifen(SPIELER_ID, 99L));

        assertEquals("Monster mit ID 99 wurde nicht gefunden!", fehler.getMessage());
        verifyNoInteractions(spielerService);
    }

    @Test
    @DisplayName("Monster ohne XP-Belohnung (null) gibt 0 XP statt abzustürzen")
    void angreifen_sollteNullBelohnungAlsNullBehandeln() {
        Spieler spieler = erstelleSpieler(30, 100);
        Monster ratte = erstelleMonster("Ratte", 5, 1, null);
        kampfteilnehmerVorbereiten(spieler, ratte);

        String ergebnis = kampfService.angreifen(SPIELER_ID, MONSTER_ID);

        assertTrue(ergebnis.endsWith("Du erhälst 0 XP."));
        verify(spielerService).xpHinzufuegen(SPIELER_ID, 0);
    }

    // Hilfsmethoden

    private Spieler erstelleSpieler(int staerke, int gesundheit) {
        Spieler spieler = new Spieler();
        spieler.setId(SPIELER_ID);
        spieler.setName("Siegfried");
        spieler.setStaerke(staerke);
        spieler.setGesundheit(gesundheit);
        return spieler;
    }

    private Monster erstelleMonster(String name, int gesundheit, int staerke, Integer belohnung) {
        Monster monster = new Monster();
        monster.setId(MONSTER_ID);
        monster.setName(name);
        monster.setGesundheit(gesundheit);
        monster.setStaerke(staerke);
        monster.setErfahrungspunkteBelohnung(belohnung);
        return monster;
    }

    private void kampfteilnehmerVorbereiten(Spieler spieler, Monster monster) {
        when(spielerRepository.findById(SPIELER_ID)).thenReturn(Optional.of(spieler));
        when(monsterRepository.findById(MONSTER_ID)).thenReturn(Optional.of(monster));
    }
}
