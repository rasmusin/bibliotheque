package com.bibliotheque.service;

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

public class BibliothequeServiceTest {

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
        }
    }

    @AfterAll
    public static void teardown() {
        DatabaseConnection.getInstance().closeConnection();
    }

    @Test
    public void testAjouterEtRechercherLivresEtMembres() throws Exception {
        BibliothequeService svc = new BibliothequeService();

        Livre l1 = new Livre("ISBN-BS-1", "TitreA", "AuteurX", 2018, 12.0);
        svc.ajouterLivre(l1);

        Livre fetched = svc.getLivreParISBN("ISBN-BS-1");
        assertNotNull(fetched);
        assertEquals("TitreA", fetched.getTitre());

        Membre m1 = new Membre("NomTest", "PrenomTest", "m.test@example.com");
        svc.ajouterMembre(m1);

        List<Membre> membres = svc.getAllMembres();
        assertTrue(membres.stream().anyMatch(mm -> mm.getEmail().equals("m.test@example.com")));

        List<Livre> parAuteur = svc.rechercherParAuteur("AuteurX");
        assertTrue(parAuteur.size() >= 1);

        assertTrue(svc.countLivres() >= 1);
    }
}
