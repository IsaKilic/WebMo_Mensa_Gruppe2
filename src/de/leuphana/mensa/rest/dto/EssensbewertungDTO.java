package de.leuphana.mensa.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.leuphana.mensa.model.Essensbewertung;

/**
 * JSON-Form einer Essensbewertung, siehe docs/REST-API.md GET
 * /api/essen/{id}/bewertungen. Zeigt den Benutzernamen statt der Id
 * ("damit die Frontends ihn direkt anzeigen koennen") und eine fertige
 * fotoUrl statt nur des Dateinamens.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssensbewertungDTO {

    private int id;
    private int essenId;
    private String benutzer;
    private int sterne;
    private String text;
    private String fotoUrl;
    private String zeitpunkt;

    /**
     * @param benutzername vom Service ueber BenutzerDAO.findById aufgeloest
     * @param basisUrl z.B. "/mensa-backend/api", siehe BasisUrl.von(request) -
     *        bewusst nicht hart codiert, weil der Context-Path je nach
     *        Deployment unterschiedlich heissen kann.
     */
    public static EssensbewertungDTO von(Essensbewertung bewertung, String benutzername, String basisUrl) {
        EssensbewertungDTO dto = new EssensbewertungDTO();
        dto.id = bewertung.getId();
        dto.essenId = bewertung.getEssenId();
        dto.benutzer = benutzername;
        dto.sterne = bewertung.getSterne();
        dto.text = bewertung.getText();
        dto.fotoUrl = (bewertung.getFotoPfad() == null)
                ? null
                : basisUrl + "/fotos/" + bewertung.getFotoPfad();
        dto.zeitpunkt = (bewertung.getZeitpunkt() == null) ? null : bewertung.getZeitpunkt().toString();
        return dto;
    }

    public int getId() { return id; }
    public int getEssenId() { return essenId; }
    public String getBenutzer() { return benutzer; }
    public int getSterne() { return sterne; }
    public String getText() { return text; }
    public String getFotoUrl() { return fotoUrl; }
    public String getZeitpunkt() { return zeitpunkt; }
}
