# Mensa-Projekt

Native iOS-App und Angular-Webanwendung fuer einen Mensa-Essensplan.
Pruefungsleistung im Kurs "Web- und mobile Anwendungsentwicklung" bei
Thomas Slotos, Leuphana Universitaet Lueneburg.

Umgesetzt wird **Alternative 1**: JavaScript Web-Framework plus native App.

## Team

| Rolle | Person | Bereich |
|---|---|---|
| A | *(Name)* | Model und Persistenz |
| A | *(Name)* | Datenbank und JDBC |
| B | *(Name)* | Service-Schicht |
| B | *(Name)* | REST-Endpunkte |

Ab Woche 2 arbeiten alle vier an den beiden Frontends.

## Aufbau

    backend/     Java: Model, DAO, Service, REST-Endpunkte. Reine API,
                 kein JSP - die Aufgabenstellung verlangt fuer die
                 Web-Anwendung ein JavaScript-Framework.
    web-app/     Angular-Projekt.
    ios-app/     Xcode-Projekt, Swift.
    docs/        Diagramme, Notizen, Abgabedokumente.

## Architektur

Beide Frontends sprechen ueber dieselbe REST-Schnittstelle mit dem
Backend. Die Model-Daten liegen auf dem Server, wie in der
Aufgabenstellung gefordert.

    Angular-Webanwendung  ──JSON──┐
                                   ├──> REST ──> Service ──> DAO ──> MySQL
    iOS-App               ──JSON──┘

In der Terminologie der Vorlesung: beide Clients realisieren die
MVC-Variante "remote Model" - View und Controller liegen beim Client,
das Model auf dem Server. Die Web-Anwendung ist eine SPA mit
Client-Side-Rendering.

## Technik

- Java 17, Jakarta EE, Apache Tomcat 11
- Jakarta REST (JAX-RS) mit Jackson fuer JSON
- MySQL 8, Zugriff ueber JDBC (kein JPA)
- Angular mit TypeScript
- Swift und Xcode

Angular deshalb, weil es in der Vorlesung behandelt wurde und TypeScript
mit Klassen, Interfaces und Dependency Injection nah an Java liegt.
Routing, Formulare, HTTP-Client und Internationalisierung sind
mitgeliefert und muessen nicht einzeln zusammengesucht werden.

## Fachklassen

    Essen             Name, Preis, Art (vegetarisch | vegan | mit Fleisch)
    Essensplan        Wochennummer, EssenProWoche (5 Essen Mo-Fr)
    Essensbewertung   Foto, Bewertung 1-5, Bewertungstext
    Benutzer          Benutzername, Passwort, Rolle (USER | ADMIN)

Der Datenbestand umfasst 8 Essensplaene und 10 Essen.

## Anforderungen aus der Aufgabenstellung

- [ ] Essen: Anlegen, Aendern, Anzeigen, Loeschen
- [ ] Essensplan: Anlegen, Aendern, Anzeigen, Loeschen
- [ ] Essensplan: Essen anzeigen, hinzufuegen, aendern, entfernen
- [ ] Essensplan: Filtern nach Woche
- [ ] Essensbewertung: Abgeben und Aendern
- [ ] Essensbewertung: 5-skalig als Sterne oder Dropdown
- [ ] Essensbewertung: Bewertungstext ist Pflicht
- [ ] Foto **aus der Anwendung heraus** mit der Kamera aufnehmen.
      Ein vorher aufgenommenes Bild hochzuladen ist ausdruecklich
      nicht erlaubt
- [ ] Vier Dialoge: Essen, Essensplan, Essensbewertung, Login
- [ ] Login mit Username und Passwort
- [ ] Admin: alle Funktionen. User: nur Bewertungen anlegen,
      sonst lesender Zugriff
- [ ] Internationalisierung deutsch und englisch
- [ ] 8 Essensplaene, 10 Essen

Abgabe: Upload in myStudy bis 21.09.2026.
Muendliches Gespraech in der letzten Septemberwoche. Jedes
Gruppenmitglied muss sich mit allen verwendeten Technologien auskennen.

## Loslegen

`backend/README.md` beschreibt die Einrichtung von Eclipse, MySQL und
den Zugangsdaten.

## Branches

    main            immer lauffaehig
    feature/<name>  ein Branch pro Aufgabe

Pull Request aufmachen, jemand aus einem anderen Bereich schaut drueber,
dann mergen. Das Review ueber Bereichsgrenzen hinweg ist Absicht - im
muendlichen Gespraech werden alle vier zu allem gefragt.

## Wichtig

`datenbank.properties` steht in `.gitignore` und darf nie committet
werden.
