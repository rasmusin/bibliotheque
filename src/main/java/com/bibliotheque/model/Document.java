package com.bibliotheque.model;

public abstract class Document {
    protected String id;
    protected String titre;
    protected boolean disponible;
    
    public Document(String id, String titre) {
        this.id = id;
        this.titre = titre;
        this.disponible = true;
    }
    
    public Document() {
        this.disponible = true;
    }
    
    // Méthode abstraite - chaque type de document calcule différemment
    public abstract double calculerPenaliteRetard(int joursRetard);
    
    // Méthode abstraite - durée d'emprunt variable
    public abstract int getDureeEmpruntJours();
    
    // Méthode concrète commune
    public String getStatut() {
        return disponible ? "Disponible" : "Emprunté";
    }
    
    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}