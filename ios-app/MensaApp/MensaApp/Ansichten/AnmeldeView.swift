import SwiftUI

/// Dialog 4 laut Aufgabenstellung: Login.
struct AnmeldeView: View {

    @EnvironmentObject private var modell: AnmeldeModell

    @State private var benutzername = ""
    @State private var passwort = ""

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "fork.knife.circle.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 80, height: 80)
                .foregroundStyle(.tint)

            Text("login.titel")
                .font(.largeTitle)
                .bold()

            VStack(spacing: 12) {
                TextField(String(localized: "login.benutzername"), text: $benutzername)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()

                SecureField(String(localized: "login.passwort"), text: $passwort)
                    .textFieldStyle(.roundedBorder)
                    .onSubmit { anmelden() }
            }

            if let meldung = modell.fehlermeldung {
                Text(meldung)
                    .font(.callout)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }

            Button(action: anmelden) {
                if modell.laeuft {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("login.knopf")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(modell.laeuft)

            Spacer()
            Spacer()
        }
        .padding(32)
    }

    private func anmelden() {
        Task {
            await modell.anmelden(benutzername: benutzername, passwort: passwort)
        }
    }
}
