package de.leuphana.mensa.persistence.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.leuphana.mensa.model.Essensbewertung;
import de.leuphana.mensa.persistence.DAOException;
import de.leuphana.mensa.persistence.EssensbewertungDAO;

public class EssensbewertungDAOInMemory implements EssensbewertungDAO {

    private final Map<Integer, Essensbewertung> bewertungen = new LinkedHashMap<>();
    private int naechsteId = 1;

    @Override
    public Essensbewertung abgeben(Essensbewertung neu) {
        if (!neu.istGueltig()) {
            throw new DAOException("Bewertung unvollstaendig: Sterne 1-5 und Text sind Pflicht");
        }
        neu.setId(naechsteId++);
        bewertungen.put(neu.getId(), neu);
        return neu;
    }

    @Override
    public void aendern(Essensbewertung geaendert) {
        if (!bewertungen.containsKey(geaendert.getId())) {
            throw new DAOException("Bewertung " + geaendert.getId() + " existiert nicht");
        }
        if (!geaendert.istGueltig()) {
            throw new DAOException("Bewertung unvollstaendig");
        }
        bewertungen.put(geaendert.getId(), geaendert);
    }

    @Override
    public Essensbewertung findById(int bewertungId) {
        return bewertungen.get(bewertungId);
    }

    @Override
    public List<Essensbewertung> findByEssen(int essenId) {
        List<Essensbewertung> treffer = new ArrayList<>();
        for (Essensbewertung b : bewertungen.values()) {
            if (b.getEssenId() == essenId) {
                treffer.add(b);
            }
        }
        return treffer;
    }

    @Override
    public double durchschnittFuerEssen(int essenId) {
        List<Essensbewertung> zumEssen = findByEssen(essenId);
        if (zumEssen.isEmpty()) {
            return 0.0;
        }
        int summe = 0;
        for (Essensbewertung b : zumEssen) {
            summe += b.getSterne();
        }
        return (double) summe / zumEssen.size();
    }

    @Override
    public boolean loeschen(int bewertungId) {
        return bewertungen.remove(bewertungId) != null;
    }
}
