-- ---------------------------------------------------------------
-- Mensa-Projekt: Datenbankschema fuer MySQL 8
--
-- Ausfuehren mit:
--   mysql -u root -p < 01_schema.sql
-- ---------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS mensa
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mensa;

-- Reihenfolge beim Loeschen ist wichtig: Kindtabellen zuerst.
DROP TABLE IF EXISTS gericht_kennzeichnung;
DROP TABLE IF EXISTS gericht_zusatzstoff;
DROP TABLE IF EXISTS gericht_allergen;
DROP TABLE IF EXISTS gericht;
DROP TABLE IF EXISTS speiseplan;
DROP TABLE IF EXISTS mensa;

CREATE TABLE mensa (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL,
    adresse      VARCHAR(200),
    breitengrad  DOUBLE,
    laengengrad  DOUBLE
) ENGINE=InnoDB;

CREATE TABLE speiseplan (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    mensa_id  INT  NOT NULL,
    datum     DATE NOT NULL,
    CONSTRAINT fk_speiseplan_mensa
        FOREIGN KEY (mensa_id) REFERENCES mensa(id)
        ON DELETE CASCADE,
    -- Pro Mensa und Tag darf es nur einen Speiseplan geben.
    -- Diese Bedingung braucht der Importer fuer ON DUPLICATE KEY UPDATE.
    CONSTRAINT uq_speiseplan_mensa_datum UNIQUE (mensa_id, datum)
) ENGINE=InnoDB;

-- Preis und Naehrwerte liegen flach in dieser Tabelle (Embedded Value Object).
-- Eigene Tabellen waeren bei einer echten 1:1-Beziehung nur unnoetige Joins.
CREATE TABLE gericht (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    speiseplan_id      INT          NOT NULL,
    name               VARCHAR(200) NOT NULL,
    kategorie          VARCHAR(30)  NOT NULL DEFAULT 'SONSTIGES',

    preis_studierende  DECIMAL(5,2),
    preis_bedienstete  DECIMAL(5,2),
    preis_gaeste       DECIMAL(5,2),

    kilojoule          INT,
    kilokalorien       INT,
    fett               DECIMAL(6,1),
    kohlenhydrate      DECIMAL(6,1),
    eiweiss            DECIMAL(6,1),
    salz               DECIMAL(6,2),

    CONSTRAINT fk_gericht_speiseplan
        FOREIGN KEY (speiseplan_id) REFERENCES speiseplan(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_gericht_speiseplan ON gericht (speiseplan_id);

-- n:m-Zuordnungen. Die Werte entsprechen exakt den Java-Enum-Konstanten.
CREATE TABLE gericht_allergen (
    gericht_id INT         NOT NULL,
    allergen   VARCHAR(30) NOT NULL,
    PRIMARY KEY (gericht_id, allergen),
    CONSTRAINT fk_ga_gericht
        FOREIGN KEY (gericht_id) REFERENCES gericht(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE gericht_zusatzstoff (
    gericht_id  INT         NOT NULL,
    zusatzstoff VARCHAR(30) NOT NULL,
    PRIMARY KEY (gericht_id, zusatzstoff),
    CONSTRAINT fk_gz_gericht
        FOREIGN KEY (gericht_id) REFERENCES gericht(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE gericht_kennzeichnung (
    gericht_id    INT         NOT NULL,
    kennzeichnung VARCHAR(30) NOT NULL,
    PRIMARY KEY (gericht_id, kennzeichnung),
    CONSTRAINT fk_gk_gericht
        FOREIGN KEY (gericht_id) REFERENCES gericht(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
