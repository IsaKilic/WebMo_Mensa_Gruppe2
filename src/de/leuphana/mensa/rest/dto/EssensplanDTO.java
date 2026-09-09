package de.leuphana.mensa.rest.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.model.Essensplan;
import de.leuphana.mensa.model.Wochentag;

/**
 * JSON-Form eines Essensplans, siehe docs/REST-API.md GET
 * /api/essensplaene?woche=3: ein Objekt mit den Wochentagen als
 * Schluessel (nicht als Liste), damit im JSON direkt sichtbar ist,
 * dass pro Tag genau ein Essen steht.
 *
 * "vollstaendig" kommt direkt von Essensplan.istVollstaendig() - die
 * Methode existiert bereits im Model, wir rechnen sie hier nicht
 * nochmal selbst nach.
 */
public class EssensplanDTO {

    private int id;
    private int wochennummer;
    private Map<String, EssenDTO> essenProWoche = new LinkedHashMap<>();
    private boolean vollstaendig;

    public static EssensplanDTO von(Essensplan plan) {
        EssensplanDTO dto = new EssensplanDTO();
        dto.id = plan.getId();
        dto.wochennummer = plan.getWochennummer();
        for (Wochentag tag : Wochentag.values()) {
            Essen essen = plan.getEssen(tag);
            if (essen != null) {
                dto.essenProWoche.put(tag.name(), EssenDTO.kompakt(essen));
            }
            // fehlt der Tag, bleibt der Schluessel im JSON komplett weg -
            // genau das Verhalten, das REST-API.md beschreibt ("Fehlt ein
            // Tag, fehlt der Schluessel").
        }
        dto.vollstaendig = plan.istVollstaendig();
        return dto;
    }

    public int getId() { return id; }
    public int getWochennummer() { return wochennummer; }
    public Map<String, EssenDTO> getEssenProWoche() { return essenProWoche; }
    public boolean isVollstaendig() { return vollstaendig; }
}
