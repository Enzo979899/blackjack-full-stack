CREATE DATABASE IF NOT EXISTS blackjack_test;

GRANT ALL PRIVILEGES ON blackjack_test.* TO 'blackjack'@'%';

USE blackjack_test;

CREATE TABLE IF NOT EXISTS players (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100)
        GENERATED ALWAYS AS (LOWER(TRIM(name))) STORED,
    wins INT NOT NULL DEFAULT 0,
    losses INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_players_normalized_name UNIQUE (normalized_name)
);

CREATE TABLE IF NOT EXISTS round_results (
    round_id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(36) NOT NULL,
    result VARCHAR(20) NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);
