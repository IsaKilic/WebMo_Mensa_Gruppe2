package de.leuphana.mensa.rest.dto;

import de.leuphana.mensa.model.Benutzer;

/**
 * JSON-Form eines Benutzers. Enthaelt bewusst NIE das Passwort oder den
 * Hash - siehe docs/REST-API.md "Das Passwort wird nie zurueckgegeben."
 * Verwendet fuer POST /api/login und GET /api/session.
 */
public class BenutzerDTO {

    private int id;
    private String benutzername;
    private String rolle;

    public static BenutzerDTO von(Benutzer benutzer) {
        BenutzerDTO dto = new BenutzerDTO();
        dto.id = benutzer.getId();
        dto.benutzername = benutzer.getBenutzername();
        dto.rolle = benutzer.getRolle().name();
        return dto;
    }

    public int getId() { return id; }
    public String getBenutzername() { return benutzername; }
    public String getRolle() { return rolle; }
}
