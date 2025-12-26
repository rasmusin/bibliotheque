package com.bibliotheque.factory;

import com.bibliotheque.model.*;

public class DocumentFactory {
    
    public static Document creerDocument(String type, String id, String titre) {
        switch (type.toUpperCase()) {
            case "LIVRE":
                Livre livre = new Livre();
                livre.setIsbn(id);
                livre.setTitre(titre);
                return livre;
                
            case "MAGAZINE":
                Magazine magazine = new Magazine();
                magazine.setId(id);
                magazine.setTitre(titre);
                return magazine;
                
            default:
                throw new IllegalArgumentException(
                    "Type de document non supporté: " + type);
        }
    }
    
    public static Document creerLivre(String isbn, String titre, String auteur) {
        return new Livre(isbn, titre, auteur);
    }
    
    public static Document creerMagazine(String id, String titre, int numero) {
        return new Magazine(id, titre, numero);
    }
}
