package com.bibliotheque.service;

import com.bibliotheque.dao.impl.*;
import com.bibliotheque.dao.*;
import com.bibliotheque.model.*;
import com.bibliotheque.exception.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EmpruntService {
    private EmpruntDAO empruntDAO;
    private LivreDAO livreDAO;
    private MembreDAO membreDAO;
    
    private static final int LIMITE_EMPRUNTS = 3;
    
    public EmpruntService() {
        this.empruntDAO = new EmpruntDAOImpl();
        this.livreDAO = new LivreDAOImpl();
        this.membreDAO = new MembreDAOImpl();
    }

    // Constructeur pour injection (utile pour les tests)
    public EmpruntService(EmpruntDAO empruntDAO,
                          LivreDAO livreDAO,
                          MembreDAO membreDAO) {
        this.empruntDAO = empruntDAO;
        this.livreDAO = livreDAO;
        this.membreDAO = membreDAO;
    }
    
    public Emprunt emprunterLivre(String isbn, int idMembre) 
            throws LivreIndisponibleException, MembreInactifException, 
                   LimiteEmpruntDepasseeException, SQLException {
        
        // Vérification 1: Le livre existe-t-il ?
        Livre livre = livreDAO.findById(isbn);
        if (livre == null) {
            throw new LivreIndisponibleException(isbn, "Livre introuvable");
        }
        
        // Vérification 2: Le livre est-il disponible ?
        if (!livre.isDisponible()) {
            throw new LivreIndisponibleException(isbn);
        }
        
        // Vérification 3: Le membre existe-t-il et est-il actif ?
        Membre membre = membreDAO.findById(idMembre);
        if (membre == null) {
            throw new MembreInactifException(idMembre, "Membre introuvable");
        }
        if (!membre.isActif()) {
            throw new MembreInactifException(idMembre);
        }
        
        // Vérification 4: Le membre n'a pas dépassé sa limite ?
        int nbEmprunts = empruntDAO.findEnCours().stream()
            .filter(e -> e.getMembre() != null && e.getMembre().getIdMembre() == idMembre)
            .toList().size();
        if (nbEmprunts >= LIMITE_EMPRUNTS) {
            throw new LimiteEmpruntDepasseeException(nbEmprunts, LIMITE_EMPRUNTS);
        }
        
        try {
            // Créer l'emprunt
            Emprunt emprunt = new Emprunt(livre, membre);
            empruntDAO.save(emprunt);
            
            // Marquer le livre comme indisponible
            livre.setDisponible(false);
            livreDAO.update(livre);
            
            return emprunt;
            
        } catch (SQLException e) {
            // Rollback si erreur
            throw new SQLException("Erreur lors de l'enregistrement de l'emprunt: " 
                + e.getMessage(), e);
        }
    }
    
    public void retournerLivre(int idEmprunt) throws SQLException {
        try {
            Emprunt emprunt = empruntDAO.findById(idEmprunt);
            
            if (emprunt == null) {
                throw new IllegalArgumentException("Emprunt introuvable");
            }
            
            if (emprunt.getDateRetourEffective() != null) {
                throw new IllegalStateException("Ce livre a déjà été retourné");
            }
            
            // Enregistrer le retour
            emprunt.setDateRetourEffective(LocalDate.now());
            emprunt.setPenalite(emprunt.calculerPenalite());
            empruntDAO.update(emprunt);
            
            // Rendre le livre disponible
            Livre livre = emprunt.getLivre();
            livre.setDisponible(true);
            livreDAO.update(livre);
            
        } catch (SQLException e) {
            throw new SQLException("Erreur lors du retour du livre: " 
                + e.getMessage(), e);
        }
    }
    
    public List<Emprunt> getAllEmprunts() throws SQLException {
        return empruntDAO.findAll();
    }
    
    public List<Emprunt> getEmpruntsEnCours() throws SQLException {
        return empruntDAO.findEnCours();
    }
    
    public List<Emprunt> searchEmprunts(String critere) throws SQLException {
        List<Emprunt> emprunts = getAllEmprunts();
        return emprunts.stream()
            .filter(e -> e.getLivre().getTitre().toLowerCase().contains(critere.toLowerCase()) ||
                        e.getMembre().getNomComplet().toLowerCase().contains(critere.toLowerCase()))
            .toList();
    }
    
    public int countAllEmprunts() throws SQLException {
        return empruntDAO.findAll().size();
    }
    
    public int countEmpruntsEnCours() throws SQLException {
        return empruntDAO.findEnCours().size();
    }
    
    public List<Emprunt> getHistoriqueEmpruntsMembre(int idMembre) throws SQLException {
        return empruntDAO.findByMembre(idMembre);
    }
    
    public double getPenalitesTotales(int idMembre) throws SQLException {
        return empruntDAO.findByMembre(idMembre).stream()
            .mapToDouble(Emprunt::getPenalite)
            .sum();
    }
    
    public int countEmpruntsTotal(int idMembre) throws SQLException {
        return empruntDAO.findByMembre(idMembre).size();
    }
    
    public int countEmpruntsEnCoursTotal() throws SQLException {
        return (int) empruntDAO.findEnCours().stream().count();
    }

    public void modifierEmprunt(Emprunt emprunt) throws SQLException {
        empruntDAO.update(emprunt);
    }

    public void supprimerEmprunt(int idEmprunt) throws SQLException {
        // Si l'emprunt est en cours, rendre le livre disponible
        Emprunt emprunt = empruntDAO.findById(idEmprunt);
        if (emprunt == null) {
            throw new SQLException("Emprunt introuvable: " + idEmprunt);
        }
        if (emprunt.getDateRetourEffective() == null && emprunt.getLivre() != null) {
            Livre livre = emprunt.getLivre();
            livre.setDisponible(true);
            livreDAO.update(livre);
        }
        empruntDAO.delete(idEmprunt);
    }
}