package com.bibliotheque.model;

public abstract class Personne {
    protected String nom;
    protected String prenom;
    protected String email;
    
    public Personne(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
    
    public Personne() {}
    
    // Méthode abstraite
    public abstract String getTypePersonne();
    
    // Méthode concrète commune
    public String getNomComplet() {
        String p = prenom == null ? "" : prenom;
        String n = nom == null ? "" : nom.toUpperCase();
        return (p + " " + n).trim();
    }
    
    public String getInitiales() {
        char p = (prenom != null && !prenom.isEmpty()) ? prenom.charAt(0) : '?';
        char n = (nom != null && !nom.isEmpty()) ? nom.charAt(0) : '?';
        return (String.valueOf(p) + "." + String.valueOf(n)).toUpperCase();
    }
    
    // Getters et setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}