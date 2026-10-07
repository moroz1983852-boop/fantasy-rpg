package de.rpg.game.service;

import de.rpg.game.model.Monster;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.MonsterRepository;
import de.rpg.game.repository.SpielerRepository;
import de.rpg.game.service.SpielerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KampfServiceTest {

    @Mock
    private SpielerRepository spielerRepository;

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private SpielerService spielerService;

    @InjectMocks
    private KampfService kampfService;

    @Test
    void angreifen_sollteMonsterGesundheitReduzierenWennMonsterUeberleben() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setStaerke(10);
        spieler.setGesundheit(100);

        Monster monster = new Monster();
        monster.setId(1L);
        monster.setName("Goblin");
        monster.setGesundheit(25);
        monster.setStaerke(15);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster));

        String ergebnis = kampfService.angreifen(1L, 1L);

        assertEquals(15, monster.getGesundheit());
        assertEquals(85, spieler.getGesundheit());

        assertTrue(ergebnis.contains("Goblin hat noch 15 HP"));
        assertTrue(ergebnis.contains("Du hast noch 85 HP"));

        verify(monsterRepository, times(1)).save(monster);
        verify(spielerRepository, times(1)).save(spieler);
    }

    @Test
    void angreifen_sollteMonsterBesiegenUndXpHinzufuegen() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setStaerke(20);
        spieler.setGesundheit(100);

        Monster monster = new Monster();
        monster.setId(1L);
        monster.setName("Goblin");
        monster.setGesundheit(20);
        monster.setErfahrungspunkteBelohnung(50);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster));

        String ergebnis = kampfService.angreifen(1L, 1L);

        assertTrue(ergebnis.contains("Goblin wurde besiegt"));

        verify(monsterRepository, times(1)).delete(monster);
        verify(spielerService, times(1)).xpHinzufuegen(1L, 50);
    }

    @Test
    void angreifen_sollteSpielerBesiegenWennMonsterStaerkeIst() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setStaerke(5);
        spieler.setGesundheit(10);

        Monster monster = new Monster();
        monster.setId(1L);
        monster.setName("Drache");
        monster.setGesundheit(100);
        monster.setStaerke(50);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster));

        String ergebnis = kampfService.angreifen(1L, 1L);

        assertEquals(95, monster.getGesundheit());
        assertEquals(0, spieler.getGesundheit());

        assertTrue(ergebnis.contains("Niederlage! Du wurdest im Kampf besiegt!"));

        verify(monsterRepository, times(1)).save(monster);
        verify(spielerRepository, times(1)).save(spieler);
    }
}
