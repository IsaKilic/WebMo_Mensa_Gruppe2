package de.leuphana.mensa.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fachklasse Essensplan.
 *
 * Attribute laut Aufgabenstellung: EssenProWoche (5 Essen fuer Mo-Fr)
 * und Wochennummer.
 *
 * Die EnumMap bildet "5 Essen fuer Mo-Fr" direkt ab: pro Wochentag
 * genau ein Essen, mehr geht strukturell nicht. Eine Liste koennte
 * sieben oder drei Eintraege haben - die EnumMap kann das nicht.
 */
public class Essensplan {

    private int id;
    private int wochennummer;
    private Map<Wochentag, Essen> essenProWoche = new EnumMap<>(Wochentag.class);

    public Essensplan() {
    }

    public Essensplan(int id, int wochennummer) {
        this.id = id;
        this.wochennummer = wochennummer;
    }

    /** Essen fuer einen Wochentag setzen oder ersetzen. */
    public void setEssen(Wochentag tag, Essen essen) {
        if (essen == null) {
            essenProWoche.remove(tag);
        } else {
            essenProWoche.put(tag, essen);
        }
    }

    /** Essen eines Wochentags entfernen. */
    public void entferneEssen(Wochentag tag) {
        essenProWoche.remove(tag);
    }

    public Essen getEssen(Wochentag tag) {
        return essenProWoche.get(tag);
    }

    /** true, wenn alle fuenf Wochentage belegt sind. */
    public boolean istVollstaendig() {
        return essenProWoche.size() == Wochentag.values().length;
    }

    public int getId()                          { return id; }
    public int getWochennummer()                { return wochennummer; }
    public Map<Wochentag, Essen> getEssenProWoche() { return essenProWoche; }

    public void setId(int id)                        { this.id = id; }
    public void setWochennummer(int wochennummer)    { this.wochennummer = wochennummer; }
    public void setEssenProWoche(Map<Wochentag, Essen> essenProWoche) {
        this.essenProWoche = (essenProWoche == null)
                ? new EnumMap<>(Wochentag.class) : new EnumMap<>(essenProWoche);
    }

    @Override
    public String toString() {
        return "Essensplan[KW " + wochennummer + ", " + essenProWoche.size() + "/5 Essen]";
    }
}
