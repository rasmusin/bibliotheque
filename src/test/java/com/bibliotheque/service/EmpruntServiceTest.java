package com.bibliotheque.service;

import com.bibliotheque.dao.EmpruntDAO;
import com.bibliotheque.dao.LivreDAO;
import com.bibliotheque.dao.MembreDAO;
import com.bibliotheque.exception.LimiteEmpruntDepasseeException;
import com.bibliotheque.exception.LivreIndisponibleException;
import com.bibliotheque.exception.MembreInactifException;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Membre;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmpruntServiceTest {

    @Test
    public void testEmprunterLivreSuccessUpdatesLivreAndSavesEmprunt() throws Exception {
        // Arrange
        Livre livre = new Livre("ISBN-A", "Titre A", "Auteur A");
        livre.setDisponible(true);

        final boolean[] livreUpdated = {false};
        LivreDAO livreDAO = new LivreDAO() {
            @Override public void save(Livre livre) throws SQLException { throw new UnsupportedOperationException(); }
            @Override public Livre findById(Object isbn) throws SQLException { return livre; }
            @Override public List<Livre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Livre l) throws SQLException { livreUpdated[0] = !l.isDisponible(); }
            @Override public void delete(Object isbn) throws SQLException { }
            @Override public List<Livre> findByAuteur(String auteur) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findByTitre(String titre) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findDisponibles() throws SQLException { return Collections.emptyList(); }
        };

        Membre membre = new Membre("Dupont","Jean","j@example.com");
        membre.setIdMembre(1);
        membre.setActif(true);

        MembreDAO membreDAO = new MembreDAO() {
            @Override public void save(Membre m) throws SQLException { }
            @Override public Membre findById(Object id) throws SQLException { return membre; }
            @Override public List<Membre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Membre m) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public Membre findByEmail(String email) throws SQLException { return null; }
            @Override public List<Membre> findActifs() throws SQLException { return Collections.emptyList(); }
            @Override public List<Membre> findByNom(String nom) throws SQLException { return Collections.emptyList(); }
        };

        final List<Emprunt> saved = new ArrayList<>();
        EmpruntDAO empruntDAO = new EmpruntDAO() {
            @Override public void save(Emprunt e) throws SQLException { saved.add(e); }
            @Override public Emprunt findById(Object id) throws SQLException { return null; }
            @Override public List<Emprunt> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Emprunt e) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public List<Emprunt> findEnCours() throws SQLException { return Collections.emptyList(); }
            @Override public List<Emprunt> findByMembre(int membreId) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntService service = new EmpruntService(empruntDAO, livreDAO, membreDAO);

        // Act
        Emprunt result = service.emprunterLivre("ISBN-A", 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, saved.size());
        assertTrue(livreUpdated[0], "Le livre doit être marqué indisponible via update");
        assertFalse(livre.isDisponible(), "L'objet livre retourné doit être mis à jour localement");
        assertEquals("ISBN-A", result.getLivre().getIsbn());
    }

    @Test
    public void testEmprunterLivreWhenLivreMissingThrows() {
        LivreDAO livreDAO = new LivreDAO() {
            @Override public void save(Livre livre) throws SQLException { }
            @Override public Livre findById(Object isbn) throws SQLException { return null; }
            @Override public List<Livre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Livre l) throws SQLException { }
            @Override public void delete(Object isbn) throws SQLException { }
            @Override public List<Livre> findByAuteur(String auteur) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findByTitre(String titre) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findDisponibles() throws SQLException { return Collections.emptyList(); }
        };

        EmpruntDAO empruntDAO = new EmpruntDAO() {
            @Override public void save(Emprunt e) throws SQLException { }
            @Override public Emprunt findById(Object id) throws SQLException { return null; }
            @Override public List<Emprunt> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Emprunt e) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public List<Emprunt> findEnCours() throws SQLException { return Collections.emptyList(); }
            @Override public List<Emprunt> findByMembre(int membreId) throws SQLException { return Collections.emptyList(); }
        };

        MembreDAO membreDAO = new MembreDAO() {
            @Override public void save(Membre m) throws SQLException { }
            @Override public Membre findById(Object id) throws SQLException { return null; }
            @Override public List<Membre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Membre m) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public Membre findByEmail(String email) throws SQLException { return null; }
            @Override public List<Membre> findActifs() throws SQLException { return Collections.emptyList(); }
            @Override public List<Membre> findByNom(String nom) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntService service = new EmpruntService(empruntDAO, livreDAO, membreDAO);

        assertThrows(LivreIndisponibleException.class, () -> service.emprunterLivre("NO-LIVRE", 1));
    }

    @Test
    public void testEmprunterLivreWhenLivreNotDisponibleThrows() throws Exception {
        Livre livre = new Livre("ISBN-X","Titre","Auteur");
        livre.setDisponible(false);

        LivreDAO livreDAO = new LivreDAO() {
            @Override public void save(Livre l) throws SQLException { }
            @Override public Livre findById(Object isbn) throws SQLException { return livre; }
            @Override public List<Livre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Livre l) throws SQLException { }
            @Override public void delete(Object isbn) throws SQLException { }
            @Override public List<Livre> findByAuteur(String auteur) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findByTitre(String titre) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findDisponibles() throws SQLException { return Collections.emptyList(); }
        };

        MembreDAO membreDAO = new MembreDAO() {
            @Override public void save(Membre m) throws SQLException { }
            @Override public Membre findById(Object id) throws SQLException { Membre mm = new Membre(); mm.setIdMembre(1); mm.setActif(true); return mm; }
            @Override public List<Membre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Membre m) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public Membre findByEmail(String email) throws SQLException { return null; }
            @Override public List<Membre> findActifs() throws SQLException { return Collections.emptyList(); }
            @Override public List<Membre> findByNom(String nom) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntDAO empruntDAO = new EmpruntDAO() {
            @Override public void save(Emprunt e) throws SQLException { }
            @Override public Emprunt findById(Object id) throws SQLException { return null; }
            @Override public List<Emprunt> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Emprunt e) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public List<Emprunt> findEnCours() throws SQLException { return Collections.emptyList(); }
            @Override public List<Emprunt> findByMembre(int membreId) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntService service = new EmpruntService(empruntDAO, livreDAO, membreDAO);
        assertThrows(LivreIndisponibleException.class, () -> service.emprunterLivre("ISBN-X", 1));
    }

    @Test
    public void testEmprunterLivreMembreInactifThrows() throws Exception {
        Livre livre = new Livre("ISBN-B","Titre B","Auteur B");
        livre.setDisponible(true);

        LivreDAO livreDAO = new LivreDAO() {
            @Override public void save(Livre l) throws SQLException { }
            @Override public Livre findById(Object isbn) throws SQLException { return livre; }
            @Override public List<Livre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Livre l) throws SQLException { }
            @Override public void delete(Object isbn) throws SQLException { }
            @Override public List<Livre> findByAuteur(String auteur) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findByTitre(String titre) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findDisponibles() throws SQLException { return Collections.emptyList(); }
        };

        MembreDAO membreDAO = new MembreDAO() {
            @Override public void save(Membre m) throws SQLException { }
            @Override public Membre findById(Object id) throws SQLException { Membre mm = new Membre(); mm.setIdMembre(2); mm.setActif(false); return mm; }
            @Override public List<Membre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Membre m) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public Membre findByEmail(String email) throws SQLException { return null; }
            @Override public List<Membre> findActifs() throws SQLException { return Collections.emptyList(); }
            @Override public List<Membre> findByNom(String nom) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntDAO empruntDAO = new EmpruntDAO() {
            @Override public void save(Emprunt e) throws SQLException { }
            @Override public Emprunt findById(Object id) throws SQLException { return null; }
            @Override public List<Emprunt> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Emprunt e) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public List<Emprunt> findEnCours() throws SQLException { return Collections.emptyList(); }
            @Override public List<Emprunt> findByMembre(int membreId) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntService service = new EmpruntService(empruntDAO, livreDAO, membreDAO);
        assertThrows(MembreInactifException.class, () -> service.emprunterLivre("ISBN-B", 2));
    }

    @Test
    public void testEmprunterLivreLimiteDepasseeThrows() throws Exception {
        Livre livre = new Livre("ISBN-C","Titre C","Auteur C");
        livre.setDisponible(true);

        LivreDAO livreDAO = new LivreDAO() {
            @Override public void save(Livre l) throws SQLException { }
            @Override public Livre findById(Object isbn) throws SQLException { return livre; }
            @Override public List<Livre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Livre l) throws SQLException { }
            @Override public void delete(Object isbn) throws SQLException { }
            @Override public List<Livre> findByAuteur(String auteur) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findByTitre(String titre) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findDisponibles() throws SQLException { return Collections.emptyList(); }
        };

        EmpruntDAO empruntDAO = new EmpruntDAO() {
            @Override public void save(Emprunt e) throws SQLException { }
            @Override public Emprunt findById(Object id) throws SQLException { return null; }
            @Override public List<Emprunt> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Emprunt e) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public List<Emprunt> findEnCours() throws SQLException {
                Emprunt e1 = new Emprunt(); e1.setMembre(new Membre()); e1.getMembre().setIdMembre(3);
                Emprunt e2 = new Emprunt(); e2.setMembre(new Membre()); e2.getMembre().setIdMembre(3);
                Emprunt e3 = new Emprunt(); e3.setMembre(new Membre()); e3.getMembre().setIdMembre(3);
                return List.of(e1, e2, e3);
            }
            @Override public List<Emprunt> findByMembre(int membreId) throws SQLException { return Collections.emptyList(); }
        };

        MembreDAO membreDAO = new MembreDAO() {
            @Override public void save(Membre m) throws SQLException { }
            @Override public Membre findById(Object id) throws SQLException { Membre mm = new Membre(); mm.setIdMembre(3); mm.setActif(true); return mm; }
            @Override public List<Membre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Membre m) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public Membre findByEmail(String email) throws SQLException { return null; }
            @Override public List<Membre> findActifs() throws SQLException { return Collections.emptyList(); }
            @Override public List<Membre> findByNom(String nom) throws SQLException { return Collections.emptyList(); }
        };

        EmpruntService service = new EmpruntService(empruntDAO, livreDAO, membreDAO);
        assertThrows(LimiteEmpruntDepasseeException.class, () -> service.emprunterLivre("ISBN-C", 3));
    }

    @Test
    public void testRetournerLivreSuccessUpdatesEmpruntAndLivre() throws Exception {
        // Arrange
        Livre livre = new Livre("ISBN-R","Titre R","Auteur R");
        livre.setDisponible(false);

        Membre membre = new Membre("Nom","Prenom","a@b.com");
        membre.setIdMembre(5);
        membre.setActif(true);

        Emprunt emprunt = new Emprunt(livre, membre);
        emprunt.setIdEmprunt(42);

        final boolean[] empruntUpdated = {false};
        final boolean[] livreUpdated = {false};

        EmpruntDAO empruntDAO = new EmpruntDAO() {
            @Override public void save(Emprunt e) throws SQLException { }
            @Override public Emprunt findById(Object id) throws SQLException { return emprunt; }
            @Override public List<Emprunt> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Emprunt e) throws SQLException { empruntUpdated[0] = e.getDateRetourEffective() != null; }
            @Override public void delete(Object id) throws SQLException { }
            @Override public List<Emprunt> findEnCours() throws SQLException { return Collections.emptyList(); }
            @Override public List<Emprunt> findByMembre(int membreId) throws SQLException { return List.of(emprunt); }
        };

        LivreDAO livreDAO = new LivreDAO() {
            @Override public void save(Livre l) throws SQLException { }
            @Override public Livre findById(Object isbn) throws SQLException { return livre; }
            @Override public List<Livre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Livre l) throws SQLException { livreUpdated[0] = l.isDisponible(); }
            @Override public void delete(Object isbn) throws SQLException { }
            @Override public List<Livre> findByAuteur(String auteur) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findByTitre(String titre) throws SQLException { return Collections.emptyList(); }
            @Override public List<Livre> findDisponibles() throws SQLException { return Collections.emptyList(); }
        };

        EmpruntService service = new EmpruntService(empruntDAO, livreDAO, new MembreDAO() {
            @Override public void save(Membre m) throws SQLException { }
            @Override public Membre findById(Object id) throws SQLException { return membre; }
            @Override public List<Membre> findAll() throws SQLException { return Collections.emptyList(); }
            @Override public void update(Membre m) throws SQLException { }
            @Override public void delete(Object id) throws SQLException { }
            @Override public Membre findByEmail(String email) throws SQLException { return null; }
            @Override public List<Membre> findActifs() throws SQLException { return Collections.emptyList(); }
            @Override public List<Membre> findByNom(String nom) throws SQLException { return Collections.emptyList(); }
        });

        // Act
        service.retournerLivre(42);

        // Assert
        assertTrue(empruntUpdated[0], "L'emprunt doit être mis à jour avec dateRetourEffective");
        assertTrue(livreUpdated[0], "Le livre doit être rendu disponible via update");
        assertNotNull(emprunt.getDateRetourEffective());
        assertTrue(livre.isDisponible());
    }
}
