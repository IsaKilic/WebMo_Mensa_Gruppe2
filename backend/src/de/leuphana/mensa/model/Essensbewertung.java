package de.leuphana.mensa.model;

import java.time.LocalDateTime;

/**
 * Fachklasse Essensbewertung.
 *
 * Attribute laut Aufgabenstellung: Essensfoto, Bewertung (1 bis 5)
 * und Bewertungstext. Der Text ist Pflicht.
 *
 * Das Foto MUSS aus der Anwendung heraus mit der Kamera aufgenommen
 * werden. Ein vorher aufgenommenes Bild hochzuladen ist laut
 * Aufgabenstellung ausdruecklich nicht erlaubt.
 */
public class Essensbewertung {

    public static final int MIN_STERNE = 1;
    public static final int MAX_STERNE = 5;

    private int id;
    private int essenId;
    private int benutzerId;
    private int sterne;
    private String text;
    private String fotoPfad;
    private LocalDateTime zeitpunkt;

    public Essensbewertung() {
    }

    public Essensbewertung(int id, int essenId, int benutzerId,
                           int sterne, String text, String fotoPfad) {
        this.id = id;
        this.essenId = essenId;
        this.benutzerId = benutzerId;
        setSterne(sterne);
        this.text = text;
        this.fotoPfad = fotoPfad;
        this.zeitpunkt = LocalDateTime.now();
    }

    /**
     * Prueft, ob die Bewertung vollstaendig ist.
     * Der Bewertungstext ist laut Aufgabenstellung verpflichtend.
     */
    public boolean istGueltig() {
        return sterne >= MIN_STERNE && sterne <= MAX_STERNE
                && text != null && !text.trim().isEmpty();
    }

    public int             getId()         { return id; }
    public int             getEssenId()    { return essenId; }
    public int             getBenutzerId() { return benutzerId; }
    public int             getSterne()     { return sterne; }
    public String          getText()       { return text; }
    public String          getFotoPfad()   { return fotoPfad; }
    public LocalDateTime   getZeitpunkt()  { return zeitpunkt; }

    public void setId(int id)                   { this.id = id; }
    public void setEssenId(int essenId)         { this.essenId = essenId; }
    public void setBenutzerId(int benutzerId)   { this.benutzerId = benutzerId; }
    public void setText(String text)            { this.text = text; }
    public void setFotoPfad(String fotoPfad)    { this.fotoPfad = fotoPfad; }
    public void setZeitpunkt(LocalDateTime z)   { this.zeitpunkt = z; }

    public void setSterne(int sterne) {
        if (sterne < MIN_STERNE || sterne > MAX_STERNE) {
            throw new IllegalArgumentException(
                "Bewertung muss zwischen " + MIN_STERNE + " und " + MAX_STERNE + " liegen");
        }
        this.sterne = sterne;
    }

    @Override
    public String toString() {
        return "Essensbewertung[" + id + ", Essen " + essenId + ", " + sterne + " Sterne]";
    }
}
