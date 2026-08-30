package de.leuphana.mensa.model;

/**
 * Rollen laut Aufgabenstellung.
 *
 * ADMIN darf nach Login alle Funktionen bedienen.
 * USER darf Essensbewertungen anlegen, auf alles andere nur lesend zugreifen.
 */
public enum Rolle {
    USER,
    ADMIN
}
