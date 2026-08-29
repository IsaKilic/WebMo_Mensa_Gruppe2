package de.leuphana.mensa.persistence.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import de.leuphana.mensa.persistence.DAOException;

/**
 * Liefert Datenbankverbindungen.
 *
 * Die Zugangsdaten stehen in datenbank.properties und NICHT im Quelltext.
 * Diese Datei gehoert nicht ins Repository (siehe .gitignore) - sonst
 * liegen Passwoerter auf GitHub.
 *
 * Fuer den Prototyp reicht DriverManager. In einer echten Anwendung
 * nimmt man einen Connection Pool, weil das Aufbauen einer Verbindung
 * teuer ist. Das waere eine gute Antwort in der muendlichen Pruefung.
 */
public final class ConnectionFactory {

    private static final String KONFIGDATEI = "datenbank.properties";

    private static String url;
    private static String benutzer;
    private static String passwort;

    static {
        ladeKonfiguration();
    }

    private ConnectionFactory() {
        // Utility-Klasse, keine Instanzen
    }

    private static void ladeKonfiguration() {
        Properties props = new Properties();
        try (InputStream stream = ConnectionFactory.class.getClassLoader()
                .getResourceAsStream(KONFIGDATEI)) {

            if (stream == null) {
                throw new DAOException(KONFIGDATEI + " nicht im Classpath gefunden. "
                        + "Kopiere datenbank.properties.vorlage nach "
                        + KONFIGDATEI + " und trage eure Zugangsdaten ein.");
            }
            props.load(stream);

        } catch (IOException e) {
            throw new DAOException("Konfiguration konnte nicht gelesen werden", e);
        }

        url      = props.getProperty("db.url");
        benutzer = props.getProperty("db.benutzer");
        passwort = props.getProperty("db.passwort");
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, benutzer, passwort);
        } catch (SQLException e) {
            throw new DAOException("Verbindung zur Datenbank fehlgeschlagen", e);
        }
    }
}
