import SwiftUI

/// Einstiegspunkt der Oberflaeche.
/// Zeigt den Login, solange niemand angemeldet ist - danach die App.
struct HauptView: View {

    @StateObject private var anmeldung = AnmeldeModell()
    @State private var sitzungGeprueft = false

    var body: some View {
        Group {
            if !sitzungGeprueft {
                ProgressView()
            } else if anmeldung.istAngemeldet {
                EssenListeView()
            } else {
                AnmeldeView()
            }
        }
        .environmentObject(anmeldung)
        .task {
            await anmeldung.sitzungPruefen()
            sitzungGeprueft = true
        }
    }
}
