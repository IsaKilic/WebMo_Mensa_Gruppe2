package de.leuphana.mensa.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.Part;

/**
 * Kleiner Helfer fuer Multipart-Anfragen (Foto-Upload), siehe
 * docs/REST-API.md "POST /api/essen/{id}/bewertungen": der Teil
 * "daten" enthaelt JSON als Text, der Teil "foto" die Bilddatei.
 * Part liefert nur einen InputStream - dieser Helfer liest den
 * Text-Teil vollstaendig ein, damit er mit Jackson geparst werden kann.
 */
final class ApiAnfrage {

    private ApiAnfrage() {
    }

    static String leseTeilAlsText(Part teil) throws IOException {
        ByteArrayOutputStream puffer = new ByteArrayOutputStream();
        teil.getInputStream().transferTo(puffer);
        return puffer.toString(StandardCharsets.UTF_8);
    }
}
