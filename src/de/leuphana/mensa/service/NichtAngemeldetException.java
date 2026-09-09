package de.leuphana.mensa.service;

/**
 * Ein Aufruf verlangt eine angemeldete Person, aber es liegt keine
 * (gueltige) Session vor. HTTP 401 - Name und Zweck exakt wie in
 * docs/AUFGABE-ROLLE-B.md, Teil 4 "Eigene Exceptions" vorgegeben.
 *
 * Nicht zu verwechseln mit UngueltigeAnmeldungException: die hier ist
 * "du bist gar nicht eingeloggt", die andere ist "dein Login-Versuch
 * selbst ist gescheitert" (falscher Benutzername/Passwort).
 */
public class NichtAngemeldetException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NichtAngemeldetException(String meldung) {
        super(meldung);
    }
}
