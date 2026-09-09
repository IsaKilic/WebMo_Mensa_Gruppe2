package de.leuphana.mensa.rest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Baut die Basis-URL der API dynamisch aus dem Context-Path der
 * aktuellen Anfrage, statt sie hart zu codieren. Der Context-Path
 * haengt vom Deployment ab - in docs/AUFGABE-ROLLE-B.md heisst das
 * Eclipse-Projekt "mensa-backend" (Teil 7: ".../mensa-backend/api/essen"),
 * in docs/REST-API.md steht als Beispiel "http://localhost:8080/mensa/api".
 * Damit ein spaeter abweichender Context-Path (z.B. beim Deployment durch
 * ein anderes Teammitglied) nicht zu falschen fotoUrl-Werten fuehrt,
 * wird er hier zur Laufzeit aus der Anfrage gelesen.
 */
public final class BasisUrl {

    private BasisUrl() {
    }

    public static String von(HttpServletRequest anfrage) {
        return anfrage.getContextPath() + "/api";
    }
}
