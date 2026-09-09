package de.leuphana.mensa.service;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.persistence.BenutzerDAO;

/**
 * Prueft Login-Daten gegen die gespeicherten Benutzer.
 *
 * Klasse und Methode heissen exakt wie in docs/AUFGABE-ROLLE-B.md,
 * Teil 4, "Vorgeschlagene Klassen" vorgegeben.
 */
public class AnmeldeService {

    private final BenutzerDAO benutzerDAO;

    public AnmeldeService(BenutzerDAO benutzerDAO) {
        this.benutzerDAO = benutzerDAO;
    }

    /**
     * @return der angemeldete Benutzer.
     * @throws UngueltigeAnmeldungException wenn Benutzername unbekannt
     *         oder Passwort falsch ist (HTTP 401, siehe REST-API.md).
     */
    public Benutzer anmelden(String benutzername, String passwort) {
        Benutzer benutzer = benutzerDAO.findByBenutzername(benutzername);
        if (benutzer == null || !passwortPasst(passwort, benutzer.getPasswortHash())) {
            throw new UngueltigeAnmeldungException("Benutzername oder Passwort falsch");
        }
        return benutzer;
    }

    /**
     * ACHTUNG, bewusst vorlaeufig: `BenutzerDAOInMemory` legt die beiden
     * Testbenutzer aktuell im Klartext ab (siehe Kommentar dort: "die
     * Passwoerter stehen hier im Klartext, damit ihr sofort testen
     * koennt"). Deshalb vergleichen wir hier vorerst direkt auf
     * Gleichheit statt mit BCrypt zu pruefen - ein echter BCrypt-Hash
     * wuerde gegen "admin123" sowieso nie passen.
     *
     * Sobald Rolle A auf echte Hashes umstellt (Benutzer-Klasse verlangt
     * das per Javadoc: BCrypt oder PBKDF2), wird NUR diese eine Methode
     * ausgetauscht, z.B. gegen:
     *
     *     return BCrypt.checkpw(eingegeben, gespeicherterHash);
     *
     * Der Rest von AnmeldeService, alle Servlets und alle anderen
     * Services aendern sich dabei nicht - das ist genau der Sinn, diese
     * eine Regel in einer eigenen Methode zu kapseln statt sie irgendwo
     * inline zu vergleichen. Fuer das muendliche Gespraech: das ist eine
     * bewusste, dokumentierte technische Schuld, keine vergessene Regel.
     */
    private boolean passwortPasst(String eingegeben, String gespeicherterHash) {
        return gespeicherterHash != null && gespeicherterHash.equals(eingegeben);
    }
}
