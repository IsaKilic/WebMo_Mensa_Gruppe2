import de.leuphana.mensa.security.PasswortHasher;

public class TestPasswortHasher {
    static int ok=0, fail=0;
    static void p(String w, boolean b){ System.out.printf("  [%s] %s%n", b?"OK ":"FEHL", w); if(b) ok++; else fail++; }

    public static void main(String[] a){
        System.out.println("=== Grundfunktion ===");
        String hash = PasswortHasher.hashen("admin123");
        p("Hash erzeugt", hash != null && hash.length() > 40);
        p("Format iterationen:salt:hash", hash.split(":").length == 3);
        p("Klartext kommt NICHT vor", !hash.contains("admin123"));
        p("richtiges Passwort passt", PasswortHasher.pruefen("admin123", hash));
        p("falsches Passwort passt nicht", !PasswortHasher.pruefen("admin124", hash));
        p("leeres Passwort passt nicht", !PasswortHasher.pruefen("", hash));
        p("null passt nicht", !PasswortHasher.pruefen(null, hash));

        System.out.println("\n=== Salt: zweimal dasselbe Passwort ===");
        String h1 = PasswortHasher.hashen("gleich");
        String h2 = PasswortHasher.hashen("gleich");
        p("verschiedene Hashes trotz gleichem Passwort", !h1.equals(h2));
        p("beide pruefen korrekt",
            PasswortHasher.pruefen("gleich", h1) && PasswortHasher.pruefen("gleich", h2));

        System.out.println("\n=== Robustheit ===");
        p("Klartext-Altwert -> false", !PasswortHasher.pruefen("admin123", "admin123"));
        p("Muell -> false", !PasswortHasher.pruefen("x", "kaputt:auch:kaputt"));
        p("zu wenige Teile -> false", !PasswortHasher.pruefen("x", "nur:zwei"));
        p("null-Hash -> false", !PasswortHasher.pruefen("x", null));
        try { PasswortHasher.hashen(null); p("null hashen -> Exception", false); }
        catch (IllegalArgumentException e){ p("null hashen -> IllegalArgumentException", true); }

        System.out.println("\n=== Umlaute und Sonderzeichen ===");
        String uml = PasswortHasher.hashen("Grüße!ÄÖÜ$123");
        p("Umlaut-Passwort prueft", PasswortHasher.pruefen("Grüße!ÄÖÜ$123", uml));

        System.out.println("\n=== Laufzeit (soll spuerbar, aber ertraeglich sein) ===");
        long start = System.currentTimeMillis();
        for (int i=0;i<10;i++) PasswortHasher.pruefen("admin123", hash);
        long ms = (System.currentTimeMillis()-start)/10;
        System.out.println("  ~" + ms + " ms pro Pruefung");
        p("zwischen 10 und 500 ms", ms >= 10 && ms <= 500);

        System.out.printf("%n===== %d OK, %d Fehler =====%n", ok, fail);
    }
}
