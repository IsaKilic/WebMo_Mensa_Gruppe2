package de.leuphana.mensa.rest;

import de.leuphana.mensa.model.Benutzer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Liest den angemeldeten Benutzer aus der HttpSession - exakt das
 * Verfahren aus docs/AUFGABE-ROLLE-B.md Teil 5 "Anmeldung ueber Session":
 *
 *     HttpSession sitzung = anfrage.getSession(false);
 *     Benutzer benutzer = (sitzung == null) ? null
 *         : (Benutzer) sitzung.getAttribute("benutzer");
 *
 * getSession(false) statt getSession(true): wir wollen nur eine
 * BESTEHENDE Session lesen, keine neue anlegen, nur weil jemand ohne
 * Cookie eine geschuetzte Seite aufruft.
 *
 * An einer Stelle zusammengefasst, damit LoginServlet, LogoutServlet
 * und alle geschuetzten Endpunkte denselben Attributnamen verwenden.
 */
public final class Anmeldung {

    public static final String SESSION_ATTRIBUT = "benutzer";

    private Anmeldung() {
    }

    /** @return der angemeldete Benutzer, oder null wenn keine Session besteht. */
    public static Benutzer angemeldeterBenutzer(HttpServletRequest anfrage) {
        HttpSession sitzung = anfrage.getSession(false);
        return (sitzung == null) ? null : (Benutzer) sitzung.getAttribute(SESSION_ATTRIBUT);
    }
}
