package de.rpg.game.service;

import de.rpg.game.model.Gegenstand;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.GegenstandRepository;
import de.rpg.game.repository.SpielerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpielerServiceTest {

    @Mock
    private SpielerRepository spielerRepository;

    @Mock
    private GegenstandRepository gegenstandRepository;

    @InjectMocks
    private SpielerService spielerService;

    @Test
    void gegenstandHinzufügen_sollteBonusStaerkeErhoehenUndInvenataErweitern() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setName("Siegfried");
        spieler.setStaerke(10);

        Gegenstand schwert = new Gegenstand();
        schwert.setId(1L);
        schwert.setName("Flammenschwert");
        schwert.setBonusStaerke(15);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(1L)).thenReturn(Optional.of(schwert));
        when(spielerRepository.save(any(Spieler.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Spieler ergebnis = spielerService.gegenstandHinzufügen(1L, 1L);

        assertNotNull(ergebnis);

        assertEquals(25, ergebnis.getStaerke());

        assertEquals(1, ergebnis.getInventar().size());
        assertEquals("Flammenschwert", ergebnis.getInventar().get(0).getName());
        verify(spielerRepository, times(1)).save(spieler);
    }

    @Test
    void trankbenutzen_sollteGesundheitErhoehenUndTrankAusInventarEntfernen() {

        Spieler spieler = new Spieler();
        spieler.setId(1L);
        spieler.setGesundheit(80);

        Gegenstand trank = new Gegenstand();
        trank.setId(1L);
        trank.setName("Heiltrank");
        trank.setTyp("Trank");
        trank.setBonusGesundheit(20);

        spieler.getInventar().add(trank);

        when(spielerRepository.findById(1L)).thenReturn(Optional.of(spieler));
        when(gegenstandRepository.findById(1L)).thenReturn(Optional.of(trank));
        when(spielerRepository.save(any(Spieler.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Spieler ergebnis = spielerService.trankBenutzen(1L, 1L);

        assertNotNull(ergebnis);
        assertEquals(100, ergebnis.getGesundheit());
        assertTrue(ergebnis.getInventar().isEmpty());

        verify(spielerRepository, times(1)).save(spieler);
    }
}
