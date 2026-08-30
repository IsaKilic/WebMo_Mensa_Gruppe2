package de.leuphana.mensa.model;

/**
 * Montag bis Freitag. Ein Essensplan hat genau fuenf Essen,
 * eines pro Wochentag - siehe Aufgabenstellung "EssenProWoche".
 */
public enum Wochentag {
    MONTAG("Montag", "Monday"),
    DIENSTAG("Dienstag", "Tuesday"),
    MITTWOCH("Mittwoch", "Wednesday"),
    DONNERSTAG("Donnerstag", "Thursday"),
    FREITAG("Freitag", "Friday");

    private final String deutsch;
    private final String englisch;

    Wochentag(String deutsch, String englisch) {
        this.deutsch = deutsch;
        this.englisch = englisch;
    }

    public String getDeutsch()  { return deutsch; }
    public String getEnglisch() { return englisch; }
}
