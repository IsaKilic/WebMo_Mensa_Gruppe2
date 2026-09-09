package de.leuphana.mensa.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Legt Bewertungsfotos im Dateisystem ab und liefert nur den (server-
 * seitig vergebenen) Dateinamen zurueck, der in der DB gespeichert wird -
 * siehe docs/REST-API.md, Abschnitt "Entscheidungen": "Die Datei landet
 * im Dateisystem, in der Datenbank steht nur der Pfad."
 *
 * Der Dateiname wird IMMER serverseitig per UUID neu vergeben und NIE
 * vom Client uebernommen. Grund steht in docs/REST-API.md bei
 * "GET /api/fotos/{dateiname}": sonst koennte jemand mit einem
 * praeparierten Namen wie "../../etc/passwd" aus dem Zielverzeichnis
 * ausbrechen (Path Traversal).
 *
 * pfadZu(...) prueft per WHITELIST-Regex, nicht per Blacklist auf
 * einzelne verbotene Zeichen wie ".." oder "/": ein Dateiname, der nicht
 * exakt "UUID.endung" entspricht, wird abgelehnt. Eine Blacklist muesste
 * jeden denkbaren Trick einzeln kennen, eine Whitelist laesst strukturell
 * gar nichts anderes durch - das ist die robustere Technik gegen Path
 * Traversal.
 */
public class FotoSpeicher {

    private static final Set<String> ERLAUBTE_ENDUNGEN = Set.of("jpg", "jpeg", "png", "webp");

    /** Exakt das Format, das speichern(...) selbst erzeugt: UUID + Punkt + erlaubte Endung. */
    private static final Pattern GUELTIGER_DATEINAME = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
                    + "\\.(jpg|jpeg|png|webp)$");

    private final Path zielVerzeichnis;

    public FotoSpeicher(Path zielVerzeichnis) {
        this.zielVerzeichnis = zielVerzeichnis;
        try {
            Files.createDirectories(zielVerzeichnis);
        } catch (IOException e) {
            throw new IllegalStateException("Foto-Verzeichnis konnte nicht angelegt werden", e);
        }
    }

    /** @return der neu vergebene Dateiname (nicht der volle Pfad). */
    public String speichern(InputStream inhalt, String urspruenglicherDateiname) {
        String endung = endungVon(urspruenglicherDateiname);
        String neuerName = UUID.randomUUID() + "." + endung;
        Path ziel = zielVerzeichnis.resolve(neuerName);
        try (OutputStream ausgabe = Files.newOutputStream(ziel)) {
            inhalt.transferTo(ausgabe);
        } catch (IOException e) {
            throw new IllegalStateException("Foto konnte nicht gespeichert werden", e);
        }
        return neuerName;
    }

    /**
     * @return der volle Pfad zu einem bereits gespeicherten Foto.
     * @throws ValidierungException wenn der Dateiname nicht dem erwarteten
     *         Muster entspricht (400) - das kann bei einem selbst vergebenen
     *         Namen eigentlich nie passieren, ausser jemand versucht, sich
     *         etwas anderes als Dateinamen unterzuschieben.
     * @throws NichtGefundenException wenn es die Datei nicht gibt (404).
     */
    public Path pfadZu(String dateiname) {
        if (dateiname == null || !GUELTIGER_DATEINAME.matcher(dateiname).matches()) {
            throw new ValidierungException("Ungueltiger Dateiname");
        }
        Path pfad = zielVerzeichnis.resolve(dateiname).normalize();
        if (!pfad.startsWith(zielVerzeichnis)) {
            throw new ValidierungException("Ungueltiger Dateiname"); // zur Sicherheit doppelt geprueft
        }
        if (!Files.exists(pfad)) {
            throw new NichtGefundenException("Das Foto " + dateiname + " gibt es nicht");
        }
        return pfad;
    }

    private String endungVon(String dateiname) {
        if (dateiname != null && dateiname.contains(".")) {
            String endung = dateiname.substring(dateiname.lastIndexOf('.') + 1).toLowerCase();
            if (ERLAUBTE_ENDUNGEN.contains(endung)) {
                return endung;
            }
        }
        return "jpg"; // sicherer Standard, falls der Client keine brauchbare Endung liefert
    }
}
