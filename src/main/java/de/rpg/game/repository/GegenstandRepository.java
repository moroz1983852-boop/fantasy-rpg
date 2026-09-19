package de.rpg.game.repository;

import de.rpg.game.model.Gegenstand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GegenstandRepository extends JpaRepository<Gegenstand, Long> {
}
