package de.leuphana.mensa.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.model.Essensbewertung;
import de.leuphana.mensa.rest.dto.EssenDTO;
import de.leuphana.mensa.rest.dto.EssensbewertungDTO;
import de.leuphana.mensa.service.BewertungService;
import de.leuphana.mensa.service.EssenService;
import de.leuphana.mensa.service.Fabrik;
import de.leuphana.mensa.service.ValidierungException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * REST-Endpunkte fuer Essen, inklusive der darunter verschachtelten
 * Bewertungsrouten - siehe docs/REST-API.md, Abschnitte "Essen" und
 * "Essensbewertung" (GET/POST .../bewertungen haengt bewusst hier und
 * nicht in EssensbewertungServlet, weil die URL /api/essen/{id}/... ist).
 *
 * @WebServlet("/api/essen/*") mit Sternchen: sowohl /api/essen als auch
 * /api/essen/5 und /api/essen/5/bewertungen landen hier,
 * getPathInfo() liefert dann null, "/5" bzw. "/5/bewertungen".
 *
 * @MultipartConfig ist noetig, weil POST .../bewertungen multipart/form-data
 * schickt (Foto-Upload). Ohne maxFileSize wuerde Tomcat gar kein Limit
 * durchsetzen - das 413 aus REST-API.md haette dann keine Wirkung.
 */
@WebServlet("/api/essen/*")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024 + 100_000)
public class EssenServlet extends JsonServlet {

    private EssenService essenService;
    private BewertungService bewertungService;

    @Override
    public void init() {
        super.init();
        // Beide Services kommen aus der Fabrik - alle Servlets teilen
        // sich dieselben DAO-Instanzen, siehe Fabrik.java fuer die
        // Begruendung (loest das DAO-Sharing-Problem des reinen
        // per-Servlet-new()-Musters).
        essenService = Fabrik.essenService();
        bewertungService = Fabrik.bewertungService();
    }

    @Override
    protected void doGet(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        String pfad = anfrage.getPathInfo();
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        if (PfadHelfer.istLeerOderWurzel(pfad)) {
            antwortenMitAllenEssen(antwort, angemeldet);
        } else if (pfad.endsWith("/bewertungen")) {
            int essenId = PfadHelfer.leseId(pfad);
            antwortenMitBewertungen(anfrage, antwort, essenId, angemeldet);
        } else {
            int essenId = PfadHelfer.leseId(pfad);
            antwortenMitEinemEssen(antwort, essenId, angemeldet);
        }
    }

    @Override
    protected void doPost(HttpServletRequest anfrage, HttpServletResponse antwort)
            throws IOException, ServletException {
        String pfad = anfrage.getPathInfo();
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        if (pfad != null && pfad.endsWith("/bewertungen")) {
            int essenId = PfadHelfer.leseId(pfad);
            bewertungAbgeben(anfrage, antwort, essenId, angemeldet);
        } else {
            Essen neu = mapper.readValue(anfrage.getReader(), Essen.class);
            Essen angelegt = essenService.anlegen(neu, angemeldet);
            antwort.setStatus(201);
            json(antwort, EssenDTO.mitBewertung(angelegt, 0.0, 0));
        }
    }

    @Override
    protected void doPut(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        String pfad = anfrage.getPathInfo();
        int essenId = PfadHelfer.leseId(pfad);
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        Essen geaendert = mapper.readValue(anfrage.getReader(), Essen.class);
        Essen ergebnis = essenService.aendern(essenId, geaendert, angemeldet);

        json(antwort, EssenDTO.mitBewertung(ergebnis,
                essenService.durchschnittFuer(essenId), essenService.anzahlBewertungenFuer(essenId)));
    }

    @Override
    protected void doDelete(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        String pfad = anfrage.getPathInfo();
        int essenId = PfadHelfer.leseId(pfad);
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        essenService.loeschen(essenId, angemeldet);
        antwort.setStatus(204);
    }

    private void antwortenMitAllenEssen(HttpServletResponse antwort, Benutzer angemeldet) throws IOException {
        List<Essen> alle = essenService.alleAnzeigen(angemeldet);
        List<EssenDTO> dtos = new ArrayList<>();
        for (Essen essen : alle) {
            dtos.add(EssenDTO.mitBewertung(essen,
                    essenService.durchschnittFuer(essen.getId()),
                    essenService.anzahlBewertungenFuer(essen.getId())));
        }
        json(antwort, dtos);
    }

    private void antwortenMitEinemEssen(HttpServletResponse antwort, int essenId, Benutzer angemeldet)
            throws IOException {
        Essen essen = essenService.anzeigen(essenId, angemeldet);
        json(antwort, EssenDTO.mitBewertung(essen,
                essenService.durchschnittFuer(essenId), essenService.anzahlBewertungenFuer(essenId)));
    }

    private void antwortenMitBewertungen(HttpServletRequest anfrage, HttpServletResponse antwort,
                                          int essenId, Benutzer angemeldet) throws IOException {
        List<Essensbewertung> bewertungen = bewertungService.bewertungenFuerEssen(essenId, angemeldet);
        String basisUrl = BasisUrl.von(anfrage);
        List<EssensbewertungDTO> dtos = new ArrayList<>();
        for (Essensbewertung b : bewertungen) {
            dtos.add(EssensbewertungDTO.von(b, bewertungService.benutzernameFuer(b.getBenutzerId()), basisUrl));
        }
        json(antwort, dtos);
    }

    private void bewertungAbgeben(HttpServletRequest anfrage, HttpServletResponse antwort,
                                   int essenId, Benutzer angemeldet) throws IOException, ServletException {
        Part datenTeil = anfrage.getPart("daten");
        Part fotoTeil = anfrage.getPart("foto");
        if (datenTeil == null) {
            throw new ValidierungException("Der Teil 'daten' (sterne, text) fehlt");
        }
        BewertungsDaten daten = mapper.readValue(ApiAnfrage.leseTeilAlsText(datenTeil), BewertungsDaten.class);

        InputStream fotoStream = null;
        String fotoDateiname = null;
        if (fotoTeil != null && fotoTeil.getSize() > 0) {
            fotoStream = fotoTeil.getInputStream();
            fotoDateiname = fotoTeil.getSubmittedFileName();
        }

        Essensbewertung angelegt = bewertungService.abgeben(
                essenId, daten.sterne, daten.text, fotoStream, fotoDateiname, angemeldet);

        antwort.setStatus(201);
        json(antwort, EssensbewertungDTO.von(
                angelegt, bewertungService.benutzernameFuer(angelegt.getBenutzerId()), BasisUrl.von(anfrage)));
    }

    /** Nur zum Einlesen des JSON-Teils "daten" mit Jackson. */
    public static class BewertungsDaten {
        public int sterne;
        public String text;
    }
}
