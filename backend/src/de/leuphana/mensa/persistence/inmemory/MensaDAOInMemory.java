package de.leuphana.mensa.persistence.inmemory;

import java.util.ArrayList;
import java.util.List;

import de.leuphana.mensa.model.Mensa;
import de.leuphana.mensa.persistence.MensaDAO;

/**
 * Stammdaten fest im Code, analog zu Catalog aus Inkrement11.
 * Dient dazu, Rolle B sofort arbeitsfähig zu machen.
 * Wird später durch MensaDAOJdbc ersetzt, ohne dass B etwas ändern muss.
 */
public class MensaDAOInMemory implements MensaDAO {

    private final List<Mensa> mensen = new ArrayList<>();

    public MensaDAOInMemory() {
        mensen.add(new Mensa(1, "Mensa Campus",
                "Scharnhorststraße 1, 21335 Lüneburg", 53.2285, 10.4012));
        mensen.add(new Mensa(2, "Mensa Campus abends",
                "Scharnhorststraße 1, 21335 Lüneburg", 53.2285, 10.4012));
    }

    @Override
    public List<Mensa> findAlle() {
        return new ArrayList<>(mensen);
    }

    @Override
    public Mensa findById(int mensaId) {
        for (Mensa mensa : mensen) {
            if (mensa.getId() == mensaId) {
                return mensa;
            }
        }
        return null;
    }
}
