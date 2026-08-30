package de.leuphana.mensa.model;

/**
 * Benutzer fuer den Login-Dialog.
 *
 * Das Passwort wird als Hash gehalten, nie im Klartext. Fuer die
 * muendliche Pruefung: BCrypt oder PBKDF2, nicht MD5 oder SHA-1.
 */
public class Benutzer {

    private int id;
    private String benutzername;
    private String passwortHash;
    private Rolle rolle = Rolle.USER;

    public Benutzer() {
    }

    public Benutzer(int id, String benutzername, String passwortHash, Rolle rolle) {
        this.id = id;
        this.benutzername = benutzername;
        this.passwortHash = passwortHash;
        this.rolle = rolle;
    }

    public boolean istAdmin() {
        return rolle == Rolle.ADMIN;
    }

    public int    getId()            { return id; }
    public String getBenutzername()  { return benutzername; }
    public String getPasswortHash()  { return passwortHash; }
    public Rolle  getRolle()         { return rolle; }

    public void setId(int id)                             { this.id = id; }
    public void setBenutzername(String benutzername)      { this.benutzername = benutzername; }
    public void setPasswortHash(String passwortHash)      { this.passwortHash = passwortHash; }
    public void setRolle(Rolle rolle)                     { this.rolle = rolle; }

    @Override
    public String toString() {
        return "Benutzer[" + id + ", " + benutzername + ", " + rolle + "]";
    }
}
