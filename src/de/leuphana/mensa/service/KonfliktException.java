package de.leuphana.mensa.service;

/**
 * Die Operation wuerde einen widerspruechlichen Zustand erzeugen -
 * etwa eine zweite Essensplan mit derselben Wochennummer, oder das
 * Loeschen eines Essens, das noch in einem Plan steht. HTTP 409,
 * siehe docs/REST-API.md "Fehlerformat".
 */
public class KonfliktException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public KonfliktException(String meldung) {
        super(meldung);
    }
}
