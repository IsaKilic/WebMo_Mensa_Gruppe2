package de.leuphana.mensa.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Der Speiseplan einer Mensa an einem Tag.
 *
 * Diese Klasse ist bewusst mehr als eine Liste: hier wohnt die Fachlogik
 * zum Filtern und Gruppieren. Analog zu Catalog aus Inkrement11, das auch
 * mehr war als eine Map. Läge diese Logik im Servlet oder in der JSP,
 * müssten Website und App sie doppelt implementieren.
 */
public class Speiseplan {

    private Mensa mensa;
    private LocalDate datum;
    private List<Gericht> gerichte = new ArrayList<>();

    public Speiseplan() {
    }

    public Speiseplan(Mensa mensa, LocalDate datum) {
        this.mensa = mensa;
        this.datum = datum;
    }

    public void addGericht(Gericht gericht) {
        gerichte.add(gericht);
    }

    /** Ein einzelnes Gericht anhand seiner Id, oder null. */
    public Gericht getGericht(int gerichtId) {
        for (Gericht gericht : gerichte) {
            if (gericht.getId() == gerichtId) {
                return gericht;
            }
        }
        return null;
    }

    /** Alle Gerichte einer Kategorie. */
    public List<Gericht> getGerichteNachKategorie(Kategorie kategorie) {
        List<Gericht> treffer = new ArrayList<>();
        for (Gericht gericht : gerichte) {
            if (gericht.getKategorie() == kategorie) {
                treffer.add(gericht);
            }
        }
        return treffer;
    }

    /**
     * Nach Kategorie gruppiert, in der Reihenfolge des Enums.
     * Praktisch für die Anzeige in JSP und App.
     */
    public Map<Kategorie, List<Gericht>> gruppiertNachKategorie() {
        Map<Kategorie, List<Gericht>> gruppen = new EnumMap<>(Kategorie.class);
        for (Gericht gericht : gerichte) {
            gruppen.computeIfAbsent(gericht.getKategorie(), k -> new ArrayList<>())
                   .add(gericht);
        }
        return gruppen;
    }

    /**
     * Kernstück der Filterung.
     *
     * @param gefordert       Kennzeichnungen, die das Gericht tragen muss
     *                        (z.B. VEGAN). Leer oder null bedeutet: egal.
     * @param auszuschliessen Allergene, die das Gericht nicht enthalten darf.
     *                        Leer oder null bedeutet: egal.
     */
    public List<Gericht> filter(Set<Kennzeichnung> gefordert,
                                Set<Allergen> auszuschliessen) {
        List<Gericht> treffer = new ArrayList<>();
        for (Gericht gericht : gerichte) {
            if (gericht.traegtAlle(gefordert)
                    && !gericht.enthaeltEinesVon(auszuschliessen)) {
                treffer.add(gericht);
            }
        }
        return treffer;
    }

    public boolean istLeer() {
        return gerichte.isEmpty();
    }

    public Mensa         getMensa()    { return mensa; }
    public LocalDate     getDatum()    { return datum; }
    public List<Gericht> getGerichte() { return gerichte; }

    public void setMensa(Mensa mensa)                { this.mensa = mensa; }
    public void setDatum(LocalDate datum)            { this.datum = datum; }
    public void setGerichte(List<Gericht> gerichte)  {
        this.gerichte = (gerichte == null) ? new ArrayList<>() : gerichte;
    }

    @Override
    public String toString() {
        return "Speiseplan[" + datum + ", " + gerichte.size() + " Gerichte]";
    }
}
