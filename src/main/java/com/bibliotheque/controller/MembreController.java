package com.bibliotheque.controller;

import com.bibliotheque.model.Membre;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.service.BibliothequeService;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.exception.ValidationException;
import com.bibliotheque.util.StringValidator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur dédié à la gestion des membres
 * Gère l'affichage, l'ajout, la modification et la consultation des membres
 */
public class MembreController implements Initializable {
    
    // Services
    private BibliothequeService bibliothequeService;
    private EmpruntService empruntService;
    
    // Composants FXML - Recherche
    @FXML
    private TextField rechercheMembreField;
    
    @FXML
    private CheckBox actifsSeulementCheck;
    
    @FXML
    private Button btnRechercherMembre;
    
    @FXML
    private Button btnTousLesMembres;
    
    // Composants FXML - Table des membres
    @FXML
    private TableView<Membre> membresTable;
    
    @FXML
    private TableColumn<Membre, Integer> idMembreColumn;
    
    @FXML
    private TableColumn<Membre, String> nomMembreColumn;
    
    @FXML
    private TableColumn<Membre, String> prenomMembreColumn;
    
    @FXML
    private TableColumn<Membre, String> emailColumn;
    
    @FXML
    private TableColumn<Membre, LocalDate> inscriptionColumn;
    
    @FXML
    private TableColumn<Membre, Boolean> actifColumn;
    
    @FXML
    private TableColumn<Membre, Integer> nbEmpruntsColumn;
    
    // Composants FXML - Boutons d'action
    @FXML
    private Button btnNouveauMembre;
    
    @FXML
    private Button btnModifierMembre;
    
    @FXML
    private Button btnActiverDesactiver;
    
    @FXML
    private Button btnSupprimerMembre;
    
    @FXML
    private Button btnVoirHistorique;
    
    @FXML
    private Button btnDetailsMembre;
    
    // Composants FXML - Statistiques
    @FXML
    private Label lblTotalMembres;
    
    @FXML
    private Label lblMembresActifs;
    
    @FXML
    private Label lblMembresInactifs;
    
    @FXML
    private Label lblEmpruntsEnCours;
    
    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les services
        bibliothequeService = new BibliothequeService();
        empruntService = new EmpruntService();
        
        // Configurer les colonnes de la table
        configurerTableColumns();
        
        // Configurer les listeners
        configurerListeners();
        
        // Charger les données initiales
        chargerTousLesMembres();
        
        // Mettre à jour les statistiques
        updateStatistiques();
    }
    
    /**
     * Configure les colonnes de la TableView
     */
    private void configurerTableColumns() {
        // Liaison des colonnes avec les propriétés du modèle
        idMembreColumn.setCellValueFactory(new PropertyValueFactory<>("idMembre"));
        nomMembreColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomMembreColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        inscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        
        // Colonne personnalisée pour le nombre d'emprunts en cours
        nbEmpruntsColumn.setCellValueFactory(cellData -> {
            Membre membre = cellData.getValue();
            int nbEmprunts = membre.getNombreEmpruntsEnCours();
            return new javafx.beans.property.SimpleObjectProperty<>(nbEmprunts);
        });
        
        // Formatter la colonne Date d'inscription
        inscriptionColumn.setCellFactory(col -> new TableCell<Membre, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    setText(date.format(formatter));
                }
            }
        });
        
        // Formatter la colonne Actif avec icônes et couleurs
        actifColumn.setCellFactory(col -> new TableCell<Membre, Boolean>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (actif) {
                        setText("✓ Actif");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setText("✗ Inactif");
                        setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Formatter la colonne Emprunts avec couleur selon le nombre
        nbEmpruntsColumn.setCellFactory(col -> new TableCell<Membre, Integer>() {
            @Override
            protected void updateItem(Integer nbEmprunts, boolean empty) {
                super.updateItem(nbEmprunts, empty);
                if (empty || nbEmprunts == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(nbEmprunts));
                    if (nbEmprunts == 0) {
                        setStyle("-fx-text-fill: gray;");
                    } else if (nbEmprunts >= 3) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: blue;");
                    }
                }
            }
        });
        
        // Rendre les colonnes triables
        membresTable.getSortOrder().add(nomMembreColumn);
    }
    
    /**
     * Configure les listeners
     */
    private void configurerListeners() {
        // Activer/désactiver les boutons selon la sélection
        membresTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean isSelected = newValue != null;
                btnModifierMembre.setDisable(!isSelected);
                btnActiverDesactiver.setDisable(!isSelected);
                btnVoirHistorique.setDisable(!isSelected);
                btnDetailsMembre.setDisable(!isSelected);
                
                // Changer le texte du bouton Activer/Désactiver
                if (isSelected) {
                    btnActiverDesactiver.setText(
                        newValue.isActif() ? "Désactiver" : "Activer"
                    );
                }
            }
        );
        
        // Désactiver les boutons par défaut
        btnModifierMembre.setDisable(true);
        btnActiverDesactiver.setDisable(true);
        btnSupprimerMembre.setDisable(true);
        btnVoirHistorique.setDisable(true);
        btnDetailsMembre.setDisable(true);
        
        // Listener pour la touche Entrée dans le champ de recherche
        rechercheMembreField.setOnAction(event -> handleRechercherMembre());
        
        // Listener pour le CheckBox "Actifs seulement"
        actifsSeulementCheck.selectedProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!rechercheMembreField.getText().trim().isEmpty()) {
                    handleRechercherMembre();
                } else {
                    chargerTousLesMembres();
                }
            }
        );
    }
    
    /**
     * Charge tous les membres dans la table
     */
    @FXML
    private void chargerTousLesMembres() {
        try {
            List<Membre> membres;
            
            if (actifsSeulementCheck.isSelected()) {
                membres = bibliothequeService.getMembresActifs();
            } else {
                membres = bibliothequeService.getAllMembres();
            }
            
            ObservableList<Membre> observableMembres = 
                FXCollections.observableArrayList(membres);
            membresTable.setItems(observableMembres);
            
            // Réinitialiser le champ de recherche
            rechercheMembreField.clear();
            
            // Afficher un message de statut
            showStatusMessage(membres.size() + " membre(s) affiché(s)", false);
            
            // Mettre à jour les statistiques
            updateStatistiques();
            
        } catch (SQLException e) {
            showError("Erreur de chargement", 
                "Impossible de charger les membres depuis la base de données", 
                e.getMessage());
        }
    }
    
    /**
     * Gère la recherche de membres
     */
    @FXML
    private void handleRechercherMembre() {
        String critere = rechercheMembreField.getText().trim();
        
        // Si le champ est vide, afficher tous les membres
        if (critere.isEmpty()) {
            chargerTousLesMembres();
            return;
        }
        
        try {
            List<Membre> resultats = 
                bibliothequeService.rechercherMembres(critere, actifsSeulementCheck.isSelected());
            
            ObservableList<Membre> observableResultats = 
                FXCollections.observableArrayList(resultats);
            membresTable.setItems(observableResultats);
            
            // Message de statut
            if (resultats.isEmpty()) {
                showStatusMessage("Aucun membre trouvé pour \"" + critere + "\"", true);
            } else {
                showStatusMessage(resultats.size() + " membre(s) trouvé(s)", false);
            }
            
        } catch (SQLException e) {
            showError("Erreur de recherche", 
                "Une erreur est survenue lors de la recherche", 
                e.getMessage());
        }
    }
    
    /**
     * Gère l'ajout d'un nouveau membre
     */
    @FXML
    private void handleNouveauMembre() {
        // Créer et afficher le dialogue
        Dialog<Membre> dialog = creerDialogueMembre(null, "Nouveau membre");
        Optional<Membre> result = dialog.showAndWait();
        
        result.ifPresent(membre -> {
            try {
                // Sauvegarder le membre
                bibliothequeService.ajouterMembre(membre);
                
                // Afficher un message de succès
                showSuccess("Membre ajouté", 
                    membre.getNomComplet() + " a été enregistré avec succès");
                
                // Recharger la table
                chargerTousLesMembres();
                
            } catch (SQLException e) {
                showError("Erreur d'ajout", 
                    "Impossible d'ajouter le membre", 
                    e.getMessage());
            } catch (IllegalArgumentException e) {
                showWarning("Email existant", 
                    "Un membre avec cet email existe déjà", 
                    e.getMessage());
            }
        });
    }
    
    /**
     * Gère la modification d'un membre existant
     */
    @FXML
    private void handleModifierMembre() {
        Membre membreSelectionne = membresTable.getSelectionModel().getSelectedItem();
        
        if (membreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un membre à modifier", 
                null);
            return;
        }
        
        // Créer et afficher le dialogue avec les données existantes
        Dialog<Membre> dialog = creerDialogueMembre(membreSelectionne, "Modifier un membre");
        Optional<Membre> result = dialog.showAndWait();
        
        result.ifPresent(membreModifie -> {
            try {
                // Mettre à jour le membre
                bibliothequeService.modifierMembre(membreModifie);
                
                // Afficher un message de succès
                showSuccess("Membre modifié", 
                    membreModifie.getNomComplet() + " a été modifié avec succès");
                
                // Recharger la table
                chargerTousLesMembres();
                
            } catch (SQLException e) {
                showError("Erreur de modification", 
                    "Impossible de modifier le membre", 
                    e.getMessage());
            }
        });
    }
    
    /**
     * Gère l'activation/désactivation d'un membre
     */
    @FXML
    private void handleActiverDesactiver() {
        Membre membreSelectionne = membresTable.getSelectionModel().getSelectedItem();
        
        if (membreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un membre", 
                null);
            return;
        }
        
        boolean estActif = membreSelectionne.isActif();
        String action = estActif ? "désactiver" : "activer";
        String actionMaj = estActif ? "Désactiver" : "Activer";
        
        // Si désactivation, vérifier qu'il n'a pas d'emprunts en cours
        if (estActif && membreSelectionne.getNombreEmpruntsEnCours() > 0) {
            showWarning("Emprunts en cours", 
                "Impossible de désactiver ce membre", 
                "Le membre a encore " + membreSelectionne.getNombreEmpruntsEnCours() + 
                " emprunt(s) en cours. Veuillez d'abord récupérer tous les livres.");
            return;
        }
        
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(actionMaj + " le membre ?");
        confirmation.setContentText(
            "Êtes-vous sûr de vouloir " + action + " le membre :\n" +
            membreSelectionne.getNomComplet() + " ?"
        );
        
        Optional<ButtonType> resultat = confirmation.showAndWait();
        
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                // Changer le statut
                membreSelectionne.setActif(!estActif);
                bibliothequeService.modifierMembre(membreSelectionne);
                
                // Afficher un message de succès
                showSuccess("Statut modifié", 
                    membreSelectionne.getNomComplet() + " a été " + 
                    (estActif ? "désactivé" : "activé") + " avec succès");
                
                // Recharger la table
                chargerTousLesMembres();
                
            } catch (SQLException e) {
                showError("Erreur", 
                    "Impossible de modifier le statut du membre", 
                    e.getMessage());
            }
        }
    }

    /**
     * Gère la suppression d'un membre
     */
    @FXML
    private void handleSupprimerMembre() {
        Membre membreSelectionne = membresTable.getSelectionModel().getSelectedItem();
        if (membreSelectionne == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un membre", null);
            return;
        }

        // Vérifier s'il a des emprunts en cours
        if (membreSelectionne.getNombreEmpruntsEnCours() > 0) {
            showWarning("Emprunts en cours", "Impossible de supprimer ce membre", "Le membre a encore des emprunts en cours.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le membre ?");
        confirmation.setContentText("Supprimer " + membreSelectionne.getNomComplet() + " ?");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                bibliothequeService.supprimerMembre(membreSelectionne.getIdMembre());
                showSuccess("Membre supprimé", "Le membre a été supprimé avec succès");
                chargerTousLesMembres();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer le membre", e.getMessage());
            }
        }
    }
    
    /**
     * Affiche l'historique des emprunts d'un membre
     */
    @FXML
    private void handleVoirHistorique() {
        Membre membreSelectionne = membresTable.getSelectionModel().getSelectedItem();
        
        if (membreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un membre", 
                null);
            return;
        }
        
        try {
            // Récupérer l'historique des emprunts
            List<Emprunt> historique = 
                empruntService.getHistoriqueEmpruntsMembre(membreSelectionne.getIdMembre());
            
            // Créer un dialogue pour afficher l'historique
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Historique des emprunts");
            dialog.setHeaderText("Historique de " + membreSelectionne.getNomComplet());
            
            // Créer une TableView pour l'historique
            TableView<Emprunt> historiqueTable = new TableView<>();
            
            // Colonnes
            TableColumn<Emprunt, String> colLivre = new TableColumn<>("Livre");
            colLivre.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getLivre().getTitre()));
            colLivre.setPrefWidth(200);
            
            TableColumn<Emprunt, LocalDate> colEmprunt = new TableColumn<>("Date emprunt");
            colEmprunt.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
            colEmprunt.setPrefWidth(100);
            
            TableColumn<Emprunt, LocalDate> colRetourPrevu = new TableColumn<>("Retour prévu");
            colRetourPrevu.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
            colRetourPrevu.setPrefWidth(100);
            
            TableColumn<Emprunt, LocalDate> colRetourEffectif = new TableColumn<>("Retour effectif");
            colRetourEffectif.setCellValueFactory(new PropertyValueFactory<>("dateRetourEffective"));
            colRetourEffectif.setPrefWidth(100);
            
            TableColumn<Emprunt, Double> colPenalite = new TableColumn<>("Pénalité");
            colPenalite.setCellValueFactory(new PropertyValueFactory<>("penalite"));
            colPenalite.setPrefWidth(80);
            
            // Formatter les dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            colEmprunt.setCellFactory(col -> new TableCell<Emprunt, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : date.format(formatter));
                }
            });
            
            colRetourPrevu.setCellFactory(col -> new TableCell<Emprunt, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : date.format(formatter));
                }
            });
            
            colRetourEffectif.setCellFactory(col -> new TableCell<Emprunt, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText("En cours");
                        setStyle("-fx-text-fill: orange; -fx-font-style: italic;");
                    } else {
                        setText(date.format(formatter));
                        setStyle("");
                    }
                }
            });
            
            colPenalite.setCellFactory(col -> new TableCell<Emprunt, Double>() {
                @Override
                protected void updateItem(Double penalite, boolean empty) {
                    super.updateItem(penalite, empty);
                    if (empty || penalite == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%.2f €", penalite));
                        setStyle(penalite > 0 ? "-fx-text-fill: red;" : "");
                    }
                }
            });
            
            historiqueTable.getColumns().addAll(
                colLivre, colEmprunt, colRetourPrevu, colRetourEffectif, colPenalite
            );
            
            // Remplir la table
            historiqueTable.setItems(FXCollections.observableArrayList(historique));
            
            // Ajouter des statistiques
            int totalEmprunts = historique.size();
            int empruntsEnCours = (int) historique.stream()
                .filter(e -> e.getDateRetourEffective() == null)
                .count();
            double totalPenalites = historique.stream()
                .mapToDouble(Emprunt::getPenalite)
                .sum();
            
            Label statsLabel = new Label(String.format(
                "Total emprunts : %d | En cours : %d | Pénalités totales : %.2f €",
                totalEmprunts, empruntsEnCours, totalPenalites
            ));
            statsLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10;");
            
            VBox content = new VBox(10);
            content.getChildren().addAll(statsLabel, historiqueTable);
            content.setPadding(new Insets(10));
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.setResizable(true);
            dialog.getDialogPane().setPrefSize(700, 400);
            
            dialog.showAndWait();
            
        } catch (SQLException e) {
            showError("Erreur", 
                "Impossible de charger l'historique des emprunts", 
                e.getMessage());
        }
    }
    
    /**
     * Affiche les détails complets d'un membre
     */
    @FXML
    private void handleDetailsMembre() {
        Membre membreSelectionne = membresTable.getSelectionModel().getSelectedItem();
        
        if (membreSelectionne == null) {
            showWarning("Aucune sélection", 
                "Veuillez sélectionner un membre pour voir ses détails", 
                null);
            return;
        }
        
        try {
            // Récupérer des informations complémentaires
            int nbEmpruntsTotal = empruntService.countEmpruntsTotal(membreSelectionne.getIdMembre());
            int nbEmpruntsEnCours = membreSelectionne.getNombreEmpruntsEnCours();
            double penalitesTotales = empruntService.getPenalitesTotales(membreSelectionne.getIdMembre());
            
            // Créer un dialogue d'information
            Alert details = new Alert(Alert.AlertType.INFORMATION);
            details.setTitle("Détails du membre");
            details.setHeaderText(membreSelectionne.getNomComplet());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            String contenu = String.format(
                "ID : %d\n" +
                "Nom : %s\n" +
                "Prénom : %s\n" +
                "Email : %s\n" +
                "Date d'inscription : %s\n" +
                "Statut : %s\n\n" +
                "--- Statistiques d'emprunts ---\n" +
                "Emprunts totaux : %d\n" +
                "Emprunts en cours : %d / 3\n" +
                "Pénalités totales : %.2f €",
                membreSelectionne.getIdMembre(),
                membreSelectionne.getNom(),
                membreSelectionne.getPrenom(),
                membreSelectionne.getEmail(),
                membreSelectionne.getDateInscription().format(formatter),
                membreSelectionne.isActif() ? "Actif" : "Inactif",
                nbEmpruntsTotal,
                nbEmpruntsEnCours,
                penalitesTotales
            );
            
            details.setContentText(contenu);
            details.getDialogPane().setPrefWidth(400);
            details.showAndWait();
            
        } catch (SQLException e) {
            showError("Erreur", 
                "Impossible de charger les détails du membre", 
                e.getMessage());
        }
    }
    
    /**
     * Crée un dialogue pour ajouter ou modifier un membre
     * 
     * @param membre Le membre à modifier (null pour un ajout)
     * @param titre Le titre du dialogue
     * @return Le dialogue configuré
     */
    private Dialog<Membre> creerDialogueMembre(Membre membre, String titre) {
        Dialog<Membre> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.setHeaderText(membre == null ? 
            "Saisissez les informations du nouveau membre" : 
            "Modifiez les informations du membre");
        
        // Boutons du dialogue
        ButtonType btnValider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        // Créer le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Champs du formulaire
        TextField nomField = new TextField();
        nomField.setPromptText("Nom de famille");
        
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        
        TextField emailField = new TextField();
        emailField.setPromptText("email@example.com");
        
        DatePicker inscriptionPicker = new DatePicker();
        inscriptionPicker.setValue(LocalDate.now());
        
        CheckBox actifCheck = new CheckBox("Membre actif");
        actifCheck.setSelected(true);
        
        // Pré-remplir les champs si modification
        if (membre != null) {
            nomField.setText(membre.getNom());
            prenomField.setText(membre.getPrenom());
            emailField.setText(membre.getEmail());
            inscriptionPicker.setValue(membre.getDateInscription());
            actifCheck.setSelected(membre.isActif());
        }
        
        // Ajouter les champs au formulaire
        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom :"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email :"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Date d'inscription :"), 0, 3);
        grid.add(inscriptionPicker, 1, 3);
        grid.add(actifCheck, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus sur le premier champ
        nomField.requestFocus();
        
        // Validation et création du membre
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                try {
                    // Validation des champs
                    String nom = nomField.getText().trim();
                    String prenom = prenomField.getText().trim();
                    String email = StringValidator.validerEmail(emailField.getText());
                    LocalDate dateInscription = inscriptionPicker.getValue();
                    
                    if (nom.isEmpty()) {
                        throw new ValidationException("Nom", "Le nom ne peut pas être vide");
                    }
                    if (prenom.isEmpty()) {
                        throw new ValidationException("Prénom", "Le prénom ne peut pas être vide");
                    }
                    if (dateInscription == null) {
                        throw new ValidationException("Date", "La date d'inscription est obligatoire");
                    }
                    if (dateInscription.isAfter(LocalDate.now())) {
                        throw new ValidationException("Date", 
                            "La date d'inscription ne peut pas être dans le futur");
                    }
                    
                    // Créer ou mettre à jour le membre
                    Membre nouveauMembre = membre == null ? new Membre() : membre;
                    nouveauMembre.setNom(StringValidator.formaterNomPropre(nom));
                    nouveauMembre.setPrenom(StringValidator.formaterNomPropre(prenom));
                    nouveauMembre.setEmail(email);
                    nouveauMembre.setDateInscription(dateInscription);
                    nouveauMembre.setActif(actifCheck.isSelected());
                    
                    return nouveauMembre;
                    
                } catch (ValidationException e) {
                    showWarning("Validation", e.getMessage(), 
                        "Champ concerné : " + e.getChamp());
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
            int total = bibliothequeService.countMembres();
            int actifs = bibliothequeService.countMembresActifs();
            int inactifs = total - actifs;
            int empruntsEnCours = empruntService.countEmpruntsEnCoursTotal();
            
            lblTotalMembres.setText(String.valueOf(total));
            lblMembresActifs.setText(String.valueOf(actifs));
            lblMembresInactifs.setText(String.valueOf(inactifs));
            lblEmpruntsEnCours.setText(String.valueOf(empruntsEnCours));
            
        } catch (SQLException e) {
            lblTotalMembres.setText("?");
            lblMembresActifs.setText("?");
            lblMembresInactifs.setText("?");
            lblEmpruntsEnCours.setText("?");
        }
    }
    
    /**
     * Affiche un message de statut temporaire
     */
    private void showStatusMessage(String message, boolean isWarning) {
        System.out.println((isWarning ? "⚠ " : "ℹ ") + message);
    }
    
    /**
     * Affiche une alerte de succès
     */
    private void showSuccess(String title, String message) {
        Alert alert= new Alert(Alert.AlertType.INFORMATION);
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