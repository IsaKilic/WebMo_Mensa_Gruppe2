package de.leuphana.mensa.rest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * POST /api/logout - siehe docs/REST-API.md: "204. Invalidiert die Session."
 * Braucht keine Service-Schicht, invalidate() gehoert zur Servlet-API selbst.
 */
@WebServlet("/api/logout")
public class LogoutServlet extends JsonServlet {

    @Override
    protected void doPost(HttpServletRequest anfrage, HttpServletResponse antwort) {
        HttpSession sitzung = anfrage.getSession(false);
        if (sitzung != null) {
            sitzung.invalidate();
        }
        antwort.setStatus(204);
    }
}
