package de.leuphana.mensa.model;

/**
 * Art eines Essens. Laut Aufgabenstellung genau diese drei Werte.
 * Ein Essen hat GENAU EINE Art - deshalb ein einzelnes Feld und
 * kein Set wie bei einer Kennzeichnung.
 */
public enum Art {
    VEGETARISCH("vegetarisch", "vegetarian"),
    VEGAN("vegan", "vegan"),
    MIT_FLEISCH("mit Fleisch", "with meat");

    private final String deutsch;
    private final String englisch;

    Art(String deutsch, String englisch) {
        this.deutsch = deutsch;
        this.englisch = englisch;
    }

    public String getDeutsch()  { return deutsch; }
    public String getEnglisch() { return englisch; }
}
