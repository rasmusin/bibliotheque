package com.bibliotheque.controller;

import com.bibliotheque.model.*;
import com.bibliotheque.service.*;
import com.bibliotheque.exception.*;
import com.bibliotheque.util.StringValidator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    
    // Services
    private BibliothequeService bibliothequeService;
    private EmpruntService empruntService;
    
    // Onglet Livres
    @FXML private TextField rechercheField;
    @FXML private TableView<Livre> livresTable;
    @FXML private TableColumn<Livre, String> isbnColumn;
    @FXML private TableColumn<Livre, String> titreColumn;
    @FXML private TableColumn<Livre, String> auteurColumn;
    @FXML private TableColumn<Livre, Integer> anneeColumn;
    @FXML private TableColumn<Livre, LocalDateTime> dateCreationColumn;
    @FXML private TableColumn<Livre, Double> prixColumn;
    @FXML private TableColumn<Livre, Boolean> disponibleColumn;
    
    // Onglet Membres
    @FXML private TextField rechercheMembreField;
    @FXML private CheckBox actifsSeulementCheck;
    @FXML private TableView<Membre> membresTable;
    @FXML private TableColumn<Membre, Integer> idMembreColumn;
    @FXML private TableColumn<Membre, String> nomMembreColumn;
    @FXML private TableColumn<Membre, String> prenomMembreColumn;
    @FXML private TableColumn<Membre, String> emailColumn;
    @FXML private TableColumn<Membre, LocalDate> inscriptionColumn;
    @FXML private TableColumn<Membre, Boolean> actifColumn;
    
    // Onglet Emprunts
    @FXML private TextField isbnEmpruntField;
    @FXML private TextField membreIdField;
    @FXML private TableView<Emprunt> empruntsTable;
    @FXML private TableColumn<Emprunt, Integer> idEmpruntColumn;
    @FXML private TableColumn<Emprunt, String> livreEmpruntColumn;
    @FXML private TableColumn<Emprunt, String> membreEmpruntColumn;
    @FXML private TableColumn<Emprunt, LocalDate> dateEmpruntColumn;
    @FXML private TableColumn<Emprunt, LocalDate> dateRetourPrevueColumn;
    @FXML private TableColumn<Emprunt, LocalDate> dateRetourEffectiveColumn;
    @FXML private TableColumn<Emprunt, Double> penaliteColumn;
    
    // Barre de statut
    @FXML private Label statusLabel;
    @FXML private Label statsLabel;
    @FXML private Label versionLabel;
    @FXML private TabPane tabPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les services
        if (bibliothequeService == null) {
            bibliothequeService = new BibliothequeService();
        }
        if (empruntService == null) {
            empruntService = new EmpruntService();
        }
        
        // Configurer les colonnes des tables
        configurerTableLivres();
        configurerTableMembres();
        configurerTableEmprunts();
        
        // Charger les données initiales
        chargerDonnees();
        
        updateStatusBar();
    }

    // Setters pour injection (utiles pour les tests)
    public void setBibliothequeService(BibliothequeService bibliothequeService) {
        this.bibliothequeService = bibliothequeService;
    }

    public void setEmpruntService(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }
    
    private void configurerTableLivres() {
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        auteurColumn.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        anneeColumn.setCellValueFactory(new PropertyValueFactory<>("anneePublication"));
        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateCreationColumn.setCellFactory(col -> new TableCell<Livre, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dtf));
                }
            }
        });
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        disponibleColumn.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        
        // Formater la colonne disponible
        disponibleColumn.setCellFactory(col -> new TableCell<Livre, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✓ Oui" : "✗ Non");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }
    
    private void configurerTableMembres() {
        idMembreColumn.setCellValueFactory(new PropertyValueFactory<>("idMembre"));
        nomMembreColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomMembreColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        inscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        
        // Formater la colonne actif
        actifColumn.setCellFactory(col -> new TableCell<Membre, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Actif" : "Inactif");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: gray;");
                }
            }
        });
    }
    
    private void configurerTableEmprunts() {
        idEmpruntColumn.setCellValueFactory(new PropertyValueFactory<>("idEmprunt"));
        
        // Colonne personnalisée pour afficher le titre du livre
        livreEmpruntColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getLivre().getTitre()));
        
        // Colonne personnalisée pour afficher le nom du membre
        membreEmpruntColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMembre().getNomComplet()));
        
        dateEmpruntColumn.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        dateRetourPrevueColumn.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        dateRetourEffectiveColumn.setCellValueFactory(new PropertyValueFactory<>("dateRetourEffective"));
        penaliteColumn.setCellValueFactory(new PropertyValueFactory<>("penalite"));
        
        // Formater la colonne pénalité
        penaliteColumn.setCellFactory(col -> new TableCell<Emprunt, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                    setStyle(item > 0 ? "-fx-text-fill: red;" : "");
                }
            }
        });
    }
    
    private void chargerDonnees() {
        try {
            // Charger les livres
            List<Livre> livres = bibliothequeService.getAllLivres();
            livresTable.setItems(FXCollections.observableArrayList(livres));
            
            // Charger les membres
            List<Membre> membres = bibliothequeService.getAllMembres();
            membresTable.setItems(FXCollections.observableArrayList(membres));
            
            // Charger les emprunts
            List<Emprunt> emprunts = empruntService.getAllEmprunts();
            empruntsTable.setItems(FXCollections.observableArrayList(emprunts));
            
        } catch (SQLException e) {
            showError("Erreur de chargement", 
                "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    // === GESTION DES LIVRES ===
    
    @FXML
    private void handleRechercheLivre() {
        String critere = rechercheField.getText().trim();
        
        if (critere.isEmpty()) {
            handleAfficherTousLivres();
            return;
        }
        
        try {
            List<Livre> resultats = bibliothequeService.rechercherLivres(critere);
            livresTable.setItems(FXCollections.observableArrayList(resultats));
            statusLabel.setText(resultats.size() + " livre(s) trouvé(s)");
        } catch (SQLException e) {
            showError("Erreur de recherche", e.getMessage());
        }
    }
    
    @FXML
    private void handleAfficherTousLivres() {
        try {
            List<Livre> livres = bibliothequeService.getAllLivres();
            livresTable.setItems(FXCollections.observableArrayList(livres));
            rechercheField.clear();
            statusLabel.setText("Tous les livres affichés");
        } catch (SQLException e) {
            showError("Erreur", e.getMessage());
        }
    }
    
    @FXML
    private void handleAjouterLivre() {
        Dialog<Livre> dialog = creerDialogLivre(null);
        Optional<Livre> result = dialog.showAndWait();
        
        result.ifPresent(livre -> {
            try {
                bibliothequeService.ajouterLivre(livre);
                showInfo("Succès", "Livre ajouté avec succès");
                handleAfficherTousLivres();
            } catch (SQLException e) {
                showError("Erreur", "Impossible d'ajouter le livre: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleModifierLivre() {
        Livre selected = livresTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un livre à modifier");
            return;
        }
        
        Dialog<Livre> dialog = creerDialogLivre(selected);
        Optional<Livre> result = dialog.showAndWait();
        
        result.ifPresent(livre -> {
            try {
                bibliothequeService.modifierLivre(livre);
                showInfo("Succès", "Livre modifié avec succès");
                handleAfficherTousLivres();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de modifier le livre: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleSupprimerLivre() {
        Livre selected = livresTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un livre à supprimer");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le livre ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitre());
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bibliothequeService.supprimerLivre(selected.getIsbn());
                showInfo("Succès", "Livre supprimé avec succès");
                handleAfficherTousLivres();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer le livre: " + e.getMessage());
            }
        }
    }
    
    // === GESTION DES MEMBRES ===
    
    @FXML
    private void handleRechercherMembre() {
        String recherche = rechercheMembreField.getText().trim();
        try {
            if (recherche.isEmpty()) {
                handleAfficherTousMembres();
            } else {
                List<Membre> membres = bibliothequeService.rechercherMembreParNom(recherche);
                membresTable.setItems(FXCollections.observableArrayList(membres));
                statusLabel.setText("Recherche complétée: " + membres.size() + " résultat(s)");
            }
        } catch (SQLException e) {
            showError("Erreur de recherche", e.getMessage());
        }
    }
    
    @FXML
    private void handleAfficherTousMembres() {
        try {
            List<Membre> membres = bibliothequeService.getAllMembres();
            membresTable.setItems(FXCollections.observableArrayList(membres));
            rechercheMembreField.clear();
            statusLabel.setText("Tous les membres affichés");
        } catch (SQLException e) {
            showError("Erreur", e.getMessage());
        }
    }
    
    @FXML
    private void handleAjouterMembre() {
        Dialog<Membre> dialog = creerDialogMembre(null);
        Optional<Membre> result = dialog.showAndWait();
        
        result.ifPresent(membre -> {
            try {
                bibliothequeService.ajouterMembre(membre);
                showInfo("Succès", "Membre ajouté avec succès");
                handleAfficherTousMembres();
            } catch (SQLException e) {
                showError("Erreur", "Impossible d'ajouter le membre: " + e.getMessage());
            }
        });
    }
    
    // === GESTION DES EMPRUNTS ===
    
    @FXML
    private void handleEmprunter() {
        String isbn = isbnEmpruntField.getText().trim();
        String membreIdStr = membreIdField.getText().trim();
        
        if (isbn.isEmpty() || membreIdStr.isEmpty()) {
            showWarning("Champs incomplets", "Veuillez remplir tous les champs");
            return;
        }
        
        try {
            int idMembre = Integer.parseInt(membreIdStr);
            Emprunt emprunt = empruntService.emprunterLivre(isbn, idMembre);
            
            showInfo("Succès", 
                "Emprunt enregistré avec succès\nDate de retour prévue: " + 
                emprunt.getDateRetourPrevue());
            
            isbnEmpruntField.clear();
            membreIdField.clear();
            chargerDonnees();
            
        } catch (NumberFormatException e) {
            showError("Erreur", "L'ID membre doit être un nombre");
        } catch (LivreIndisponibleException e) {
            showWarning("Livre indisponible", e.getMessage());
        } catch (MembreInactifException e) {
            showWarning("Membre inactif", e.getMessage());
        } catch (LimiteEmpruntDepasseeException e) {
            showWarning("Limite dépassée", e.getMessage());
        } catch (SQLException e) {
            showError("Erreur base de données", e.getMessage());
        }
    }
    
    @FXML
    private void handleRetourner() {
        Emprunt selected = empruntsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un emprunt");
            return;
        }
        
        if (selected.getDateRetourEffective() != null) {
            showWarning("Déjà retourné", "Ce livre a déjà été retourné");
            return;
        }
        
        try {
            empruntService.retournerLivre(selected.getIdEmprunt());
            
            double penalite = selected.calculerPenalite();
            String message = penalite > 0 ? 
                String.format("Livre retourné\nPénalité: %.2f €", penalite) :
                "Livre retourné sans pénalité";
            
            showInfo("Succès", message);
            chargerDonnees();
            
        } catch (SQLException e) {
            showError("Erreur", e.getMessage());
        }
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    private Dialog<Livre> creerDialogLivre(Livre livre) {
        Dialog<Livre> dialog = new Dialog<>();
        dialog.setTitle(livre == null ? "Ajouter un livre" : "Modifier un livre");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField isbnField = new TextField();
        TextField titreField = new TextField();
        TextField auteurField = new TextField();
        TextField anneeField = new TextField();
        TextField prixField = new TextField();
        
        if (livre != null) {
            isbnField.setText(livre.getIsbn());
            isbnField.setDisable(true); // ISBN non modifiable
            titreField.setText(livre.getTitre());
            auteurField.setText(livre.getAuteur());
            anneeField.setText(String.valueOf(livre.getAnneePublication()));
            prixField.setText(String.valueOf(livre.getPrix()));
        }
        
        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Titre:"), 0, 1);
        grid.add(titreField, 1, 1);
        grid.add(new Label("Auteur:"), 0, 2);
        grid.add(auteurField, 1, 2);
        grid.add(new Label("Année:"), 0, 3);
        grid.add(anneeField, 1, 3);
        grid.add(new Label("Prix:"), 0, 4);
        grid.add(prixField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    Livre nouveauLivre = livre == null ? new Livre() : livre;
                    
                    nouveauLivre.setIsbn(StringValidator.validerISBN(isbnField.getText()));
                    nouveauLivre.setTitre(titreField.getText().trim());
                    nouveauLivre.setAuteur(auteurField.getText().trim());
                    nouveauLivre.setAnneePublication(Integer.parseInt(anneeField.getText()));
                    nouveauLivre.setPrix(Double.parseDouble(prixField.getText()));
                    
                    return nouveauLivre;
                } catch (ValidationException e) {
                    showError("Validation", e.getMessage());
                    return null;
                } catch (NumberFormatException e) {
                    showError("Format invalide", "Vérifiez l'année et le prix");
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    private Dialog<Membre> creerDialogMembre(Membre membre) {
        Dialog<Membre> dialog = new Dialog<>();
        dialog.setTitle(membre == null ? "Ajouter un membre" : "Modifier un membre");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField nomField = new TextField();
        TextField prenomField = new TextField();
        TextField emailField = new TextField();
        CheckBox actifCheck = new CheckBox();
        
        if (membre != null) {
            nomField.setText(membre.getNom());
            prenomField.setText(membre.getPrenom());
            emailField.setText(membre.getEmail());
            actifCheck.setSelected(membre.isActif());
        } else {
            actifCheck.setSelected(true);
        }
        
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Actif:"), 0, 3);
        grid.add(actifCheck, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    Membre nouveauMembre = membre == null ? new Membre() : membre;
                    
                    nouveauMembre.setNom(nomField.getText().trim());
                    nouveauMembre.setPrenom(prenomField.getText().trim());
                    nouveauMembre.setEmail(StringValidator.validerEmail(emailField.getText()));
                    nouveauMembre.setActif(actifCheck.isSelected());
                    
                    return nouveauMembre;
                } catch (ValidationException e) {
                    showError("Validation", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    private void updateStatusBar() {
        try {
            int nbLivres = bibliothequeService.countLivres();
            int nbMembres = bibliothequeService.countMembres();
            int nbEmprunts = empruntService.countEmpruntsEnCours();
            
            statsLabel.setText(String.format("%d livres | %d membres | %d emprunts en cours",
                nbLivres, nbMembres, nbEmprunts));
        } catch (SQLException e) {
            statsLabel.setText("Erreur de chargement des statistiques");
        }
    }
    
    @FXML
    private void handleRefresh() {
        chargerDonnees();
        statusLabel.setText("Données rechargées");
    }
    
    @FXML
    private void handleQuit() {
        System.exit(0);
    }
    
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("Système de Gestion de Bibliothèque");
        alert.setContentText("Version 1.0\nProjet Java - POO & JavaFX\n© 2024");
        alert.showAndWait();
    }
    
    // Méthodes d'alerte
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Erreur");
        alert.setContentText(content);
        alert.showAndWait();
    }
}