package de.leuphana.mensa.model;

/**
 * Preisgruppe des Nutzers. Bestimmt, welcher Preis angezeigt wird.
 */
public enum Nutzergruppe {
    STUDIERENDE("Studierende"),
    BEDIENSTETE("Bedienstete"),
    GAESTE("Gäste");

    private final String bezeichnung;

    Nutzergruppe(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
