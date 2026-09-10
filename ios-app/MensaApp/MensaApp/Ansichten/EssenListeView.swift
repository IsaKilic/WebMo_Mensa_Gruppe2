import SwiftUI

/// Dialog 1 laut Aufgabenstellung: Essen anzeigen.
/// Anlegen, Aendern und Loeschen kommen als naechstes dazu - nur fuer Admin.
struct EssenListeView: View {

    @EnvironmentObject private var anmeldung: AnmeldeModell
    @StateObject private var modell = EssenListeModell()

    var body: some View {
        NavigationStack {
            Group {
                if modell.laeuft && modell.essen.isEmpty {
                    ProgressView()
                } else if let meldung = modell.fehlermeldung {
                    FehlerAnsicht(meldung: meldung) {
                        Task { await modell.laden() }
                    }
                } else {
                    Liste
                }
            }
            .navigationTitle(Text("essen.titel"))
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("login.abmelden") {
                        Task { await anmeldung.abmelden() }
                    }
                }
            }
            .task { await modell.laden() }
            .refreshable { await modell.laden() }
        }
    }

    private var Liste: some View {
        List(modell.essen) { gericht in
            VStack(alignment: .leading, spacing: 4) {
                Text(gericht.name)
                    .font(.headline)

                HStack(spacing: 8) {
                    Text(gericht.art.anzeigename)
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(farbeFuer(gericht.art).opacity(0.2))
                        .clipShape(Capsule())

                    Text(gericht.preisFormatiert)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    Spacer()

                    if gericht.hatBewertungen,
                       let schnitt = gericht.durchschnittsbewertung {
                        Label(String(format: "%.1f", schnitt), systemImage: "star.fill")
                            .font(.caption)
                            .foregroundStyle(.orange)
                    }
                }
            }
            .padding(.vertical, 4)
        }
    }

    private func farbeFuer(_ art: Art) -> Color {
        switch art {
        case .vegan:       return .green
        case .vegetarisch: return .mint
        case .mitFleisch:  return .orange
        }
    }
}

/// Wiederverwendbare Fehleranzeige mit Knopf zum erneuten Versuchen.
struct FehlerAnsicht: View {
    let meldung: String
    let erneutVersuchen: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.largeTitle)
                .foregroundStyle(.secondary)

            Text(meldung)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)

            Button("allgemein.erneut_versuchen", action: erneutVersuchen)
                .buttonStyle(.bordered)
        }
        .padding(32)
    }
}
