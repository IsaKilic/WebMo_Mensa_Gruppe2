package de.leuphana.mensa.persistence;

import java.util.List;

import de.leuphana.mensa.model.Essensbewertung;

/**
 * Abgeben und Aendern von Essensbewertungen.
 * Ein User darf anlegen und die eigene Bewertung aendern.
 */
public interface EssensbewertungDAO {

    Essensbewertung abgeben(Essensbewertung bewertung);

    void aendern(Essensbewertung bewertung);

    Essensbewertung findById(int bewertungId);

    /** Alle Bewertungen zu einem Essen, fuer die Detailansicht. */
    List<Essensbewertung> findByEssen(int essenId);

    /** Durchschnitt der Sterne, oder 0 wenn es keine Bewertung gibt. */
    double durchschnittFuerEssen(int essenId);

    boolean loeschen(int bewertungId);
}
