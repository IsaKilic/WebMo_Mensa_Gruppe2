package de.leuphana.mensa.model;

/**
 * Fachklasse Essen.
 *
 * Attribute laut Aufgabenstellung: Name, Preis, Art.
 * Bewusst nicht mehr: keine Allergene, keine Naehrwerte, keine
 * Preisgruppen. Was nicht gefordert ist, kostet nur Zeit.
 */
public class Essen {

    private int id;
    private String name;
    private double preis;
    private Art art;

    public Essen() {
    }

    public Essen(int id, String name, double preis, Art art) {
        this.id = id;
        this.name = name;
        this.preis = preis;
        this.art = art;
    }

    public int    getId()    { return id; }
    public String getName()  { return name; }
    public double getPreis() { return preis; }
    public Art    getArt()   { return art; }

    public void setId(int id)          { this.id = id; }
    public void setName(String name)   { this.name = name; }
    public void setPreis(double preis) { this.preis = preis; }
    public void setArt(Art art)        { this.art = art; }

    @Override
    public String toString() {
        return String.format("Essen[%d, %s, %.2f EUR, %s]", id, name, preis, art);
    }
}
