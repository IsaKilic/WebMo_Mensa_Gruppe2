package de.leuphana.mensa.persistence;

/**
 * Kapselt technische Fehler der Persistenzschicht.
 *
 * Wichtig: die Service-Schicht soll nie eine SQLException sehen. Sonst
 * wüsste sie, dass darunter eine relationale Datenbank liegt, und wir
 * könnten die Implementierung nicht mehr austauschen.
 */
public class DAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DAOException(String meldung) {
        super(meldung);
    }

    public DAOException(String meldung, Throwable ursache) {
        super(meldung, ursache);
    }
}
