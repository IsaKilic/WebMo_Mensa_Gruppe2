package de.leuphana.mensa.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.leuphana.mensa.model.Essen;

/**
 * JSON-Form eines Essens fuer die REST-Antworten.
 *
 * Bewusst eine eigene Klasse statt Essen direkt zu serialisieren:
 * durchschnittsbewertung/anzahlBewertungen stehen nicht im Model
 * (Essen kennt seine Bewertungen nicht), sondern werden vom
 * EssenService aus zwei DAO-Aufrufen zusammengerechnet - siehe
 * docs/REST-API.md, GET /api/essen: "Die beiden Bewertungsfelder
 * sind berechnet, nicht gespeichert."
 *
 * @JsonInclude(NON_NULL) sorgt dafuer, dass die beiden Felder im JSON
 * komplett fehlen, wenn sie null sind (kompakt() setzt sie nicht) -
 * genau die schlanke Form, die REST-API.md fuer das verschachtelte
 * Essen innerhalb eines Essensplans zeigt.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssenDTO {

    private int id;
    private String name;
    private double preis;
    private String art;
    private Double durchschnittsbewertung;
    private Integer anzahlBewertungen;

    /** Fuer die Verwendung verschachtelt in EssensplanDTO.essenProWoche. */
    public static EssenDTO kompakt(Essen essen) {
        EssenDTO dto = new EssenDTO();
        dto.id = essen.getId();
        dto.name = essen.getName();
        dto.preis = essen.getPreis();
        dto.art = essen.getArt().name();
        return dto;
    }

    /** Fuer GET /api/essen und GET /api/essen/{id} - inklusive Bewertungsfeldern. */
    public static EssenDTO mitBewertung(Essen essen, double durchschnitt, int anzahlBewertungen) {
        EssenDTO dto = kompakt(essen);
        dto.durchschnittsbewertung = Math.round(durchschnitt * 10.0) / 10.0;
        dto.anzahlBewertungen = anzahlBewertungen;
        return dto;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPreis() { return preis; }
    public String getArt() { return art; }
    public Double getDurchschnittsbewertung() { return durchschnittsbewertung; }
    public Integer getAnzahlBewertungen() { return anzahlBewertungen; }
}
