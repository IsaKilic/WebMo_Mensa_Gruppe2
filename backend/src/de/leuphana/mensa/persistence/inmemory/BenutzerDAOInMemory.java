package de.leuphana.mensa.persistence.inmemory;

import java.util.LinkedHashMap;
import java.util.Map;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Rolle;
import de.leuphana.mensa.persistence.BenutzerDAO;

/**
 * Zwei Testbenutzer.
 *
 * ACHTUNG: die Passwoerter stehen hier im Klartext, damit ihr sofort
 * testen koennt. Vor der Abgabe unbedingt durch echte Hashes ersetzen
 * (BCrypt) - das ist eine typische Nachfrage im muendlichen Gespraech.
 */
public class BenutzerDAOInMemory implements BenutzerDAO {

    private final Map<String, Benutzer> benutzer = new LinkedHashMap<>();

    public BenutzerDAOInMemory() {
        Benutzer admin = new Benutzer(1, "admin", "admin123", Rolle.ADMIN);
        Benutzer user  = new Benutzer(2, "user",  "user123",  Rolle.USER);
        benutzer.put(admin.getBenutzername(), admin);
        benutzer.put(user.getBenutzername(),  user);
    }

    @Override
    public Benutzer findByBenutzername(String benutzername) {
        return benutzer.get(benutzername);
    }

    @Override
    public Benutzer findById(int benutzerId) {
        for (Benutzer b : benutzer.values()) {
            if (b.getId() == benutzerId) {
                return b;
            }
        }
        return null;
    }
}
