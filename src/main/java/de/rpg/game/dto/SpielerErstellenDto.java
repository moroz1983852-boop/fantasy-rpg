package de.rpg.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpielerErstellenDto {

    @NotBlank(message = "Der Name darf nicht leer sein!")
    @Size(min = 3, max = 20, message = "Der Name muss zwischen 3 und 20 Zeichen lang sein!")
    private String name;

    @NotBlank(message = "Die Klasse darf nicht leer sein!")
    private String klasse;
}
