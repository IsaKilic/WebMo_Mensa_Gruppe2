package de.leuphana.mensa.rest;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CORS-Filter fuer alle /api/*-Aufrufe, exakt nach docs/AUFGABE-ROLLE-B.md
 * Teil 5 und docs/REST-API.md Abschnitt "CORS".
 *
 * Warum ueberhaupt noetig: Angular laeuft in der Entwicklung auf Port 4200,
 * Tomcat auf 8080 - unterschiedliche Origins, der Browser blockt die
 * Anfrage per Same-Origin-Policy, wenn der Server nicht ausdruecklich
 * erlaubt, dass 4200 mitlesen darf.
 *
 * Access-Control-Allow-Credentials MUSS "true" sein, sonst schickt der
 * Browser das Session-Cookie (JSESSIONID) nicht mit - und dann darf
 * Allow-Origin laut Spezifikation kein "*" sein, sondern muss die
 * konkrete Adresse nennen.
 *
 * Der Browser schickt vor "unsicheren" Anfragen (PUT/DELETE/POST mit
 * Content-Type application/json, oder generell bei eigenen Headern)
 * automatisch eine OPTIONS-Preflight-Anfrage. Die beantworten wir hier
 * direkt mit 200, ohne sie an das eigentliche Servlet weiterzureichen.
 */
@WebFilter("/api/*")
public class CorsFilter implements Filter {

    private static final String ERLAUBTE_ORIGIN = "http://localhost:4200";

    @Override
    public void doFilter(ServletRequest anfrage, ServletResponse antwort, FilterChain kette)
            throws IOException, ServletException {

        HttpServletResponse http = (HttpServletResponse) antwort;
        http.setHeader("Access-Control-Allow-Origin", ERLAUBTE_ORIGIN);
        http.setHeader("Access-Control-Allow-Credentials", "true");
        http.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        http.setHeader("Access-Control-Allow-Headers", "Content-Type");
        // Der Browser darf das Ergebnis der Preflight-Anfrage eine Stunde
        // lang wiederverwenden, statt vor jedem einzelnen PUT/DELETE erneut
        // per OPTIONS nachzufragen.
        http.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) anfrage).getMethod())) {
            http.setStatus(200);
            return;
        }
        kette.doFilter(anfrage, antwort);
    }
}
