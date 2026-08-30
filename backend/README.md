# Backend

Java-Backend fuer das Mensa-Projekt: Model, Persistenz, Service und
REST-Endpunkte. Liefert ausschliesslich JSON - die Web-Anwendung ist ein
eigenstaendiges Angular-Projekt unter `../web-app/`.

## Stand

Fertig:

- Model-Klassen und Enums (`de.leuphana.mensa.model`)
- DAO-Interfaces mit vollem CRUD (`de.leuphana.mensa.persistence`)
- In-Memory-Implementierungen mit 10 Essen und 8 Essensplaenen
- MySQL-Schema (`sql/01_schema.sql`)
- `ConnectionFactory` fuer JDBC

Offen:

- JDBC-Implementierungen der vier DAOs
- Passwort-Hashing (aktuell Klartext in `BenutzerDAOInMemory`)
- Service-Schicht und REST-Endpunkte
- Foto-Ablage: Datei im Dateisystem, Pfad in der Datenbank

## Klassen

    model/
      Essen                 Name, Preis, Art
      Essensplan            Wochennummer, EnumMap<Wochentag, Essen>
      Essensbewertung       Sterne 1-5, Text, Fotopfad
      Benutzer              Benutzername, Passworthash, Rolle
      Art                   VEGETARISCH | VEGAN | MIT_FLEISCH
      Wochentag             MONTAG .. FREITAG
      Rolle                 USER | ADMIN

    persistence/
      EssenDAO              anlegen, aendern, findById, findAlle, loeschen
      EssensplanDAO         dito, plus findByWoche
      EssensbewertungDAO    abgeben, aendern, findByEssen, durchschnitt
      BenutzerDAO           findByBenutzername, findById

## Einrichtung

### Eclipse

Dynamic Web Project anlegen, `src` als Source Folder eintragen.

### MySQL

    mysql -u root -p < sql/01_schema.sql

Eigenen Benutzer anlegen, nicht mit root arbeiten:

    CREATE USER 'mensa_app'@'localhost' IDENTIFIED BY 'EUER_PASSWORT';
    GRANT SELECT, INSERT, UPDATE, DELETE ON mensa.* TO 'mensa_app'@'localhost';
    FLUSH PRIVILEGES;

### Zugangsdaten

`datenbank.properties.vorlage` nach `src/datenbank.properties` kopieren
und ausfuellen. Steht in `.gitignore` und darf nicht ins Repository.

### JDBC-Treiber

MySQL Connector/J herunterladen, nach `WebContent/WEB-INF/lib` legen und
in Eclipse dem Build Path hinzufuegen.

## Fuer Rolle B

Ihr programmiert gegen die Interfaces, nicht gegen die
JDBC-Implementierungen. Damit koennt ihr sofort starten:

    EssenDAO essenDAO = new EssenDAOInMemory();
    EssensplanDAO planDAO = new EssensplanDAOInMemory(essenDAO);
    EssensbewertungDAO bewDAO = new EssensbewertungDAOInMemory();
    BenutzerDAO benDAO = new BenutzerDAOInMemory();

    Essensplan kw3 = planDAO.findByWoche(3);
    Essen montag = kw3.getEssen(Wochentag.MONTAG);
    double schnitt = bewDAO.durchschnittFuerEssen(montag.getId());

Testbenutzer: `admin` / `admin123` und `user` / `user123`.

Sobald die JDBC-Implementierungen stehen, tauschen wir nur die
`new`-Aufrufe. Euer Code aendert sich nicht.

## Designentscheidungen

**Essensplan haelt eine `EnumMap<Wochentag, Essen>`, keine Liste.**
Die Aufgabe verlangt genau fuenf Essen fuer Montag bis Freitag. Eine
Liste koennte drei oder sieben Eintraege haben, die EnumMap nicht.

**`Art` ist ein einzelner Wert, kein Set.** Ein Essen ist vegetarisch
ODER vegan ODER mit Fleisch, nicht mehreres gleichzeitig.

**Die DAO-Schicht wirft `DAOException`, nie `SQLException`.** Die
Service-Schicht soll nicht wissen, dass darunter eine relationale
Datenbank liegt.

**Preis und Naehrwerte flach in der Tabelle.** Bei echten
1:1-Beziehungen waeren eigene Tabellen nur unnoetige Joins.

**Bewertungstext wird in `istGueltig()` geprueft.** Laut Aufgabe ist er
verpflichtend, also darf eine Bewertung ohne Text nicht gespeichert
werden.
