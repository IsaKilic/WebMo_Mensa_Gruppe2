package de.leuphana.mensa.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.leuphana.mensa.persistence.BenutzerDAO;
import de.leuphana.mensa.persistence.EssenDAO;
import de.leuphana.mensa.persistence.EssensbewertungDAO;
import de.leuphana.mensa.persistence.EssensplanDAO;
import de.leuphana.mensa.persistence.inmemory.BenutzerDAOInMemory;
import de.leuphana.mensa.persistence.inmemory.EssenDAOInMemory;
import de.leuphana.mensa.persistence.inmemory.EssensbewertungDAOInMemory;
import de.leuphana.mensa.persistence.inmemory.EssensplanDAOInMemory;

/**
 * Einziger Ort, an dem DAOs und Services tatsaechlich mit "new" entstehen.
 * Jedes Servlet ruft in seinem init() nur noch Fabrik.xyzService() auf und
 * bekommt IMMER dieselbe Instanz zurueck.
 *
 * Ersetzt das fruehere Muster "jedes Servlet baut in init() sein eigenes
 * new EssenDAOInMemory()". Das Muster stammte wortwoertlich aus
 * docs/AUFGABE-ROLLE-B.md, hat aber einen konkreten Fehler: die vier
 * InMemory-DAOs halten ihre Daten in normalen (nicht static) Instanzfeldern.
 * Zwei Servlets mit je eigener DAO-Instanz sehen sich gegenseitig nicht -
 * ein in EssenServlet neu angelegtes Essen war z.B. in EssensplanServlet
 * unsichtbar (404). Diese Fabrik loest das direkt in Rolle Bs eigenem
 * Code: alle Servlets teilen sich dieselben Instanzen, ganz ohne dass
 * Rolle A etwas an den InMemory-Klassen aendern muesste (die Bitte an
 * Rolle A dazu ist damit hinfaellig - siehe Bitte-an-Rolle-A.md).
 *
 * Wichtig fuer die Pruefung: das ist funktional dasselbe Prinzip wie ein
 * Application-Scope-Singleton (siehe Catalog aus dem Webshop), nur ohne
 * ServletContextListener - eine ganz normale Java-Klasse mit statischen
 * Feldern reicht hier aus, weil wir keinen Zugriff auf den ServletContext
 * brauchen.
 *
 * synchronized: falls Tomcat beim Start mehrere Servlets nebenlaeufig
 * initialisiert, verhindert das, dass zwei Threads gleichzeitig zwei
 * verschiedene Instanzen anlegen.
 */
public final class Fabrik {

    /** Auf true stellen, sobald die Jdbc-Implementierungen von Rolle A uebernommen werden. */
    private static final boolean MIT_DATENBANK = false;

    private static final Path FOTO_VERZEICHNIS =
            Paths.get(System.getProperty("user.home"), "mensa-fotos");

    private static EssenDAO essenDAO;
    private static EssensplanDAO essensplanDAO;
    private static EssensbewertungDAO bewertungDAO;
    private static BenutzerDAO benutzerDAO;
    private static FotoSpeicher fotoSpeicher;

    private static AnmeldeService anmeldeService;
    private static EssenService essenService;
    private static EssensplanService essensplanService;
    private static BewertungService bewertungService;

    private Fabrik() {
    }

    public static synchronized EssenDAO essenDAO() {
        if (essenDAO == null) {
            essenDAO = MIT_DATENBANK
                    ? new de.leuphana.mensa.persistence.jdbc.EssenDAOJdbc()
                    : new EssenDAOInMemory();
        }
        return essenDAO;
    }

    public static synchronized EssensplanDAO essensplanDAO() {
        if (essensplanDAO == null) {
            essensplanDAO = MIT_DATENBANK
                    ? new de.leuphana.mensa.persistence.jdbc.EssensplanDAOJdbc()
                    : new EssensplanDAOInMemory(essenDAO());
        }
        return essensplanDAO;
    }

    public static synchronized EssensbewertungDAO bewertungDAO() {
        if (bewertungDAO == null) {
            bewertungDAO = MIT_DATENBANK
                    ? new de.leuphana.mensa.persistence.jdbc.EssensbewertungDAOJdbc()
                    : new EssensbewertungDAOInMemory();
        }
        return bewertungDAO;
    }

    public static synchronized BenutzerDAO benutzerDAO() {
        if (benutzerDAO == null) {
            benutzerDAO = MIT_DATENBANK
                    ? new de.leuphana.mensa.persistence.jdbc.BenutzerDAOJdbc()
                    : new BenutzerDAOInMemory();
        }
        return benutzerDAO;
    }

    public static synchronized FotoSpeicher fotoSpeicher() {
        if (fotoSpeicher == null) {
            fotoSpeicher = new FotoSpeicher(FOTO_VERZEICHNIS);
        }
        return fotoSpeicher;
    }

    public static synchronized AnmeldeService anmeldeService() {
        if (anmeldeService == null) {
            anmeldeService = new AnmeldeService(benutzerDAO());
        }
        return anmeldeService;
    }

    public static synchronized EssenService essenService() {
        if (essenService == null) {
            essenService = new EssenService(essenDAO(), bewertungDAO(), essensplanDAO());
        }
        return essenService;
    }

    public static synchronized EssensplanService essensplanService() {
        if (essensplanService == null) {
            essensplanService = new EssensplanService(essensplanDAO(), essenDAO());
        }
        return essensplanService;
    }

    public static synchronized BewertungService bewertungService() {
        if (bewertungService == null) {
            bewertungService = new BewertungService(bewertungDAO(), essenDAO(), benutzerDAO(), fotoSpeicher());
        }
        return bewertungService;
    }
}
