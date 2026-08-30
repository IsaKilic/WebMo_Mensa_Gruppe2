package de.leuphana.mensa.persistence;

/** Kapselt technische Fehler der Persistenzschicht. Nie SQLException nach oben durchreichen. */
public class DAOException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public DAOException(String meldung) { super(meldung); }
    public DAOException(String meldung, Throwable ursache) { super(meldung, ursache); }
}
