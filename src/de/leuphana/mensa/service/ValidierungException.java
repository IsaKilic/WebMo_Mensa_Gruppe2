package de.leuphana.mensa.service;

/**
 * Eine fachliche Eingabe ist ungueltig (leerer Name, negativer Preis,
 * Wochennummer ausserhalb 1-53, fehlender Bewertungstext, ...).
 * Wird vom Servlet auf HTTP 400 abgebildet, siehe docs/REST-API.md,
 * Abschnitt "Fehlerformat".
 */
public class ValidierungException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidierungException(String meldung) {
        super(meldung);
    }
}
