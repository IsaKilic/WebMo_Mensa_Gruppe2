# iOS-App: Grundgeruest

Model, API-Client und Uebersetzungsdateien fuer die Mensa-App.
Diese Dateien brauchen kein Xcode zum Schreiben - sie muessen aber in
Xcode uebersetzt werden, und dort koennen noch Syntaxfehler auftauchen.

## Was drin ist

    Model/Modelle.swift        Fachklassen und Enums, passend zum JSON
    Netzwerk/ApiClient.swift   alle Aufrufe ans Backend
    Netzwerk/ApiFehler.swift   HTTP-Codes als Swift-Fehler
    Ressourcen/de.lproj/       deutsche Texte
    Ressourcen/en.lproj/       englische Texte

## Was noch fehlt

Die Oberflaeche. Vier Dialoge laut Aufgabenstellung:
Essen, Essensplan, Essensbewertung, Login.

## Einrichten in Xcode

1. Xcode -> Create New Project -> iOS -> App
2. Product Name: MensaApp
3. Interface: SwiftUI, Language: Swift
4. Speichern unter `ios-app/` im geklonten Repository
5. Die Ordner Model, Netzwerk und Ressourcen per Drag and Drop ins
   Projekt ziehen. Dabei "Copy items if needed" ankreuzen und
   "Create groups" waehlen.

### Sprachen aktivieren

Projekt anklicken -> Info -> Localizations -> Plus -> German.
English ist meist schon da. Danach bei jeder Localizable.strings
im File Inspector rechts die Sprachen ankreuzen.

### HTTP erlauben

Das Backend laeuft unverschluesselt ueber http. iOS blockiert das
standardmaessig (App Transport Security). Fuer die Entwicklung in
`Info.plist` eintragen:

    <key>NSAppTransportSecurity</key>
    <dict>
        <key>NSAllowsLocalNetworking</key>
        <true/>
    </dict>

### Kamera-Berechtigung

Ohne diesen Eintrag stuerzt die App beim Kamerazugriff ab:

    <key>NSCameraUsageDescription</key>
    <string>Zum Aufnehmen eines Fotos der Essensbewertung.</string>

## Die Serveradresse

In `ApiClient.swift` steht:

    http://localhost:8080/mensa-backend/api

Das funktioniert nur im Simulator. Auf einem echten iPhone ist
localhost das Geraet selbst - dort muss die IP-Adresse des Rechners
stehen, auf dem Tomcat laeuft:

    http://192.168.1.42:8080/mensa-backend/api

Die eigene IP findet ihr am Mac mit:

    ipconfig getifaddr en0

Beide Geraete muessen im selben WLAN sein.

## Zur Anmeldung

Das Backend nutzt HttpSession, also ein JSESSIONID-Cookie.
URLSession verwaltet Cookies von sich aus - deshalb steht im Code
nichts davon. Wichtig ist nur, dass ueberall dieselbe URLSession
benutzt wird. Deshalb ist ApiClient ein Singleton (`ApiClient.shared`).

Fuer das muendliche Gespraech: das ist SessionTracking ueber Cookies,
genau wie in der Vorlesung. Der Server vergibt beim Login eine
Kennung, der Client schickt sie bei jeder weiteren Anfrage mit.

## Zum Foto-Upload

Der Aufruf ist multipart, nicht base64 im JSON. Ein Foto vom iPhone
hat schnell mehrere Megabyte - als base64 waere es ein Drittel
groesser und laege komplett im Arbeitsspeicher.

WICHTIG laut Aufgabenstellung: Das Foto MUSS aus der Anwendung heraus
mit der Kamera aufgenommen werden. Ein vorher aufgenommenes Bild aus
der Fotomediathek hochzuladen ist ausdruecklich nicht erlaubt. In
SwiftUI heisst das: UIImagePickerController mit sourceType .camera,
oder die Kamera-Ansicht von AVFoundation. NICHT PhotosPicker.

Das laesst sich nur auf einem echten Geraet testen - der Simulator
hat keine Kamera.

## Feldnamen

Die Swift-Strukturen wurden gegen die Java-DTOs geprueft, alle
Feldnamen stimmen ueberein. Wenn das Backend spaeter ein Feld
umbenennt, muss es hier mitgeaendert werden - sonst schlaegt das
Decodieren zur Laufzeit fehl.

Beim Essensplan kommt `essenProWoche` als Objekt mit Wochentagen als
Schluesseln, nicht als Liste. In Swift ist das ein Dictionary. Da
Dictionaries keine feste Reihenfolge haben, gibt es die Hilfsmethode
`tageInReihenfolge`, die ueber das Enum laeuft.

## Naechster Schritt

Login-Ansicht bauen und einmal gegen das laufende Backend testen.
Sobald die Anmeldung durchgeht und die Essensliste erscheint, ist
bewiesen, dass die ganze Kette funktioniert - App, CORS, Servlet,
Service, DAO, Datenbank.
