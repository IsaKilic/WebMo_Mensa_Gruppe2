package de.leuphana.mensa.persistence;

import java.time.LocalDate;
import java.util.List;

import de.leuphana.mensa.model.Speiseplan;

/**
 * Zugriff auf Speisepläne.
 *
 * Die Methoden entsprechen genau den REST-Endpunkten, die Rolle B baut.
 * Das ist Absicht: so muss die Service-Schicht nicht mehrere Aufrufe
 * kombinieren.
 */
public interface SpeiseplanDAO {

    /** @return der Speiseplan oder null, wenn für den Tag keiner vorliegt. */
    Speiseplan findByMensaUndDatum(int mensaId, LocalDate datum);

    /**
     * Speisepläne ab dem Starttag, aufsteigend nach Datum.
     * Tage ohne Speiseplan werden übersprungen, nicht als leere Objekte geliefert.
     */
    List<Speiseplan> findWoche(int mensaId, LocalDate start);

    /** Legt an oder aktualisiert, je nachdem ob der Tag schon existiert. */
    void speichern(Speiseplan speiseplan);
}
