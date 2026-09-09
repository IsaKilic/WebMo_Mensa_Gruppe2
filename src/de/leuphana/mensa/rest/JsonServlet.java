package de.leuphana.mensa.rest;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.persistence.DAOException;
import de.leuphana.mensa.rest.dto.ApiFehler;
import de.leuphana.mensa.service.KeineBerechtigungException;
import de.leuphana.mensa.service.KonfliktException;
import de.leuphana.mensa.service.NichtAngemeldetException;
import de.leuphana.mensa.service.NichtGefundenException;
import de.leuphana.mensa.service.UngueltigeAnmeldungException;
import de.leuphana.mensa.service.ValidierungException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gemeinsame Basisklasse aller JSON-Servlets. Nimmt jedem einzelnen
 * Servlet die Wiederholung derselben HTTP-Arbeit ab: Content-Type
 * setzen, den angemeldeten Benutzer aus der Session lesen, und vor
 * allem die Uebersetzung von Exceptions in HTTP-Statuscodes.
 *
 * Vorher hatte jede doGet/doPost/... -Methode ihren eigenen Aufruf
 * `fehlerbehandlung.ausfuehren(antwort, () -> { ... })`. Hier reicht es,
 * `service(...)` EINMAL zentral zu ueberschreiben: `service(...)` ist die
 * Methode, die Tomcat fuer jede Anfrage aufruft und die intern an
 * doGet/doPost/doPut/doDelete weiterverzweigt (siehe HttpServlet aus der
 * Vorlesung) - legen wir das try/catch dort drumherum, gilt es
 * automatisch fuer alle HTTP-Methoden aller Unterklassen.
 */
public abstract class JsonServlet extends HttpServlet {

    protected ObjectMapper mapper;

    @Override
    public void init() {
        mapper = new ObjectMapper();
    }

    @Override
    protected void service(HttpServletRequest anfrage, HttpServletResponse antwort)
            throws ServletException, IOException {
        antwort.setContentType("application/json");
        antwort.setCharacterEncoding("UTF-8");
        try {
            super.service(anfrage, antwort);
        } catch (ValidierungException | NumberFormatException e) {
            fehler(antwort, 400, "VALIDIERUNG", e.getMessage());
        } catch (UngueltigeAnmeldungException e) {
            fehler(antwort, 401, "ANMELDUNG_FEHLGESCHLAGEN", e.getMessage());
        } catch (NichtAngemeldetException e) {
            fehler(antwort, 401, "NICHT_ANGEMELDET", e.getMessage());
        } catch (KeineBerechtigungException e) {
            fehler(antwort, 403, "KEINE_BERECHTIGUNG", e.getMessage());
        } catch (NichtGefundenException e) {
            fehler(antwort, 404, "NICHT_GEFUNDEN", e.getMessage());
        } catch (KonfliktException e) {
            fehler(antwort, 409, "KONFLIKT", e.getMessage());
        } catch (IllegalStateException e) {
            // Wirft z.B. HttpServletRequest.getPart(...), wenn ein Multipart-Teil
            // groesser ist als @MultipartConfig(maxFileSize = ...) erlaubt.
            fehler(antwort, 413, "DATEI_ZU_GROSS", "Die Datei ist zu gross");
        } catch (DAOException e) {
            fehler(antwort, 500, "SERVERFEHLER", "Technischer Fehler, bitte spaeter erneut versuchen");
        } catch (RuntimeException e) {
            // Auffangnetz: der Client soll immer sauberes JSON bekommen,
            // nie eine rohe Tomcat-Fehlerseite.
            fehler(antwort, 500, "SERVERFEHLER", "Unerwarteter Fehler");
        }
    }

    /** @return der angemeldete Benutzer, oder null wenn keine Session besteht. */
    protected Benutzer angemeldeterBenutzer(HttpServletRequest anfrage) {
        return Anmeldung.angemeldeterBenutzer(anfrage);
    }

    protected void json(HttpServletResponse antwort, Object daten) throws IOException {
        mapper.writeValue(antwort.getWriter(), daten);
    }

    private void fehler(HttpServletResponse antwort, int status, String code, String meldung)
            throws IOException {
        antwort.setStatus(status);
        mapper.writeValue(antwort.getWriter(), new ApiFehler(code, meldung));
    }
}
