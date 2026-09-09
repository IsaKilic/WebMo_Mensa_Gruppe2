package de.leuphana.mensa.rest;

import java.io.IOException;
import java.io.InputStream;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Essensbewertung;
import de.leuphana.mensa.rest.dto.EssensbewertungDTO;
import de.leuphana.mensa.service.BewertungService;
import de.leuphana.mensa.service.Fabrik;
import de.leuphana.mensa.service.ValidierungException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * PUT/DELETE /api/bewertungen/{id} - siehe docs/REST-API.md, Abschnitt
 * "Essensbewertung". Das Anlegen (POST) und Lesen einer Liste (GET)
 * liegt bewusst in EssenServlet, weil dafuer die URL
 * /api/essen/{id}/bewertungen lautet - siehe EssenServlet.
 */
@WebServlet("/api/bewertungen/*")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024 + 100_000)
public class EssensbewertungServlet extends JsonServlet {

    private BewertungService service;

    @Override
    public void init() {
        super.init();
        service = Fabrik.bewertungService();
    }

    @Override
    protected void doPut(HttpServletRequest anfrage, HttpServletResponse antwort)
            throws IOException, ServletException {
        int bewertungId = PfadHelfer.leseId(anfrage.getPathInfo());
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);

        Part datenTeil = anfrage.getPart("daten");
        if (datenTeil == null) {
            throw new ValidierungException("Der Teil 'daten' (sterne, text) fehlt");
        }
        EssenServlet.BewertungsDaten daten = mapper.readValue(
                ApiAnfrage.leseTeilAlsText(datenTeil), EssenServlet.BewertungsDaten.class);

        // Foto darf fehlen - dann bleibt laut REST-API.md das alte Bild.
        Part fotoTeil = anfrage.getPart("foto");
        InputStream fotoStream = null;
        String fotoDateiname = null;
        if (fotoTeil != null && fotoTeil.getSize() > 0) {
            fotoStream = fotoTeil.getInputStream();
            fotoDateiname = fotoTeil.getSubmittedFileName();
        }

        Essensbewertung geaendert = service.aendern(
                bewertungId, daten.sterne, daten.text, fotoStream, fotoDateiname, angemeldet);

        json(antwort, EssensbewertungDTO.von(
                geaendert, service.benutzernameFuer(geaendert.getBenutzerId()), BasisUrl.von(anfrage)));
    }

    @Override
    protected void doDelete(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        int bewertungId = PfadHelfer.leseId(anfrage.getPathInfo());
        Benutzer angemeldet = angemeldeterBenutzer(anfrage);
        service.loeschen(bewertungId, angemeldet);
        antwort.setStatus(204);
    }
}
