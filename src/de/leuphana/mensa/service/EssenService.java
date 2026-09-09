package de.leuphana.mensa.service;

import java.util.List;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.model.Essensplan;
import de.leuphana.mensa.persistence.EssenDAO;
import de.leuphana.mensa.persistence.EssensbewertungDAO;
import de.leuphana.mensa.persistence.EssensplanDAO;

/**
 * Fachlogik rund um Essen: CRUD mit Rollenpruefung, siehe
 * docs/AUFGABE-ROLLE-B.md Teil 4 und die Rollen-Tabelle in
 * docs/REST-API.md ("Essen und Essensplan aendern": nur Admin;
 * "Lesen (GET)": User oder Admin, Gast nicht).
 *
 * Bekommt alle DAOs ueber den Konstruktor (Dependency Injection) - so
 * kann beim Testen die InMemory-Variante und im Betrieb die Jdbc-
 * Variante eingesetzt werden, ohne dass sich diese Klasse aendert.
 */
public class EssenService {

    private final EssenDAO essenDAO;
    private final EssensbewertungDAO bewertungDAO;
    private final EssensplanDAO essensplanDAO;

    public EssenService(EssenDAO essenDAO, EssensbewertungDAO bewertungDAO,
                         EssensplanDAO essensplanDAO) {
        this.essenDAO = essenDAO;
        this.bewertungDAO = bewertungDAO;
        this.essensplanDAO = essensplanDAO;
    }

    public List<Essen> alleAnzeigen(Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        return essenDAO.findAlle();
    }

    public Essen anzeigen(int essenId, Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        return essenOderFehler(essenId);
    }

    /** @return Durchschnitt der Sterne, 0.0 wenn es noch keine Bewertung gibt. */
    public double durchschnittFuer(int essenId) {
        return bewertungDAO.durchschnittFuerEssen(essenId);
    }

    public int anzahlBewertungenFuer(int essenId) {
        return bewertungDAO.findByEssen(essenId).size();
    }

    public Essen anlegen(Essen essen, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        pruefeGueltig(essen);
        essen.setId(0); // die Id vergibt allein die Persistenzschicht
        return essenDAO.anlegen(essen);
    }

    public Essen aendern(int essenId, Essen geaendert, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        pruefeGueltig(geaendert);
        essenOderFehler(essenId); // wirft NichtGefundenException, wenn es das Essen nicht gibt
        geaendert.setId(essenId);
        essenDAO.aendern(geaendert);
        return geaendert;
    }

    /**
     * @throws KonfliktException wenn das Essen noch in mindestens einem
     *         Essensplan verwendet wird. Entscheidung laut REST-API.md
     *         ("Alternativ im Backend die Zuordnung mitloeschen;
     *         entscheidet euch fuer eines und haltet es durch") - wir
     *         haben uns fuer den 409-Weg entschieden: ein Essensplan
     *         soll nie stillschweigend eine Luecke bekommen.
     */
    public void loeschen(int essenId, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        essenOderFehler(essenId);
        if (wirdInEinemPlanVerwendet(essenId)) {
            throw new KonfliktException(
                    "Essen " + essenId + " wird noch in mindestens einem Essensplan verwendet");
        }
        essenDAO.loeschen(essenId);
    }

    private boolean wirdInEinemPlanVerwendet(int essenId) {
        for (Essensplan plan : essensplanDAO.findAlle()) {
            for (Essen essen : plan.getEssenProWoche().values()) {
                if (essen.getId() == essenId) {
                    return true;
                }
            }
        }
        return false;
    }

    private Essen essenOderFehler(int essenId) {
        Essen essen = essenDAO.findById(essenId);
        if (essen == null) {
            throw new NichtGefundenException("Essen " + essenId + " nicht gefunden");
        }
        return essen;
    }

    private void pruefeGueltig(Essen essen) {
        if (essen.getName() == null || essen.getName().trim().isEmpty()) {
            throw new ValidierungException("Name darf nicht leer sein");
        }
        if (essen.getPreis() < 0) {
            throw new ValidierungException("Preis darf nicht negativ sein");
        }
        if (essen.getArt() == null) {
            throw new ValidierungException("Art muss angegeben werden");
        }
    }

    private void pruefeAngemeldet(Benutzer benutzer) {
        if (benutzer == null) {
            throw new NichtAngemeldetException("Anmeldung erforderlich");
        }
    }

    private void pruefeAdmin(Benutzer benutzer) {
        pruefeAngemeldet(benutzer);
        if (!benutzer.istAdmin()) {
            throw new KeineBerechtigungException("Nur Admin darf Essen anlegen, aendern oder loeschen");
        }
    }
}
