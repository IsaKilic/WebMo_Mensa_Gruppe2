package de.leuphana.mensa.model;

/**
 * Ein Mensa-Standort. Stammdaten, die sich praktisch nie ändern.
 */
public class Mensa {

    private int id;
    private String name;
    private String adresse;
    private double breitengrad;
    private double laengengrad;

    public Mensa() {
    }

    public Mensa(int id, String name, String adresse,
                 double breitengrad, double laengengrad) {
        this.id = id;
        this.name = name;
        this.adresse = adresse;
        this.breitengrad = breitengrad;
        this.laengengrad = laengengrad;
    }

    public int    getId()           { return id; }
    public String getName()         { return name; }
    public String getAdresse()      { return adresse; }
    public double getBreitengrad()  { return breitengrad; }
    public double getLaengengrad()  { return laengengrad; }

    public void setId(int id)                            { this.id = id; }
    public void setName(String name)                     { this.name = name; }
    public void setAdresse(String adresse)               { this.adresse = adresse; }
    public void setBreitengrad(double breitengrad)       { this.breitengrad = breitengrad; }
    public void setLaengengrad(double laengengrad)       { this.laengengrad = laengengrad; }

    @Override
    public String toString() {
        return "Mensa[" + id + ", " + name + "]";
    }
}
