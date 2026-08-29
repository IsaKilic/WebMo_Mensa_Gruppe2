package de.leuphana.mensa.model;

/**
 * Positive Auszeichnung eines Gerichts.
 * Wunschkriterium beim Filtern: der Nutzer sucht danach.
 * Bewusst getrennt von {@link Allergen}, das ein Ausschlusskriterium ist.
 */
public enum Kennzeichnung {
    VEGAN("vegan"),
    VEGETARISCH("vegetarisch"),
    RIND("Rind"),
    SCHWEIN("Schwein"),
    GEFLUEGEL("Geflügel"),
    FISCH("Fisch"),
    BIO("Bio");

    private final String bezeichnung;

    Kennzeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
