import de.leuphana.mensa.model.*;
import de.leuphana.mensa.persistence.*;
import de.leuphana.mensa.persistence.jdbc.*;
import java.util.*;

/**
 * Funktionstest aller vier JDBC-DAOs gegen die echte Datenbank.
 *
 * Raeumt zu Beginn auf, damit der Test beliebig oft laufen kann.
 * Die Stammdaten (10 Essen, 2 Benutzer) bleiben unberuehrt - die
 * kommen aus 03_testdaten.sql.
 */
public class TestAlleDAOs {

    static int ok = 0, fail = 0;

    static void pruefe(String was, boolean bedingung) {
        System.out.printf("  [%s] %s%n", bedingung ? "OK " : "FEHL", was);
        if (bedingung) ok++; else fail++;
    }

    public static void main(String[] args) {
        EssenDAO essenDAO = new EssenDAOJdbc();
        BenutzerDAO benDAO = new BenutzerDAOJdbc();
        EssensplanDAO planDAO = new EssensplanDAOJdbc();
        EssensbewertungDAO bewDAO = new EssensbewertungDAOJdbc();

        aufraeumen(essenDAO, planDAO, bewDAO);

        System.out.println("\n=== BenutzerDAOJdbc ===");
        Benutzer admin = benDAO.findByBenutzername("admin");
        pruefe("admin gefunden", admin != null);
        pruefe("admin ist ADMIN", admin != null && admin.istAdmin());
        pruefe("Hash geladen", admin != null && admin.getPasswortHash() != null);

        Benutzer u = benDAO.findByBenutzername("user");
        pruefe("user ist kein Admin", u != null && !u.istAdmin());
        pruefe("findById funktioniert", benDAO.findById(u.getId()) != null);
        pruefe("Unbekannter -> null", benDAO.findByBenutzername("gibtsnicht") == null);

        System.out.println("\n=== EssensplanDAOJdbc: anlegen mit 5 Essen ===");
        List<Essen> alle = essenDAO.findAlle();
        Essensplan plan = new Essensplan(0, 1);
        int i = 0;
        for (Wochentag t : Wochentag.values()) {
            plan.setEssen(t, alle.get(i++));
        }
        planDAO.anlegen(plan);
        pruefe("Id vergeben", plan.getId() > 0);

        Essensplan geladen = planDAO.findByWoche(1);
        pruefe("findByWoche findet Plan", geladen != null);
        pruefe("5 Essen geladen", geladen != null && geladen.getEssenProWoche().size() == 5);
        pruefe("vollstaendig", geladen != null && geladen.istVollstaendig());
        pruefe("Montag korrekt", geladen != null
                && geladen.getEssen(Wochentag.MONTAG).getName().equals(alle.get(0).getName()));
        pruefe("Preis mitgeladen", geladen != null
                && geladen.getEssen(Wochentag.MONTAG).getPreis() > 0);
        pruefe("Art mitgeladen", geladen != null
                && geladen.getEssen(Wochentag.MONTAG).getArt() != null);

        System.out.println("\n=== Essen im Plan aendern und entfernen ===");
        geladen.setEssen(Wochentag.MONTAG, alle.get(9));
        geladen.entferneEssen(Wochentag.FREITAG);
        planDAO.aendern(geladen);
        Essensplan nach = planDAO.findByWoche(1);
        pruefe("Montag ersetzt",
                nach.getEssen(Wochentag.MONTAG).getName().equals(alle.get(9).getName()));
        pruefe("Freitag entfernt", nach.getEssen(Wochentag.FREITAG) == null);
        pruefe("nicht mehr vollstaendig", !nach.istVollstaendig());

        System.out.println("\n=== Leerer Plan (LEFT JOIN) ===");
        Essensplan leer = planDAO.anlegen(new Essensplan(0, 2));
        Essensplan leerGeladen = planDAO.findById(leer.getId());
        pruefe("leerer Plan wird geliefert", leerGeladen != null);
        pruefe("hat 0 Essen", leerGeladen != null && leerGeladen.getEssenProWoche().isEmpty());

        System.out.println("\n=== findAlle ===");
        pruefe("2 Plaene", planDAO.findAlle().size() == 2);

        System.out.println("\n=== Transaktion: Rollback bei ungueltigem Essen ===");
        Essensplan kaputt = new Essensplan(0, 3);
        Essen phantom = new Essen();
        phantom.setId(99999);
        phantom.setName("x");
        phantom.setPreis(1);
        phantom.setArt(Art.VEGAN);
        kaputt.setEssen(Wochentag.MONTAG, phantom);
        try {
            planDAO.anlegen(kaputt);
            pruefe("haette scheitern muessen", false);
        } catch (DAOException e) {
            pruefe("Fehler geworfen", true);
        }
        pruefe("Woche 3 nicht angelegt (Rollback)", planDAO.findByWoche(3) == null);

        System.out.println("\n=== EssensbewertungDAOJdbc ===");
        int essenId = alle.get(0).getId();
        pruefe("Schnitt ohne Bewertungen = 0", bewDAO.durchschnittFuerEssen(essenId) == 0.0);

        Essensbewertung b1 = bewDAO.abgeben(
                new Essensbewertung(0, essenId, u.getId(), 4, "Panade knusprig", "/fotos/a.jpg"));
        bewDAO.abgeben(
                new Essensbewertung(0, essenId, u.getId(), 2, "heute trocken", null));
        pruefe("Id vergeben", b1.getId() > 0);
        pruefe("2 Bewertungen", bewDAO.findByEssen(essenId).size() == 2);
        pruefe("Schnitt = 3.0", bewDAO.durchschnittFuerEssen(essenId) == 3.0);
        pruefe("Zeitpunkt gesetzt", bewDAO.findById(b1.getId()).getZeitpunkt() != null);
        pruefe("Foto null moeglich",
                bewDAO.findByEssen(essenId).get(0).getFotoPfad() == null
             || bewDAO.findByEssen(essenId).get(1).getFotoPfad() == null);

        b1.setSterne(5);
        b1.setText("doch besser als gedacht");
        bewDAO.aendern(b1);
        pruefe("Aenderung gespeichert", bewDAO.findById(b1.getId()).getSterne() == 5);

        try {
            bewDAO.abgeben(new Essensbewertung(0, essenId, u.getId(), 3, "   ", null));
            pruefe("leerer Text haette scheitern muessen", false);
        } catch (DAOException e) {
            pruefe("leerer Text abgelehnt", true);
        }

        System.out.println("\n=== CASCADE ===");
        int planId = nach.getId();
        planDAO.loeschen(planId);
        pruefe("Plan geloescht", planDAO.findById(planId) == null);
        pruefe("Bewertungen unberuehrt", bewDAO.findByEssen(essenId).size() == 2);

        System.out.printf("%n===== %d OK, %d Fehler =====%n", ok, fail);
    }

    /**
     * Loescht Essensplaene und Bewertungen aus vorherigen Testlaeufen.
     * Ohne das scheitert der zweite Lauf an der eindeutigen Wochennummer.
     */
    private static void aufraeumen(EssenDAO essenDAO, EssensplanDAO planDAO,
                                   EssensbewertungDAO bewDAO) {
        for (Essensplan altPlan : planDAO.findAlle()) {
            planDAO.loeschen(altPlan.getId());
        }
        for (Essen essen : essenDAO.findAlle()) {
            for (Essensbewertung altBewertung : bewDAO.findByEssen(essen.getId())) {
                bewDAO.loeschen(altBewertung.getId());
            }
        }
    }
}
