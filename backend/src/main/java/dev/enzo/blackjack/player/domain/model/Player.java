package dev.enzo.blackjack.player.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Player {
    private final UUID id;
    private final String name;
    private int wins;
    private int losses;
    private int draws;

    public Player(UUID id, String name) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;

        if (name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
    }


    public Player(UUID id, String name, int wins, int losses, int draws) {
        this(id, name);

        if (wins < 0 || losses < 0 || draws < 0) {
            throw new IllegalArgumentException("Player statistics cannot be negative");
        }

        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
    }


    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }


    public void recordWin() {
        this.wins++;
    }

    public void recordLoss() {
        this.losses++;
    }

    public void recordDraw() {
        this.draws++;
    }

}
