package com.bibliotheque.exception;

public class ValidationException extends Exception {
    private String champ;
    
    public ValidationException(String champ, String message) {
        super(message);
        this.champ = champ;
    }
    
    public String getChamp() {
        return champ;
    }
}