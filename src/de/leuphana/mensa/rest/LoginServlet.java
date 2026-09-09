package de.leuphana.mensa.rest;

import java.io.IOException;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.rest.dto.BenutzerDTO;
import de.leuphana.mensa.service.AnmeldeService;
import de.leuphana.mensa.service.Fabrik;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * POST /api/login - siehe docs/REST-API.md, Abschnitt "Anmeldung".
 *
 * Legt bei Erfolg eine HttpSession an und legt den Benutzer hinein
 * (SessionTracking per JSESSIONID-Cookie, aus der Vorlesung bekannt).
 * Das Passwort wird nie zurueckgegeben - BenutzerDTO kennt es gar nicht.
 */
@WebServlet("/api/login")
public class LoginServlet extends JsonServlet {

    private AnmeldeService service;

    @Override
    public void init() {
        super.init();
        service = Fabrik.anmeldeService();
    }

    @Override
    protected void doPost(HttpServletRequest anfrage, HttpServletResponse antwort) throws IOException {
        LoginDaten daten = mapper.readValue(anfrage.getReader(), LoginDaten.class);
        Benutzer benutzer = service.anmelden(daten.benutzername, daten.passwort);

        // Eine evtl. schon bestehende Session verwerfen, BEVOR die neue
        // angelegt wird - sonst koennte sich eine fremde Session-ID auf
        // den frisch angemeldeten Benutzer "aufsatteln" (Session Fixation).
        HttpSession alte = anfrage.getSession(false);
        if (alte != null) {
            alte.invalidate();
        }
        HttpSession sitzung = anfrage.getSession(true);
        sitzung.setAttribute(Anmeldung.SESSION_ATTRIBUT, benutzer);

        json(antwort, BenutzerDTO.von(benutzer));
    }

    /** Nur zum Einlesen des JSON-Bodys mit Jackson - braucht oeffentliche Felder oder Getter/Setter. */
    public static class LoginDaten {
        public String benutzername;
        public String passwort;
    }
}
