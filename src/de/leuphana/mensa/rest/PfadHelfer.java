package de.leuphana.mensa.rest;

import de.leuphana.mensa.service.ValidierungException;

/**
 * Kleine Helfer, um getPathInfo() auszuwerten. Bei @WebServlet("/api/essen/*")
 * liefert getPathInfo() den Teil hinter dem Stern, z.B. null oder "/" fuer
 * "/api/essen", "/5" fuer "/api/essen/5", "/5/bewertungen" fuer die
 * verschachtelte Bewertungsroute - siehe docs/AUFGABE-ROLLE-B.md, Teil 5.
 */
public final class PfadHelfer {

    private PfadHelfer() {
    }

    public static boolean istLeerOderWurzel(String pfad) {
        return pfad == null || pfad.equals("/");
    }

    /** Liest die Id aus "/5" oder "/5/bewertungen" - also das erste Segment. */
    public static int leseId(String pfad) {
        String segment = erstesSegment(pfad);
        try {
            return Integer.parseInt(segment);
        } catch (NumberFormatException e) {
            throw new ValidierungException("Ungueltige Id in der URL: " + segment);
        }
    }

    public static String erstesSegment(String pfad) {
        String ohneFuehrendenSlash = pfad.startsWith("/") ? pfad.substring(1) : pfad;
        int naechsterSlash = ohneFuehrendenSlash.indexOf('/');
        return naechsterSlash < 0 ? ohneFuehrendenSlash : ohneFuehrendenSlash.substring(0, naechsterSlash);
    }

    /** Liefert den Teil nach dem ersten Segment, oder null wenn es keinen gibt. */
    public static String restNachErstemSegment(String pfad) {
        String ohneFuehrendenSlash = pfad.startsWith("/") ? pfad.substring(1) : pfad;
        int naechsterSlash = ohneFuehrendenSlash.indexOf('/');
        return naechsterSlash < 0 ? null : ohneFuehrendenSlash.substring(naechsterSlash + 1);
    }
}
