package com.bibliotheque.dao.impl;

import com.bibliotheque.model.Membre;
import com.bibliotheque.util.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MembreDAOImplTest {

    @BeforeAll
    public static void setup() throws Exception {
        System.setProperty("jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        System.setProperty("jdbc.user", "sa");
        System.setProperty("jdbc.password", "");
        System.setProperty("jdbc.driver", "org.h2.Driver");

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS membres (" +
                    "id_membre INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(255), " +
                    "prenom VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "date_inscription DATE, " +
                    "actif BOOLEAN)");
        }
    }

    @AfterAll
    public static void teardown() {
        DatabaseConnection.getInstance().closeConnection();
    }

    @Test
    public void testSaveAndFindByIdAndEmail() throws Exception {
        MembreDAOImpl dao = new MembreDAOImpl();
        Membre m = new Membre("Dupont", "Jean", "j.dupont@example.com");
        dao.save(m);

        assertTrue(m.getIdMembre() > 0);

        Membre found = dao.findById(m.getIdMembre());
        assertNotNull(found);
        assertEquals("Dupont", found.getNom());

        Membre byEmail = dao.findByEmail("j.dupont@example.com");
        assertNotNull(byEmail);
        assertEquals(found.getIdMembre(), byEmail.getIdMembre());
    }

    @Test
    public void testUpdateDeleteAndQueries() throws Exception {
        MembreDAOImpl dao = new MembreDAOImpl();
        Membre m = new Membre("Martin", "Alice", "a.martin@example.com");
        dao.save(m);

        m.setNom("Martin-Updated");
        dao.update(m);

        Membre updated = dao.findById(m.getIdMembre());
        assertEquals("Martin-Updated", updated.getNom());

        List<Membre> all = dao.findAll();
        assertTrue(all.size() >= 1);

        List<Membre> actifs = dao.findActifs();
        assertTrue(actifs.stream().anyMatch(x -> x.getIdMembre() == m.getIdMembre()));

        List<Membre> byNom = dao.findByNom("Martin");
        assertTrue(byNom.size() >= 1);

        dao.delete(m.getIdMembre());
        assertNull(dao.findById(m.getIdMembre()));
    }
}
