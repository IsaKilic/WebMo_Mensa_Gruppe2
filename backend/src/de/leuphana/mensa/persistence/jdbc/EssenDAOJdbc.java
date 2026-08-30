package de.leuphana.mensa.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.leuphana.mensa.model.Art;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.persistence.DAOException;
import de.leuphana.mensa.persistence.EssenDAO;

/**
 * JDBC-Implementierung von {@link EssenDAO}.
 *
 * Muster fuer die drei uebrigen DAOs. Drei Dinge, die hier durchgaengig
 * gelten:
 *
 * 1. Immer PreparedStatement, nie String-Verkettung im SQL. Sonst waere
 *    die Anwendung anfaellig fuer SQL-Injection - ein Name wie
 *    "'; DROP TABLE essen; --" wuerde sonst ausgefuehrt.
 *
 * 2. try-with-resources fuer Connection, Statement und ResultSet. Die
 *    werden dadurch garantiert geschlossen, auch wenn eine Exception
 *    fliegt. Ohne das laufen die Verbindungen irgendwann voll.
 *
 * 3. SQLException wird zu DAOException. Die Service-Schicht soll nicht
 *    wissen, dass darunter eine relationale Datenbank liegt.
 */
public class EssenDAOJdbc implements EssenDAO {

    private static final String SQL_ANLEGEN =
            "INSERT INTO essen (name, preis, art) VALUES (?, ?, ?)";

    private static final String SQL_AENDERN =
            "UPDATE essen SET name = ?, preis = ?, art = ? WHERE id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, name, preis, art FROM essen WHERE id = ?";

    private static final String SQL_FIND_ALLE =
            "SELECT id, name, preis, art FROM essen ORDER BY name";

    private static final String SQL_LOESCHEN =
            "DELETE FROM essen WHERE id = ?";

    @Override
    public Essen anlegen(Essen essen) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(
                     SQL_ANLEGEN, Statement.RETURN_GENERATED_KEYS)) {

            anweisung.setString(1, essen.getName());
            anweisung.setDouble(2, essen.getPreis());
            anweisung.setString(3, essen.getArt().name());

            anweisung.executeUpdate();

            // Die Datenbank vergibt die Id per AUTO_INCREMENT.
            // Wir holen sie zurueck und setzen sie ins Objekt.
            try (ResultSet schluessel = anweisung.getGeneratedKeys()) {
                if (schluessel.next()) {
                    essen.setId(schluessel.getInt(1));
                }
            }
            return essen;

        } catch (SQLException e) {
            throw new DAOException("Essen konnte nicht angelegt werden", e);
        }
    }

    @Override
    public void aendern(Essen essen) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_AENDERN)) {

            anweisung.setString(1, essen.getName());
            anweisung.setDouble(2, essen.getPreis());
            anweisung.setString(3, essen.getArt().name());
            anweisung.setInt(4, essen.getId());

            int betroffeneZeilen = anweisung.executeUpdate();
            if (betroffeneZeilen == 0) {
                throw new DAOException("Essen " + essen.getId() + " existiert nicht");
            }

        } catch (SQLException e) {
            throw new DAOException("Essen konnte nicht geaendert werden", e);
        }
    }

    @Override
    public Essen findById(int essenId) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_FIND_BY_ID)) {

            anweisung.setInt(1, essenId);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                if (ergebnis.next()) {
                    return ausZeile(ergebnis);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DAOException("Essen " + essenId + " konnte nicht geladen werden", e);
        }
    }

    @Override
    public List<Essen> findAlle() {
        List<Essen> alle = new ArrayList<>();

        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_FIND_ALLE);
             ResultSet ergebnis = anweisung.executeQuery()) {

            while (ergebnis.next()) {
                alle.add(ausZeile(ergebnis));
            }
            return alle;

        } catch (SQLException e) {
            throw new DAOException("Essen konnten nicht geladen werden", e);
        }
    }

    @Override
    public boolean loeschen(int essenId) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_LOESCHEN)) {

            anweisung.setInt(1, essenId);
            return anweisung.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Essen " + essenId + " konnte nicht geloescht werden", e);
        }
    }

    /**
     * Baut aus einer Ergebniszeile ein Essen-Objekt.
     *
     * Diese Hilfsmethode gehoert in jedes DAO: sonst steht das Mapping
     * in findById und findAlle doppelt, und beim naechsten neuen Feld
     * vergisst man eine der beiden Stellen.
     */
    private Essen ausZeile(ResultSet ergebnis) throws SQLException {
        Essen essen = new Essen();
        essen.setId(ergebnis.getInt("id"));
        essen.setName(ergebnis.getString("name"));
        essen.setPreis(ergebnis.getDouble("preis"));
        essen.setArt(Art.valueOf(ergebnis.getString("art")));
        return essen;
    }
}
