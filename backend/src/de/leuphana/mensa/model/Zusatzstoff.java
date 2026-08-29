package de.leuphana.mensa.model;

/**
 * Kennzeichnungspflichtige Zusatzstoffe nach deutscher
 * Zusatzstoff-Zulassungsverordnung.
 */
public enum Zusatzstoff {
    FARBSTOFF("mit Farbstoff"),
    KONSERVIERUNGSSTOFF("mit Konservierungsstoff"),
    ANTIOXIDATIONSMITTEL("mit Antioxidationsmittel"),
    GESCHMACKSVERSTAERKER("mit Geschmacksverstärker"),
    GESCHWEFELT("geschwefelt"),
    GESCHWAERZT("geschwärzt"),
    GEWACHST("gewachst"),
    PHOSPHAT("mit Phosphat"),
    SUESSUNGSMITTEL("mit Süßungsmittel"),
    KOFFEINHALTIG("koffeinhaltig");

    private final String bezeichnung;

    Zusatzstoff(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
