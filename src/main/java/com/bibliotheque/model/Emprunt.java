package com.bibliotheque.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Emprunt {
    private int idEmprunt;
    private Livre livre;
    private Membre membre;
    private LocalDate dateEmprunt;
    private LocalDate dateRetourPrevue;
    private LocalDate dateRetourEffective;
    private double penalite;
    
    private static final int DUREE_EMPRUNT_JOURS = 14;
    private static final double PENALITE_PAR_JOUR = 0.50;
    
    public Emprunt(Livre livre, Membre membre) {
        this.livre = livre;
        this.membre = membre;
        this.dateEmprunt = LocalDate.now();
        this.dateRetourPrevue = dateEmprunt.plusDays(DUREE_EMPRUNT_JOURS);
        this.penalite = 0.0;
    }
    
    public Emprunt() {}
    
    // Méthode métier : calcul de pénalité
    public double calculerPenalite() {
        if (dateRetourEffective == null) {
            // Emprunt en cours
            long joursRetard = ChronoUnit.DAYS.between(dateRetourPrevue, LocalDate.now());
            if (joursRetard > 0) {
                return joursRetard * PENALITE_PAR_JOUR;
            }
        } else {
            // Emprunt retourné
            long joursRetard = ChronoUnit.DAYS.between(dateRetourPrevue, dateRetourEffective);
            if (joursRetard > 0) {
                return joursRetard * PENALITE_PAR_JOUR;
            }
        }
        return 0.0;
    }
    
    public boolean estEnRetard() {
        if (dateRetourEffective != null) {
            return dateRetourEffective.isAfter(dateRetourPrevue);
        }
        return LocalDate.now().isAfter(dateRetourPrevue);
    }
    
    // Getters et setters
    public int getIdEmprunt() { return idEmprunt; }
    public void setIdEmprunt(int idEmprunt) { this.idEmprunt = idEmprunt; }
    
    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }
    
    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }
    
    public LocalDate getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDate dateEmprunt) { 
        this.dateEmprunt = dateEmprunt; 
    }
    
    public LocalDate getDateRetourPrevue() { return dateRetourPrevue; }
    public void setDateRetourPrevue(LocalDate dateRetourPrevue) { 
        this.dateRetourPrevue = dateRetourPrevue; 
    }
    
    public LocalDate getDateRetourEffective() { return dateRetourEffective; }
    public void setDateRetourEffective(LocalDate dateRetourEffective) { 
        this.dateRetourEffective = dateRetourEffective; 
    }
    
    public double getPenalite() { return penalite; }
    public void setPenalite(double penalite) { this.penalite = penalite; }
    
    @Override
    public String toString() {
        return String.format("Emprunt[%d: %s par %s, du %s au %s]",
            idEmprunt, livre.getTitre(), membre.getNomComplet(),
            dateEmprunt, dateRetourPrevue);
    }
}