package de.leuphana.mensa.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Essensplan;
import de.leuphana.mensa.model.Wochentag;
import de.leuphana.mensa.rest.dto.EssensplanDTO;
import de.leuphana.mensa.service.EssensplanService;
import de.leuphana.mensa.service.Fabrik;
import de.leuphana.mensa.service.ValidierungException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST-Endpunkte fuer Essensplaene, siehe docs/REST-API.md, Abschnitt
 * "Essensplan". Deckt auch die verschachtelten Tages-Routen ab
 * (PUT/DELETE .../{id}/tage/{wochentag}), weil die URL unter
 * /api/essensplaene/* liegt.
 */
@WebServlet("/api/essensplaene/*")
public class EssensplanServlet extends JsonServlet {

    private EssensplanService service;

    @Override
    public void init() {
        super.init();
        service = Fabrik.essensplanService();
    }

    @Override
    protected void doGet(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        String pfad = anfrage.getPathInfo();
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);
        String wocheParameter = anfrage.getParameter("woche");

        if (PfadHelfer.istLeerOderWurzel(pfad)) {
            if (wocheParameter != null) {
                antworteMitPlan(antwort, service.planFuerWoche(parseWoche(wocheParameter), angemeldet));
            } else {
                antworteMitAllenPlaenen(antwort, angemeldet);
            }
        } else {
            int planId = PfadHelfer.leseId(pfad);
            antworteMitPlan(antwort, service.planFinden(planId, angemeldet));
        }
    }

    @Override
    protected void doPost(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);
        NeuerPlan daten = mapper.readValue(anfrage.getReader(), NeuerPlan.class);
        Essensplan angelegt = service.anlegen(daten.wochennummer, angemeldet);
        antwort.setStatus(201);
        antworteMitPlan(antwort, angelegt);
    }

    @Override
    protected void doPut(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        String pfad = anfrage.getPathInfo();
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        if (pfad != null && pfad.contains("/tage/")) {
            behandleTagSetzen(anfrage, antwort, pfad, angemeldet);
        } else {
            int planId = PfadHelfer.leseId(pfad);
            NeuerPlan daten = mapper.readValue(anfrage.getReader(), NeuerPlan.class);
            // Wichtig: wir laden den BESTEHENDEN Plan und aendern nur die
            // Wochennummer darauf - wir lesen KEIN komplettes Essensplan-
            // Objekt aus dem Body ein. Wuerde man stattdessen ein frisches
            // Essensplan-Objekt aus dem (nur {"wochennummer": n} enthaltenden)
            // Body bauen und das 1:1 speichern, waeren alle bisher gesetzten
            // Tage (essenProWoche) weg, weil das frische Objekt dort leer ist.
            Essensplan geaendert = service.wochennummerAendern(planId, daten.wochennummer, angemeldet);
            antworteMitPlan(antwort, geaendert);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        String pfad = anfrage.getPathInfo();
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        if (pfad != null && pfad.contains("/tage/")) {
            int planId = PfadHelfer.leseId(pfad);
            Wochentag tag = leseWochentag(pfad);
            service.essenFuerTagEntfernen(planId, tag, angemeldet);
            antwort.setStatus(204);
        } else {
            int planId = PfadHelfer.leseId(pfad);
            service.loeschen(planId, angemeldet);
            antwort.setStatus(204);
        }
    }

    private void behandleTagSetzen(HttpServletRequest anfrage, HttpServletResponse antwort,
                                    String pfad, Benutzer angemeldet) throws IOException {
        int planId = PfadHelfer.leseId(pfad);
        Wochentag tag = leseWochentag(pfad);
        EssenIdDaten daten = mapper.readValue(anfrage.getReader(), EssenIdDaten.class);
        Essensplan geaendert = service.essenFuerTagSetzen(planId, tag, daten.essenId, angemeldet);
        antworteMitPlan(antwort, geaendert);
    }

    /** Liest "{wochentag}" aus ".../tage/{wochentag}" - z.B. "MONTAG" aus "/3/tage/MONTAG". */
    private Wochentag leseWochentag(String pfad) {
        String[] teile = pfad.split("/tage/");
        if (teile.length < 2 || teile[1].isEmpty()) {
            throw new ValidierungException("Wochentag fehlt in der URL");
        }
        try {
            return Wochentag.valueOf(teile[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidierungException("Unbekannter Wochentag: " + teile[1]
                    + " (erlaubt: MONTAG bis FREITAG)");
        }
    }

    private int parseWoche(String wert) {
        try {
            return Integer.parseInt(wert);
        } catch (NumberFormatException e) {
            throw new ValidierungException("Ungueltige Wochennummer: " + wert);
        }
    }

    private void antworteMitAllenPlaenen(HttpServletResponse antwort, Benutzer angemeldet) throws IOException {
        List<Essensplan> plaene = service.allePlaene(angemeldet);
        List<EssensplanDTO> dtos = new ArrayList<>();
        for (Essensplan plan : plaene) {
            dtos.add(EssensplanDTO.von(plan));
        }
        json(antwort, dtos);
    }

    private void antworteMitPlan(HttpServletResponse antwort, Essensplan plan) throws IOException {
        json(antwort, EssensplanDTO.von(plan));
    }

    public static class NeuerPlan {
        public int wochennummer;
    }

    public static class EssenIdDaten {
        public int essenId;
    }
}
