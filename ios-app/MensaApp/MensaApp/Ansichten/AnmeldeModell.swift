import Foundation
import Combine
/// Haelt den Anmeldezustand fuer die ganze App.
///
/// @MainActor, weil alle Eigenschaften die Oberflaeche steuern und
/// SwiftUI Aenderungen nur auf dem Hauptthread verarbeiten darf.
/// Die Netzwerkaufrufe selbst laufen im ApiClient, der ein eigener
/// actor ist - deshalb das await.
@MainActor
final class AnmeldeModell: ObservableObject {

    @Published var angemeldeterBenutzer: Benutzer?
    @Published var laeuft = false
    @Published var fehlermeldung: String?

    var istAngemeldet: Bool { angemeldeterBenutzer != nil }
    var istAdmin: Bool { angemeldeterBenutzer?.istAdmin ?? false }

    /// Beim App-Start pruefen, ob noch eine Sitzung besteht.
    /// Das Session-Cookie ueberlebt einen Neustart der App nicht
    /// zwingend - deshalb ist ein nil hier voellig normal.
    func sitzungPruefen() async {
        angemeldeterBenutzer = await ApiClient.shared.aktuelleSitzung()
    }

    func anmelden(benutzername: String, passwort: String) async {
        guard !benutzername.isEmpty, !passwort.isEmpty else {
            fehlermeldung = NSLocalizedString("login.felder_leer", comment: "")
            return
        }

        laeuft = true
        fehlermeldung = nil
        defer { laeuft = false }

        do {
            angemeldeterBenutzer = try await ApiClient.shared.anmelden(
                benutzername: benutzername, passwort: passwort)
        } catch let fehler as ApiFehler {
            // 401 heisst hier: Benutzername oder Passwort falsch.
            if case .nichtAngemeldet = fehler {
                fehlermeldung = NSLocalizedString("login.falsche_daten", comment: "")
            } else {
                fehlermeldung = fehler.errorDescription
            }
        } catch {
            fehlermeldung = error.localizedDescription
        }
    }

    func abmelden() async {
        try? await ApiClient.shared.abmelden()
        angemeldeterBenutzer = nil
    }
}
