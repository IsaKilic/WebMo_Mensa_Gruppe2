# Passwort-Hashing ist umgestellt (Rolle A)

Der offene Punkt aus `Bitte-an-Rolle-A.md` ist erledigt.

## Kurzfassung

**Keine BCrypt-JAR noetig.** Wir nehmen PBKDF2 - das steckt im JDK.
Damit muss niemand eine zusaetzliche Bibliothek herunterladen und in
`WEB-INF/lib` legen. Sicherheitstechnisch ist PBKDF2 fuer unseren
Zweck gleichwertig zu BCrypt (beide sind absichtlich langsam und
verwenden ein Salt).

Neue Klasse: `de.leuphana.mensa.security.PasswortHasher`

    PasswortHasher.hashen(passwort)          -> String
    PasswortHasher.pruefen(passwort, hash)   -> boolean

`BenutzerDAOInMemory` und `03_testdaten.sql` enthalten jetzt echte
Hashes statt Klartext. Die Zugangsdaten bleiben unveraendert:

    admin / admin123    (ADMIN)
    user  / user123     (USER)

## Was ihr aendern muesst: genau eine Zeile

In `AnmeldeService.passwortPasst(...)`:

    // vorher
    return gespeicherterHash != null && gespeicherterHash.equals(eingegeben);

    // nachher
    return PasswortHasher.pruefen(eingegeben, gespeicherterHash);

Plus der Import:

    import de.leuphana.mensa.security.PasswortHasher;

Den Javadoc-Kommentar ueber der Methode koennt ihr entsprechend
kuerzen - die technische Schuld ist damit abgetragen.

Wir haben die Umstellung mit eurem Code durchgespielt: kompiliert
sauber, und der Login funktioniert (richtiges Passwort geht durch,
falsches wird abgelehnt, unbekannter Benutzer ebenfalls).

`pruefen` gibt uebrigens `false` zurueck, wenn im Feld noch ein alter
Klartextwert steht - es fliegt also keine Exception, falls irgendwo
noch alte Testdaten liegen.

## Format der Hashes

    120000:VpAvMrH5DJIzU3zN//bdgw==:XWZB1QCoBjKC5jXmzFExEjhKgmkEN8rnEMP2xbl4X88=
    ^      ^                        ^
    |      |                        abgeleiteter Schluessel (Base64)
    |      Salt, pro Benutzer zufaellig (Base64)
    Iterationen

Die Iterationszahl steht mit im Wert. Erhoehen wir sie spaeter,
lassen sich alte Hashes weiterhin pruefen.

## Fuer das muendliche Gespraech

**Warum ueberhaupt hashen?** Wer die Datenbank in die Haende bekommt,
haette bei Klartext sofort alle Zugaenge - und weil viele Leute
Passwoerter mehrfach verwenden, auch anderswo.

**Warum nicht SHA-256?** Viel zu schnell. Eine Grafikkarte schafft
Milliarden SHA-256-Hashes pro Sekunde und probiert damit ganze
Woerterbuecher durch. PBKDF2 mit 120.000 Iterationen braucht bei uns
rund 31 ms pro Pruefung - beim Login unmerkbar, aber eine Million
Rateversuche dauern damit rund neun Stunden statt Sekunden.

**Warum ein Salt?** Ohne Salt ergibt dasselbe Passwort immer denselben
Hash. Angreifer koennten vorberechnete Tabellen nutzen (Rainbow
Tables) und saehen ausserdem sofort, welche Nutzer dasselbe Passwort
haben. Unser Salt ist pro Benutzer zufaellig - deshalb ergeben zwei
Benutzer mit demselben Passwort trotzdem zwei verschiedene Hashes.

**Warum MessageDigest.isEqual statt Arrays.equals?** Der Vergleich
laeuft in konstanter Zeit. Arrays.equals bricht beim ersten
Unterschied ab - aus den Laufzeitunterschieden liesse sich der Hash
Byte fuer Byte erraten (Timing-Angriff).
