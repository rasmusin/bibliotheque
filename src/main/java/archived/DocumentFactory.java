package archived;

/* Archived copy of com.bibliotheque.factory.DocumentFactory */

import com.bibliotheque.model.*;

public class DocumentFactory {
    
    public static com.bibliotheque.model.Document creerDocument(String type, String id, String titre) {
        switch (type.toUpperCase()) {
            case "LIVRE":
                com.bibliotheque.model.Livre livre = new com.bibliotheque.model.Livre();
                livre.setIsbn(id);
                livre.setTitre(titre);
                return livre;
                
            case "MAGAZINE":
                com.bibliotheque.model.Magazine magazine = new com.bibliotheque.model.Magazine();
                magazine.setId(id);
                magazine.setTitre(titre);
                return magazine;
                
            default:
                throw new IllegalArgumentException(
                    "Type de document non supporté: " + type);
        }
    }
    
    public static com.bibliotheque.model.Document creerLivre(String isbn, String titre, String auteur) {
        return new com.bibliotheque.model.Livre(isbn, titre, auteur);
    }
    
    public static com.bibliotheque.model.Document creerMagazine(String id, String titre, int numero) {
        return new com.bibliotheque.model.Magazine(id, titre, numero);
    }
}
