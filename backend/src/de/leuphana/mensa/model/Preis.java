package de.leuphana.mensa.model;

import java.util.Objects;

/**
 * Preis eines Gerichts für die drei Nutzergruppen.
 *
 * Bewusst eine eigene Klasse und nicht drei lose double-Felder in
 * {@link Gericht}: so lebt die Auswahllogik an einer Stelle
 * (Value Object). In der Datenbank wird der Preis flach in die
 * Tabelle gericht eingebettet, weil es eine echte 1:1-Beziehung ist.
 */
public class Preis {

    private double studierende;
    private double bedienstete;
    private double gaeste;

    public Preis() {
    }

    public Preis(double studierende, double bedienstete, double gaeste) {
        this.studierende = studierende;
        this.bedienstete = bedienstete;
        this.gaeste = gaeste;
    }

    /**
     * Liefert den Preis für die angegebene Nutzergruppe.
     * Diese Methode ist der Grund, warum Preis eine eigene Klasse ist.
     */
    public double fuer(Nutzergruppe nutzergruppe) {
        Objects.requireNonNull(nutzergruppe, "nutzergruppe darf nicht null sein");
        switch (nutzergruppe) {
            case STUDIERENDE: return studierende;
            case BEDIENSTETE: return bedienstete;
            case GAESTE:      return gaeste;
            default:
                throw new IllegalArgumentException("Unbekannte Nutzergruppe: " + nutzergruppe);
        }
    }

    public double getStudierende()  { return studierende; }
    public double getBedienstete()  { return bedienstete; }
    public double getGaeste()       { return gaeste; }

    public void setStudierende(double studierende)   { this.studierende = studierende; }
    public void setBedienstete(double bedienstete)   { this.bedienstete = bedienstete; }
    public void setGaeste(double gaeste)             { this.gaeste = gaeste; }

    @Override
    public String toString() {
        return String.format("Preis[Studi=%.2f, Bed=%.2f, Gast=%.2f]",
                studierende, bedienstete, gaeste);
    }
}
