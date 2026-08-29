# Datenquelle: API des Studentenwerks OstNiedersachsen

## Warum diese Quelle

Die Speiseplaene werden auf stw-on.de nur als PDF angeboten, und die Seite
untersagt automatisierten Zugriff per robots.txt. Scraping scheidet damit
aus - technisch wie rechtlich.

Das Studentenwerk betreibt aber eine offizielle HTTP-API:

- Einstieg: https://api.stw-on.de
- Dokumentation: https://github.com/stw-on/api-docs (Apache-2.0)

Die aeltere XML-API ist als veraltet markiert und wird bald abgeschaltet.
Wir nutzen die JSON-Variante.

## Rate Limits

Die API ist auf ein bestimmtes Anfragenvolumen pro Zeitraum begrenzt.
Bei Ueberschreitung kommt HTTP 429 (Too Many Requests) mit einem
`Retry-After`-Header, der angibt, wann die naechste Anfrage frueheste
erfolgen darf.

Konsequenz fuer unsere Architektur: der Importer laeuft periodisch und
schreibt in unsere Datenbank. App und Website lesen ausschliesslich aus
unserer DB, nie direkt gegen die API. Das ist der Hauptgrund, warum wir
ueberhaupt eine eigene Datenbank brauchen.

## Noch zu klaeren

- [ ] Id der Mensa Campus im System des Studentenwerks
- [ ] Id der Mensa Campus abends
- [ ] Genaue Struktur der JSON-Antwort (Beispiel hier ablegen)
- [ ] Konkretes Rate Limit (Anfragen pro Zeitraum)
- [ ] Wie werden Allergene geliefert - als Kuerzel oder ausgeschrieben?
- [ ] Werden Naehrwerte geliefert?
- [ ] Wie weit im Voraus sind Plaene verfuegbar?

## Mapping auf unsere Enums

Die API liefert Allergene in ihrem eigenen Format, nicht als unsere
Enum-Konstanten. Hier die Uebersetzungstabelle eintragen, sobald die
Struktur bekannt ist:

| Wert der API | Unser Enum |
|---|---|
| ... | `Allergen.GLUTEN` |
| ... | `Allergen.MILCH` |

Wichtig: unbekannte Werte im Importer protokollieren, nicht stillschweigend
verwerfen. Bei Allergenen ist ein stiller Datenverlust nicht akzeptabel.

## Beispielantwort

Sobald ihr einen Aufruf gemacht habt, die Antwort hier einfuegen. Daran
baut der Parser.

```json
(hier einfuegen)
```
