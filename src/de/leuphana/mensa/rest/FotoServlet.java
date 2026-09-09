package de.leuphana.mensa.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.service.Fabrik;
import de.leuphana.mensa.service.FotoSpeicher;
import de.leuphana.mensa.service.NichtGefundenException;
import de.leuphana.mensa.service.ValidierungException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * GET /api/fotos/{dateiname} - liefert die Bilddatei mit passendem
 * Content-Type, siehe docs/REST-API.md. Den Dateinamen prueft
 * FotoSpeicher.pfadZu(...) per Whitelist gegen Path Traversal.
 *
 * Erbt bewusst NICHT von JsonServlet: die Antwort ist Bilddaten, kein
 * JSON, ein JSON-Fehlerkoerper waere hier fehl am Platz. Fehler werden
 * deshalb direkt mit HttpServletResponse.sendError(...) beantwortet.
 *
 * Wie die Bewertungen selbst nur fuer angemeldete Benutzer sichtbar
 * (Gast darf laut Rollen-Tabelle in REST-API.md nirgends lesen).
 */
@WebServlet("/api/fotos/*")
public class FotoServlet extends HttpServlet {

    private FotoSpeicher fotoSpeicher;

    @Override
    public void init() {
        fotoSpeicher = Fabrik.fotoSpeicher();
    }

    @Override
    protected void doGet(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        Benutzer angemeldet = Anmeldung.angemeldeterBenutzer(anfrage);
        if (angemeldet == null) {
            antwort.sendError(401, "Anmeldung erforderlich");
            return;
        }

        String pfad = anfrage.getPathInfo();
        if (PfadHelfer.istLeerOderWurzel(pfad)) {
            antwort.sendError(404, "Kein Dateiname angegeben");
            return;
        }
        String dateiname = pfad.substring(1);

        try {
            Path datei = fotoSpeicher.pfadZu(dateiname);
            antwort.setContentType(contentTypeFuer(dateiname));
            try (OutputStream ausgabe = antwort.getOutputStream()) {
                Files.copy(datei, ausgabe);
            }
        } catch (NichtGefundenException e) {
            antwort.sendError(404, e.getMessage());
        } catch (ValidierungException e) {
            antwort.sendError(400, e.getMessage());
        }
    }

    private String contentTypeFuer(String dateiname) {
        String name = dateiname.toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        return "image/jpeg"; // deckt .jpg und .jpeg ab
    }
}
