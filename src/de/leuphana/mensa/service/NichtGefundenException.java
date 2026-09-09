package de.leuphana.mensa.service;

/**
 * Die angefragte Ressource (Essen, Essensplan, Bewertung, ...) existiert
 * nicht. HTTP 404, siehe docs/REST-API.md "Fehlerformat".
 */
public class NichtGefundenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NichtGefundenException(String meldung) {
        super(meldung);
    }
}
