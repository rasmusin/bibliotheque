package com.bibliotheque.service;

import com.bibliotheque.dao.impl.*;
import com.bibliotheque.model.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion globale de la bibliothèque
 * Utilise les DAOs pour accéder aux données persistantes
 */
public class BibliothequeService {
    
    private LivreDAOImpl livreDAO;
    private MembreDAOImpl membreDAO;
    private EmpruntDAOImpl empruntDAO;
    
    public BibliothequeService() {
        this.livreDAO = new LivreDAOImpl();
        this.membreDAO = new MembreDAOImpl();
        this.empruntDAO = new EmpruntDAOImpl();
    }
    
    // ============ GESTION DES LIVRES ============
    
    public void ajouterLivre(Livre livre) throws SQLException {
        livreDAO.save(livre);
    }
    
    public void modifierLivre(Livre livre) throws SQLException {
        livreDAO.update(livre);
    }
    
    public void supprimerLivre(String isbn) throws SQLException {
        livreDAO.delete(isbn);
    }
    
    public Livre getLivreParISBN(String isbn) throws SQLException {
        return livreDAO.findById(isbn);
    }
    
    public List<Livre> getAllLivres() throws SQLException {
        return livreDAO.findAll();
    }
    
    public List<Livre> rechercherLivres(String critere) throws SQLException {
        String critereLower = critere.toLowerCase();
        return livreDAO.findAll().stream()
            .filter(livre -> 
                livre.getTitre().toLowerCase().contains(critereLower) ||
                livre.getAuteur().toLowerCase().contains(critereLower) ||
                livre.getIsbn().contains(critere)
            )
            .collect(Collectors.toList());
    }
    
    public List<Livre> getLivresDisponibles() throws SQLException {
        return livreDAO.findAll().stream()
            .filter(Livre::isDisponible)
            .collect(Collectors.toList());
    }
    
    public List<Livre> getLivresTriesParTitre() throws SQLException {
        return livreDAO.findAll().stream()
            .sorted(Comparator.comparing(Livre::getTitre))
            .collect(Collectors.toList());
    }
    
    public List<Livre> getLivresTriesParAnnee() throws SQLException {
        return livreDAO.findAll().stream()
            .sorted(Comparator.comparing(Livre::getAnneePublication).reversed())
            .collect(Collectors.toList());
    }
    
    public Map<String, List<Livre>> grouperParAuteur() throws SQLException {
        return livreDAO.findAll().stream()
            .collect(Collectors.groupingBy(Livre::getAuteur));
    }
    
    public Map<String, Long> getStatistiquesParAuteur() throws SQLException {
        return livreDAO.findAll().stream()
            .collect(Collectors.groupingBy(Livre::getAuteur, Collectors.counting()));
    }
    
    public double getPrixMoyen() throws SQLException {
        return livreDAO.findAll().stream()
            .mapToDouble(Livre::getPrix)
            .average()
            .orElse(0.0);
    }
    
    public int countLivres() throws SQLException {
        return livreDAO.findAll().size();
    }

    // Compatibilité - méthodes utilisées par les controllers
    public List<Membre> rechercherMembreParNom(String nom) throws SQLException {
        return rechercherMembres(nom, false);
    }

    public List<Livre> rechercherParTitre(String titre) throws SQLException {
        String crit = titre.toLowerCase();
        return livreDAO.findAll().stream()
            .filter(l -> l.getTitre() != null && l.getTitre().toLowerCase().contains(crit))
            .collect(Collectors.toList());
    }

    public List<Livre> rechercherParAuteur(String auteur) throws SQLException {
        String crit = auteur.toLowerCase();
        return livreDAO.findAll().stream()
            .filter(l -> l.getAuteur() != null && l.getAuteur().toLowerCase().contains(crit))
            .collect(Collectors.toList());
    }

    public int countLivresDisponibles() throws SQLException {
        return (int) livreDAO.findAll().stream().filter(Livre::isDisponible).count();
    }
    
    // ============ GESTION DES MEMBRES ============
    
    public void ajouterMembre(Membre membre) throws SQLException {
        membreDAO.save(membre);
    }
    
    public void modifierMembre(Membre membre) throws SQLException {
        membreDAO.update(membre);
    }
    
    public void supprimerMembre(int idMembre) throws SQLException {
        membreDAO.delete(idMembre);
    }
    
    public Membre getMembreParId(int idMembre) throws SQLException {
        return membreDAO.findById(idMembre);
    }
    
    public List<Membre> getAllMembres() throws SQLException {
        return membreDAO.findAll();
    }
    
    public List<Membre> getMembresActifs() throws SQLException {
        return membreDAO.findActifs();
    }
    
    public List<Membre> rechercherMembres(String critere, boolean actifsSeulementCheck) throws SQLException {
        List<Membre> membres = actifsSeulementCheck ? getMembresActifs() : getAllMembres();
        String critereLower = critere.toLowerCase();
        return membres.stream()
            .filter(m -> m.getNom().toLowerCase().contains(critereLower) ||
                        m.getPrenom().toLowerCase().contains(critereLower) ||
                        m.getEmail().toLowerCase().contains(critereLower))
            .collect(Collectors.toList());
    }
    
    public int countMembres() throws SQLException {
        return membreDAO.findAll().size();
    }
    
    public int countMembresActifs() throws SQLException {
        return membreDAO.findActifs().size();
    }
    
    // ============ RECHERCHE AVANCÉE ============
    
    public List<Livre> rechercherAvancee(String auteur, Integer anneeMin, 
                                         Integer anneeMax, Boolean disponible) throws SQLException {
        return livreDAO.findAll().stream()
            .filter(livre -> auteur == null || 
                livre.getAuteur().toLowerCase().contains(auteur.toLowerCase()))
            .filter(livre -> anneeMin == null || 
                livre.getAnneePublication() >= anneeMin)
            .filter(livre -> anneeMax == null || 
                livre.getAnneePublication() <= anneeMax)
            .filter(livre -> disponible == null || 
                livre.isDisponible() == disponible)
            .collect(Collectors.toList());
    }
}
