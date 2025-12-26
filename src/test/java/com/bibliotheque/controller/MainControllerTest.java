package com.bibliotheque.controller;

import com.bibliotheque.model.Livre;
import com.bibliotheque.service.BibliothequeService;
import com.bibliotheque.service.EmpruntService;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import javafx.fxml.FXMLLoader;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainControllerTest extends ApplicationTest {
    private MainController controller;

    @Start
    public void start(Stage stage) throws Exception {
        // Create controller instance and inject stub services
        controller = new MainController();
        controller.setBibliothequeService(new BibliothequeService() {
            @Override
            public List<Livre> getAllLivres() throws SQLException {
                return List.of(new Livre("ISBN-TST","Un titre de test","Auteur"));
            }

            @Override
            public List<Livre> rechercherLivres(String critere) throws SQLException {
                return List.of(new Livre("ISBN-TST","Un titre de test","Auteur"));
            }

            // Provide minimal stub implementations for other abstract methods used by controller
            @Override
            public List<com.bibliotheque.model.Membre> getAllMembres() throws SQLException { return List.of(); }
            @Override
            public void ajouterLivre(Livre livre) throws SQLException {}
            @Override
            public void modifierLivre(Livre livre) throws SQLException {}
            @Override
            public void supprimerLivre(String isbn) throws SQLException {}
            @Override
            public List<com.bibliotheque.model.Membre> rechercherMembreParNom(String nom) throws SQLException { return List.of(); }
            @Override
            public int countLivres() throws SQLException { return 1; }
            @Override
            public int countMembres() throws SQLException { return 0; }
        });

        controller.setEmpruntService(new EmpruntService() {
            @Override
            public List<com.bibliotheque.model.Emprunt> getAllEmprunts() throws SQLException { return List.of(); }
            @Override
            public int countEmpruntsEnCours() throws SQLException { return 0; }
        });

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bibliotheque/primary.fxml"));
        // FXML already declares fx:controller; provide our instance via controller factory
        loader.setControllerFactory(cls -> controller);
        Parent root = loader.load();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
        stage.toFront();
    }

    @Test
    public void testRechercheLivreUpdatesStatus() {
        // Type into the recherche field and click the Rechercher button
        TextField recherche = lookup("#rechercheField").query();
        clickOn(recherche).write("test");

        // Invoke the private handler to ensure the search runs in the FX thread
        try {
            java.lang.reflect.Method m = controller.getClass().getDeclaredMethod("handleRechercheLivre");
            m.setAccessible(true);
            interact(() -> {
                try {
                    m.invoke(controller);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Label status = lookup("#statusLabel").query();
        // Expect the stub to return 1 livre
        assertEquals("1 livre(s) trouvé(s)", status.getText());
    }
}
