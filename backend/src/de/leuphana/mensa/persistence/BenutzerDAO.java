package de.leuphana.mensa.persistence;

import de.leuphana.mensa.model.Benutzer;

/** Zugriff auf Benutzer fuer den Login. */
public interface BenutzerDAO {

    /** @return der Benutzer oder null, wenn es ihn nicht gibt. */
    Benutzer findByBenutzername(String benutzername);

    Benutzer findById(int benutzerId);
}
