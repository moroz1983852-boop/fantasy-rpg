package de.rpg.game.service;

import de.rpg.game.model.Monster;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.MonsterRepository;
import de.rpg.game.repository.SpielerRepository;
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

    @InjectMocks
    private KampfService kampfService;

    @Test
    void angreifen_sollteMonsterGesundheitReduzierenWennMonsterÜberlebt() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setStaerke(10);

        Monster monster = new Monster();
        monster.setId(1L);
        monster.setName("Goblin");
        monster.setGesundheit(25);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster));

        String ergebnis = kampfService.angreifen(1L, 1L);

        assertEquals(15, monster.getGesundheit());
        assertTrue(ergebnis.contains("Goblin hat noch 15 HP"));

        verify(monsterRepository, times(1)).save(monster);
    }

    @Test
    void angreifen_sollteMonsterBesiegenUndSpielerErfahrungspunkteGeben() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setStaerke(20);
        spieler.setErfahrungspunkte(0);

        Monster monster = new Monster();
        monster.setId(1L);
        monster.setName("Goblin");
        monster.setGesundheit(20);
        monster.setErfahrungspunkteBelohnung(50);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(monsterRepository.findById(1L)).thenReturn(Optional.of(monster));

        String ergebnis = kampfService.angreifen(1L, 1L);

        assertEquals(50, spieler.getErfahrungspunkte());
        assertTrue(ergebnis.contains("Goblin wurde besiegt"));

        verify(monsterRepository, times(1)).delete(monster);
        verify(spielerRepository, times(1)).save(spieler);
    }
}
