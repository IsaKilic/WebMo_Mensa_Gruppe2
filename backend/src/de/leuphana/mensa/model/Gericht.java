package de.leuphana.mensa.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ein einzelnes Gericht auf einem Speiseplan.
 *
 * Allergene, Zusatzstoffe und Kennzeichnungen sind EnumSets:
 * typsicher, speichereffizient und ohne Tippfehler-Risiko.
 */
public class Gericht {

    private int id;
    private String name;
    private Kategorie kategorie = Kategorie.SONSTIGES;
    private Preis preis = new Preis();
    private Naehrwerte naehrwerte = new Naehrwerte();

    private Set<Allergen> allergene = EnumSet.noneOf(Allergen.class);
    private Set<Zusatzstoff> zusatzstoffe = EnumSet.noneOf(Zusatzstoff.class);
    private Set<Kennzeichnung> kennzeichnungen = EnumSet.noneOf(Kennzeichnung.class);

    public Gericht() {
    }

    public Gericht(int id, String name, Kategorie kategorie, Preis preis) {
        this.id = id;
        this.name = name;
        this.kategorie = kategorie;
        this.preis = preis;
    }

    /** true, wenn das Gericht mindestens eines der übergebenen Allergene enthält. */
    public boolean enthaeltEinesVon(Set<Allergen> auszuschliessen) {
        if (auszuschliessen == null || auszuschliessen.isEmpty()) {
            return false;
        }
        for (Allergen allergen : auszuschliessen) {
            if (allergene.contains(allergen)) {
                return true;
            }
        }
        return false;
    }

    /** true, wenn das Gericht alle geforderten Kennzeichnungen trägt. */
    public boolean traegtAlle(Set<Kennzeichnung> gefordert) {
        return gefordert == null || gefordert.isEmpty()
                || kennzeichnungen.containsAll(gefordert);
    }

    public boolean istVegan() {
        return kennzeichnungen.contains(Kennzeichnung.VEGAN);
    }

    public boolean istVegetarisch() {
        return kennzeichnungen.contains(Kennzeichnung.VEGETARISCH) || istVegan();
    }

    public double getPreisFuer(Nutzergruppe nutzergruppe) {
        return preis.fuer(nutzergruppe);
    }

    public int            getId()               { return id; }
    public String         getName()             { return name; }
    public Kategorie      getKategorie()        { return kategorie; }
    public Preis          getPreis()            { return preis; }
    public Naehrwerte     getNaehrwerte()       { return naehrwerte; }
    public Set<Allergen>      getAllergene()        { return allergene; }
    public Set<Zusatzstoff>   getZusatzstoffe()     { return zusatzstoffe; }
    public Set<Kennzeichnung> getKennzeichnungen()  { return kennzeichnungen; }

    public void setId(int id)                        { this.id = id; }
    public void setName(String name)                 { this.name = name; }
    public void setKategorie(Kategorie kategorie)    { this.kategorie = kategorie; }
    public void setPreis(Preis preis)                { this.preis = preis; }
    public void setNaehrwerte(Naehrwerte naehrwerte) { this.naehrwerte = naehrwerte; }

    public void setAllergene(Set<Allergen> allergene) {
        this.allergene = (allergene == null)
                ? EnumSet.noneOf(Allergen.class) : EnumSet.copyOf(allergene);
    }

    public void setZusatzstoffe(Set<Zusatzstoff> zusatzstoffe) {
        this.zusatzstoffe = (zusatzstoffe == null)
                ? EnumSet.noneOf(Zusatzstoff.class) : EnumSet.copyOf(zusatzstoffe);
    }

    public void setKennzeichnungen(Set<Kennzeichnung> kennzeichnungen) {
        this.kennzeichnungen = (kennzeichnungen == null)
                ? EnumSet.noneOf(Kennzeichnung.class) : EnumSet.copyOf(kennzeichnungen);
    }

    @Override
    public String toString() {
        return "Gericht[" + id + ", " + name + ", " + kategorie + "]";
    }
}
