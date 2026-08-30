import java.util.List;

import de.leuphana.mensa.model.Art;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.persistence.EssenDAO;
import de.leuphana.mensa.persistence.jdbc.EssenDAOJdbc;

/**
 * Schneller Funktionstest gegen die echte Datenbank.
 *
 * In Eclipse: Rechtsklick -> Run As -> Java Application.
 * Legt ein Testessen an, aendert es und loescht es wieder.
 */
public class TestEssenDAOJdbc {

    public static void main(String[] args) {
        EssenDAO dao = new EssenDAOJdbc();

        System.out.println("Vorhandene Essen: " + dao.findAlle().size());

        Essen neu = dao.anlegen(new Essen(0, "Testgericht", 1.99, Art.VEGAN));
        System.out.println("Angelegt mit Id " + neu.getId());
        if (neu.getId() == 0) {
            System.out.println("FEHLER: keine Id von der Datenbank zurueckbekommen");
            return;
        }

        Essen geladen = dao.findById(neu.getId());
        System.out.println("Geladen: " + geladen);

        geladen.setPreis(2.49);
        geladen.setName("Testgericht geaendert");
        dao.aendern(geladen);
        System.out.println("Nach Aenderung: " + dao.findById(neu.getId()));

        List<Essen> alle = dao.findAlle();
        System.out.println("Jetzt insgesamt: " + alle.size());

        System.out.println("Geloescht: " + dao.loeschen(neu.getId()));
        System.out.println("Nach Loeschen: " + dao.findById(neu.getId()) + " (null erwartet)");
        System.out.println("Unbekannte Id 99999: " + dao.findById(99999) + " (null erwartet)");

        System.out.println("\nAlles durchgelaufen.");
    }
}
