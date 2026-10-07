package de.rpg.game.service;

import de.rpg.game.exception.RessourceNichtGefundenException;
import de.rpg.game.model.Monster;
import de.rpg.game.model.Spieler;
import de.rpg.game.repository.MonsterRepository;
import de.rpg.game.repository.SpielerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class KampfService {

    private SpielerRepository spielerRepository;
    private MonsterRepository monsterRepository;


}
