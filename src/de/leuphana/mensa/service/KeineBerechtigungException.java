package de.leuphana.mensa.service;

/**
 * Angemeldet, aber ohne die noetige Rolle (z.B. User versucht, ein
 * Essen anzulegen - laut Rollen-Tabelle in docs/REST-API.md nur Admin).
 * HTTP 403. Name exakt wie in docs/AUFGABE-ROLLE-B.md vorgegeben.
 */
public class KeineBerechtigungException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public KeineBerechtigungException(String meldung) {
        super(meldung);
    }
}
