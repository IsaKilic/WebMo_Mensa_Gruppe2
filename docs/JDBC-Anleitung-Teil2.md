# Die restlichen drei JDBC-DAOs

Alle drei wurden gegen eine echte Datenbank getestet: 31 Pruefungen,
keine Fehler.

## Einbauen

Die drei Java-Dateien nach

    backend/src/de/leuphana/mensa/persistence/jdbc/

`TestAlleDAOs.java` nach `backend/src/` (Default Package, wie die
erste Testklasse).

`03_testdaten.sql` nach `backend/sql/`.

## Testdaten einspielen

Die alte Testzeile aus dem ersten Durchlauf stoert. Erst aufraeumen,
dann neu befuellen - in PowerShell:

    Get-Content backend\sql\01_schema.sql   | mysql -u root -p
    Get-Content backend\sql\03_testdaten.sql | mysql -u mensa_app -p

Das erste Skript legt die Tabellen neu an und loescht dabei alles.

## Test ausfuehren

`TestAlleDAOs.java` -> Rechtsklick -> Run As -> Java Application.

Erwartet: `===== 31 OK, 0 Fehler =====`

## Was in diesen Klassen neu ist

### BenutzerDAOJdbc

Nichts Neues - dasselbe Muster wie EssenDAOJdbc, nur mit zwei
Lesemethoden. Benutzer werden laut Aufgabenstellung nicht ueber die
Anwendung angelegt.

Wichtig: das DAO laedt nur den Hash. Der Passwortvergleich gehoert in
die Service-Schicht. Ein DAO liefert Daten, es entscheidet nicht ueber
Anmeldungen.

### EssensbewertungDAOJdbc

**LocalDateTime braucht eine Uebersetzung.** JDBC kennt den Typ nicht
direkt, deshalb `Timestamp.valueOf(...)` beim Schreiben und
`.toLocalDateTime()` beim Lesen.

**AVG in SQL statt Schleife in Java.** `durchschnittFuerEssen` laesst
die Datenbank rechnen. Ohne Bewertungen liefert AVG NULL - `getDouble`
macht daraus stillschweigend 0.0, deshalb prueft `wasNull()` nach, ob
wirklich ein Wert kam.

### EssensplanDAOJdbc

Die aufwendigste Klasse, weil ein Plan auf zwei Tabellen verteilt ist.

**Transaktion.** Anlegen schreibt erst in `essensplan`, dann in
`essensplan_essen`. Geht der zweite Schritt schief, muss der erste
zurueckgenommen werden - sonst steht ein Plan ohne Essen in der
Datenbank. Deshalb `setAutoCommit(false)`, am Ende `commit()`, im
Fehlerfall `rollback()`.

Das ist im Test nachgewiesen: bei einem Essen mit ungueltiger Id wird
die Zuordnung abgelehnt UND der Plan verschwindet wieder.

Wichtig dabei: `setAutoCommit(false)` verhindert try-with-resources
fuer die Connection, weil man vor dem Schliessen noch committen oder
rollen muss. Deshalb hier `try/catch/finally` von Hand.

**LEFT JOIN statt N+1 Abfragen.** `findAlle` koennte pro Plan eine
zweite Abfrage schicken - bei acht Plaenen waeren das neun Abfragen.
Ein LEFT JOIN holt alles auf einmal, und `ausErgebnis` gruppiert die
Zeilen in Java zu Objekten.

LEFT und nicht INNER, damit auch ein Plan ohne Essen geliefert wird.
Bei einem INNER JOIN waere ein leerer Plan unsichtbar. Deshalb die
Null-Pruefung auf `wochentag` in `ausErgebnis`.

**Aendern loescht alle Zuordnungen und schreibt sie neu.** Einfacher
und weniger fehleranfaellig, als einzeln zu vergleichen was sich
geaendert hat. Bei fuenf Zeilen pro Plan auch schnell genug.

**addBatch statt einzelner Anweisungen.** Die fuenf Zuordnungen werden
gesammelt und mit `executeBatch()` in einem Rutsch geschickt.

## Fuer die muendliche Pruefung

Drei Fragen, die zu diesem Code naheliegen:

**Warum PreparedStatement?** Gegen SQL-Injection. Bei String-Verkettung
koennte ein Gericht namens `'; DROP TABLE essen; --` die Tabelle
loeschen.

**Warum eine Transaktion?** Weil ein Essensplan auf zwei Tabellen
verteilt ist und beide Schritte zusammen gelingen oder zusammen
scheitern muessen. Stichwort Atomaritaet, das A in ACID.

**Warum LEFT JOIN?** Damit auch Plaene ohne zugeordnete Essen im
Ergebnis erscheinen. Und um das N+1-Problem zu vermeiden.

## Was jetzt noch fehlt

- Passwort-Hashing mit BCrypt statt Klartext
- Service-Schicht und REST-Endpunkte (Rolle B)
