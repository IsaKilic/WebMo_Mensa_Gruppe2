package de.leuphana.mensa.persistence.inmemory;

import java.util.LinkedHashMap;
import java.util.Map;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Rolle;
import de.leuphana.mensa.persistence.BenutzerDAO;

/**
 * Zwei Testbenutzer mit echten PBKDF2-Hashes.
 *
 *   admin / admin123   Rolle ADMIN
 *   user  / user123    Rolle USER
 *
 * Die Hashes wurden mit PasswortHasher.main(...) erzeugt. Sie enthalten
 * kein Klartextpasswort - im Format iterationen:salt:hash. Wer nur den
 * Hash kennt, kann daraus das Passwort nicht zurueckrechnen.
 *
 * Geprueft wird ueber PasswortHasher.pruefen(eingabe, hash), nicht
 * ueber String-Vergleich.
 */
public class BenutzerDAOInMemory implements BenutzerDAO {

    private static final String HASH_ADMIN =
            "120000:VpAvMrH5DJIzU3zN//bdgw==:XWZB1QCoBjKC5jXmzFExEjhKgmkEN8rnEMP2xbl4X88=";

    private static final String HASH_USER =
            "120000:xaUKYrIypJelS81UFx8p1Q==:AcWXgBThErCQrHYnkts/efR4DptsCMKvR8GhmlYv90g=";

    private final Map<String, Benutzer> benutzer = new LinkedHashMap<>();

    public BenutzerDAOInMemory() {
        Benutzer admin = new Benutzer(1, "admin", HASH_ADMIN, Rolle.ADMIN);
        Benutzer user  = new Benutzer(2, "user",  HASH_USER,  Rolle.USER);
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
