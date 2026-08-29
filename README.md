# Mensa-Projekt

Native iOS-App und Website fuer die Mensa der Leuphana Universitaet Lueneburg.
Semesterprojekt im Kurs Web- und mobile Anwendungssysteme bei Thomas Slotos.

## Team

| Rolle | Person | Bereich |
|---|---|---|
| A | *(Name eintragen)* | Model und Persistenz |
| A | *(Name eintragen)* | Datenbank und JDBC |
| B | *(Name eintragen)* | Service-Schicht |
| B | *(Name eintragen)* | REST-Endpunkte |
| C | *(spaeter)* | iOS-App |
| D | *(spaeter)* | Website |

## Aufbau

    backend/     Java-Backend. Enthaelt Model, DAO, Service, REST-Endpunkte
                 UND die Website (JSP/JSTL) - alles ein Eclipse-Projekt,
                 alles eine WAR-Datei.
    ios-app/     Xcode-Projekt, Swift.
    docs/        API-Notizen, Diagramme, Abgabedokumente.

## Architektur

Die App spricht ueber REST/JSON mit dem Backend, die Website wird
serverseitig mit JSP und JSTL gerendert. Beide teilen sich Service-Schicht,
Model und Datenbank.

    iOS-App  ──JSON──>  REST-Endpunkte  ─┐
                                          ├─> Service ─> DAO ─> MySQL
    Browser  ──HTML──>  Servlets + JSP  ──┘

In der Terminologie der Vorlesung: die Website ist die klassische
Server-MVC-Variante, die App die Variante "remote Model".

## Technik

- Java 17, Jakarta EE, Apache Tomcat 11
- MySQL 8, Zugriff ueber JDBC (kein JPA)
- JSP und JSTL 2.0 fuer die Website
- Swift und Xcode fuer die App
- Datenquelle: offizielle API des Studentenwerks unter api.stw-on.de

## Loslegen

Siehe `backend/README.md` fuer die Einrichtung von Eclipse, MySQL und
den Zugangsdaten.

## Branches

    main            immer lauffaehig, hier wird nicht direkt entwickelt
    feature/<name>  ein Branch pro Aufgabe

Beispiele: `feature/jdbc-dao`, `feature/rest-endpunkte`,
`feature/stw-importer`, `feature/jsp-tagesplan`.

Ablauf: Branch von `main` abzweigen, arbeiten, Pull Request aufmachen,
jemand aus einem anderen Bereich schaut drueber, dann mergen.
Das Review ueber Bereichsgrenzen hinweg ist Absicht - am Ende muessen
alle vier die gesamte Architektur erklaeren koennen.

## Wichtig

`datenbank.properties` steht in `.gitignore` und darf nie committet
werden. Wer sie versehentlich pusht, muss das Passwort aendern - aus der
Git-Historie bekommt man sie nur mit Aufwand wieder heraus.
