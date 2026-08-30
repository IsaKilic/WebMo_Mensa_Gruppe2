package de.leuphana.mensa.persistence.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.model.Essensplan;
import de.leuphana.mensa.model.Wochentag;
import de.leuphana.mensa.persistence.EssenDAO;
import de.leuphana.mensa.persistence.EssensplanDAO;

/**
 * Acht Essensplaene laut Aufgabenstellung, jeweils fuenf Essen Mo bis Fr.
 */
public class EssensplanDAOInMemory implements EssensplanDAO {

    private final Map<Integer, Essensplan> plaene = new LinkedHashMap<>();
    private int naechsteId = 1;

    public EssensplanDAOInMemory(EssenDAO essenDAO) {
        List<Essen> alleEssen = essenDAO.findAlle();

        // 8 Wochen, jede Woche 5 Essen. Rotiert durch die 10 Essen.
        for (int woche = 1; woche <= 8; woche++) {
            Essensplan plan = new Essensplan(0, woche);
            int index = 0;
            for (Wochentag tag : Wochentag.values()) {
                Essen zugewiesen = alleEssen.get(
                        ((woche - 1) * Wochentag.values().length + index) % alleEssen.size());
                plan.setEssen(tag, zugewiesen);
                index++;
            }
            anlegen(plan);
        }
    }

    @Override
    public Essensplan anlegen(Essensplan neu) {
        neu.setId(naechsteId++);
        plaene.put(neu.getId(), neu);
        return neu;
    }

    @Override
    public void aendern(Essensplan geaendert) {
        if (!plaene.containsKey(geaendert.getId())) {
            throw new de.leuphana.mensa.persistence.DAOException(
                    "Essensplan " + geaendert.getId() + " existiert nicht");
        }
        plaene.put(geaendert.getId(), geaendert);
    }

    @Override
    public Essensplan findById(int essensplanId) {
        return plaene.get(essensplanId);
    }

    @Override
    public Essensplan findByWoche(int wochennummer) {
        for (Essensplan plan : plaene.values()) {
            if (plan.getWochennummer() == wochennummer) {
                return plan;
            }
        }
        return null;
    }

    @Override
    public List<Essensplan> findAlle() {
        return new ArrayList<>(plaene.values());
    }

    @Override
    public boolean loeschen(int essensplanId) {
        return plaene.remove(essensplanId) != null;
    }
}
