package com.bibliotheque.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Membre extends Personne {
    private int idMembre;
    private LocalDate dateInscription;
    private boolean actif;
    private List<Emprunt> historique;
    
    public Membre(String nom, String prenom, String email) {
        super(nom, prenom, email);
        this.dateInscription = LocalDate.now();
        this.actif = true;
        this.historique = new ArrayList<>();
    }
    
    public Membre() {
        super();
        this.dateInscription = LocalDate.now();
        this.actif = true;
        this.historique = new ArrayList<>();
    }
    
    @Override
    public String getTypePersonne() {
        return "Membre";
    }
    
    public int getNombreEmpruntsEnCours() {
        return (int) historique.stream()
            .filter(e -> e.getDateRetourEffective() == null)
            .count();
    }
    
    // Getters et setters spécifiques
    public int getIdMembre() { return idMembre; }
    public void setIdMembre(int idMembre) { this.idMembre = idMembre; }
    
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { 
        this.dateInscription = dateInscription; 
    }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    
    public List<Emprunt> getHistorique() { return historique; }
    public void setHistorique(List<Emprunt> historique) { 
        this.historique = historique; 
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", 
            getTypePersonne(), getNomComplet(), email);
    }
}