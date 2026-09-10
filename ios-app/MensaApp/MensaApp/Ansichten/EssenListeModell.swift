import Foundation
import Combine

@MainActor
final class EssenListeModell: ObservableObject {

    @Published var essen: [Essen] = []
    @Published var laeuft = false
    @Published var fehlermeldung: String?

    func laden() async {
        laeuft = true
        fehlermeldung = nil
        defer { laeuft = false }

        do {
            essen = try await ApiClient.shared.alleEssen()
        } catch let fehler as ApiFehler {
            fehlermeldung = fehler.errorDescription
        } catch {
            fehlermeldung = error.localizedDescription
        }
    }
}
