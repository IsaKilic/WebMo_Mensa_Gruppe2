package de.leuphana.mensa.persistence.inmemory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.leuphana.mensa.model.Allergen;
import de.leuphana.mensa.model.Gericht;
import de.leuphana.mensa.model.Kategorie;
import de.leuphana.mensa.model.Kennzeichnung;
import de.leuphana.mensa.model.Mensa;
import de.leuphana.mensa.model.Naehrwerte;
import de.leuphana.mensa.model.Preis;
import de.leuphana.mensa.model.Speiseplan;
import de.leuphana.mensa.model.Zusatzstoff;
import de.leuphana.mensa.persistence.MensaDAO;
import de.leuphana.mensa.persistence.SpeiseplanDAO;

/**
 * Testdaten für die ersten Wochen der Entwicklung.
 *
 * Erzeugt für die kommenden Werktage einen Speiseplan, damit Rolle B, C und D
 * gegen realistische Daten arbeiten können, ohne dass MySQL oder der
 * Importer fertig sein müssen.
 */
public class SpeiseplanDAOInMemory implements SpeiseplanDAO {

    /** Schlüssel: mensaId + "#" + ISO-Datum */
    private final Map<String, Speiseplan> plaene = new HashMap<>();
    private final MensaDAO mensaDAO;
    private int naechsteGerichtId = 1;

    public SpeiseplanDAOInMemory() {
        this(new MensaDAOInMemory());
    }

    public SpeiseplanDAOInMemory(MensaDAO mensaDAO) {
        this.mensaDAO = mensaDAO;
        erzeugeTestdaten();
    }

    private static String schluessel(int mensaId, LocalDate datum) {
        return mensaId + "#" + datum;
    }

    @Override
    public Speiseplan findByMensaUndDatum(int mensaId, LocalDate datum) {
        return plaene.get(schluessel(mensaId, datum));
    }

    @Override
    public List<Speiseplan> findWoche(int mensaId, LocalDate start) {
        List<Speiseplan> woche = new ArrayList<>();
        for (int tag = 0; tag < 7; tag++) {
            Speiseplan plan = findByMensaUndDatum(mensaId, start.plusDays(tag));
            if (plan != null) {
                woche.add(plan);
            }
        }
        woche.sort(Comparator.comparing(Speiseplan::getDatum));
        return woche;
    }

    @Override
    public void speichern(Speiseplan speiseplan) {
        plaene.put(schluessel(speiseplan.getMensa().getId(), speiseplan.getDatum()),
                   speiseplan);
    }

    // ---------------------------------------------------------------
    // Testdaten
    // ---------------------------------------------------------------

    private void erzeugeTestdaten() {
        Mensa campus = mensaDAO.findById(1);
        LocalDate heute = LocalDate.now();

        for (int tag = 0; tag < 10; tag++) {
            LocalDate datum = heute.plusDays(tag);
            if (datum.getDayOfWeek().getValue() >= 6) {
                continue; // Wochenende: kein Speiseplan
            }
            speichern(erzeugeTagesplan(campus, datum, tag));
        }
    }

    private Speiseplan erzeugeTagesplan(Mensa mensa, LocalDate datum, int variante) {
        Speiseplan plan = new Speiseplan(mensa, datum);

        // Hauptgericht: Name, Kennzeichnung und Allergene gehoeren zusammen
        // und werden deshalb gemeinsam ausgewaehlt.
        String[] namen = {
            "Hähnchenbrust in Sesampanade",
            "Rindergulasch mit Rotkohl",
            "Seelachsfilet auf Blattspinat",
            "Schweineschnitzel mit Pommes",
            "Putengeschnetzeltes Züricher Art"
        };
        Kennzeichnung[] fleischart = {
            Kennzeichnung.GEFLUEGEL,
            Kennzeichnung.RIND,
            Kennzeichnung.FISCH,
            Kennzeichnung.SCHWEIN,
            Kennzeichnung.GEFLUEGEL
        };
        @SuppressWarnings("unchecked")
        Set<Allergen>[] hauptAllergene = new Set[] {
            EnumSet.of(Allergen.GLUTEN, Allergen.EIER, Allergen.SESAM),
            EnumSet.of(Allergen.SELLERIE),
            EnumSet.of(Allergen.FISCH, Allergen.MILCH),
            EnumSet.of(Allergen.GLUTEN, Allergen.EIER),
            EnumSet.of(Allergen.MILCH)
        };

        String[] vegetarisch = {
            "Gemüselasagne",
            "Kichererbsen-Curry mit Reis",
            "Spinatknödel mit Salbeibutter",
            "Süßkartoffel-Auflauf",
            "Linsenbolognese mit Penne"
        };

        int i = variante % namen.length;

        Gericht haupt = neuesGericht(namen[i], Kategorie.HAUPTGERICHT,
                new Preis(2.85, 4.70, 6.50));
        haupt.setAllergene(hauptAllergene[i]);
        haupt.setKennzeichnungen(EnumSet.of(fleischart[i]));
        haupt.setZusatzstoffe(EnumSet.of(Zusatzstoff.ANTIOXIDATIONSMITTEL));
        haupt.setNaehrwerte(new Naehrwerte(590, 141, 8.5, 13.5, 1.7, 0.6));
        plan.addGericht(haupt);

        Gericht veggie = neuesGericht(
                vegetarisch[i],
                Kategorie.VEGETARISCH,
                new Preis(2.40, 4.20, 5.90));
        veggie.setAllergene(EnumSet.of(Allergen.GLUTEN, Allergen.MILCH));
        veggie.setKennzeichnungen(EnumSet.of(Kennzeichnung.VEGETARISCH));
        veggie.setNaehrwerte(new Naehrwerte(2100, 502, 18.2, 62.0, 17.4, 1.9));
        plan.addGericht(veggie);

        Gericht vegan = neuesGericht(
                "Ofengemüse mit Hirse und Tahin",
                Kategorie.VEGETARISCH,
                new Preis(2.40, 4.20, 5.90));
        vegan.setAllergene(EnumSet.of(Allergen.SESAM));
        vegan.setKennzeichnungen(EnumSet.of(Kennzeichnung.VEGAN, Kennzeichnung.BIO));
        vegan.setNaehrwerte(new Naehrwerte(1750, 418, 14.1, 55.3, 11.8, 1.2));
        plan.addGericht(vegan);

        Gericht suppe = neuesGericht(
                "Karotten-Ingwer-Suppe",
                Kategorie.SUPPE,
                new Preis(0.90, 1.40, 1.90));
        suppe.setAllergene(EnumSet.of(Allergen.SELLERIE));
        suppe.setKennzeichnungen(EnumSet.of(Kennzeichnung.VEGAN));
        plan.addGericht(suppe);

        Gericht beilage = neuesGericht(
                "Kartoffelsalat",
                Kategorie.BEILAGE,
                new Preis(0.80, 1.20, 1.60));
        beilage.setAllergene(EnumSet.of(Allergen.SENF, Allergen.EIER));
        beilage.setKennzeichnungen(EnumSet.of(Kennzeichnung.VEGETARISCH));
        plan.addGericht(beilage);

        Gericht dessert = neuesGericht(
                "Schokoladenpudding",
                Kategorie.DESSERT,
                new Preis(0.90, 1.30, 1.70));
        dessert.setAllergene(EnumSet.of(Allergen.MILCH));
        dessert.setKennzeichnungen(EnumSet.of(Kennzeichnung.VEGETARISCH));
        plan.addGericht(dessert);

        return plan;
    }

    private Gericht neuesGericht(String name, Kategorie kategorie, Preis preis) {
        return new Gericht(naechsteGerichtId++, name, kategorie, preis);
    }
}
