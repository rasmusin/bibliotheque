package com.bibliotheque.dao.impl;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Membre;
import com.bibliotheque.util.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmpruntDAOImplTest {

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

            stmt.execute("CREATE TABLE IF NOT EXISTS membres (" +
                    "id_membre INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(255), " +
                    "prenom VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "date_inscription DATE, " +
                    "actif BOOLEAN)");

            stmt.execute("CREATE TABLE IF NOT EXISTS emprunts (" +
                    "id_emprunt INT AUTO_INCREMENT PRIMARY KEY, " +
                    "isbn VARCHAR(50), " +
                    "id_membre INT, " +
                    "date_emprunt DATE, " +
                    "date_retour_prevue DATE, " +
                    "date_retour_effective DATE, " +
                    "penalite DOUBLE)");
        }
    }

    @AfterAll
    public static void teardown() {
        DatabaseConnection.getInstance().closeConnection();
    }

    @Test
    public void testSaveFindUpdateDeleteAndQueries() throws Exception {
        LivreDAOImpl livreDAO = new LivreDAOImpl();
        MembreDAOImpl membreDAO = new MembreDAOImpl();
        EmpruntDAOImpl empruntDAO = new EmpruntDAOImpl();

        Livre livre = new Livre("ISBN-EMPR-1", "Essai", "AuteurTest", 2020, 9.9);
        livre.setIsbn("ISBN-EMPR-1");
        livre.setTitre("Essai");
        livre.setAuteur("AuteurTest");
        livreDAO.save(livre);

        Membre membre = new Membre("Test", "User", "test.user@example.com");
        membreDAO.save(membre);

        Emprunt emprunt = new Emprunt(livre, membre);
        empruntDAO.save(emprunt);

        assertTrue(emprunt.getIdEmprunt() > 0);

        Emprunt found = empruntDAO.findById(emprunt.getIdEmprunt());
        assertNotNull(found);
        assertEquals("ISBN-EMPR-1", found.getLivre().getIsbn());

        List<Emprunt> enCours = empruntDAO.findEnCours();
        assertTrue(enCours.stream().anyMatch(e -> e.getIdEmprunt() == emprunt.getIdEmprunt()));

        List<Emprunt> byMembre = empruntDAO.findByMembre(membre.getIdMembre());
        assertTrue(byMembre.size() >= 1);

        int count = empruntDAO.countEmpruntsEnCoursByMembre(membre.getIdMembre());
        assertTrue(count >= 1);

        empruntDAO.delete(emprunt.getIdEmprunt());
        assertNull(empruntDAO.findById(emprunt.getIdEmprunt()));
    }
}
