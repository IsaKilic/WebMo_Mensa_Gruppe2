# Rolle B: Service-Schicht und REST-Endpunkte - Erklaerung

Diese Version ist gegen die ECHTEN Dateien aus dem Repository
abgeglichen (Model, Persistence, `docs/AUFGABE-ROLLE-B.md`,
`docs/REST-API.md`, `docs/AENDERUNGEN.md`, `backend/README.md`) und
setzt die vier gemeinsamen Entscheidungen um: Jackson statt
Hand-JSON, jedes Servlet holt sich seine Services in `init()` (ueber
eine gemeinsame `Fabrik`, siehe Punkt 4), die Namen aus
`docs/AUFGABE-ROLLE-B.md` (Package `de.leuphana.mensa.rest`, Klassen
`AnmeldeService`/`EssenService`/`EssensplanService`/
`BewertungService`, Exceptions `NichtAngemeldetException`/
`KeineBerechtigungException`).

Ein Teammitglied aus Rolle B hatte parallel eine eigene Umsetzung
gebaut. Wir haben beide Versionen verglichen (gegen die echten
Model-/Persistence-Klassen und gegen `docs/REST-API.md` geprueft) und
drei gute Ideen daraus in diese Version uebernommen: die
`Fabrik`-Klasse (Punkt 4), die zentrale Fehlerbehandlung ueber eine
`JsonServlet`-Basisklasse (Punkt 6) und zwei kleinere
Sicherheits-/Komfort-Details (Session-Fixation-Schutz beim Login,
Punkt 7; `Access-Control-Max-Age`, Punkt 8). Nicht uebernommen wurde
alles, was gegen die echten Model-Methoden nicht kompilierte oder
Vorgaben aus `docs/REST-API.md` verletzte (z.B. fehlende
409-Konflikt-Pruefungen, ein Gast, der trotz Rollen-Tabelle lesen
konnte, und ein `PUT` auf den Essensplan, das den kompletten
Plan ueberschrieben und damit alle bereits eingetragenen Tage
geloescht haette) - dazu unten mehr, wo es jeweils passt.

## 1. Die vier Schichten und wo eure Dateien liegen

    Angular / iOS  ──JSON──>  Servlet  ──>  Service  ──>  DAO  ──>  Model/DB
                              (Rolle B)     (Rolle B)      (Rolle A)

    de.leuphana.mensa.rest         <- Servlets, Filter, DTOs (NEU, dieses Paket)
    de.leuphana.mensa.service      <- Fachlogik, Exceptions (NEU, dieses Paket)
    de.leuphana.mensa.persistence  <- DAO-Interfaces + InMemory/Jdbc (Rolle A, unveraendert)
    de.leuphana.mensa.model        <- Essen, Essensplan, Essensbewertung, Benutzer (Rolle A, unveraendert)

Das ist dieselbe Grundidee wie der Webshop aus dem Kurs
(`WebshopDispatcherServlet` -> Action -> `Catalog`), nur ohne Front
Controller: statt einem Servlet, das per `dispatchAction`-Parameter in
Action-Klassen verzweigt, hat hier jede REST-Ressource ihr eigenes
Servlet mit eigenem URL-Muster (`@WebServlet("/api/essen/*")`). Das
passt besser zu REST, weil die URL selbst schon die Ressource benennt
- ein Front Controller wuerde hier nur eine zusaetzliche
Umleitungsebene einziehen, ohne dass es beim Kurs-Webshop eine
API-Route pro Ressource gibt, die man bequem einem Servlet-Pattern
zuordnen koennte.

## 2. Warum ueberhaupt eine Service-Schicht?

Ohne sie wuerden Rollenpruefung ("nur Admin darf Essen anlegen") und
fachliche Regeln (Preis nicht negativ, Bewertungstext Pflicht) in den
Servlets stehen. Zwei Probleme daran: erstens gilt dieselbe Regel oft
fuer mehrere HTTP-Methoden (POST, PUT und DELETE auf `/api/essen`
brauchen alle "nur Admin"), zweitens laesst sich eine Regel im Servlet
nicht ohne laufenden Tomcat testen. Die Service-Klassen
(`EssenService`, `EssensplanService`, `BewertungService`,
`AnmeldeService`) kennen kein HTTP - sie bekommen normale Java-Objekte
und werfen normale Exceptions.

## 3. Wie eine Anfrage durchlaeuft (Beispiel: `POST /api/essen`)

1. `CorsFilter` laesst die Anfrage durch (Origin passt, keine
   OPTIONS-Preflight-Anfrage).
2. `EssenServlet.doPost(...)` liest den JSON-Body mit Jackson in ein
   `Essen`-Objekt ein, liest den angemeldeten Benutzer aus der Session.
3. `EssenService.anlegen(essen, angemeldet)` prueft zuerst die Rolle
   (`pruefeAdmin`: 401 wenn nicht angemeldet, 403 wenn nicht Admin),
   dann die fachlichen Regeln (`pruefeGueltig`: 400 bei leerem Namen,
   negativem Preis, fehlender Art), dann erst `essenDAO.anlegen(...)`.
4. Bei Erfolg: 201 mit dem angelegten Essen als JSON. Bei einer
   Exception faengt die `service(...)`-Methode der gemeinsamen
   `JsonServlet`-Basisklasse sie zentral ab und schreibt den passenden
   HTTP-Status plus `{"fehler": "...", "meldung": "..."}` - siehe
   Punkt 6.

## 4. DAO-Instanziierung: die `Fabrik`-Klasse

`docs/AUFGABE-ROLLE-B.md` (Teil 5) zeigt als Muster, dass jedes
Servlet seine DAOs selbst in `init()` baut:

    public void init() {
        EssenDAO essenDAO = new EssenDAOInMemory();
        EssensbewertungDAO bewDAO = new EssensbewertungDAOInMemory();
        service = new EssenService(essenDAO, bewDAO, ...);
    }

Woertlich so umgesetzt haette das einen Bug: die vier InMemory-DAOs
(`EssenDAOInMemory` &c.) speichern ihre Daten in normalen
Instanzfeldern, nicht `static`. Jedes `new EssenDAOInMemory()` bekaeme
also seinen eigenen, unabhaengigen Datenspeicher. Da `EssenServlet`
UND `EssensplanServlet` beide eine `EssenDAO`-Instanz brauchen, waere
ein frisch ueber `EssenServlet` angelegtes Essen fuer
`EssensplanServlet` nicht sichtbar (404), solange die InMemory-Variante
laeuft.

Deshalb gibt es `de.leuphana.mensa.service.Fabrik`: eine kleine
statische Fabrik-Klasse (Faktorenmuster / Singleton-artig, aber ohne
`ServletContextListener` und ohne Application-Scope), die jede DAO- und
Service-Instanz genau EINMAL erzeugt und beim ersten Aufruf
zwischenspeichert ("lazy initialization"):

    public class Fabrik {
        private static EssenDAO essenDAO;

        public static synchronized EssenDAO essenDAO() {
            if (essenDAO == null) {
                essenDAO = new EssenDAOInMemory();
            }
            return essenDAO;
        }
        // genauso fuer essensplanDAO(), bewertungDAO(), benutzerDAO(),
        // fotoSpeicher() und die vier Service-Klassen, die diese DAOs
        // per Konstruktor bekommen.
    }

Jedes Servlet ruft in seinem `init()` jetzt nur noch z.B.
`Fabrik.essenService()` auf:

    public void init() {
        super.init();               // JsonServlet.init(), siehe Punkt 6
        essenService = Fabrik.essenService();
    }

Damit bekommen alle Servlets dieselbe, geteilte DAO-Instanz - das
frisch angelegte Essen ist sofort auch fuer `EssensplanServlet`
sichtbar. `synchronized` schuetzt nur den kurzen Moment der
Erst-Erzeugung vor einem Wettlauf zwischen zwei gleichzeitigen
HTTP-Anfragen (Tomcat bedient Anfragen mit mehreren Threads
gleichzeitig) - danach wird nur noch das bereits erzeugte Objekt
zurueckgegeben.

Das ist eine Idee, die wir aus dem Vergleich mit der parallelen
Rolle-B-Umsetzung uebernommen haben (siehe Hinweis oben) - sie loest
das Problem sauberer als die urspruenglich geplante Bitte an Rolle A,
die vier DAO-Klassen `static` zu machen (das haette Rolle-A-Code
angefasst, obwohl das Problem rein in Rolle B entsteht). Die
entsprechende Bitte in `Bitte-an-Rolle-A.md` ist damit zurueckgezogen.

Sobald auf Jdbc umgestellt wird (Schritt 8), aendert sich nur die eine
Zeile in `Fabrik`, die die Instanz erzeugt (`new EssenDAOJdbc(...)`
statt `new EssenDAOInMemory()`) - der Rest von `Fabrik` und alle
Servlets bleiben unveraendert, weil ueberall gegen das `EssenDAO`-
Interface programmiert wird (siehe auch Punkt 16, Dependency
Injection).

Fuer das muendliche Gespraech ist das ein gutes Beispiel fuer den
Unterschied zwischen In-Memory-Zustand (lebt im Java-Heap, pro
Objekt-Instanz getrennt, solange man nicht bewusst teilt) und
Datenbank-Zustand (lebt in der DB, automatisch von allen Instanzen
geteilt) - und dafuer, dass ein Zustands-Teilungsproblem nicht
zwingend einen Application-Scope-Singleton (`ServletContextListener`)
braucht, wenn eine einfache statische Fabrik reicht.

## 5. JSON mit Jackson

Frueher (erste Version) hatte ich eine eigene kleine JSON-Bibliothek
geschrieben, weil das Sandbox-Netzwerk keinen Zugriff auf Maven
Central hatte, um Jackson zum Kompilieren zu laden. Das war ein reiner
Verlegenheits-Workaround von mir, keine Team-Entscheidung. Jetzt, nach
`docs/AUFGABE-ROLLE-B.md` (Teil 2, "Weg A: Servlets plus Jackson,
empfohlen") und eurer Entscheidung, ist auf `com.fasterxml.jackson`
umgestellt:

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(antwort.getWriter(), objekt);          // Java -> JSON
    Essen neu = mapper.readValue(anfrage.getReader(), Essen.class); // JSON -> Java

Jackson serialisiert ueber Getter (Essen hat `getName()`,
`getPreis()`, ... -> JSON-Felder `name`, `preis`, ...) und
deserialisiert ueber den parameterlosen Konstruktor plus Setter -
beides ist in `Essen`, `Essensplan`, `Essensbewertung` bereits
vorhanden.

**In Eclipse:** die drei JARs `jackson-core`, `jackson-databind`,
`jackson-annotations` (Version 2.17 oder neuer, von search.maven.org
oder mvnrepository.com) nach `WebContent/WEB-INF/lib` legen und dem
Build Path hinzufuegen - genau wie in `docs/AUFGABE-ROLLE-B.md`, Teil 3
beschrieben.

## 6. Fehlerbehandlung

Alle Servlets ausser `FotoServlet` (siehe Punkt 10, das liefert
Bilddaten statt JSON) erben von `de.leuphana.mensa.rest.JsonServlet`
statt direkt von `HttpServlet`. `JsonServlet` ueberschreibt die
Methode `service(HttpServletRequest, HttpServletResponse)` (die
Methode, die `HttpServlet` normalerweise intern nutzt, um je nach
HTTP-Methode an `doGet`/`doPost`/`doPut`/`doDelete` weiterzuverzweigen)
genau EINMAL zentral:

    @Override
    protected void service(HttpServletRequest anfrage, HttpServletResponse antwort)
            throws ServletException, IOException {
        antwort.setContentType("application/json; charset=UTF-8");
        try {
            super.service(anfrage, antwort);   // verzweigt an doGet/doPost/...
        } catch (ValidierungException | NumberFormatException e) {
            fehler(antwort, 400, "VALIDIERUNG", e.getMessage());
        } catch (UngueltigeAnmeldungException e) {
            fehler(antwort, 401, "ANMELDUNG_FEHLGESCHLAGEN", e.getMessage());
        } catch (NichtAngemeldetException e) {
            fehler(antwort, 401, "NICHT_ANGEMELDET", e.getMessage());
        } catch (KeineBerechtigungException e) {
            fehler(antwort, 403, "KEINE_BERECHTIGUNG", e.getMessage());
        } catch (NichtGefundenException e) {
            fehler(antwort, 404, "NICHT_GEFUNDEN", e.getMessage());
        } catch (KonfliktException e) {
            fehler(antwort, 409, "KONFLIKT", e.getMessage());
        } catch (IllegalStateException e) {
            fehler(antwort, 413, "DATEI_ZU_GROSS", "Die Datei ist zu gross");
        } catch (RuntimeException e) {
            fehler(antwort, 500, "SERVERFEHLER", "Unerwarteter Fehler");
        }
    }

Jedes einzelne `doGet`/`doPost`/`doPut`/`doDelete` darf die passende
Exception also einfach werfen (`throw new ValidierungException(...)`),
ohne selbst try/catch zu schreiben - das ist der Vorteil gegenueber
einer Loesung, die jede Methode einzeln in einen Hilfsaufruf
einpackt: die Abbildung Exception -> HTTP-Status steht nur an EINER
Stelle im Projekt.

Sechs eigene Exceptions, zentral auf HTTP-Codes abgebildet:

| Exception | HTTP | Bedeutung |
|---|---|---|
| `ValidierungException` | 400 | Eingabe ungueltig (Name leer, Preis negativ, Wochennummer ausserhalb 1-53, Bewertungstext fehlt, ...) |
| `NichtAngemeldetException` | 401 | keine Session vorhanden |
| `UngueltigeAnmeldungException` | 401 | Login-Versuch selbst falsch (Benutzername/Passwort) |
| `KeineBerechtigungException` | 403 | angemeldet, aber falsche Rolle |
| `NichtGefundenException` | 404 | Essen/Plan/Bewertung existiert nicht |
| `KonfliktException` | 409 | doppelte Wochennummer, Essen noch in Plan verwendet |

`docs/AUFGABE-ROLLE-B.md` (Teil 4) verlangt woertlich nur die beiden
mittleren (401/403) - die anderen vier kommen dazu, weil
`docs/REST-API.md` unter "Fehlerformat" ausdruecklich 400/404/409/413
fordert. Zusaetzlich faengt `JsonServlet` `IllegalStateException` ab
(das wirft unser eigener Code in `FotoSpeicher`, wenn das Speichern
fehlschlaegt, und sinngemaess auch ein zu grosser Multipart-Teil) und
bildet es auf 413 ab, sowie `RuntimeException` als Auffangnetz auf 500
- damit ein unerwarteter Bug nie als rohe Tomcat-Fehlerseite beim
Client landet, sondern immer als sauberes JSON im vereinbarten Format.
Wichtig ist die Reihenfolge der `catch`-Bloecke: `IllegalStateException`
muss VOR dem allgemeinen `RuntimeException`-Fangnetz stehen, sonst
wuerde ein zu grosses Foto faelschlich als 500 statt als 413
beantwortet werden.

## 7. Anmeldung ueber Session

Wie in der Vorlesung (SessionTracking):

    // Login:
    HttpSession sitzung = anfrage.getSession(true);
    sitzung.setAttribute("benutzer", angemeldeterBenutzer);

    // Bei jedem geschuetzten Aufruf (Anmeldung.angemeldeterBenutzer(...)):
    HttpSession sitzung = anfrage.getSession(false);
    Benutzer benutzer = (sitzung == null) ? null : (Benutzer) sitzung.getAttribute("benutzer");

    // Logout:
    anfrage.getSession().invalidate();

Der Browser verwaltet das `JSESSIONID`-Cookie automatisch, die
iOS-App (`URLSession`) ebenfalls im Standardverhalten - deshalb diese
Variante statt eines Tokens im Authorization-Header, siehe
`docs/REST-API.md` "Entscheidungen": naeher am Kursstoff.

Das Passwort wird nie zurueckgegeben - `BenutzerDTO` kennt gar kein
Passwortfeld.

**Session-Fixation-Schutz beim Login:** `LoginServlet.doPost(...)`
invalidiert zuerst eine eventuell schon bestehende Session, bevor eine
neue angelegt wird:

    HttpSession alte = anfrage.getSession(false);
    if (alte != null) {
        alte.invalidate();
    }
    HttpSession sitzung = anfrage.getSession(true);
    sitzung.setAttribute(Anmeldung.SESSION_ATTRIBUT, benutzer);

Warum das noetig ist: ohne diesen Schritt wuerde ein erfolgreicher
Login einfach die Session-ID weiterverwenden, die der Client (bzw. ein
Angreifer) schon vorher hatte. Session Fixation heisst der Angriff,
bei dem jemand einem Opfer vorab eine bekannte Session-ID unterschiebt
und nach dessen Login dieselbe ID benutzt, um sich als das Opfer
auszugeben. Eine frische Session-ID bei jedem Login schliesst das aus.
Diese Idee stammt aus dem Vergleich mit der parallelen
Rolle-B-Umsetzung (siehe Hinweis am Anfang dieser Datei).

## 8. CORS

`CorsFilter` (`@WebFilter("/api/*")`) setzt bei jeder Antwort
`Access-Control-Allow-Origin: http://localhost:4200` und
`Access-Control-Allow-Credentials: true` (damit der Browser das
Session-Cookie ueberhaupt mitschickt - dafuer darf `Allow-Origin` laut
Spezifikation kein `*` sein) und beantwortet die
`OPTIONS`-Preflight-Anfrage direkt mit 200, ohne sie ans eigentliche
Servlet weiterzuleiten.

Zusaetzlich setzt der Filter `Access-Control-Max-Age: 3600`: der
Browser darf das Ergebnis einer Preflight-Anfrage dann eine Stunde
lang wiederverwenden, statt vor jedem einzelnen PUT/DELETE erneut per
OPTIONS nachzufragen - spart unnoetige Zusatz-Anfragen, ohne die
eigentliche CORS-Pruefung abzuschwaechen. Auch das eine Idee aus dem
Vergleich mit der parallelen Rolle-B-Umsetzung.

## 9. Passwort-Pruefung - bewusste, dokumentierte Zwischenloesung

`Benutzer` verlangt per Javadoc einen echten Hash (BCrypt/PBKDF2), aber
`BenutzerDAOInMemory` legt die beiden Testbenutzer aktuell im
**Klartext** an (`"admin123"` direkt im `passwortHash`-Feld - Kommentar
von Rolle A selbst: "damit ihr sofort testen koennt"). Ein echter
BCrypt-Vergleich wuerde gegen diesen Klartext sofort scheitern.

`AnmeldeService.passwortPasst(...)` vergleicht deshalb vorerst per
`String.equals(...)`, in einer eigenen, klar kommentierten Methode.
Sobald Rolle A auf echte Hashes umstellt, wird NUR diese eine Methode
ausgetauscht (z.B. gegen `BCrypt.checkpw(...)`), der Rest von
`AnmeldeService` und alle Servlets bleiben unveraendert. Fuers
muendliche Gespraech: das ist eine bewusste, benannte technische
Schuld mit klarem Migrationspfad - keine vergessene Sicherheitsluecke.
Siehe auch `Bitte-an-Rolle-A.md`.

## 10. Foto-Upload

`POST /api/essen/{id}/bewertungen` und `PUT /api/bewertungen/{id}` sind
`multipart/form-data` mit zwei Teilen: `daten` (JSON-Text mit
`sterne`/`text`) und `foto` (die Bilddatei, bei PUT optional - fehlt
er, bleibt laut `docs/REST-API.md` das alte Bild).

`@MultipartConfig(maxFileSize = 10 * 1024 * 1024, ...)` erzwingt das
10-MB-Limit aus `docs/REST-API.md` technisch - ohne diese Annotation
wuerde Tomcat gar keine Grenze durchsetzen.

`FotoSpeicher` vergibt IMMER selbst einen `UUID`-Dateinamen und
uebernimmt nie den vom Client mitgeschickten Namen
(`Part.getSubmittedFileName()` wird nur fuer die Dateiendung
angeschaut). Grund: sonst koennte ein praeparierter Name wie
`../../etc/passwd` aus dem Zielverzeichnis ausbrechen (Path
Traversal) - explizit so in `docs/REST-API.md` gefordert.

`FotoSpeicher.pfadZu(...)` prueft beim Lesen (`FotoServlet`) den
Dateinamen gegen eine **Whitelist** (regulaerer Ausdruck), nicht gegen
eine Blacklist einzelner verbotener Zeichen wie `..`, `/` oder `\`:

    private static final Pattern GUELTIGER_DATEINAME = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
        + "[0-9a-fA-F]{12}\\.(jpg|jpeg|png|webp)$");

Das ist genau das Muster, das `speichern(...)` selbst erzeugt
(`UUID.randomUUID() + "." + endung`). Ein Dateiname, der nicht exakt
diesem Muster entspricht, wird sofort abgelehnt (`ValidierungException`,
400) - egal mit welchem Trick jemand versucht, daraus auszubrechen. Der
Unterschied zur Blacklist: eine Blacklist muesste jede denkbare
Umgehung einzeln kennen (`..`, doppelt kodierte Schraegstriche,
Backslashes unter Windows, ...), eine Whitelist laesst strukturell gar
nichts anderes durch. Zur Sicherheit wird der aufgeloeste Pfad danach
trotzdem noch mit `normalize()` bereinigt und geprueft, dass er
weiterhin innerhalb des Zielverzeichnisses liegt - zwei unabhaengige
Sicherungen gegen denselben Angriff. Diese Whitelist-Variante haben
wir aus dem Vergleich mit der parallelen Rolle-B-Umsetzung
uebernommen (dort allerdings nur fuer `jpg`/`png` - wir haben
`jpeg`/`webp` mit aufgenommen, weil unser `FotoSpeicher` diese
Endungen auch beim Speichern zulaesst).

## 11. DTOs statt Model direkt serialisieren

`EssenDTO`, `EssensplanDTO`, `EssensbewertungDTO`, `BenutzerDTO` (Paket
`de.leuphana.mensa.rest.dto`) statt `Essen`/`Essensplan`/... direkt mit
Jackson zu serialisieren. Gruende:

- `durchschnittsbewertung`/`anzahlBewertungen` stehen laut
  `docs/REST-API.md` bei jedem Essen dabei, sind aber im Model nicht
  vorhanden (berechnet aus zwei DAO-Aufrufen im Service). `EssenDTO`
  hat dafuer zwei Fabrikmethoden: `kompakt(essen)` (ohne die beiden
  Felder, fuer die verschachtelte Verwendung in `EssensplanDTO`) und
  `mitBewertung(essen, schnitt, anzahl)` (fuer die eigenstaendigen
  `GET /api/essen`-Endpunkte). `@JsonInclude(NON_NULL)` sorgt dafuer,
  dass die beiden Felder im JSON komplett fehlen, wenn `kompakt(...)`
  verwendet wurde - genau die schlanke Form, die `docs/REST-API.md`
  fuer das Essen innerhalb eines Essensplans zeigt.
- `EssensplanDTO.essenProWoche` ist ein Objekt mit den
  Wochentag-Namen als Schluesseln (`"MONTAG": {...}`), keine Liste -
  damit im JSON direkt sichtbar ist, dass pro Tag hoechstens ein Essen
  steht. `vollstaendig` kommt direkt von `Essensplan.istVollstaendig()`
  (existiert bereits im Model), wird hier nicht nochmal selbst
  nachgerechnet.
- `EssensbewertungDTO` zeigt den Benutzernamen statt der
  `benutzerId` (damit die Frontends ihn direkt anzeigen koennen) und
  eine fertige `fotoUrl` statt nur des gespeicherten Dateinamens.
- `BenutzerDTO` hat gar kein Passwortfeld - kann also nie versehentlich
  mitgeschickt werden.

## 12. Essen loeschen: 409 statt Mitloeschen

`docs/REST-API.md` laesst zwei Wege offen ("409 ... Alternativ im
Backend die Zuordnung mitloeschen; entscheidet euch fuer eines und
haltet es durch"). Wir liefern 409, wenn ein Essen noch in
mindestens einem Essensplan steht (`EssenService.loeschen(...)` prueft
das ueber `essensplanDAO.findAlle()`). Begruendung: ein Essensplan soll
nie stillschweigend eine Luecke bekommen, nur weil irgendwo ein Essen
geloescht wurde - das waere fuer Nutzer der App verwirrend
("Warum fehlt Mittwoch?").

## 13. Wochennummer-Validierung

`EssensplanService` prueft beim Anlegen und beim Aendern, dass die
Wochennummer zwischen 1 und 53 liegt (`ValidierungException`, 400) -
Vorgabe aus `docs/AUFGABE-ROLLE-B.md`, Teil 4 ("Wochennummer zwischen
1 und 53").

## 14. Offene Punkte / Was von Rolle A noch kommt

Siehe `Bitte-an-Rolle-A.md` fuer die Details. Nur noch ein Punkt
offen (die Datenteilung zwischen Servlets aus Punkt 4 ist inzwischen
durch die `Fabrik`-Klasse geloest, ganz ohne Aenderung an Rolle A):

1. Echtes Passwort-Hashing (BCrypt/PBKDF2) statt Klartext in
   `BenutzerDAOInMemory` - `AnmeldeService` ist schon so gebaut, dass
   nur eine Methode angepasst werden muss.

## 15. Testen (Meilenstein)

Nach Einrichtung als Dynamic Web Project (`docs/AUFGABE-ROLLE-B.md`,
Teil 3) im Browser pruefen:

    http://localhost:8080/mensa-backend/api/essen

Sobald das JSON liefert, koennen Rolle C (App) und Rolle D (Website)
mit Mock- bzw. echten Daten weiterarbeiten. Fuer POST/PUT/DELETE
Postman oder curl verwenden (Beispiel in `docs/AUFGABE-ROLLE-B.md`,
Teil 7).

## 16. Fuer das muendliche Gespraech - kurze Antworten

**Warum Servlets statt JAX-RS?** Tomcat ist nur ein Servlet-Container
und bringt JAX-RS nicht mit - man muesste rund zehn Jersey-JARs korrekt
einbinden. Bei drei Wochen Restzeit ein vermeidbares Risiko, und
Servlets sind direkter Kursstoff (Frage dazu ist im muendlichen
Gespraech wahrscheinlich).

**Warum Session statt Token im Header?** SessionTracking per Cookie
ist das Verfahren aus der Vorlesung. Token im Authorization-Header
waere fuer eine native App heute ueblicher, aber weiter weg vom
Kursstoff (Begruendung so auch in `docs/REST-API.md`).

**401 vs. 403?** 401 = nicht angemeldet. 403 = angemeldet, aber ohne
die noetige Rolle.

**Warum eine Service-Schicht?** Damit Rollenpruefung und fachliche
Regeln an einer Stelle stehen, mehrfach wiederverwendet werden
koennen (POST/PUT/DELETE brauchen alle "nur Admin") und ohne
laufenden Server testbar sind.

**Warum Dependency Injection ueber den Konstruktor?** Damit die
Service-Klassen gegen das DAO-Interface programmieren, nicht gegen
`...InMemory` oder `...Jdbc` konkret - beim Umstieg auf die
Datenbank aendert sich nur die `new`-Zeile in `init()`, nicht die
Service- oder Servlet-Klassen selbst.

**Warum UUID-Dateinamen fuer Fotos?** Der vom Client geschickte
Dateiname darf serverseitig nie uebernommen werden - sonst koennte
jemand mit `../../etc/passwd` aus dem Zielverzeichnis ausbrechen
(Path Traversal). Ein zufaellig generierter Name schliesst das aus.

**Warum eine `Fabrik`-Klasse statt eines `ServletContextListener`s?**
Beide loesen dasselbe Problem (eine geteilte Instanz statt vieler
unabhaengiger). Ein `ServletContextListener` haengt seine Objekte in
den Application-Scope (`ServletContext`) und muss dort ueber
`getAttribute("...")` mit einem String-Schluessel und einem Cast wieder
herausgeholt werden. Die statische Fabrik gibt stattdessen typsichere
Methoden (`Fabrik.essenService()`) zurueck - kein Cast, kein
Tippfehler-Risiko beim Schluessel-String, und sie ist unabhaengig vom
Servlet-Container in einem einfachen `main`-Test verwendbar. Fachlich
ist es dieselbe Idee wie Application-Scope aus dem Kurs: eine Instanz,
von allen geteilt, einmal erzeugt.
