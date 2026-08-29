package de.leuphana.mensa.model;

/**
 * Die 14 kennzeichnungspflichtigen Allergene nach EU-Verordnung 1169/2011.
 * Bewusst als Enum: die Liste ist gesetzlich festgelegt und ändert sich nicht.
 * Ausschlusskriterium beim Filtern.
 */
public enum Allergen {
    GLUTEN("Glutenhaltiges Getreide"),
    KREBSTIERE("Krebstiere"),
    EIER("Eier"),
    FISCH("Fisch"),
    ERDNUESSE("Erdnüsse"),
    SOJA("Soja"),
    MILCH("Milch und Laktose"),
    NUESSE("Schalenfrüchte"),
    SELLERIE("Sellerie"),
    SENF("Senf"),
    SESAM("Sesam"),
    SULFITE("Schwefeldioxid und Sulfite"),
    LUPINEN("Lupinen"),
    WEICHTIERE("Weichtiere");

    private final String bezeichnung;

    Allergen(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
