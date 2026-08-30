package de.leuphana.mensa.persistence;

import java.util.List;

import de.leuphana.mensa.model.Essen;

/**
 * CRUD fuer Essen.
 * Anlegen, Aendern, Anzeigen, Loeschen - genau die vier geforderten Funktionen.
 */
public interface EssenDAO {

    /** @return das angelegte Essen mit vergebener Id. */
    Essen anlegen(Essen essen);

    void aendern(Essen essen);

    /** @return das Essen oder null. */
    Essen findById(int essenId);

    List<Essen> findAlle();

    /** @return true, wenn geloescht wurde. */
    boolean loeschen(int essenId);
}
