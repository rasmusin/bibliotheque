package com.bibliotheque.exception;

public class LivreIndisponibleException extends Exception {
    private String isbn;
    
    public LivreIndisponibleException(String isbn) {
        super("Le livre avec ISBN " + isbn + " n'est pas disponible actuellement");
        this.isbn = isbn;
    }
    
    public LivreIndisponibleException(String isbn, String message) {
        super(message);
        this.isbn = isbn;
    }
    
    public String getIsbn() {
        return isbn;
    }
}