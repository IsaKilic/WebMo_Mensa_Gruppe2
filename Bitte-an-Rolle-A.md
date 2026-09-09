# Bitte an Rolle A

Nur noch ein Punkt offen (der DAO-Instanzen-Punkt aus der ersten Version
dieser Datei ist erledigt - siehe unten).

## Echtes Passwort-Hashing

`BenutzerDAOInMemory` legt die Testbenutzer aktuell mit Klartext-
Passwort im `passwortHash`-Feld an (euer eigener Kommentar dort weist
schon darauf hin: "vor der Abgabe unbedingt durch echte Hashes ersetzen
(BCrypt) - das ist eine typische Nachfrage im mündlichen Gespräch").

`AnmeldeService` (Rolle B) vergleicht deshalb vorerst per einfachem
Textvergleich, in einer eigenen, klar kommentierten Methode
(`passwortPasst(...)`). Sobald ihr auf echte Hashes umstellt, muss auf
unserer Seite nur diese eine Methode angepasst werden.

Bitte kurz Bescheid geben, wenn ihr die Umstellung macht, und welche
Bibliothek ihr fuer BCrypt einbindet (z.B. `org.mindrot:jbcrypt`) -
dann binden wir dieselbe JAR ein.

## Erledigt: DAO-Instanzen teilen sich jetzt die Daten

In der ersten Version dieser Datei stand hier eine Bitte, die vier
InMemory-DAOs (`EssenDAOInMemory` &c.) `static` zu machen, weil das
Servlet-Muster aus `docs/AUFGABE-ROLLE-B.md` (jedes Servlet baut seine
DAOs selbst in `init()`) sonst dazu fuehrt, dass z.B. `EssenServlet`
und `EssensplanServlet` unterschiedliche, unabhaengige Datenbestaende
haetten (ein frisch angelegtes Essen waere im jeweils anderen Servlet
unsichtbar, 404).

Das ist inzwischen geloest, OHNE dass sich an euren Klassen etwas
aendern muss: Rolle B hat eine kleine `Fabrik`-Klasse
(`de.leuphana.mensa.service.Fabrik`) eingefuehrt, die jede DAO- und
Service-Instanz genau einmal erzeugt (statische, "lazy" initialisierte
Felder) und an alle Servlets weitergibt. Jedes Servlet ruft in seinem
`init()` nur noch z.B. `Fabrik.essenService()` auf, statt selbst `new
EssenDAOInMemory()` zu schreiben. Ihr muesst dafuer nichts anpassen -
diese Bitte ist damit erledigt.
