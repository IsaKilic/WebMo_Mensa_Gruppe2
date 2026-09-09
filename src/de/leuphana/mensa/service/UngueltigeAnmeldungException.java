package de.leuphana.mensa.service;

/**
 * Der Login-Versuch selbst ist gescheitert: Benutzername unbekannt oder
 * Passwort falsch. HTTP 401, siehe docs/REST-API.md "POST /api/login".
 * Bewusst eine eigene Klasse statt NichtAngemeldetException - beide
 * enden zwar bei 401, aber fachlich sind es zwei verschiedene Faelle
 * (siehe Javadoc dort).
 */
public class UngueltigeAnmeldungException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UngueltigeAnmeldungException(String meldung) {
        super(meldung);
    }
}
