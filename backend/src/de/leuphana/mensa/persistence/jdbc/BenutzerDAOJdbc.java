package de.leuphana.mensa.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Rolle;
import de.leuphana.mensa.persistence.BenutzerDAO;
import de.leuphana.mensa.persistence.DAOException;

/**
 * JDBC-Implementierung von {@link BenutzerDAO}.
 *
 * Die kleinste der vier Klassen: nur zwei Lesemethoden, weil Benutzer
 * laut Aufgabenstellung nicht ueber die Anwendung angelegt werden.
 *
 * Hinweis zum Passwort: hier wird nur der Hash aus der Datenbank
 * geladen. Der Vergleich gehoert in die Service-Schicht, nicht ins DAO -
 * das DAO liefert Daten, es entscheidet nicht ueber Anmeldungen.
 */
public class BenutzerDAOJdbc implements BenutzerDAO {

    private static final String SQL_FIND_BY_BENUTZERNAME =
            "SELECT id, benutzername, passwort_hash, rolle "
            + "FROM benutzer WHERE benutzername = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, benutzername, passwort_hash, rolle "
            + "FROM benutzer WHERE id = ?";

    @Override
    public Benutzer findByBenutzername(String benutzername) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung =
                     verbindung.prepareStatement(SQL_FIND_BY_BENUTZERNAME)) {

            anweisung.setString(1, benutzername);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                return ergebnis.next() ? ausZeile(ergebnis) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Benutzer konnte nicht geladen werden", e);
        }
    }

    @Override
    public Benutzer findById(int benutzerId) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung =
                     verbindung.prepareStatement(SQL_FIND_BY_ID)) {

            anweisung.setInt(1, benutzerId);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                return ergebnis.next() ? ausZeile(ergebnis) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Benutzer " + benutzerId
                    + " konnte nicht geladen werden", e);
        }
    }

    private Benutzer ausZeile(ResultSet ergebnis) throws SQLException {
        Benutzer benutzer = new Benutzer();
        benutzer.setId(ergebnis.getInt("id"));
        benutzer.setBenutzername(ergebnis.getString("benutzername"));
        benutzer.setPasswortHash(ergebnis.getString("passwort_hash"));
        benutzer.setRolle(Rolle.valueOf(ergebnis.getString("rolle")));
        return benutzer;
    }
}
