package de.leuphana.mensa.persistence.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.leuphana.mensa.model.Art;
import de.leuphana.mensa.model.Essen;
import de.leuphana.mensa.persistence.EssenDAO;

/**
 * Die zehn Essen laut Aufgabenstellung.
 * Ersetzt spaeter durch EssenDAOJdbc, ohne dass die Service-Schicht sich aendert.
 */
public class EssenDAOInMemory implements EssenDAO {

    private final Map<Integer, Essen> essen = new LinkedHashMap<>();
    private int naechsteId = 1;

    public EssenDAOInMemory() {
        anlegen(new Essen(0, "Hähnchenbrust in Sesampanade", 2.85, Art.MIT_FLEISCH));
        anlegen(new Essen(0, "Rindergulasch mit Rotkohl",    3.20, Art.MIT_FLEISCH));
        anlegen(new Essen(0, "Seelachsfilet auf Blattspinat", 3.10, Art.MIT_FLEISCH));
        anlegen(new Essen(0, "Schweineschnitzel mit Pommes",  2.95, Art.MIT_FLEISCH));
        anlegen(new Essen(0, "Putengeschnetzeltes",           3.05, Art.MIT_FLEISCH));
        anlegen(new Essen(0, "Gemüselasagne",                 2.40, Art.VEGETARISCH));
        anlegen(new Essen(0, "Spinatknödel mit Salbeibutter", 2.50, Art.VEGETARISCH));
        anlegen(new Essen(0, "Kichererbsen-Curry mit Reis",   2.30, Art.VEGAN));
        anlegen(new Essen(0, "Ofengemüse mit Hirse und Tahin", 2.60, Art.VEGAN));
        anlegen(new Essen(0, "Linsenbolognese mit Penne",     2.45, Art.VEGAN));
    }

    @Override
    public Essen anlegen(Essen neu) {
        neu.setId(naechsteId++);
        essen.put(neu.getId(), neu);
        return neu;
    }

    @Override
    public void aendern(Essen geaendert) {
        if (!essen.containsKey(geaendert.getId())) {
            throw new de.leuphana.mensa.persistence.DAOException(
                    "Essen " + geaendert.getId() + " existiert nicht");
        }
        essen.put(geaendert.getId(), geaendert);
    }

    @Override
    public Essen findById(int essenId) {
        return essen.get(essenId);
    }

    @Override
    public List<Essen> findAlle() {
        return new ArrayList<>(essen.values());
    }

    @Override
    public boolean loeschen(int essenId) {
        return essen.remove(essenId) != null;
    }
}
