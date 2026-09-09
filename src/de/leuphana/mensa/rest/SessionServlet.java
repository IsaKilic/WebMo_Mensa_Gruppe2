package de.leuphana.mensa.rest;

import java.io.IOException;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.rest.dto.BenutzerDTO;
import de.leuphana.mensa.service.NichtAngemeldetException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * GET /api/session - siehe docs/REST-API.md: "Liefert den angemeldeten
 * Benutzer oder 401. Die Frontends rufen das beim Start auf, um zu
 * wissen, ob noch eine Sitzung besteht."
 */
@WebServlet("/api/session")
public class SessionServlet extends JsonServlet {

    @Override
    protected void doGet(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        Benutzer benutzer = angemeldeterBenutzer(anfrage);
        if (benutzer == null) {
            throw new NichtAngemeldetException("Keine aktive Sitzung");
        }
        json(antwort, BenutzerDTO.von(benutzer));
    }
}
