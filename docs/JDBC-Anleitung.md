# EssenDAOJdbc einbauen

## 1. Datei kopieren

`EssenDAOJdbc.java` nach

    backend/src/de/leuphana/mensa/persistence/jdbc/

Dort liegt schon die `ConnectionFactory`, die diese Klasse benutzt.

## 2. Connector/J in Eclipse einbinden

Rechtsklick auf das Projekt -> Properties -> Java Build Path -> Libraries
-> Classpath auswaehlen -> Add External JARs -> die Datei

    C:\Users\ikili\Downloads\mysql-connector-j-26.7.0\mysql-connector-j-26.7.0.jar

Apply and Close.

Spaeter, wenn das Projekt ein Dynamic Web Project ist, muss die JAR
zusaetzlich nach `WebContent/WEB-INF/lib` - sonst findet Tomcat den
Treiber zur Laufzeit nicht. Fuer den Test aus Eclipse heraus reicht der
Build Path.

## 3. Testdaten einspielen

Die Tabelle essen ist noch leer. In PowerShell:

    mysql -u mensa_app -p mensa -e "INSERT INTO essen (name,preis,art) VALUES ('Haehnchenbrust in Sesampanade',2.85,'MIT_FLEISCH'),('Gemueselasagne',2.40,'VEGETARISCH'),('Ofengemuese mit Hirse',2.60,'VEGAN');"

## 4. Test ausfuehren

`TestEssenDAOJdbc.java` nach `backend/src/` legen (ohne Package, direkt
im Wurzelverzeichnis der Quellen), dann Rechtsklick -> Run As ->
Java Application.

Erwartete Ausgabe: Anlegen mit vergebener Id, Aendern, Loeschen, und
zweimal null fuer nicht vorhandene Ids.

## Wenn es nicht laeuft

**"datenbank.properties nicht im Classpath gefunden"**
Die Datei liegt nicht unter `backend/src/`. Sie muss im Source Folder
liegen, damit sie beim Kompilieren nach `bin/` mitkopiert wird.

**"No suitable driver found"**
Connector/J fehlt im Build Path, siehe Schritt 2.

**"Access denied for user 'mensa_app'"**
Passwort in `datenbank.properties` stimmt nicht.

**"Unknown database 'mensa'"**
Der Datenbankname in der URL stimmt nicht, oder das Schema wurde nicht
eingespielt.

## Muster fuer die drei uebrigen DAOs

Diese Klasse ist die Vorlage. Was ihr uebernehmt:

- SQL als `private static final String` oben in der Klasse. Dann sieht
  man auf einen Blick, was die Klasse mit der Datenbank macht.
- Immer `PreparedStatement` mit Platzhaltern, nie String-Verkettung.
  Sonst SQL-Injection.
- `try-with-resources` fuer Connection, Statement und ResultSet.
- `Statement.RETURN_GENERATED_KEYS` beim Anlegen, um die vergebene Id
  zurueckzubekommen.
- Eine private Hilfsmethode `ausZeile(ResultSet)` fuer das Mapping,
  damit es nicht doppelt in findById und findAlle steht.
- `SQLException` wird immer zu `DAOException`.

`EssensplanDAOJdbc` wird die aufwendigste: dort muss die EnumMap ueber
die Tabelle essensplan_essen geladen werden, also ein Join oder eine
zweite Abfrage pro Plan. Sagt Bescheid, wenn ihr da seid.
