# Rolle B: Service-Schicht und REST-Endpunkte

Dieses Dokument beschreibt, was zu tun ist. Ihr koennt es auch komplett
in Claude einfuegen und darunter eure konkrete Frage stellen - es
enthaelt allen Kontext, den ein Assistent braucht.

---

## Teil 1: Kontext

### Das Projekt

Pruefungsleistung im Kurs "Web- und mobile Anwendungsentwicklung" bei
Thomas Slotos, Leuphana Lueneburg. Gruppe von vier Personen.
Abgabe: 21.09.2026 in myStudy. Muendliches Gespraech in der letzten
Septemberwoche, jedes Gruppenmitglied wird zu allen Technologien
befragt.

Umgesetzt wird Alternative 1: JavaScript-Web-Framework (Angular) plus
native App (iOS/Swift). Beide Frontends sprechen ueber dieselbe
REST-Schnittstelle mit einem Java-Backend.

Repository: github.com/IsaKilic/WebMo_Mensa_Gruppe2

### Die Schichten

    Angular-Website  ──JSON──┐
                              ├──> REST ──> Service ──> DAO ──> MySQL
    iOS-App          ──JSON──┘
                              ^         ^
                              |         |
                          eure Aufgabe  |
                                    steht bereits

Rolle A hat Model, Datenbank und DAO-Schicht gebaut. Ihr baut die zwei
Schichten darueber. Danach helfen alle vier an den Frontends.

### Was bereits fertig ist

Fachklassen in `de.leuphana.mensa.model`:

    Essen              id, name, preis, art
    Essensplan         id, wochennummer, EnumMap<Wochentag, Essen>
    Essensbewertung    id, essenId, benutzerId, sterne, text,
                       fotoPfad, zeitpunkt
    Benutzer           id, benutzername, passwortHash, rolle

Enums: `Art` (VEGETARISCH, VEGAN, MIT_FLEISCH), `Wochentag`
(MONTAG..FREITAG), `Rolle` (USER, ADMIN).

DAO-Interfaces in `de.leuphana.mensa.persistence`:

    EssenDAO             anlegen, aendern, findById, findAlle, loeschen
    EssensplanDAO        dito, plus findByWoche
    EssensbewertungDAO   abgeben, aendern, findById, findByEssen,
                         durchschnittFuerEssen, loeschen
    BenutzerDAO          findByBenutzername, findById

Von jedem gibt es zwei Implementierungen: `...InMemory` (Testdaten im
Arbeitsspeicher) und `...Jdbc` (MySQL). Beide sind getestet.

Datenbank: MySQL, Schema in `backend/sql/01_schema.sql`, Testdaten in
`03_testdaten.sql` (10 Essen, 2 Benutzer).

Der REST-Vertrag steht in `docs/REST-API.md` - alle Endpunkte, Rollen
und Fehlercodes sind dort festgelegt.

### Wichtig: gegen Interfaces programmieren

Schreibt IMMER:

    EssenDAO dao = new EssenDAOInMemory();

und NIE:

    EssenDAOJdbc dao = new EssenDAOJdbc();

Der Variablentyp ist das Interface. Nur so laesst sich die
Implementierung tauschen, ohne euren Code zu aendern. Zum Entwickeln
nehmt ihr die InMemory-Variante - dann braucht ihr kein MySQL auf
eurem Rechner. Am Ende wird eine Zeile umgestellt.

Testbenutzer der InMemory-Variante: `admin`/`admin123` und
`user`/`user123`.

---

## Teil 2: Technische Entscheidung, die ihr treffen muesst

Fuer die REST-Endpunkte gibt es zwei Wege.

### Weg A: Servlets plus Jackson (empfohlen)

Ihr schreibt normale `HttpServlet`-Klassen wie in den Inkrementen und
nutzt die Bibliothek Jackson, um Java-Objekte in JSON zu verwandeln.

Vorteile:
- Servlets kennt ihr aus der Vorlesung. Im muendlichen Gespraech werden
  Fragen zur Servlet-Technologie gestellt - das ist ein direkter Bezug.
- Nur drei JAR-Dateien noetig: jackson-core, jackson-databind,
  jackson-annotations.
- Keine Konfiguration ausser web.xml, die ihr aus Inkrement11 kennt.

Nachteil: etwas mehr Handarbeit pro Endpunkt.

### Weg B: Jakarta REST (JAX-RS) mit Jersey

Der modernere Weg mit Annotationen wie `@GET` und `@Path`.

Nachteil: Tomcat ist nur ein Servlet-Container und bringt JAX-RS nicht
mit. Ihr muesstet rund zehn Jersey-JARs korrekt einbinden. Bei drei
Wochen Restzeit ist das ein vermeidbares Risiko.

**Empfehlung: Weg A.** Der Rest dieses Dokuments geht davon aus.

---

## Teil 3: Das Projekt muss ein Web-Projekt werden

Aktuell ist `backend` ein einfaches Java-Projekt in Eclipse. Fuer
Servlets braucht es ein **Dynamic Web Project** mit Tomcat.

Schritte in Eclipse:

1. File -> New -> Dynamic Web Project
2. Project name: `mensa-backend`
3. "Use default location" abwaehlen, Pfad auf den `backend`-Ordner
   des geklonten Repositories setzen
4. Target runtime: Apache Tomcat v11.0 (falls nicht vorhanden:
   New Runtime -> Apache Tomcat v11.0 -> Verzeichnis auswaehlen)
5. Dynamic web module version: 6.0
6. Content directory: `WebContent`
7. "Generate web.xml deployment descriptor" ankreuzen
8. Finish

Danach entsteht `WebContent/WEB-INF/`. Dorthin gehoeren:

- `web.xml` (wird erzeugt)
- `lib/` - hier hinein alle JAR-Dateien: MySQL Connector/J und die
  drei Jackson-JARs

JARs im Build Path allein reichen nicht. Tomcat sucht sie zur
Laufzeit in `WEB-INF/lib`.

### Jackson beschaffen

Von search.maven.org oder mvnrepository.com herunterladen, Version
2.17 oder neuer:

    jackson-core
    jackson-databind
    jackson-annotations

Alle drei nach `WebContent/WEB-INF/lib` legen und in Eclipse dem
Build Path hinzufuegen.

---

## Teil 4: Die Service-Schicht

Neues Package: `de.leuphana.mensa.service`

### Wofuer die Schicht da ist

Die Servlets sollen nur HTTP verstehen: Anfrage lesen, Antwort
schreiben. Die Regeln der Anwendung gehoeren in die Service-Schicht.

Warum getrennt? Weil die Regeln sonst in den Servlets stuenden und
niemand sie testen koennte, ohne einen Server zu starten. Und weil
dieselbe Regel bei mehreren Endpunkten gilt - "nur Admin darf Essen
aendern" betrifft POST, PUT und DELETE.

### Was hier hineingehoert

**Rollenpruefung.** Laut Aufgabenstellung darf der Admin nach dem
Login alle Funktionen bedienen, der User darf Bewertungen anlegen und
hat auf alles andere nur lesenden Zugriff.

**Fachliche Pruefungen.** Preis nicht negativ, Name nicht leer,
Wochennummer zwischen 1 und 53, Bewertungstext vorhanden.

**Zusammengesetzte Operationen.** Beispiel: beim Laden eines Essens
soll auch die Durchschnittsbewertung mitkommen. Das sind zwei
DAO-Aufrufe, die der Service zu einem Ergebnis verbindet.

### Vorgeschlagene Klassen

    AnmeldeService        anmelden(benutzername, passwort) -> Benutzer
    EssenService          CRUD mit Rollenpruefung
    EssensplanService     CRUD, Wochenfilter, Essen pro Tag setzen
    BewertungService      abgeben, aendern, Foto speichern

### Beispielaufbau

    public class EssenService {

        private final EssenDAO essenDAO;
        private final EssensbewertungDAO bewertungDAO;

        public EssenService(EssenDAO essenDAO,
                            EssensbewertungDAO bewertungDAO) {
            this.essenDAO = essenDAO;
            this.bewertungDAO = bewertungDAO;
        }

        public List<Essen> alleAnzeigen() {
            return essenDAO.findAlle();
        }

        public Essen anlegen(Essen essen, Benutzer angemeldet) {
            pruefeAdmin(angemeldet);
            pruefeGueltig(essen);
            return essenDAO.anlegen(essen);
        }

        private void pruefeAdmin(Benutzer benutzer) {
            if (benutzer == null) {
                throw new NichtAngemeldetException();
            }
            if (!benutzer.istAdmin()) {
                throw new KeineBerechtigungException();
            }
        }
    }

Die DAOs kommen ueber den Konstruktor herein, nicht per `new` in der
Klasse. So koennt ihr beim Testen die InMemory-Variante hineingeben
und im Betrieb die JDBC-Variante. Das nennt man Dependency Injection.

### Eigene Exceptions

Legt zwei Ausnahmen an, die die Servlets in HTTP-Codes uebersetzen:

    NichtAngemeldetException   -> HTTP 401
    KeineBerechtigungException -> HTTP 403

---

## Teil 5: Die REST-Endpunkte

Neues Package: `de.leuphana.mensa.rest`

Der vollstaendige Vertrag steht in `docs/REST-API.md`. Hier die
Kurzfassung:

    POST   /api/login                        anmelden
    POST   /api/logout                       abmelden
    GET    /api/session                      wer ist angemeldet

    GET    /api/essen                        alle Essen
    GET    /api/essen/{id}                   ein Essen
    POST   /api/essen                        anlegen (Admin)
    PUT    /api/essen/{id}                   aendern (Admin)
    DELETE /api/essen/{id}                   loeschen (Admin)

    GET    /api/essensplaene                 alle Plaene
    GET    /api/essensplaene?woche=3         nach Woche filtern
    POST   /api/essensplaene                 anlegen (Admin)
    PUT    /api/essensplaene/{id}            aendern (Admin)
    DELETE /api/essensplaene/{id}            loeschen (Admin)
    PUT    /api/essensplaene/{id}/tage/{tag} Essen setzen (Admin)
    DELETE /api/essensplaene/{id}/tage/{tag} Essen entfernen (Admin)

    GET    /api/essen/{id}/bewertungen       Bewertungen lesen
    POST   /api/essen/{id}/bewertungen       abgeben (multipart)
    PUT    /api/bewertungen/{id}             aendern
    DELETE /api/bewertungen/{id}             loeschen
    GET    /api/fotos/{dateiname}            Bild ausliefern

### Aufbau eines Servlets

    @WebServlet("/api/essen/*")
    public class EssenServlet extends HttpServlet {

        private EssenService service;
        private ObjectMapper mapper;

        @Override
        public void init() {
            EssenDAO essenDAO = new EssenDAOInMemory();
            EssensbewertungDAO bewDAO = new EssensbewertungDAOInMemory();
            service = new EssenService(essenDAO, bewDAO);
            mapper = new ObjectMapper();
        }

        @Override
        protected void doGet(HttpServletRequest anfrage,
                             HttpServletResponse antwort)
                throws IOException {

            antwort.setContentType("application/json");
            antwort.setCharacterEncoding("UTF-8");

            String pfad = anfrage.getPathInfo();   // null oder "/5"

            if (pfad == null || pfad.equals("/")) {
                mapper.writeValue(antwort.getWriter(),
                                  service.alleAnzeigen());
            } else {
                int id = Integer.parseInt(pfad.substring(1));
                Essen essen = service.anzeigen(id);
                if (essen == null) {
                    antwort.setStatus(404);
                    return;
                }
                mapper.writeValue(antwort.getWriter(), essen);
            }
        }
    }

Beachtet `/api/essen/*` mit Sternchen im `@WebServlet`. Damit landen
sowohl `/api/essen` als auch `/api/essen/5` bei diesem Servlet.
`getPathInfo()` liefert den Teil hinter dem Muster.

### JSON lesen

    Essen neu = mapper.readValue(anfrage.getReader(), Essen.class);

Damit das klappt, brauchen die Model-Klassen einen parameterlosen
Konstruktor und Getter/Setter. Beides ist vorhanden.

### Anmeldung ueber Session

Ihr nutzt `HttpSession`, wie in der Vorlesung behandelt
(SessionTracking, Cookies).

Beim Login:

    HttpSession sitzung = anfrage.getSession(true);
    sitzung.setAttribute("benutzer", angemeldeterBenutzer);

Bei jedem geschuetzten Aufruf:

    HttpSession sitzung = anfrage.getSession(false);
    Benutzer benutzer = (sitzung == null) ? null
        : (Benutzer) sitzung.getAttribute("benutzer");

Beim Logout:

    anfrage.getSession().invalidate();

Das Passwort wird NIE zurueckgegeben. Auch nicht der Hash.

### Foto-Upload

Das Servlet braucht die Annotation:

    @MultipartConfig(maxFileSize = 10 * 1024 * 1024)

Datei auslesen:

    Part foto = anfrage.getPart("foto");
    Part daten = anfrage.getPart("daten");

WICHTIG: den Dateinamen selbst vergeben, niemals den vom Client
uebernehmen. Sonst kann jemand mit `../../` aus dem Verzeichnis
ausbrechen.

    String name = UUID.randomUUID() + ".jpg";

### CORS - unbedingt frueh einbauen

Angular laeuft in der Entwicklung auf Port 4200, Tomcat auf 8080.
Das sind verschiedene Origins, und der Browser blockiert die Anfragen.
Ohne einen Filter funktioniert die Website gar nicht.

    @WebFilter("/api/*")
    public class CorsFilter implements Filter {
        public void doFilter(ServletRequest anfrage,
                             ServletResponse antwort,
                             FilterChain kette)
                throws IOException, ServletException {

            HttpServletResponse http = (HttpServletResponse) antwort;
            http.setHeader("Access-Control-Allow-Origin",
                           "http://localhost:4200");
            http.setHeader("Access-Control-Allow-Credentials", "true");
            http.setHeader("Access-Control-Allow-Methods",
                           "GET, POST, PUT, DELETE, OPTIONS");
            http.setHeader("Access-Control-Allow-Headers",
                           "Content-Type");

            if ("OPTIONS".equals(((HttpServletRequest) anfrage)
                    .getMethod())) {
                http.setStatus(200);
                return;
            }
            kette.doFilter(anfrage, antwort);
        }
    }

`Allow-Credentials` auf true ist noetig, sonst schickt der Browser das
Session-Cookie nicht mit. Und dann darf `Allow-Origin` kein `*` sein,
sondern muss die konkrete Adresse nennen.

---

## Teil 6: Reihenfolge

Arbeitet in dieser Reihenfolge, damit ihr frueh etwas Lauffaehiges habt.

1. Dynamic Web Project einrichten, Tomcat anbinden, JARs nach
   `WEB-INF/lib`
2. Ein einziges Servlet: `GET /api/essen` gibt die Liste als JSON
   zurueck. Im Browser aufrufen und pruefen.
3. CORS-Filter dazu
4. `AnmeldeService` und die drei Login-Endpunkte
5. `EssenService` und die restlichen Essen-Endpunkte mit Rollenpruefung
6. `EssensplanService` und die Plan-Endpunkte inklusive Wochenfilter
7. `BewertungService`, Foto-Upload, Foto-Ausliefern
8. Am Ende: von InMemory auf Jdbc umstellen

Schritt 2 ist der wichtigste Meilenstein. Sobald ein Endpunkt JSON
liefert, koennen die Frontend-Leute anfangen.

---

## Teil 7: Testen

Zum Ausprobieren braucht ihr kein Frontend. Im Browser gehen alle
GET-Aufrufe direkt:

    http://localhost:8080/mensa-backend/api/essen

Fuer POST, PUT und DELETE nehmt Postman oder curl:

    curl -X POST http://localhost:8080/mensa-backend/api/essen ^
      -H "Content-Type: application/json" ^
      -d "{\"name\":\"Test\",\"preis\":2.5,\"art\":\"VEGAN\"}"

---

## Teil 8: Fuer das muendliche Gespraech

Fragen, die zu eurem Teil naheliegen:

**Warum eine Service-Schicht zwischen Servlet und DAO?**
Damit die Fachregeln an einer Stelle stehen und ohne Server testbar
sind. Das Servlet kuemmert sich nur um HTTP.

**Wie funktioniert SessionTracking?**
Der Server legt eine `HttpSession` an und schickt eine Kennung als
Cookie (JSESSIONID) zum Browser. Bei jeder weiteren Anfrage schickt
der Browser das Cookie zurueck, und der Server findet die Sitzung
wieder. Alternative ohne Cookies: URL-Rewriting.

**Was ist der Unterschied zwischen 401 und 403?**
401 heisst nicht angemeldet, 403 heisst angemeldet aber nicht
berechtigt.

**Warum CORS?**
Die Same-Origin-Policy des Browsers verbietet Anfragen an eine andere
Herkunft. Angular auf 4200 und Tomcat auf 8080 sind verschiedene
Origins.

**Warum GET fuer Lesen und POST fuer Anlegen?**
GET ist sicher und idempotent - es aendert nichts und kann beliebig
wiederholt werden. POST erzeugt bei jedem Aufruf etwas Neues.

---

## Teil 9: Eure konkrete Frage

(Hier eintragen, wenn ihr dieses Dokument als Prompt nutzt.)

Beispiele:

- "Bau mir das EssenServlet komplett mit allen fuenf Methoden und
  Rollenpruefung."
- "Erklaer mir Schritt fuer Schritt, wie ich das Dynamic Web Project
  einrichte und Tomcat anbinde."
- "Ich bekomme beim Aufruf von /api/essen einen 404. Hier ist meine
  web.xml und mein Servlet: ..."
- "Schreib mir den AnmeldeService und das LoginServlet mit
  HttpSession."
