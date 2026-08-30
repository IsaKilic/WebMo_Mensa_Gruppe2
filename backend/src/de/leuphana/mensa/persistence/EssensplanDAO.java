package de.leuphana.mensa.persistence;

import java.util.List;

import de.leuphana.mensa.model.Essensplan;

/**
 * CRUD fuer Essensplaene plus Filtern nach Woche.
 *
 * Das Hinzufuegen, Aendern und Entfernen einzelner Essen im Plan
 * geschieht ueber das Essensplan-Objekt selbst und danach aendern().
 */
public interface EssensplanDAO {

    Essensplan anlegen(Essensplan essensplan);

    void aendern(Essensplan essensplan);

    Essensplan findById(int essensplanId);

    /** Filtern nach Woche. @return der Plan oder null. */
    Essensplan findByWoche(int wochennummer);

    List<Essensplan> findAlle();

    boolean loeschen(int essensplanId);
}
