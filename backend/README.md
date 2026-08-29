# Mensa-Projekt — Backend (Rolle A)

Grundgeruest der Model- und Persistenzschicht fuer die Mensa-Anwendung.
Web- und mobile Anwendungssysteme, Leuphana Universitaet Lueneburg.

## Was hier schon fertig ist

- Model-Klassen und Enums (`de.leuphana.mensa.model`)
- DAO-Interfaces (`de.leuphana.mensa.persistence`)
- In-Memory-Implementierung mit Testdaten fuer 10 Tage
- MySQL-Schema (`sql/01_schema.sql`, `sql/02_stammdaten.sql`)
- ConnectionFactory fuer JDBC

## Was noch fehlt

- `MensaDAOJdbc` und `SpeiseplanDAOJdbc`
- Importer fuer die API unter api.stw-on.de
- Service-Schicht und REST-Endpunkte (Rolle B)

## Einrichtung

### 1. Projekt in Eclipse

Neues Dynamic Web Project anlegen, `src` als Source Folder eintragen,
den Inhalt dieses Ordners hineinkopieren.

### 2. MySQL

    mysql -u root -p < sql/01_schema.sql
    mysql -u root -p < sql/02_stammdaten.sql

Danach einen eigenen Benutzer anlegen, nicht mit root arbeiten:

    CREATE USER 'mensa_app'@'localhost' IDENTIFIED BY 'EUER_PASSWORT';
    GRANT SELECT, INSERT, UPDATE, DELETE ON mensa.* TO 'mensa_app'@'localhost';
    FLUSH PRIVILEGES;

### 3. Zugangsdaten

`datenbank.properties.vorlage` nach `src/datenbank.properties` kopieren
und ausfuellen. Diese Datei steht in `.gitignore` und darf nicht ins
Repository — sonst liegen eure Passwoerter oeffentlich auf GitHub.

### 4. JDBC-Treiber

MySQL Connector/J herunterladen und in `WebContent/WEB-INF/lib` legen,
danach in Eclipse dem Build Path hinzufuegen.

## Fuer Rolle B

Ihr programmiert ausschliesslich gegen die Interfaces `MensaDAO` und
`SpeiseplanDAO`. Zum Starten:

    MensaDAO mensaDAO = new MensaDAOInMemory();
    SpeiseplanDAO planDAO = new SpeiseplanDAOInMemory(mensaDAO);

    Speiseplan heute = planDAO.findByMensaUndDatum(1, LocalDate.now());
    List<Gericht> vegan = heute.filter(EnumSet.of(Kennzeichnung.VEGAN), null);

Sobald die JDBC-Implementierung fertig ist, tauschen wir nur die beiden
`new`-Aufrufe aus. Euer Code aendert sich nicht.

## Wichtige Designentscheidungen

**Preis und Naehrwerte sind eigene Klassen (Value Objects), liegen in der
Datenbank aber flach in der Tabelle `gericht`.** Bei einer echten
1:1-Beziehung waeren eigene Tabellen nur unnoetige Joins.

**Allergene, Zusatzstoffe und Kennzeichnungen sind Enums.** Die Liste der
14 Allergene ist gesetzlich festgelegt und aendert sich nicht. Enums geben
Typsicherheit und machen Tippfehler unmoeglich.

**Kennzeichnung und Allergen sind getrennt.** "vegan" ist ein
Wunschkriterium, "Senf" ein Ausschlusskriterium — die Filterlogik
behandelt beide unterschiedlich.

**Speiseplan ist mehr als eine Liste.** Filtern und Gruppieren liegt dort,
nicht im Servlet oder in der JSP. Sonst muessten Website und App dieselbe
Logik doppelt implementieren.

**Die DAO-Schicht wirft `DAOException`, nie `SQLException`.** Die
Service-Schicht soll nicht wissen, dass darunter eine relationale
Datenbank liegt.

## Datenquelle

Nicht scrapen. Das Studentenwerk OstNiedersachsen betreibt eine offizielle
API unter api.stw-on.de, dokumentiert unter github.com/stw-on/api-docs.
stw-on.de selbst untersagt automatisierten Zugriff per robots.txt und
liefert die Plaene ohnehin nur als PDF.

Die API ist ratenbegrenzt: bei HTTP 429 den `Retry-After`-Header beachten.
Der Importer soll periodisch laufen und in die Datenbank schreiben — nicht
bei jedem Request der App.
