package com.bibliotheque.model;

import java.time.LocalDate;

public class Magazine extends Document {
    private int numero;
    private LocalDate dateParution;
    private String editeur;
    
    private static final double PENALITE_PAR_JOUR = 0.30;
    private static final int DUREE_EMPRUNT = 7;
    
    public Magazine(String id, String titre, int numero) {
        super(id, titre);
        this.numero = numero;
    }
    
    public Magazine() {
        super();
    }
    
    @Override
    public double calculerPenaliteRetard(int joursRetard) {
        // Magazine : 0.30€ par jour (moins cher que livre)
        return joursRetard * PENALITE_PAR_JOUR;
    }
    
    @Override
    public int getDureeEmpruntJours() {
        return DUREE_EMPRUNT; // Plus court que livre
    }
    
    // Getters et setters
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    
    public LocalDate getDateParution() { return dateParution; }
    public void setDateParution(LocalDate dateParution) { 
        this.dateParution = dateParution; 
    }
    
    public String getEditeur() { return editeur; }
    public void setEditeur(String editeur) { this.editeur = editeur; }
    
    @Override
    public String toString() {
        return String.format("Magazine[%s: '%s' n°%d - %s]",
            id, titre, numero, getStatut());
    }
}