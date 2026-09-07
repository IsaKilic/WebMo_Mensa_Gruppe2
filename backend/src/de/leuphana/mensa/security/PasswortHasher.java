package de.leuphana.mensa.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashen und Pruefen von Passwoertern mit PBKDF2.
 *
 * Warum ueberhaupt hashen? Passwoerter duerfen nie im Klartext in der
 * Datenbank stehen. Wer die Datenbank in die Haende bekommt, haette
 * sonst sofort alle Zugaenge - und weil viele Leute dasselbe Passwort
 * mehrfach verwenden, auch anderswo.
 *
 * Warum nicht einfach SHA-256? Weil das viel zu schnell ist. Eine
 * Grafikkarte rechnet Milliarden SHA-256-Hashes pro Sekunde durch und
 * probiert damit ganze Woerterbuecher aus. PBKDF2 wiederholt die
 * Berechnung 120.000 Mal und macht jeden Rateversuch entsprechend teuer.
 *
 * Warum ein Salt? Ohne Salt ergibt dasselbe Passwort immer denselben
 * Hash. Angreifer koennen dann vorberechnete Tabellen benutzen
 * (Rainbow Tables) und sehen ausserdem sofort, welche Nutzer dasselbe
 * Passwort haben. Das Salt ist pro Benutzer zufaellig und macht beides
 * unmoeglich.
 *
 * PBKDF2 statt BCrypt, weil es im JDK enthalten ist und keine weitere
 * Bibliothek braucht. Sicherheitstechnisch ist beides in Ordnung.
 *
 * Gespeichertes Format:  iterationen:salt:hash   (beides Base64)
 * Das Format traegt die Iterationszahl mit sich - so lassen sich alte
 * Hashes weiter pruefen, wenn wir die Zahl spaeter erhoehen.
 */
public final class PasswortHasher {

    private static final String ALGORITHMUS = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONEN = 120_000;
    private static final int SALT_LAENGE_BYTES = 16;
    private static final int SCHLUESSEL_LAENGE_BITS = 256;
    private static final String TRENNER = ":";

    private static final SecureRandom ZUFALL = new SecureRandom();

    private PasswortHasher() {
        // Utility-Klasse
    }

    /**
     * Erzeugt einen Hash mit frischem, zufaelligem Salt.
     * Zweimal dasselbe Passwort ergibt zwei verschiedene Hashes -
     * das ist gewollt.
     */
    public static String hashen(String passwort) {
        if (passwort == null || passwort.isEmpty()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein");
        }

        byte[] salt = new byte[SALT_LAENGE_BYTES];
        ZUFALL.nextBytes(salt);

        byte[] hash = berechne(passwort.toCharArray(), salt, ITERATIONEN);

        Base64.Encoder kodierer = Base64.getEncoder();
        return ITERATIONEN + TRENNER
             + kodierer.encodeToString(salt) + TRENNER
             + kodierer.encodeToString(hash);
    }

    /**
     * Prueft ein eingegebenes Passwort gegen einen gespeicherten Hash.
     *
     * @param passwort         was der Nutzer eingetippt hat
     * @param gespeicherterWert der Wert aus der Spalte passwort_hash
     * @return true, wenn das Passwort stimmt
     */
    public static boolean pruefen(String passwort, String gespeicherterWert) {
        if (passwort == null || gespeicherterWert == null) {
            return false;
        }

        String[] teile = gespeicherterWert.split(TRENNER);
        if (teile.length != 3) {
            // Kein gueltiges Format - etwa ein alter Klartexteintrag.
            // Bewusst false statt einer Exception: ein kaputter
            // Datensatz soll nicht die Anmeldung aller lahmlegen.
            return false;
        }

        try {
            int iterationen = Integer.parseInt(teile[0]);
            byte[] salt = Base64.getDecoder().decode(teile[1]);
            byte[] erwarteterHash = Base64.getDecoder().decode(teile[2]);

            byte[] berechneterHash = berechne(passwort.toCharArray(), salt, iterationen);

            // MessageDigest.isEqual vergleicht in konstanter Zeit.
            // Ein normales Arrays.equals bricht beim ersten Unterschied
            // ab - aus den Laufzeitunterschieden liesse sich der Hash
            // Byte fuer Byte erraten (Timing-Angriff).
            return MessageDigest.isEqual(erwarteterHash, berechneterHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] berechne(char[] passwort, byte[] salt, int iterationen) {
        try {
            PBEKeySpec spezifikation =
                    new PBEKeySpec(passwort, salt, iterationen, SCHLUESSEL_LAENGE_BITS);
            SecretKeyFactory fabrik = SecretKeyFactory.getInstance(ALGORITHMUS);
            return fabrik.generateSecret(spezifikation).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Passwort-Hashing fehlgeschlagen", e);
        }
    }

    /**
     * Hilfsprogramm: erzeugt Hashes fuer die Testdaten.
     *
     * Aufruf in Eclipse: Run As -> Java Application.
     * Die Ausgabe koennt ihr direkt in 03_testdaten.sql einsetzen.
     */
    public static void main(String[] args) {
        String[] passwoerter = (args.length > 0)
                ? args : new String[] { "admin123", "user123" };

        for (String passwort : passwoerter) {
            System.out.println(passwort + "  ->  " + hashen(passwort));
        }
    }
}
