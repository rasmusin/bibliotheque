package com.bibliotheque.controller;

import com.bibliotheque.model.Livre;
import com.bibliotheque.service.BibliothequeService;
import com.bibliotheque.exception.ValidationException;
import com.bibliotheque.util.StringValidator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur dédié à la gestion des livres
 * Gère l'affichage, l'ajout, la modification et la suppression des livres
 */
public class LivreController implements Initializable {
    
    // Service métier
    private BibliothequeService bibliothequeService;
    
    // Composants FXML - Recherche
    @FXML
    private TextField rechercheField;
    
    @FXML
    private ComboBox<String> critereRechercheCombo;
    
    @FXML
    private Button btnRechercher;
    
    @FXML
    private Button btnTousLesLivres;
    
    // Composants FXML - Table
    @FXML
    private TableView<Livre> livresTable;
    
    @FXML
    private TableColumn<Livre, String> isbnColumn;
    
    @FXML
    private TableColumn<Livre, String> titreColumn;
    
    @FXML
    private TableColumn<Livre, String> auteurColumn;
    
    @FXML
    private TableColumn<Livre, Integer> anneeColumn;
    
    @FXML
    private TableColumn<Livre, Double> prixColumn;
    
    @FXML
    private TableColumn<Livre, Boolean> disponibleColumn;
    
    @FXML
    private TableColumn<Livre, java.time.LocalDateTime> dateCreationColumn;
    
    // Composants FXML - Boutons d'action
    @FXML
    private Button btnAjouter;
    
    @FXML
    private Button btnModifier;
    
    @FXML
    private Button btnSupprimer;
    
    @FXML
    private Button btnDetails;
    
    // Composants FXML - Statistiques
    @FXML
    private Label lblTotalLivres;
    
    @FXML
    private Label lblLivresDisponibles;
    
    @FXML
    private Label lblLivresEmpruntes;
    
    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service
        bibliothequeService = new BibliothequeService();
        
        // Configurer le ComboBox de recherche
        initializeComboBox();
        
        // Configurer les colonnes de la table
        configurerTableColumns();
        
        // Configurer les listeners
        configurerListeners();
        
        // Charger les données initiales
        chargerTousLesLivres();
        
        // Mettre à jour les statistiques
        updateStatistiques();
    }
    
    /**
     * Initialise le ComboBox des critères de recherche
     */
    private void initializeComboBox() {
        critereRechercheCombo.setItems(FXCollections.observableArrayList(
            "Tous",
            "Titre",
            "Auteur",
            "ISBN"
        ));
        critereRechercheCombo.getSelectionModel().selectFirst();
    }
    
    /**
     * Configure les colonnes de la TableView
     */
    private void configurerTableColumns() {
        // Liaison des colonnes avec les propriétés du modèle
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        auteurColumn.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        anneeColumn.setCellValueFactory(new PropertyValueFactory<>("anneePublication"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        disponibleColumn.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        
        // Formatter la colonne Prix
        prixColumn.setCellFactory(col -> new TableCell<Livre, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", prix));
                }
            }
        });
        
        // Formatter la colonne Disponible avec icônes et couleurs
        disponibleColumn.setCellFactory(col -> new TableCell<Livre, Boolean>() {
            @Override
            protected void updateItem(Boolean disponible, boolean empty) {
                super.updateItem(disponible, empty);
                if (empty || disponible == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (disponible) {
                        setText("✓ Disponible");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setText("✗ Emprunté");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Rendre les colonnes triables
        livresTable.getSortOrder().add(titreColumn);

        // Formatter la colonne date de création
        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateCreationColumn.setCellFactory(col -> new TableCell<Livre, java.time.LocalDateTime>() {
            @Override
            protected void updateItem(java.time.LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(dtf));
            }
        });
    }
    
    /**
     * Configure les listeners pour la sélection dans la table
     */
    private void configurerListeners() {
        // Activer/désactiver les boutons selon la sélection
        livresTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean isSelected = newValue != null;
                btnModifier.setDisable(!isSelected);
                btnSupprimer.setDisable(!isSelected);
                btnDetails.setDisable(!isSelected);
            }
        );
        
        // Désactiver les boutons par défaut
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnDetails.setDisable(true);
        
        // Listener pour la touche Entrée dans le champ de recherche
        rechercheField.setOnAction(event -> handleRechercher());
    }
    
    /**
     * Charge tous les livres dans la table
     */
    @FXML
    private void chargerTousLesLivres() {
        try {
            List<Livre> livres = bibliothequeService.getAllLivres();
            ObservableList<Livre> observableLivres = FXCollections.observableArrayList(livres);
            livresTable.setItems(observableLivres);
            
            // Réinitialiser le champ de recherche
            rechercheField.clear();
            
            // Afficher un message de statut
            showStatusMessage(livres.size() + " livre(s) affiché(s)", false);
            
            // Mettre à jour les statistiques
            updateStatistiques();
            
        } catch (SQLException e) {
            showError("Erreur de chargement", 
                "Impossible de charger les livres depuis la base de données", 
                e.getMessage());
        }
    }
    
    /**
     * Gère la recherche de livres
     */
    @FXML
    private void handleRechercher() {
        String critere = rechercheField.getText().trim();
        
        // Si le champ est vide, afficher tous les livres
        if (critere.isEmpty()) {
            chargerTousLesLivres();
            return;
        }
        
        try {
            List<Livre> resultats;
            String typeRecherche = critereRechercheCombo.getValue();
            
            // Recherche selon le critère sélectionné
            switch (typeRecherche) {
                case "Titre":
                    resultats = bibliothequeService.rechercherParTitre(critere);
                    break;
                case "Auteur":
                    resultats = bibliothequeService.rechercherParAuteur(critere);
                    break;
                case "ISBN":
                    Livre livre = bibliothequeService.getLivreParISBN(critere);
                    resultats = livre != null ? List.of(livre) : List.of();
                    break;
                default: // "Tous"
                    resultats = bibliothequeService.rechercherLivres(critere);
                    break;
            }
            
            // Afficher les résultats
            ObservableList<Livre> observableResultats = 
                FXCollections.observableArrayList(resultats);
            livresTable.setItems(observableResultats);
            
            // Message de statut
            if (resultats.isEmpty()) {
                showStatusMessage("Aucun livre trouvé pour \"" + critere + "\"", true);
            } else {
                showStatusMessage(resultats.size() + " livre(s) trouvé(s)", false);
            }
            
        } catch (SQLException e) {
            showError("Erreur de recherche", 
                "Une erreur est survenue lors de la recherche", 
                e.getMessage());
        }
    }
    
    /**
     * Gère l'ajout d'un nouveau livre
     */
    @FXML
    private void handleAjouter() {
        // Créer et afficher le dialogue
        Dialog<Livre> dialog = creerDialogueLivre(null, "Ajouter un livre");
        Optional<Livre> result = dialog.showAndWait();
        
        result.ifPresent(livre -> {
            try {
                // Sauvegarder le livre
                bibliothequeService.ajouterLivre(livre);
                
                // Afficher un message de succès
                showSuccess("Livre ajouté", 
                    "Le livre \"" + livre.getTitre() + "\" a été ajouté avec succès");
                
                // Recharger la table
                chargerTousLesLivres();
                
            } catch (SQLException e) {
                showError("Erreur d'ajout", 
                    "Impossible d'ajouter le livre", 
                    e.getMessage());
            } catch (IllegalArgumentException e) {
                showWarning("Livre existant", 
                    "Un livre avec cet ISBN existe déjà", 
                    e.getMessage());
            }
        });
    }
    
    /**
     * Gère la modification d'un livre existant
     */
    @FXML
    private void handleModifier() {
        Livre livreSelectionne = livresTable.getSelectionModel().getSelectedItem();
        
        if (livreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un livre à modifier", 
                null);
            return;
        }
        
        // Créer et afficher le dialogue avec les données existantes
        Dialog<Livre> dialog = creerDialogueLivre(livreSelectionne, "Modifier un livre");
        Optional<Livre> result = dialog.showAndWait();
        
        result.ifPresent(livreModifie -> {
            try {
                // Mettre à jour le livre
                bibliothequeService.modifierLivre(livreModifie);
                
                // Afficher un message de succès
                showSuccess("Livre modifié", 
                    "Le livre \"" + livreModifie.getTitre() + "\" a été modifié avec succès");
                
                // Recharger la table
                chargerTousLesLivres();
                
            } catch (SQLException e) {
                showError("Erreur de modification", 
                    "Impossible de modifier le livre", 
                    e.getMessage());
            }
        });
    }
    
    /**
     * Gère la suppression d'un livre
     */
    @FXML
    private void handleSupprimer() {
        Livre livreSelectionne = livresTable.getSelectionModel().getSelectedItem();
        
        if (livreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un livre à supprimer", 
                null);
            return;
        }
        
        // Vérifier si le livre est emprunté
        if (!livreSelectionne.isDisponible()) {
            showWarning("Livre emprunté", 
                "Impossible de supprimer un livre actuellement emprunté", 
                "Veuillez attendre le retour du livre avant de le supprimer.");
            return;
        }
        
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le livre ?");
        confirmation.setContentText(
            "Êtes-vous sûr de vouloir supprimer le livre :\n\n" +
            "Titre : " + livreSelectionne.getTitre() + "\n" +
            "Auteur : " + livreSelectionne.getAuteur() + "\n" +
            "ISBN : " + livreSelectionne.getIsbn() + "\n\n" +
            "Cette action est irréversible."
        );
        
        Optional<ButtonType> resultat = confirmation.showAndWait();
        
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                // Supprimer le livre
                bibliothequeService.supprimerLivre(livreSelectionne.getIsbn());
                
                // Afficher un message de succès
                showSuccess("Livre supprimé", 
                    "Le livre \"" + livreSelectionne.getTitre() + "\" a été supprimé avec succès");
                
                // Recharger la table
                chargerTousLesLivres();
                
            } catch (SQLException e) {
                showError("Erreur de suppression", 
                    "Impossible de supprimer le livre", 
                    e.getMessage());
            }
        }
    }
    
    /**
     * Affiche les détails d'un livre
     */
    @FXML
    private void handleDetails() {
        Livre livreSelectionne = livresTable.getSelectionModel().getSelectedItem();
        
        if (livreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un livre pour voir ses détails", 
                null);
            return;
        }
        
        // Créer un dialogue d'information
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Détails du livre");
        details.setHeaderText(livreSelectionne.getTitre());
        
        String contenu = String.format(
            "ISBN : %s\n" +
            "Titre : %s\n" +
            "Auteur : %s\n" +
            "Année de publication : %d\n" +
            "Prix : %.2f €\n" +
            "Statut : %s",
            livreSelectionne.getIsbn(),
            livreSelectionne.getTitre(),
            livreSelectionne.getAuteur(),
            livreSelectionne.getAnneePublication(),
            livreSelectionne.getPrix(),
            livreSelectionne.isDisponible() ? "Disponible" : "Emprunté"
        );
        
        // Ajouter la date de création si disponible
        if (livreSelectionne.getDateCreation() != null) {
            java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            contenu += "\nDate de création : " + livreSelectionne.getDateCreation().format(df);
        }
        details.setContentText(contenu);
        details.showAndWait();
    }
    
    /**
     * Crée un dialogue pour ajouter ou modifier un livre
     * 
     * @param livre Le livre à modifier (null pour un ajout)
     * @param titre Le titre du dialogue
     * @return Le dialogue configuré
     */
    private Dialog<Livre> creerDialogueLivre(Livre livre, String titre) {
        Dialog<Livre> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.setHeaderText(livre == null ? 
            "Saisissez les informations du nouveau livre" : 
            "Modifiez les informations du livre");
        
        // Boutons du dialogue
        ButtonType btnValider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        // Créer le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Champs du formulaire
        TextField isbnField = new TextField();
        isbnField.setPromptText("978-X-XXXX-XXXX-X");
        
        TextField titreField = new TextField();
        titreField.setPromptText("Titre du livre");
        
        TextField auteurField = new TextField();
        auteurField.setPromptText("Nom de l'auteur");
        
        TextField anneeField = new TextField();
        anneeField.setPromptText("2024");
        
        TextField prixField = new TextField();
        prixField.setPromptText("29.99");
        
        CheckBox disponibleCheck = new CheckBox("Disponible");
        disponibleCheck.setSelected(true);
        
        // Pré-remplir les champs si modification
        if (livre != null) {
            isbnField.setText(livre.getIsbn());
            isbnField.setDisable(true); // ISBN non modifiable
            titreField.setText(livre.getTitre());
            auteurField.setText(livre.getAuteur());
            anneeField.setText(String.valueOf(livre.getAnneePublication()));
            prixField.setText(String.valueOf(livre.getPrix()));
            disponibleCheck.setSelected(livre.isDisponible());
            disponibleCheck.setDisable(true); // Géré par les emprunts
        }
        
        // Ajouter les champs au formulaire
        grid.add(new Label("ISBN :"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Titre :"), 0, 1);
        grid.add(titreField, 1, 1);
        grid.add(new Label("Auteur :"), 0, 2);
        grid.add(auteurField, 1, 2);
        grid.add(new Label("Année :"), 0, 3);
        grid.add(anneeField, 1, 3);
        grid.add(new Label("Prix (€) :"), 0, 4);
        grid.add(prixField, 1, 4);
        grid.add(disponibleCheck, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus sur le premier champ
        if (livre == null) {
            isbnField.requestFocus();
        } else {
            titreField.requestFocus();
        }
        
        // Validation et création du livre
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                try {
                    // Validation des champs
                    String isbn = StringValidator.validerISBN(isbnField.getText());
                    String titreValue = titreField.getText().trim();
                    String auteurValue = auteurField.getText().trim();
                    
                    if (titreValue.isEmpty()) {
                        throw new ValidationException("Titre", "Le titre ne peut pas être vide");
                    }
                    if (auteurValue.isEmpty()) {
                        throw new ValidationException("Auteur", "L'auteur ne peut pas être vide");
                    }
                    
                    int annee = Integer.parseInt(anneeField.getText().trim());
                    double prix = Double.parseDouble(prixField.getText().trim().replace(",", "."));
                    
                    if (annee < 1000 || annee > 2100) {
                        throw new ValidationException("Année", 
                            "L'année doit être comprise entre 1000 et 2100");
                    }
                    if (prix < 0) {
                        throw new ValidationException("Prix", 
                            "Le prix ne peut pas être négatif");
                    }
                    
                    // Créer ou mettre à jour le livre
                    Livre nouveauLivre = livre == null ? new Livre() : livre;
                    nouveauLivre.setIsbn(isbn);
                    nouveauLivre.setTitre(titreValue);
                    nouveauLivre.setAuteur(auteurValue);
                    nouveauLivre.setAnneePublication(annee);
                    nouveauLivre.setPrix(prix);
                    
                    if (livre == null) {
                        nouveauLivre.setDisponible(disponibleCheck.isSelected());
                    }
                    
                    return nouveauLivre;
                    
                } catch (ValidationException e) {
                    showWarning("Validation", e.getMessage(), 
                        "Champ concerné : " + e.getChamp());
                    return null;
                } catch (NumberFormatException e) {
                    showWarning("Format invalide", 
                        "Veuillez vérifier le format des nombres", 
                        "L'année doit être un nombre entier et le prix un nombre décimal");
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Met à jour les statistiques affichées
     */
    private void updateStatistiques() {
        try {
            int total = bibliothequeService.countLivres();
            int disponibles = bibliothequeService.countLivresDisponibles();
            int empruntes = total - disponibles;
            
            lblTotalLivres.setText(String.valueOf(total));
            lblLivresDisponibles.setText(String.valueOf(disponibles));
            lblLivresEmpruntes.setText(String.valueOf(empruntes));
            
        } catch (SQLException e) {
            lblTotalLivres.setText("?");
            lblLivresDisponibles.setText("?");
            lblLivresEmpruntes.setText("?");
        }
    }
    
    /**
     * Affiche un message de statut temporaire
     */
    private void showStatusMessage(String message, boolean isWarning) {
        // Cette méthode peut être implémentée pour afficher un message
        // dans une barre de statut ou un label dédié
        System.out.println((isWarning ? "⚠ " : "ℹ ") + message);
    }
    
    /**
     * Affiche une alerte de succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'avertissement
     */
    private void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}