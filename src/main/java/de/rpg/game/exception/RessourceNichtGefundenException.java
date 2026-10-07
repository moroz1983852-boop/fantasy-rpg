package de.rpg.game.exception;

public class RessourceNichtGefundenException extends RuntimeException {

    public RessourceNichtGefundenException(String ressource, Long id) {
        super(ressource + " mit ID " + id + " wurde nicht gefunden!");
    }
}