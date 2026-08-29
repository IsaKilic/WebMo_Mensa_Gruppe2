package de.leuphana.mensa.model;

/**
 * Kategorie eines Gerichts innerhalb eines Speiseplans.
 * Wird zum Gruppieren in der Anzeige verwendet.
 */
public enum Kategorie {
    HAUPTGERICHT("Hauptgericht"),
    VEGETARISCH("Vegetarisch"),
    BEILAGE("Beilage"),
    SALAT("Salat"),
    SUPPE("Suppe"),
    DESSERT("Dessert"),
    SONSTIGES("Sonstiges");

    private final String bezeichnung;

    Kategorie(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
