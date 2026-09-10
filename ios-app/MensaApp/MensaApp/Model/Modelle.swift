import Foundation

// MARK: - Enums
// Die Rohwerte entsprechen exakt den Java-Enum-Konstanten.
// Deshalb ohne Umlaute: GEFLUEGEL, nicht GEFLÜGEL.

enum Art: String, Codable, CaseIterable {
    case vegetarisch = "VEGETARISCH"
    case vegan       = "VEGAN"
    case mitFleisch  = "MIT_FLEISCH"

    /// Anzeigetext. Die Uebersetzung passiert im Frontend,
    /// das Backend liefert nur die Konstante.
    var anzeigename: String {
        switch self {
        case .vegetarisch: return NSLocalizedString("art.vegetarisch", comment: "")
        case .vegan:       return NSLocalizedString("art.vegan", comment: "")
        case .mitFleisch:  return NSLocalizedString("art.mit_fleisch", comment: "")
        }
    }
}

enum Wochentag: String, Codable, CaseIterable {
    case montag     = "MONTAG"
    case dienstag   = "DIENSTAG"
    case mittwoch   = "MITTWOCH"
    case donnerstag = "DONNERSTAG"
    case freitag    = "FREITAG"

    var anzeigename: String {
        NSLocalizedString("wochentag.\(rawValue.lowercased())", comment: "")
    }
}

enum Rolle: String, Codable {
    case user  = "USER"
    case admin = "ADMIN"
}

// MARK: - Fachklassen
// Spiegeln die DTOs des Backends. Feldnamen muessen exakt zum JSON
// passen, sonst schlaegt das Decodieren fehl.

struct Essen: Codable, Identifiable, Hashable {
    let id: Int
    var name: String
    var preis: Double
    var art: Art

    // Optional, weil das Backend sie nur bei /api/essen mitliefert.
    // In einem Essensplan kommt das Essen ohne Bewertungsangaben.
    var durchschnittsbewertung: Double?
    var anzahlBewertungen: Int?

    var preisFormatiert: String {
        String(format: "%.2f €", preis)
    }

    var hatBewertungen: Bool {
        (anzahlBewertungen ?? 0) > 0
    }
}

struct Essensplan: Codable, Identifiable {
    let id: Int
    var wochennummer: Int
    var essenProWoche: [String: Essen]
    var vollstaendig: Bool

    /// Bequemer Zugriff ueber das Enum statt ueber den String-Schluessel.
    func essen(fuer tag: Wochentag) -> Essen? {
        essenProWoche[tag.rawValue]
    }

    /// Alle Tage in fester Reihenfolge Montag bis Freitag.
    /// Ein Dictionary hat keine garantierte Reihenfolge, deshalb
    /// laufen wir ueber das Enum.
    var tageInReihenfolge: [(tag: Wochentag, essen: Essen?)] {
        Wochentag.allCases.map { ($0, essen(fuer: $0)) }
    }
}

struct Essensbewertung: Codable, Identifiable {
    let id: Int
    var essenId: Int
    var benutzer: String
    var sterne: Int
    var text: String
    var fotoUrl: String?
    var zeitpunkt: String?

    /// Vollstaendige URL zum Foto. Das Backend liefert nur den Pfad.
    func fotoAdresse(basis: URL) -> URL? {
        guard let pfad = fotoUrl else { return nil }
        return URL(string: pfad, relativeTo: basis)
    }
}

struct Benutzer: Codable, Identifiable {
    let id: Int
    var benutzername: String
    var rolle: Rolle

    var istAdmin: Bool { rolle == .admin }
}

// MARK: - Anfragekoerper

struct AnmeldeDaten: Encodable {
    let benutzername: String
    let passwort: String
}

struct EssenEingabe: Encodable {
    let name: String
    let preis: Double
    let art: String

    init(name: String, preis: Double, art: Art) {
        self.name = name
        self.preis = preis
        self.art = art.rawValue
    }
}

struct WochennummerEingabe: Encodable {
    let wochennummer: Int
}

struct EssenIdEingabe: Encodable {
    let essenId: Int
}

struct BewertungEingabe: Encodable {
    let sterne: Int
    let text: String
}

// MARK: - Fehler

struct FehlerAntwort: Decodable {
    let fehler: String?
    let meldung: String?
}
