import Foundation

/// Fehler, die beim Aufruf des Backends auftreten koennen.
/// Bilden die HTTP-Codes aus dem REST-Vertrag ab.
enum ApiFehler: LocalizedError {
    case nichtAngemeldet            // 401
    case keineBerechtigung          // 403
    case nichtGefunden              // 404
    case konflikt(String)           // 409
    case ungueltigeEingabe(String)  // 400
    case dateiZuGross               // 413
    case serverFehler(Int, String)  // 500 und andere
    case keineVerbindung(Error)
    case ungueltigeAntwort

    var errorDescription: String? {
        switch self {
        case .nichtAngemeldet:
            return NSLocalizedString("fehler.nicht_angemeldet", comment: "")
        case .keineBerechtigung:
            return NSLocalizedString("fehler.keine_berechtigung", comment: "")
        case .nichtGefunden:
            return NSLocalizedString("fehler.nicht_gefunden", comment: "")
        case .konflikt(let meldung):
            return meldung
        case .ungueltigeEingabe(let meldung):
            return meldung
        case .dateiZuGross:
            return NSLocalizedString("fehler.datei_zu_gross", comment: "")
        case .serverFehler(let code, let meldung):
            return "\(code): \(meldung)"
        case .keineVerbindung:
            return NSLocalizedString("fehler.keine_verbindung", comment: "")
        case .ungueltigeAntwort:
            return NSLocalizedString("fehler.ungueltige_antwort", comment: "")
        }
    }
}
