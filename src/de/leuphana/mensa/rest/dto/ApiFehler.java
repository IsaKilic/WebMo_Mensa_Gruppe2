package de.leuphana.mensa.rest.dto;

/**
 * Einheitliches Fehlerformat fuer alle Fehler, siehe docs/REST-API.md,
 * Abschnitt "Fehlerformat": { "fehler": "VALIDIERUNG", "meldung": "..." }
 */
public class ApiFehler {

    private String fehler;
    private String meldung;

    public ApiFehler(String fehler, String meldung) {
        this.fehler = fehler;
        this.meldung = meldung;
    }

    public String getFehler() { return fehler; }
    public String getMeldung() { return meldung; }
}
