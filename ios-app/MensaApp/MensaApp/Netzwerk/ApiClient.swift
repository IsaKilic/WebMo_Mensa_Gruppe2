import Foundation

/// Zentraler Zugang zum Backend.
///
/// WICHTIG zur Anmeldung: Das Backend nutzt HttpSession, also ein
/// JSESSIONID-Cookie. URLSession verwaltet Cookies standardmaessig
/// selbst (HTTPCookieStorage.shared) - deshalb muss hier nichts
/// von Hand mitgeschickt werden. Entscheidend ist nur, dass ueberall
/// dieselbe URLSession verwendet wird, sonst geht die Sitzung verloren.
///
/// Fuer das muendliche Gespraech: das ist SessionTracking ueber Cookies,
/// genau wie in der Vorlesung. Der Server vergibt beim Login eine
/// Kennung, der Client schickt sie bei jeder weiteren Anfrage zurueck.
actor ApiClient {

    static let shared = ApiClient()

    /// Beim Simulator geht localhost. Auf einem echten iPhone muss hier
    /// die IP-Adresse des Entwicklungsrechners stehen, etwa
    /// http://192.168.1.42:8080/mensa-backend/api
    /// Das Geraet kann localhost nicht erreichen - das ist es selbst.
    private let basis: URL

    private let session: URLSession
    private let dekodierer = JSONDecoder()
    private let kodierer = JSONEncoder()

    init(basis: URL = URL(string: "http://localhost:8080/mensa-backend/api")!) {
        self.basis = basis

        let konfiguration = URLSessionConfiguration.default
        konfiguration.httpCookieAcceptPolicy = .always
        konfiguration.httpShouldSetCookies = true
        konfiguration.timeoutIntervalForRequest = 15
        self.session = URLSession(configuration: konfiguration)
    }

    var basisAdresse: URL { basis }

    // MARK: - Anmeldung

    func anmelden(benutzername: String, passwort: String) async throws -> Benutzer {
        try await senden(
            "/login",
            methode: "POST",
            koerper: AnmeldeDaten(benutzername: benutzername, passwort: passwort),
            antwort: Benutzer.self)
    }

    func abmelden() async throws {
        try await sendenOhneAntwort("/logout", methode: "POST")
    }

    /// Prueft beim App-Start, ob noch eine Sitzung besteht.
    /// Liefert nil statt eines Fehlers, wenn niemand angemeldet ist.
    func aktuelleSitzung() async -> Benutzer? {
        try? await senden("/session", methode: "GET", antwort: Benutzer.self)
    }

    // MARK: - Essen

    func alleEssen() async throws -> [Essen] {
        try await senden("/essen", methode: "GET", antwort: [Essen].self)
    }

    func essen(id: Int) async throws -> Essen {
        try await senden("/essen/\(id)", methode: "GET", antwort: Essen.self)
    }

    func essenAnlegen(name: String, preis: Double, art: Art) async throws -> Essen {
        try await senden("/essen", methode: "POST",
                         koerper: EssenEingabe(name: name, preis: preis, art: art),
                         antwort: Essen.self)
    }

    func essenAendern(id: Int, name: String, preis: Double, art: Art) async throws -> Essen {
        try await senden("/essen/\(id)", methode: "PUT",
                         koerper: EssenEingabe(name: name, preis: preis, art: art),
                         antwort: Essen.self)
    }

    func essenLoeschen(id: Int) async throws {
        try await sendenOhneAntwort("/essen/\(id)", methode: "DELETE")
    }

    // MARK: - Essensplan

    func alleEssensplaene() async throws -> [Essensplan] {
        try await senden("/essensplaene", methode: "GET", antwort: [Essensplan].self)
    }

    func essensplan(woche: Int) async throws -> Essensplan {
        try await senden("/essensplaene?woche=\(woche)", methode: "GET",
                         antwort: Essensplan.self)
    }

    func essensplanAnlegen(wochennummer: Int) async throws -> Essensplan {
        try await senden("/essensplaene", methode: "POST",
                         koerper: WochennummerEingabe(wochennummer: wochennummer),
                         antwort: Essensplan.self)
    }

    func essensplanAendern(id: Int, wochennummer: Int) async throws -> Essensplan {
        try await senden("/essensplaene/\(id)", methode: "PUT",
                         koerper: WochennummerEingabe(wochennummer: wochennummer),
                         antwort: Essensplan.self)
    }

    func essensplanLoeschen(id: Int) async throws {
        try await sendenOhneAntwort("/essensplaene/\(id)", methode: "DELETE")
    }

    func essenFuerTagSetzen(planId: Int, tag: Wochentag, essenId: Int) async throws -> Essensplan {
        try await senden("/essensplaene/\(planId)/tage/\(tag.rawValue)", methode: "PUT",
                         koerper: EssenIdEingabe(essenId: essenId),
                         antwort: Essensplan.self)
    }

    func essenFuerTagEntfernen(planId: Int, tag: Wochentag) async throws {
        try await sendenOhneAntwort("/essensplaene/\(planId)/tage/\(tag.rawValue)",
                                    methode: "DELETE")
    }

    // MARK: - Bewertungen

    func bewertungen(essenId: Int) async throws -> [Essensbewertung] {
        try await senden("/essen/\(essenId)/bewertungen", methode: "GET",
                         antwort: [Essensbewertung].self)
    }

    /// Bewertung mit Foto abgeben.
    ///
    /// Der Aufruf ist multipart, weil ein Foto vom iPhone schnell mehrere
    /// Megabyte hat. Als Base64 im JSON waere es ein Drittel groesser und
    /// laege komplett im Arbeitsspeicher.
    func bewertungAbgeben(essenId: Int, sterne: Int, text: String,
                          fotoDaten: Data?) async throws -> Essensbewertung {

        let grenze = "Grenze-\(UUID().uuidString)"
        var anfrage = URLRequest(url: basis.appendingPathComponent("essen/\(essenId)/bewertungen"))
        anfrage.httpMethod = "POST"
        anfrage.setValue("multipart/form-data; boundary=\(grenze)",
                         forHTTPHeaderField: "Content-Type")

        let daten = try kodierer.encode(BewertungEingabe(sterne: sterne, text: text))
        anfrage.httpBody = multipartKoerper(grenze: grenze, jsonTeil: daten, foto: fotoDaten)

        return try await ausfuehren(anfrage, antwort: Essensbewertung.self)
    }

    func bewertungAendern(id: Int, sterne: Int, text: String,
                          fotoDaten: Data?) async throws -> Essensbewertung {

        let grenze = "Grenze-\(UUID().uuidString)"
        var anfrage = URLRequest(url: basis.appendingPathComponent("bewertungen/\(id)"))
        anfrage.httpMethod = "PUT"
        anfrage.setValue("multipart/form-data; boundary=\(grenze)",
                         forHTTPHeaderField: "Content-Type")

        let daten = try kodierer.encode(BewertungEingabe(sterne: sterne, text: text))
        anfrage.httpBody = multipartKoerper(grenze: grenze, jsonTeil: daten, foto: fotoDaten)

        return try await ausfuehren(anfrage, antwort: Essensbewertung.self)
    }

    func bewertungLoeschen(id: Int) async throws {
        try await sendenOhneAntwort("/bewertungen/\(id)", methode: "DELETE")
    }

    // MARK: - Innereien

    private func multipartKoerper(grenze: String, jsonTeil: Data, foto: Data?) -> Data {
        var koerper = Data()

        func anhaengen(_ text: String) {
            koerper.append(text.data(using: .utf8)!)
        }

        anhaengen("--\(grenze)\r\n")
        anhaengen("Content-Disposition: form-data; name=\"daten\"\r\n")
        anhaengen("Content-Type: application/json\r\n\r\n")
        koerper.append(jsonTeil)
        anhaengen("\r\n")

        if let foto {
            anhaengen("--\(grenze)\r\n")
            anhaengen("Content-Disposition: form-data; name=\"foto\"; filename=\"foto.jpg\"\r\n")
            anhaengen("Content-Type: image/jpeg\r\n\r\n")
            koerper.append(foto)
            anhaengen("\r\n")
        }

        anhaengen("--\(grenze)--\r\n")
        return koerper
    }

    private func senden<A: Decodable>(_ pfad: String, methode: String,
                                      antwort: A.Type) async throws -> A {
        try await ausfuehren(anfrageBauen(pfad, methode: methode), antwort: antwort)
    }

    private func senden<K: Encodable, A: Decodable>(_ pfad: String, methode: String,
                                                    koerper: K,
                                                    antwort: A.Type) async throws -> A {
        var anfrage = anfrageBauen(pfad, methode: methode)
        anfrage.setValue("application/json", forHTTPHeaderField: "Content-Type")
        anfrage.httpBody = try kodierer.encode(koerper)
        return try await ausfuehren(anfrage, antwort: antwort)
    }

    private func sendenOhneAntwort(_ pfad: String, methode: String) async throws {
        let anfrage = anfrageBauen(pfad, methode: methode)
        let (daten, antwort) = try await abrufen(anfrage)
        try pruefeStatus(antwort, daten: daten)
    }

    private func anfrageBauen(_ pfad: String, methode: String) -> URLRequest {
        // Bei Pfaden mit Fragezeichen darf appendingPathComponent nicht
        // benutzt werden, sonst wird das ? mitkodiert.
        let adresse = URL(string: basis.absoluteString + pfad)!
        var anfrage = URLRequest(url: adresse)
        anfrage.httpMethod = methode
        anfrage.setValue("application/json", forHTTPHeaderField: "Accept")
        return anfrage
    }

    private func abrufen(_ anfrage: URLRequest) async throws -> (Data, URLResponse) {
        do {
            return try await session.data(for: anfrage)
        } catch {
            throw ApiFehler.keineVerbindung(error)
        }
    }

    private func ausfuehren<A: Decodable>(_ anfrage: URLRequest,
                                          antwort typ: A.Type) async throws -> A {
        let (daten, antwort) = try await abrufen(anfrage)
        try pruefeStatus(antwort, daten: daten)

        do {
            return try dekodierer.decode(typ, from: daten)
        } catch {
            throw ApiFehler.ungueltigeAntwort
        }
    }

    private func pruefeStatus(_ antwort: URLResponse, daten: Data) throws {
        guard let http = antwort as? HTTPURLResponse else {
            throw ApiFehler.ungueltigeAntwort
        }
        if (200...299).contains(http.statusCode) { return }

        let meldung = (try? dekodierer.decode(FehlerAntwort.self, from: daten))?.meldung
            ?? String(data: daten, encoding: .utf8)
            ?? ""

        switch http.statusCode {
        case 400: throw ApiFehler.ungueltigeEingabe(meldung)
        case 401: throw ApiFehler.nichtAngemeldet
        case 403: throw ApiFehler.keineBerechtigung
        case 404: throw ApiFehler.nichtGefunden
        case 409: throw ApiFehler.konflikt(meldung)
        case 413: throw ApiFehler.dateiZuGross
        default:  throw ApiFehler.serverFehler(http.statusCode, meldung)
        }
    }
}
