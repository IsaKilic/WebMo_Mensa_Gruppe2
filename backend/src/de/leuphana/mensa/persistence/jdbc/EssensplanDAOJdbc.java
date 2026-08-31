package de.leuphana.mensa.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.leuphana.mensa.model.Art;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.model.Essensplan;
import de.leuphana.mensa.model.Wochentag;
import de.leuphana.mensa.persistence.DAOException;
import de.leuphana.mensa.persistence.EssensplanDAO;

/**
 * JDBC-Implementierung von {@link EssensplanDAO}.
 *
 * Die aufwendigste der vier Klassen, weil ein Essensplan auf zwei
 * Tabellen verteilt ist: essensplan haelt die Wochennummer,
 * essensplan_essen die Zuordnung Wochentag zu Essen.
 *
 * Zwei Konzepte, die hier neu sind:
 *
 * TRANSAKTION. Anlegen und Aendern schreiben in beide Tabellen. Geht
 * der zweite Schritt schief, muss auch der erste zurueckgenommen
 * werden - sonst steht ein Plan ohne Essen in der Datenbank. Deshalb
 * setAutoCommit(false), am Ende commit(), im Fehlerfall rollback().
 *
 * JOIN STATT N+1. findAlle koennte pro Plan eine zweite Abfrage
 * schicken - bei acht Plaenen waeren das neun Abfragen. Stattdessen
 * holt ein LEFT JOIN alles auf einmal, und die Zeilen werden in Java
 * zu Objekten gruppiert. LEFT und nicht INNER, damit auch ein noch
 * leerer Plan geliefert wird.
 */
public class EssensplanDAOJdbc implements EssensplanDAO {

    private static final String SQL_PLAN_ANLEGEN =
            "INSERT INTO essensplan (wochennummer) VALUES (?)";

    private static final String SQL_PLAN_AENDERN =
            "UPDATE essensplan SET wochennummer = ? WHERE id = ?";

    private static final String SQL_ZUORDNUNG_ANLEGEN =
            "INSERT INTO essensplan_essen (essensplan_id, wochentag, essen_id) "
            + "VALUES (?, ?, ?)";

    private static final String SQL_ZUORDNUNGEN_LOESCHEN =
            "DELETE FROM essensplan_essen WHERE essensplan_id = ?";

    private static final String SQL_LOESCHEN =
            "DELETE FROM essensplan WHERE id = ?";

    /**
     * Basisabfrage mit LEFT JOIN ueber alle drei Tabellen.
     * Die WHERE-Klausel wird von den Aufrufern angehaengt.
     */
    private static final String SQL_BASIS =
            "SELECT p.id AS plan_id, p.wochennummer, "
            + "       ze.wochentag, "
            + "       e.id AS essen_id, e.name, e.preis, e.art "
            + "FROM essensplan p "
            + "LEFT JOIN essensplan_essen ze ON ze.essensplan_id = p.id "
            + "LEFT JOIN essen e ON e.id = ze.essen_id ";

    private static final String SQL_FIND_BY_ID =
            SQL_BASIS + "WHERE p.id = ? ORDER BY p.id";

    private static final String SQL_FIND_BY_WOCHE =
            SQL_BASIS + "WHERE p.wochennummer = ? ORDER BY p.id";

    private static final String SQL_FIND_ALLE =
            SQL_BASIS + "ORDER BY p.wochennummer";

    // ---------------------------------------------------------------
    // Schreiben
    // ---------------------------------------------------------------

    @Override
    public Essensplan anlegen(Essensplan essensplan) {
        Connection verbindung = null;
        try {
            verbindung = ConnectionFactory.getConnection();
            verbindung.setAutoCommit(false);   // Transaktion beginnen

            try (PreparedStatement anweisung = verbindung.prepareStatement(
                    SQL_PLAN_ANLEGEN, Statement.RETURN_GENERATED_KEYS)) {

                anweisung.setInt(1, essensplan.getWochennummer());
                anweisung.executeUpdate();

                try (ResultSet schluessel = anweisung.getGeneratedKeys()) {
                    if (schluessel.next()) {
                        essensplan.setId(schluessel.getInt(1));
                    }
                }
            }

            schreibeZuordnungen(verbindung, essensplan);

            verbindung.commit();
            return essensplan;

        } catch (SQLException e) {
            zurueckrollen(verbindung);
            throw new DAOException("Essensplan konnte nicht angelegt werden", e);
        } finally {
            schliessen(verbindung);
        }
    }

    @Override
    public void aendern(Essensplan essensplan) {
        Connection verbindung = null;
        try {
            verbindung = ConnectionFactory.getConnection();
            verbindung.setAutoCommit(false);

            try (PreparedStatement anweisung =
                         verbindung.prepareStatement(SQL_PLAN_AENDERN)) {
                anweisung.setInt(1, essensplan.getWochennummer());
                anweisung.setInt(2, essensplan.getId());

                if (anweisung.executeUpdate() == 0) {
                    throw new DAOException("Essensplan " + essensplan.getId()
                            + " existiert nicht");
                }
            }

            // Alte Zuordnungen weg, neue rein. Einfacher und weniger
            // fehleranfaellig als einzeln zu vergleichen, was sich
            // geaendert hat - bei fuenf Zeilen pro Plan auch schnell genug.
            try (PreparedStatement anweisung =
                         verbindung.prepareStatement(SQL_ZUORDNUNGEN_LOESCHEN)) {
                anweisung.setInt(1, essensplan.getId());
                anweisung.executeUpdate();
            }

            schreibeZuordnungen(verbindung, essensplan);

            verbindung.commit();

        } catch (SQLException e) {
            zurueckrollen(verbindung);
            throw new DAOException("Essensplan konnte nicht geaendert werden", e);
        } finally {
            schliessen(verbindung);
        }
    }

    /** Schreibt die EnumMap in die Zuordnungstabelle. */
    private void schreibeZuordnungen(Connection verbindung, Essensplan plan)
            throws SQLException {

        if (plan.getEssenProWoche().isEmpty()) {
            return;
        }

        try (PreparedStatement anweisung =
                     verbindung.prepareStatement(SQL_ZUORDNUNG_ANLEGEN)) {

            for (Map.Entry<Wochentag, Essen> eintrag
                    : plan.getEssenProWoche().entrySet()) {

                anweisung.setInt(1, plan.getId());
                anweisung.setString(2, eintrag.getKey().name());
                anweisung.setInt(3, eintrag.getValue().getId());
                anweisung.addBatch();   // sammeln statt einzeln schicken
            }
            anweisung.executeBatch();
        }
    }

    @Override
    public boolean loeschen(int essensplanId) {
        // Die Zuordnungen verschwinden per ON DELETE CASCADE mit,
        // deshalb reicht hier eine einzelne Anweisung ohne Transaktion.
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_LOESCHEN)) {

            anweisung.setInt(1, essensplanId);
            return anweisung.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Essensplan " + essensplanId
                    + " konnte nicht geloescht werden", e);
        }
    }

    // ---------------------------------------------------------------
    // Lesen
    // ---------------------------------------------------------------

    @Override
    public Essensplan findById(int essensplanId) {
        List<Essensplan> treffer = ladeMitParameter(SQL_FIND_BY_ID, essensplanId);
        return treffer.isEmpty() ? null : treffer.get(0);
    }

    @Override
    public Essensplan findByWoche(int wochennummer) {
        List<Essensplan> treffer = ladeMitParameter(SQL_FIND_BY_WOCHE, wochennummer);
        return treffer.isEmpty() ? null : treffer.get(0);
    }

    @Override
    public List<Essensplan> findAlle() {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(SQL_FIND_ALLE);
             ResultSet ergebnis = anweisung.executeQuery()) {

            return ausErgebnis(ergebnis);

        } catch (SQLException e) {
            throw new DAOException("Essensplaene konnten nicht geladen werden", e);
        }
    }

    private List<Essensplan> ladeMitParameter(String sql, int parameter) {
        try (Connection verbindung = ConnectionFactory.getConnection();
             PreparedStatement anweisung = verbindung.prepareStatement(sql)) {

            anweisung.setInt(1, parameter);

            try (ResultSet ergebnis = anweisung.executeQuery()) {
                return ausErgebnis(ergebnis);
            }

        } catch (SQLException e) {
            throw new DAOException("Essensplan konnte nicht geladen werden", e);
        }
    }

    /**
     * Baut aus den Join-Zeilen die Plan-Objekte.
     *
     * Pro Plan kommen bis zu fuenf Zeilen zurueck - eine je Wochentag.
     * Die Map sammelt sie zusammen, damit jeder Plan nur einmal
     * entsteht. LinkedHashMap, damit die Reihenfolge aus dem ORDER BY
     * erhalten bleibt.
     */
    private List<Essensplan> ausErgebnis(ResultSet ergebnis) throws SQLException {
        Map<Integer, Essensplan> plaene = new LinkedHashMap<>();

        while (ergebnis.next()) {
            int planId = ergebnis.getInt("plan_id");

            Essensplan plan = plaene.get(planId);
            if (plan == null) {
                plan = new Essensplan(planId, ergebnis.getInt("wochennummer"));
                plaene.put(planId, plan);
            }

            // Beim LEFT JOIN ist wochentag null, wenn der Plan noch
            // keine Essen hat. Dann gibt es fuer diese Zeile nichts
            // einzutragen.
            String wochentag = ergebnis.getString("wochentag");
            if (wochentag != null) {
                Essen essen = new Essen();
                essen.setId(ergebnis.getInt("essen_id"));
                essen.setName(ergebnis.getString("name"));
                essen.setPreis(ergebnis.getDouble("preis"));
                essen.setArt(Art.valueOf(ergebnis.getString("art")));

                plan.setEssen(Wochentag.valueOf(wochentag), essen);
            }
        }
        return new ArrayList<>(plaene.values());
    }

    // ---------------------------------------------------------------
    // Hilfsmethoden fuer die Transaktion
    // ---------------------------------------------------------------

    private void zurueckrollen(Connection verbindung) {
        if (verbindung == null) {
            return;
        }
        try {
            verbindung.rollback();
        } catch (SQLException e) {
            // Beim Rollback nichts mehr werfen - sonst ueberdeckt es
            // die eigentliche Ursache.
            System.err.println("Rollback fehlgeschlagen: " + e.getMessage());
        }
    }

    private void schliessen(Connection verbindung) {
        if (verbindung == null) {
            return;
        }
        try {
            verbindung.setAutoCommit(true);
            verbindung.close();
        } catch (SQLException e) {
            System.err.println("Verbindung schliessen fehlgeschlagen: " + e.getMessage());
        }
    }
}
