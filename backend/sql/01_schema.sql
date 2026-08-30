-- Schema fuer MySQL 8, abgeleitet aus der Aufgabenstellung.

CREATE DATABASE IF NOT EXISTS mensa
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mensa;

DROP TABLE IF EXISTS essensbewertung;
DROP TABLE IF EXISTS essensplan_essen;
DROP TABLE IF EXISTS essensplan;
DROP TABLE IF EXISTS essen;
DROP TABLE IF EXISTS benutzer;

CREATE TABLE benutzer (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    benutzername  VARCHAR(50)  NOT NULL UNIQUE,
    passwort_hash VARCHAR(255) NOT NULL,
    rolle         VARCHAR(10)  NOT NULL DEFAULT 'USER'
) ENGINE=InnoDB;

CREATE TABLE essen (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(200)  NOT NULL,
    preis DECIMAL(5,2)  NOT NULL,
    art   VARCHAR(20)   NOT NULL   -- VEGETARISCH | VEGAN | MIT_FLEISCH
) ENGINE=InnoDB;

CREATE TABLE essensplan (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    wochennummer INT NOT NULL UNIQUE
) ENGINE=InnoDB;

-- EssenProWoche: pro Plan und Wochentag genau ein Essen.
-- Der Primaerschluessel (essensplan_id, wochentag) erzwingt genau das.
CREATE TABLE essensplan_essen (
    essensplan_id INT         NOT NULL,
    wochentag     VARCHAR(12) NOT NULL,  -- MONTAG .. FREITAG
    essen_id      INT         NOT NULL,
    PRIMARY KEY (essensplan_id, wochentag),
    CONSTRAINT fk_epe_plan  FOREIGN KEY (essensplan_id)
        REFERENCES essensplan(id) ON DELETE CASCADE,
    CONSTRAINT fk_epe_essen FOREIGN KEY (essen_id)
        REFERENCES essen(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE essensbewertung (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    essen_id    INT       NOT NULL,
    benutzer_id INT       NOT NULL,
    sterne      TINYINT   NOT NULL,
    text        TEXT      NOT NULL,          -- Bewertungstext ist Pflicht
    foto_pfad   VARCHAR(255),
    zeitpunkt   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bew_essen    FOREIGN KEY (essen_id)
        REFERENCES essen(id) ON DELETE CASCADE,
    CONSTRAINT fk_bew_benutzer FOREIGN KEY (benutzer_id)
        REFERENCES benutzer(id) ON DELETE CASCADE,
    CONSTRAINT ck_sterne CHECK (sterne BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE INDEX idx_bew_essen ON essensbewertung (essen_id);
