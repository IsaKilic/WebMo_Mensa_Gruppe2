# REST-Schnittstelle

Vertrag zwischen Backend (Rolle B) und den beiden Frontends.
Sobald er steht, koennen alle parallel arbeiten: die Frontends gegen
Mock-Daten, das Backend gegen die In-Memory-DAOs.

Basis-URL: `http://localhost:8080/mensa/api`

Alle Antworten sind JSON in UTF-8. Ausnahme: der Foto-Upload ist
multipart, und der Foto-Abruf liefert Bilddaten.

## Entscheidungen

**Anmeldung ueber Session mit Cookie.** Der Server legt beim Login eine
`HttpSession` an und legt den Benutzer hinein. Der Browser schickt das
`JSESSIONID`-Cookie automatisch mit, die iOS-App muss Cookies aktiviert
lassen (bei `URLSession` Standardverhalten).

Begruendung fuer das muendliche Gespraech: SessionTracking ueber Cookies
ist genau das Verfahren aus der Vorlesung. Die Alternative waere ein
Token im Authorization-Header - fuer eine native App heute ueblicher,
aber weiter weg vom Kursstoff.

**Foto als Multipart-Upload.** Fotos vom Handy sind schnell mehrere
Megabyte. Als base64 im JSON waechst das um ein Drittel und liegt
komplett im Arbeitsspeicher. Multipart streamt.

Die Datei landet im Dateisystem, in der Datenbank steht nur der Pfad.

## Rollen

| Endpunkt | Gast | User | Admin |
|---|---|---|---|
| Lesen (GET) | nein | ja | ja |
| Bewertung anlegen und aendern | nein | ja | ja |
| Essen und Essensplan aendern | nein | nein | ja |

Ein nicht angemeldeter Aufruf liefert 401, ein angemeldeter ohne
Berechtigung 403.

---

## Anmeldung

### POST /api/login

    { "benutzername": "admin", "passwort": "admin123" }

200:

    { "id": 1, "benutzername": "admin", "rolle": "ADMIN" }

401 bei falschen Daten. Das Passwort wird nie zurueckgegeben.

### POST /api/logout

204. Invalidiert die Session.

### GET /api/session

Liefert den angemeldeten Benutzer oder 401. Die Frontends rufen das beim
Start auf, um zu wissen, ob noch eine Sitzung besteht.

---

## Essen

### GET /api/essen

    [ { "id": 1, "name": "Hähnchenbrust in Sesampanade",
        "preis": 2.85, "art": "MIT_FLEISCH",
        "durchschnittsbewertung": 3.7, "anzahlBewertungen": 12 } ]

Die beiden Bewertungsfelder sind berechnet, nicht gespeichert. Sie
sparen den Frontends einen zweiten Aufruf pro Essen.

### GET /api/essen/{id}

Ein einzelnes Essen, gleiche Struktur. 404 wenn es das nicht gibt.

### POST /api/essen

Nur Admin.

    { "name": "Gemüselasagne", "preis": 2.40, "art": "VEGETARISCH" }

201 mit dem angelegten Objekt inklusive vergebener Id.
400 wenn Name leer, Preis negativ oder Art unbekannt ist.

### PUT /api/essen/{id}

Nur Admin. Gleicher Rumpf wie POST. 200 mit dem geaenderten Objekt.

### DELETE /api/essen/{id}

Nur Admin. 204.

409 wenn das Essen noch in einem Essensplan verwendet wird - sonst
haette der Plan eine Luecke. Alternativ im Backend die Zuordnung
mitloeschen; entscheidet euch fuer eines und haltet es durch.

---

## Essensplan

### GET /api/essensplaene

Alle acht Plaene, aufsteigend nach Wochennummer.

### GET /api/essensplaene?woche=3

Filtern nach Woche. Liefert den einen Plan oder 404.

    { "id": 3, "wochennummer": 3,
      "essenProWoche": {
        "MONTAG":     { "id": 1, "name": "...", "preis": 2.85, "art": "MIT_FLEISCH" },
        "DIENSTAG":   { "id": 2, ... },
        "MITTWOCH":   { "id": 3, ... },
        "DONNERSTAG": { "id": 4, ... },
        "FREITAG":    { "id": 5, ... }
      },
      "vollstaendig": true }

Ein Objekt mit Wochentagen als Schluesseln, keine Liste. Damit ist im
JSON sichtbar, dass pro Tag genau ein Essen steht, und die Frontends
koennen direkt `plan.essenProWoche.MONTAG` lesen.

Fehlt ein Tag, fehlt der Schluessel. `vollstaendig` sagt, ob alle fuenf
belegt sind.

### GET /api/essensplaene/{id}

Ein Plan ueber seine Id.

### POST /api/essensplaene

Nur Admin.

    { "wochennummer": 9 }

201. Der Plan ist zunaechst leer, die Essen kommen ueber die
Tages-Endpunkte dazu.
409 wenn es die Wochennummer schon gibt.

### PUT /api/essensplaene/{id}

Nur Admin. Aendert die Wochennummer.

### DELETE /api/essensplaene/{id}

Nur Admin. 204.

### PUT /api/essensplaene/{id}/tage/{wochentag}

Nur Admin. Essen hinzufuegen oder aendern - beides derselbe Aufruf, weil
pro Tag nur eines existieren kann.

`{wochentag}` ist MONTAG bis FREITAG.

    { "essenId": 7 }

200 mit dem aktualisierten Plan.
404 wenn Plan oder Essen nicht existieren.

### DELETE /api/essensplaene/{id}/tage/{wochentag}

Nur Admin. Entfernt das Essen dieses Tages. 204.

---

## Essensbewertung

### GET /api/essen/{id}/bewertungen

    [ { "id": 5, "essenId": 1, "benutzer": "user",
        "sterne": 4, "text": "Panade schön knusprig",
        "fotoUrl": "/mensa/api/fotos/a3f9c2.jpg",
        "zeitpunkt": "2026-09-02T12:41:00" } ]

Der Benutzername statt der Id, damit die Frontends ihn direkt anzeigen
koennen.

### POST /api/essen/{id}/bewertungen

Angemeldet, User genuegt.

`Content-Type: multipart/form-data` mit zwei Teilen:

| Teil | Inhalt |
|---|---|
| `daten` | `{ "sterne": 4, "text": "Panade schön knusprig" }` |
| `foto`  | die Bilddatei |

201 mit der angelegten Bewertung.

400 wenn der Text fehlt oder nur Leerzeichen enthaelt - der
Bewertungstext ist laut Aufgabenstellung Pflicht. Ebenso wenn `sterne`
ausserhalb von 1 bis 5 liegt.

413 wenn die Datei zu gross ist. Legt ein Limit fest, etwa 10 MB.

### PUT /api/bewertungen/{id}

Nur die eigene Bewertung, oder Admin. Gleicher Aufbau wie POST. Der
Foto-Teil darf fehlen, dann bleibt das alte Bild.

403 wenn jemand eine fremde Bewertung aendern will.

### DELETE /api/bewertungen/{id}

Eigene Bewertung oder Admin. 204.

### GET /api/fotos/{dateiname}

Liefert die Bilddatei mit passendem Content-Type.

Wichtig: den Dateinamen serverseitig vergeben, etwa als UUID, und
niemals den vom Client geschickten Namen verwenden. Sonst kann jemand
mit `../../etc/passwd` aus dem Verzeichnis ausbrechen.

---

## Fehlerformat

Einheitlich fuer alle Fehler:

    { "fehler": "VALIDIERUNG",
      "meldung": "Bewertungstext ist erforderlich" }

| Code | Bedeutung |
|---|---|
| 400 | Eingabe ungueltig |
| 401 | nicht angemeldet |
| 403 | angemeldet, aber keine Berechtigung |
| 404 | nicht gefunden |
| 409 | Konflikt, etwa doppelte Wochennummer |
| 413 | Datei zu gross |

## CORS

Angular laeuft in der Entwicklung auf Port 4200, das Backend auf 8080.
Das sind verschiedene Origins, der Browser blockt die Anfragen.

Ihr braucht einen Filter im Backend, der `Access-Control-Allow-Origin`
auf `http://localhost:4200` setzt und `Access-Control-Allow-Credentials`
auf `true` - sonst schickt der Browser das Session-Cookie nicht mit.

Das kostet erfahrungsgemaess einen halben Tag, wenn man es nicht kennt.
Baut es ein, bevor Rolle D anfaengt, gegen das echte Backend zu testen.

## Internationalisierung

Die Uebersetzung geschieht in den Frontends, nicht im Backend. Die API
liefert Enum-Konstanten wie `MIT_FLEISCH` und `MONTAG`, die Frontends
bilden sie auf deutschen oder englischen Text ab.

So bleibt die API sprachneutral und die Sprachumschaltung braucht keinen
Server-Roundtrip.
