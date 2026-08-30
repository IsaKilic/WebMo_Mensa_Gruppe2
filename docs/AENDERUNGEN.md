# Korrektur nach Lektuere der Aufgabenstellung

Die erste Modellversion entstand vor der Aufgabenstellung und passt nicht
darauf. Diese Version ersetzt sie vollstaendig.

## Was sich geaendert hat

| Alt | Neu | Grund |
|---|---|---|
| `Gericht` | `Essen` | Fachklasse laut Aufgabe |
| `Speiseplan` (nach Datum) | `Essensplan` (nach Wochennummer) | Attribut Wochennummer gefordert |
| Liste beliebiger Laenge | `EnumMap<Wochentag, Essen>` | genau 5 Essen Mo-Fr |
| `Preis` mit 3 Nutzergruppen | ein `double` | nur "Preis" gefordert |
| `Kennzeichnung` als Set | `Art` als einzelner Wert | vegetarisch / vegan / mit Fleisch |
| `Mensa` | entfaellt | nicht gefordert |
| `Allergen`, `Zusatzstoff`, `Naehrwerte` | entfallen | nicht gefordert |
| - | `Essensbewertung` | **fehlte komplett** |
| - | `Benutzer`, `Rolle` | **fehlte komplett** |
| nur Lesen | volles CRUD | Anlegen/Aendern/Anzeigen/Loeschen |

## Was ersatzlos wegfaellt

Der Importer fuer api.stw-on.de wird nicht gebraucht. Die Aufgabe verlangt
8 Essensplaene und 10 Essen, gepflegt vom Admin ueber die CRUD-Dialoge.
Ein Import echter Daten waere Mehraufwand ohne Gegenwert.

## Was noch fehlt

- JDBC-Implementierungen der vier DAOs
- Passwort-Hashing (aktuell Klartext in `BenutzerDAOInMemory`)
- Service-Schicht und REST-Endpunkte (Rolle B)
- Foto-Speicherung: Ablage im Dateisystem, Pfad in die DB

## Anforderungen aus der Aufgabenstellung im Blick behalten

- [ ] 4 Dialoge: Essen, Essensplan, Essensbewertung, Login
- [ ] Admin: alle Funktionen. User: nur Bewertungen anlegen, sonst lesend
- [ ] Kamera MUSS aus der App heraus aufgerufen werden. Ein vorher
      aufgenommenes Foto hochzuladen ist ausdruecklich nicht erlaubt
- [ ] Bewertung als Dropdown oder Sterne, 5-skalig
- [ ] Bewertungstext verpflichtend
- [ ] Internationalisierung deutsch und englisch
- [ ] Web-Anwendung mit JavaScript-Framework (Angular, React oder Vue),
      NICHT mit JSP - das erfuellt Alternative 1 nicht
- [ ] Upload in myStudy bis 21.09.2026
