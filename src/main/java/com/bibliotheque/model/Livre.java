package com.bibliotheque.model;

import java.time.LocalDateTime;

/**
 * Classe représentant un livre dans la bibliothèque
 */
public class Livre extends Document implements Empruntable {
    private String auteur;
    private String isbn;
    private int anneePublication;
    private double prix;
    private LocalDateTime dateCreation;
    
    private static final double PENALITE_PAR_JOUR = 0.50;
    private static final int DUREE_EMPRUNT = 14;
    private static final int LIMITE_EMPRUNT = 3;
    
    // Constructeurs
    public Livre() {
        super();
        this.disponible = true;
    }
    
    public Livre(String isbn, String titre, String auteur) {
        super(isbn, titre);
        this.isbn = isbn;
        this.auteur = auteur;
        this.disponible = true;
    }
    
    public Livre(String isbn, String titre, String auteur, int anneePublication, double prix) {
        super(isbn, titre);
        this.isbn = isbn;
        this.auteur = auteur;
        this.anneePublication = anneePublication;
        this.prix = prix;
        this.disponible = true;
    }
    
    // Implémentation de Empruntable
    @Override
    public boolean peutEtreEmprunte() {
        return disponible;
    }
    
    @Override
    public void emprunter() {
        if (!peutEtreEmprunte()) {
            throw new IllegalStateException("Ce livre n'est pas disponible");
        }
        this.disponible = false;
    }
    
    @Override
    public void retourner() {
        this.disponible = true;
    }
    
    @Override
    public int getLimiteEmprunt() {
        return LIMITE_EMPRUNT;
    }
    
    @Override
    public double calculerPenaliteRetard(int joursRetard) {
        return joursRetard * PENALITE_PAR_JOUR;
    }
    
    @Override
    public int getDureeEmpruntJours() {
        return DUREE_EMPRUNT;
    }
    
    
    // Getters et setters
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getAuteur() {
        return auteur;
    }
    
    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }
    
    public int getAnneePublication() {
        return anneePublication;
    }
    
    public void setAnneePublication(int anneePublication) {
        this.anneePublication = anneePublication;
    }
    
    public double getPrix() {
        return prix;
    }
    
    public void setPrix(double prix) {
        this.prix = prix;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s, %d) - ISBN: %s",
            titre, auteur, anneePublication, isbn);
    }
}