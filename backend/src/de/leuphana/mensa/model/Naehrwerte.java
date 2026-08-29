package de.leuphana.mensa.model;

/**
 * Nährwertangaben eines Gerichts, jeweils pro Portion.
 * Ebenfalls ein Value Object, das flach in der Tabelle gericht liegt.
 * Alle Felder sind optional, weil die Quelle sie nicht immer liefert.
 */
public class Naehrwerte {

    private Integer kilojoule;
    private Integer kilokalorien;
    private Double fett;
    private Double kohlenhydrate;
    private Double eiweiss;
    private Double salz;

    public Naehrwerte() {
    }

    public Naehrwerte(Integer kilojoule, Integer kilokalorien, Double fett,
                      Double kohlenhydrate, Double eiweiss, Double salz) {
        this.kilojoule = kilojoule;
        this.kilokalorien = kilokalorien;
        this.fett = fett;
        this.kohlenhydrate = kohlenhydrate;
        this.eiweiss = eiweiss;
        this.salz = salz;
    }

    /** true, wenn überhaupt eine Angabe vorliegt. Für die Anzeige nützlich. */
    public boolean sindVorhanden() {
        return kilokalorien != null || fett != null || kohlenhydrate != null
                || eiweiss != null || salz != null;
    }

    public Integer getKilojoule()      { return kilojoule; }
    public Integer getKilokalorien()   { return kilokalorien; }
    public Double  getFett()           { return fett; }
    public Double  getKohlenhydrate()  { return kohlenhydrate; }
    public Double  getEiweiss()        { return eiweiss; }
    public Double  getSalz()           { return salz; }

    public void setKilojoule(Integer kilojoule)          { this.kilojoule = kilojoule; }
    public void setKilokalorien(Integer kilokalorien)    { this.kilokalorien = kilokalorien; }
    public void setFett(Double fett)                     { this.fett = fett; }
    public void setKohlenhydrate(Double kohlenhydrate)   { this.kohlenhydrate = kohlenhydrate; }
    public void setEiweiss(Double eiweiss)               { this.eiweiss = eiweiss; }
    public void setSalz(Double salz)                     { this.salz = salz; }
}
