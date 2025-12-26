package com.bibliotheque.dao.impl;

import com.bibliotheque.model.Livre;
import com.bibliotheque.util.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LivreDAOImplTest {

    @BeforeAll
    public static void setup() throws Exception {
        System.setProperty("jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        System.setProperty("jdbc.user", "sa");
        System.setProperty("jdbc.password", "");
        System.setProperty("jdbc.driver", "org.h2.Driver");

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS livres (" +
                    "isbn VARCHAR(50) PRIMARY KEY, " +
                    "titre VARCHAR(255), " +
                    "auteur VARCHAR(255), " +
                    "annee_publication INT, " +
                    "prix DOUBLE, " +
                    "disponible BOOLEAN, " +
                    "date_creation TIMESTAMP)");
        }
    }

    @AfterAll
    public static void teardown() {
        DatabaseConnection.getInstance().closeConnection();
    }

    @Test
    public void testSaveAndFindById() throws Exception {
        LivreDAOImpl dao = new LivreDAOImpl();
        Livre livre = new Livre("ISBN-123", "Mon Livre", "Dupont", 2021, 15.0);
        dao.save(livre);

        Livre found = dao.findById("ISBN-123");
        assertNotNull(found);
        assertEquals("Mon Livre", found.getTitre());
        assertEquals("Dupont", found.getAuteur());
    }

    @Test
    public void testUpdateDeleteFindAllAndQueries() throws Exception {
        LivreDAOImpl dao = new LivreDAOImpl();
        Livre livre = new Livre("ISBN-456", "Livre2", "Martin", 2019, 10.0);
        dao.save(livre);

        livre.setTitre("Livre2-mod");
        dao.update(livre);

        Livre updated = dao.findById("ISBN-456");
        assertEquals("Livre2-mod", updated.getTitre());

        List<Livre> all = dao.findAll();
        assertTrue(all.size() >= 1);

        List<Livre> byAuteur = dao.findByAuteur("Martin");
        assertTrue(byAuteur.stream().anyMatch(l -> "ISBN-456".equals(l.getIsbn())));

        List<Livre> disponibles = dao.findDisponibles();
        assertTrue(disponibles.size() >= 1);

        dao.delete("ISBN-456");
        assertNull(dao.findById("ISBN-456"));
    }
}
