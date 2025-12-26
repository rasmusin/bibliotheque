package com.bibliotheque.model;

public interface Empruntable {
    /**
     * Vérifie si le document peut être emprunté
     */
    boolean peutEtreEmprunte();
    
    /**
     * Marque le document comme emprunté
     */
    void emprunter();
    
    /**
     * Marque le document comme retourné
     */
    void retourner();
    
    /**
     * Retourne le nombre maximum d'emprunts simultanés autorisés
     */
    int getLimiteEmprunt();
}