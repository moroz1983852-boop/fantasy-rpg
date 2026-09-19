package de.rpg.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Spieler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String klasse;
    private Integer level = 1;
    private Integer erfahrungspunkte = 0;
    private Integer gesundheit = 100;
    private Integer staerke = 10;

    @ManyToMany
    @JoinTable(
            name = "spieler_inventar",
            joinColumns = @JoinColumn(name = "spieler_id"),
            inverseJoinColumns = @JoinColumn(name = "gegenstand_id")
    )
    private List<Gegenstand> inventar = new ArrayList<>();
}