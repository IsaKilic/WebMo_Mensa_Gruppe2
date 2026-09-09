package de.leuphana.mensa.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.model.Essensplan;
import de.leuphana.mensa.model.Wochentag;
import de.leuphana.mensa.persistence.EssenDAO;
import de.leuphana.mensa.persistence.EssensplanDAO;

/**
 * Fachlogik rund um Essensplaene: CRUD, Wochenfilter, Essen pro Tag
 * setzen/entfernen. Klassenname wie in docs/AUFGABE-ROLLE-B.md Teil 4
 * vorgeschlagen.
 */
public class EssensplanService {

    private static final int MIN_WOCHENNUMMER = 1;
    private static final int MAX_WOCHENNUMMER = 53;

    private final EssensplanDAO essensplanDAO;
    private final EssenDAO essenDAO;

    public EssensplanService(EssensplanDAO essensplanDAO, EssenDAO essenDAO) {
        this.essensplanDAO = essensplanDAO;
        this.essenDAO = essenDAO;
    }

    /** Alle Plaene, aufsteigend nach Wochennummer - siehe REST-API.md "GET /api/essensplaene". */
    public List<Essensplan> allePlaene(Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        List<Essensplan> plaene = new ArrayList<>(essensplanDAO.findAlle());
        plaene.sort(Comparator.comparingInt(Essensplan::getWochennummer));
        return plaene;
    }

    public Essensplan planFuerWoche(int wochennummer, Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        Essensplan plan = essensplanDAO.findByWoche(wochennummer);
        if (plan == null) {
            throw new NichtGefundenException("Kein Essensplan fuer Woche " + wochennummer);
        }
        return plan;
    }

    public Essensplan planFinden(int essensplanId, Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        return planOderFehler(essensplanId);
    }

    /**
     * @throws ValidierungException wenn die Wochennummer ausserhalb 1-53 liegt.
     * @throws KonfliktException wenn es die Wochennummer schon gibt.
     */
    public Essensplan anlegen(int wochennummer, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        pruefeWochennummer(wochennummer);
        if (essensplanDAO.findByWoche(wochennummer) != null) {
            throw new KonfliktException("Fuer Wochennummer " + wochennummer + " existiert bereits ein Plan");
        }
        return essensplanDAO.anlegen(new Essensplan(0, wochennummer));
    }

    public Essensplan wochennummerAendern(int essensplanId, int neueWochennummer, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        pruefeWochennummer(neueWochennummer);
        Essensplan plan = planOderFehler(essensplanId);
        Essensplan belegtVon = essensplanDAO.findByWoche(neueWochennummer);
        if (belegtVon != null && belegtVon.getId() != essensplanId) {
            throw new KonfliktException("Fuer Wochennummer " + neueWochennummer + " existiert bereits ein Plan");
        }
        plan.setWochennummer(neueWochennummer);
        essensplanDAO.aendern(plan);
        return plan;
    }

    public void loeschen(int essensplanId, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        planOderFehler(essensplanId);
        essensplanDAO.loeschen(essensplanId);
    }

    /** PUT /api/essensplaene/{id}/tage/{wochentag} - Essen setzen oder ersetzen. */
    public Essensplan essenFuerTagSetzen(int essensplanId, Wochentag tag, int essenId, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        Essensplan plan = planOderFehler(essensplanId);
        Essen essen = essenDAO.findById(essenId);
        if (essen == null) {
            throw new NichtGefundenException("Essen " + essenId + " nicht gefunden");
        }
        plan.setEssen(tag, essen);
        essensplanDAO.aendern(plan);
        return plan;
    }

    /** DELETE /api/essensplaene/{id}/tage/{wochentag} - Essen des Tages entfernen. */
    public Essensplan essenFuerTagEntfernen(int essensplanId, Wochentag tag, Benutzer angemeldet) {
        pruefeAdmin(angemeldet);
        Essensplan plan = planOderFehler(essensplanId);
        plan.entferneEssen(tag);
        essensplanDAO.aendern(plan);
        return plan;
    }

    private Essensplan planOderFehler(int essensplanId) {
        Essensplan plan = essensplanDAO.findById(essensplanId);
        if (plan == null) {
            throw new NichtGefundenException("Essensplan " + essensplanId + " nicht gefunden");
        }
        return plan;
    }

    private void pruefeWochennummer(int wochennummer) {
        if (wochennummer < MIN_WOCHENNUMMER || wochennummer > MAX_WOCHENNUMMER) {
            throw new ValidierungException(
                    "Wochennummer muss zwischen " + MIN_WOCHENNUMMER + " und " + MAX_WOCHENNUMMER + " liegen");
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
            throw new KeineBerechtigungException("Nur Admin darf Essensplaene aendern");
        }
    }
}
