package de.leuphana.mensa.service;

import java.io.InputStream;
import java.util.List;

import de.leuphana.mensa.model.Benutzer;
import de.leuphana.mensa.model.Essensbewertung;
import de.leuphana.mensa.persistence.BenutzerDAO;
import de.leuphana.mensa.persistence.EssenDAO;
import de.leuphana.mensa.persistence.EssensbewertungDAO;

/**
 * Fachlogik rund um Essensbewertungen: abgeben, aendern, loeschen,
 * Foto speichern. Klassenname "BewertungService" wie in
 * docs/AUFGABE-ROLLE-B.md Teil 4 vorgeschlagen.
 *
 * Rollenregel laut docs/REST-API.md: jede angemeldete Person (User
 * oder Admin) darf eine Bewertung anlegen; aendern/loeschen nur die
 * eigene Bewertung oder ein Admin.
 */
public class BewertungService {

    private final EssensbewertungDAO bewertungDAO;
    private final EssenDAO essenDAO;
    private final BenutzerDAO benutzerDAO;
    private final FotoSpeicher fotoSpeicher;

    public BewertungService(EssensbewertungDAO bewertungDAO, EssenDAO essenDAO,
                             BenutzerDAO benutzerDAO, FotoSpeicher fotoSpeicher) {
        this.bewertungDAO = bewertungDAO;
        this.essenDAO = essenDAO;
        this.benutzerDAO = benutzerDAO;
        this.fotoSpeicher = fotoSpeicher;
    }

    public List<Essensbewertung> bewertungenFuerEssen(int essenId, Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        pruefeEssenExistiert(essenId);
        return bewertungDAO.findByEssen(essenId);
    }

    /** Fuer die Anzeige: Benutzername statt Id, siehe REST-API.md GET .../bewertungen. */
    public String benutzernameFuer(int benutzerId) {
        Benutzer benutzer = benutzerDAO.findById(benutzerId);
        return (benutzer == null) ? "unbekannt" : benutzer.getBenutzername();
    }

    /**
     * @param fotoInhalt darf null sein, wenn kein Foto mitgeschickt wurde -
     *        laut Aufgabenstellung ist das Foto beim Anlegen aber verpflichtend
     *        aus der App heraus aufzunehmen; ob das erzwungen wird, entscheidet
     *        das Servlet (siehe EssensbewertungServlet).
     */
    public Essensbewertung abgeben(int essenId, int sterne, String text,
                                    InputStream fotoInhalt, String fotoDateiname,
                                    Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        pruefeEssenExistiert(essenId);
        pruefeText(text);
        pruefeSterne(sterne);
        String fotoPfad = (fotoInhalt == null) ? null : fotoSpeicher.speichern(fotoInhalt, fotoDateiname);
        Essensbewertung neu = new Essensbewertung(0, essenId, angemeldet.getId(), sterne, text, fotoPfad);
        return bewertungDAO.abgeben(neu);
    }

    /** Foto darf fehlen (fotoInhalt == null) - dann bleibt laut REST-API.md das alte Bild. */
    public Essensbewertung aendern(int bewertungId, int sterne, String text,
                                    InputStream fotoInhalt, String fotoDateiname,
                                    Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        Essensbewertung bestehend = bewertungOderFehler(bewertungId);
        pruefeEigentuemerOderAdmin(bestehend, angemeldet);
        pruefeText(text);
        pruefeSterne(sterne);
        bestehend.setSterne(sterne);
        bestehend.setText(text);
        if (fotoInhalt != null) {
            bestehend.setFotoPfad(fotoSpeicher.speichern(fotoInhalt, fotoDateiname));
        }
        bewertungDAO.aendern(bestehend);
        return bestehend;
    }

    public void loeschen(int bewertungId, Benutzer angemeldet) {
        pruefeAngemeldet(angemeldet);
        Essensbewertung bestehend = bewertungOderFehler(bewertungId);
        pruefeEigentuemerOderAdmin(bestehend, angemeldet);
        bewertungDAO.loeschen(bewertungId);
    }

    private void pruefeEssenExistiert(int essenId) {
        if (essenDAO.findById(essenId) == null) {
            throw new NichtGefundenException("Essen " + essenId + " nicht gefunden");
        }
    }

    private Essensbewertung bewertungOderFehler(int bewertungId) {
        Essensbewertung bewertung = bewertungDAO.findById(bewertungId);
        if (bewertung == null) {
            throw new NichtGefundenException("Bewertung " + bewertungId + " nicht gefunden");
        }
        return bewertung;
    }

    /** 403, wenn jemand eine fremde Bewertung aendern/loeschen will - siehe REST-API.md. */
    private void pruefeEigentuemerOderAdmin(Essensbewertung bewertung, Benutzer angemeldet) {
        if (bewertung.getBenutzerId() != angemeldet.getId() && !angemeldet.istAdmin()) {
            throw new KeineBerechtigungException("Nur die eigene Bewertung darf geaendert oder geloescht werden");
        }
    }

    private void pruefeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidierungException("Bewertungstext ist erforderlich");
        }
    }

    private void pruefeSterne(int sterne) {
        if (sterne < Essensbewertung.MIN_STERNE || sterne > Essensbewertung.MAX_STERNE) {
            throw new ValidierungException(
                    "Sterne muessen zwischen " + Essensbewertung.MIN_STERNE
                            + " und " + Essensbewertung.MAX_STERNE + " liegen");
        }
    }

    private void pruefeAngemeldet(Benutzer benutzer) {
        if (benutzer == null) {
            throw new NichtAngemeldetException("Anmeldung erforderlich");
        }
    }
}
