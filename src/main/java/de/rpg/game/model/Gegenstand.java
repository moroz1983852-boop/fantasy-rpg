package de.rpg.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "gegenstaende")
@NoArgsConstructor
@AllArgsConstructor
public class Gegenstand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String typ;
    private Integer bonusStaerke;
    private Integer bonusGesundheit;
}
