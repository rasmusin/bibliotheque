package com.bibliotheque.exception;

public class MembreInactifException extends Exception {
    private int idMembre;
    
    public MembreInactifException(int idMembre) {
        super("Le membre avec l'ID " + idMembre + " n'est pas actif");
        this.idMembre = idMembre;
    }
    
    public MembreInactifException(int idMembre, String message) {
        super(message);
        this.idMembre = idMembre;
    }
    
    public int getIdMembre() {
        return idMembre;
    }
}