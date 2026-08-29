package de.leuphana.mensa.persistence;

import java.util.List;

import de.leuphana.mensa.model.Mensa;

/**
 * Zugriff auf die Mensa-Stammdaten.
 *
 * Rolle B programmiert gegen dieses Interface, nicht gegen die
 * JDBC-Implementierung. Dadurch kann B sofort mit der In-Memory-Variante
 * arbeiten, während die MySQL-Anbindung noch entsteht.
 */
public interface MensaDAO {

    List<Mensa> findAlle();

    /** @return die Mensa oder null, wenn es keine mit dieser Id gibt. */
    Mensa findById(int mensaId);
}
