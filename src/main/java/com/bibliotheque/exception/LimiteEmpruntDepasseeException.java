package com.bibliotheque.exception;

public class LimiteEmpruntDepasseeException extends Exception {
    private int nombreEmpruntsActuels;
    private int limiteMaximale;
    
    public LimiteEmpruntDepasseeException(int actuels, int limite) {
        super(String.format("Limite d'emprunts dépassée: %d/%d emprunts en cours", 
            actuels, limite));
        this.nombreEmpruntsActuels = actuels;
        this.limiteMaximale = limite;
    }
    
    public int getNombreEmpruntsActuels() {
        return nombreEmpruntsActuels;
    }
    
    public int getLimiteMaximale() {
        return limiteMaximale;
    }
}