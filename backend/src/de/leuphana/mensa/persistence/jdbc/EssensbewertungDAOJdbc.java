package de.leuphana.mensa.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.leuphana.mensa.model.Essensbewertung;
import de.leuphana.mensa.persistence.DAOException;
import de.leuphana.mensa.persistence.EssensbewertungDAO;

/**
 * JDBC-Implementierung von {@link EssensbewertungDAO}.
 *
 * Zwei Besonderheiten gegenueber EssenDAOJdbc:
 *
 * 1. LocalDateTime muss ueber java.sql.Timestamp uebersetzt werden.
 *    JDBC kennt LocalDateTime nicht direkt.
 *
 * 2. durchschnittFuerEssen nutzt AVG in SQL statt alle Bewertungen zu
 *    laden und in Java zu rechnen. Die Datenbank kann das besser, und
 *    es spart Datenverkehr.
 */
public class EssensbewertungDAOJdbc implements EssensbewertungDAO {

    private static final String SQL_ABGEBEN =
            "INSERT INTO essensbewertung "
            + "(essen_id, benutzer_id, sterne, text, foto_pfad, zeitpunkt) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_AENDERN =
            "UPDATE essensbewertung SET sterne = ?, text = ?, foto_pfad = ? "
            + "WHERE id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, essen_id, benutzer_id, sterne, text, foto_pfad, zeitpunkt "
            + "FROM essensbewertung WHERE id = ?";

    private static final String SQL_FIND_BY_ESSEN =
            "SELECT id, essen_id, benutzer_id, sterne, text, foto_pfad, zeitpunkt "
            + "FROM essensbewertung WHERE essen_id = ? ORDER BY zeitpunkt DESC";

    private static final String SQL_DURCHSCHNITT =
            "SELECT AVG(sterne) AS schnitt FROM essensbewertung WHERE essen_id = ?";

    private static final String SQL_LOESCHEN =
            "DELETE FROM essensbewertung WHERE id = ?";

    @Override
    public Essensbewertung abgeben(Essensbewertung bewertung) {
        // Der Bewertungstext ist laut Aufgabenstellung Pflicht. Die Pruefung
        // steht schon im Modell - hier nur der Aufruf, damit ungueltige
        // Daten gar nicht erst in die Datenbank gelangen.
        if (!bewertung.istGueltig()) {
            throw new DAOException(
                    "Bewertung unvollstaendig: Sterne 1-5 und Text sind Pflicht");
        }

        if (bewertung.getZeitpunkt() == null) {
            bewertung.setZeitpunkt(LocalDateTime.now());
        }

        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(
                     SQL_ABGEBEN, Statement.RETURN_GENERATED_KEYS)) {

            anweisung.setInt(1, bewertung.getEssenId());
            anweisung.setInt(2, bewertung.getBenutzerId());
            anweisung.setInt(3, bewertung.getSterne());
            anweisung.setString(4, bewertung.getText());
            anweisung.setString(5, bewertung.getFotoPfad());
            anweisung.setTimestamp(6, Timestamp.valueOf(bewertung.getZeitpunkt()));

            anweisung.executeUpdate();

            try (ResultSet schluessel = anweisung.getGeneratedKeys()) {
                if (schluessel.next()) {
                    bewertung.setId(schluessel.getInt(1));
                }
            }
            return bewertung;

        } catch (SQLException e) {
            throw new DAOException("Bewertung konnte nicht gespeichert werden", e);
        }
    }

    @Override
    public void aendern(Essensbewertung bewertung) {
        if (!bewertung.istGueltig()) {
            throw new DAOException("Bewertung unvollstaendig");
        }

        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_AENDERN)) {

            anweisung.setInt(1, bewertung.getSterne());
            anweisung.setString(2, bewertung.getText());
            anweisung.setString(3, bewertung.getFotoPfad());
            anweisung.setInt(4, bewertung.getId());

            if (anweisung.executeUpdate() == 0) {
                throw new DAOException(
                        "Bewertung " + bewertung.getId() + " existiert nicht");
            }

        } catch (SQLException e) {
            throw new DAOException("Bewertung konnte nicht geaendert werden", e);
        }
    }

    @Override
    public Essensbewertung findById(int bewertungId) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_FIND_BY_ID)) {

            anweisung.setInt(1, bewertungId);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                return ergebnis.next() ? ausZeile(ergebnis) : null;
            }

        } catch (SQLException e) {
            throw new DAOException("Bewertung " + bewertungId
                    + " konnte nicht geladen werden", e);
        }
    }

    @Override
    public List<Essensbewertung> findByEssen(int essenId) {
        List<Essensbewertung> bewertungen = new ArrayList<>();

        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung =
                     verbindung.prepareStatement(SQL_FIND_BY_ESSEN)) {

            anweisung.setInt(1, essenId);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                while (ergebnis.next()) {
                    bewertungen.add(ausZeile(ergebnis));
                }
            }
            return bewertungen;

        } catch (SQLException e) {
            throw new DAOException("Bewertungen zu Essen " + essenId
                    + " konnten nicht geladen werden", e);
        }
    }

    @Override
    public double durchschnittFuerEssen(int essenId) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung =
                     verbindung.prepareStatement(SQL_DURCHSCHNITT)) {

            anweisung.setInt(1, essenId);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                if (ergebnis.next()) {
                    double schnitt = ergebnis.getDouble("schnitt");
                    // AVG liefert NULL, wenn es keine Zeilen gibt.
                    // getDouble macht daraus 0.0, wasNull() deckt das auf.
                    return ergebnis.wasNull() ? 0.0 : schnitt;
                }
                return 0.0;
            }

        } catch (SQLException e) {
            throw new DAOException("Durchschnitt fuer Essen " + essenId
                    + " konnte nicht berechnet werden", e);
        }
    }

    @Override
    public boolean loeschen(int bewertungId) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_LOESCHEN)) {

            anweisung.setInt(1, bewertungId);
            return anweisung.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Bewertung " + bewertungId
                    + " konnte nicht geloescht werden", e);
        }
    }

    private Essensbewertung ausZeile(ResultSet ergebnis) throws SQLException {
        Essensbewertung bewertung = new Essensbewertung();
        bewertung.setId(ergebnis.getInt("id"));
        bewertung.setEssenId(ergebnis.getInt("essen_id"));
        bewertung.setBenutzerId(ergebnis.getInt("benutzer_id"));
        bewertung.setSterne(ergebnis.getInt("sterne"));
        bewertung.setText(ergebnis.getString("text"));
        bewertung.setFotoPfad(ergebnis.getString("foto_pfad"));

        Timestamp zeitpunkt = ergebnis.getTimestamp("zeitpunkt");
        if (zeitpunkt != null) {
            bewertung.setZeitpunkt(zeitpunkt.toLocalDateTime());
        }
        return bewertung;
    }
}
