package com.bibliotheque.controller;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Membre;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.BibliothequeService;
import com.bibliotheque.exception.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
 * Contrôleur dédié à la gestion des emprunts
 * Gère l'affichage, l'ajout et le retour des emprunts
 */
public class EmpruntController implements Initializable {
    
    // Services
    private EmpruntService empruntService;
    private BibliothequeService bibliothequeService;
    
    // Composants FXML - Recherche
    @FXML
    private TextField rechercheEmpruntField;
    
    @FXML
    private Button btnRechercherEmprunt;
    
    @FXML
    private Button btnTousLesEmprunts;
    
    @FXML
    private ToggleButton toggleEnCours;
    
    // Composants FXML - Table des emprunts
    @FXML
    private TableView<Emprunt> empruntsTable;
    
    @FXML
    private TableColumn<Emprunt, Integer> idEmpruntColumn;
    
    @FXML
    private TableColumn<Emprunt, String> livreEmpruntColumn;
    
    @FXML
    private TableColumn<Emprunt, String> membreEmpruntColumn;
    
    @FXML
    private TableColumn<Emprunt, LocalDate> dateEmpruntColumn;
    
    @FXML
    private TableColumn<Emprunt, LocalDate> dateRetourPrevueColumn;
    
    @FXML
    private TableColumn<Emprunt, LocalDate> dateRetourEffectifColumn;
    
    @FXML
    private TableColumn<Emprunt, Double> penaliteColumn;
    
    // Composants FXML - Boutons d'action
    @FXML
    private Button btnNouvelEmprunt;
    
    @FXML
    private Button btnRetourEmprunt;
    
    @FXML
    private Button btnModifierEmprunt;
    
    @FXML
    private Button btnSupprimerEmprunt;
    
    // Composants FXML - Statistiques
    @FXML
    private Label lblTotalEmprunts;
    
    @FXML
    private Label lblEmpruntsEnCours;
    
    @FXML
    private Label lblEmpruntsTerminés;
    
    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les services
        empruntService = new EmpruntService();
        bibliothequeService = new BibliothequeService();
        
        // Configurer les colonnes de la table
        configurerTableColumns();
        
        // Configurer les listeners
        configurerListeners();
        
        // Charger les données initiales
        chargerTousLesEmprunts();
        
        // Mettre à jour les statistiques
        updateStatistiques();
    }
    
    /**
     * Configure les colonnes de la TableView
     */
    private void configurerTableColumns() {
        idEmpruntColumn.setCellValueFactory(new PropertyValueFactory<>("idEmprunt"));
        dateEmpruntColumn.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        dateRetourPrevueColumn.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        dateRetourEffectifColumn.setCellValueFactory(new PropertyValueFactory<>("dateRetourEffective"));
        penaliteColumn.setCellValueFactory(new PropertyValueFactory<>("penalite"));
        
        // Colonnes personnalisées pour livre et membre
        livreEmpruntColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getLivre().getTitre()));
        
        membreEmpruntColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMembre().getNomComplet()));
        
        // Formatter les dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        dateEmpruntColumn.setCellFactory(col -> new TableCell<Emprunt, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(formatter));
            }
        });
        
        dateRetourPrevueColumn.setCellFactory(col -> new TableCell<Emprunt, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(formatter));
            }
        });
        
        dateRetourEffectifColumn.setCellFactory(col -> new TableCell<Emprunt, LocalDate>() {
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
        
        penaliteColumn.setCellFactory(col -> new TableCell<Emprunt, Double>() {
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
    }
    
    /**
     * Configure les listeners
     */
    private void configurerListeners() {
        empruntsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean isSelected = newValue != null;
                btnRetourEmprunt.setDisable(!isSelected || (newValue != null && newValue.getDateRetourEffective() != null));
                btnModifierEmprunt.setDisable(!isSelected);
                btnSupprimerEmprunt.setDisable(!isSelected);
            }
        );
        
        btnRetourEmprunt.setDisable(true);
        btnModifierEmprunt.setDisable(true);
        btnSupprimerEmprunt.setDisable(true);
        rechercheEmpruntField.setOnAction(event -> handleRechercherEmprunt());
    }
    
    /**
     * Charge tous les emprunts dans la table
     */
    @FXML
    private void chargerTousLesEmprunts() {
        try {
            List<Emprunt> emprunts = empruntService.getAllEmprunts();
            ObservableList<Emprunt> observableEmprunts = FXCollections.observableArrayList(emprunts);
            empruntsTable.setItems(observableEmprunts);
            rechercheEmpruntField.clear();
            toggleEnCours.setSelected(false);
            updateStatistiques();
            showStatusMessage(emprunts.size() + " emprunt(s) affichés", false);
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les emprunts", e.getMessage());
        }
    }
    
    /**
     * Gère la recherche d'emprunts
     */
    @FXML
    private void handleRechercherEmprunt() {
        String critere = rechercheEmpruntField.getText().trim();
        if (critere.isEmpty()) {
            chargerTousLesEmprunts();
            return;
        }
        try {
            List<Emprunt> resultats = empruntService.searchEmprunts(critere);
            empruntsTable.setItems(FXCollections.observableArrayList(resultats));
            showStatusMessage(resultats.size() + " emprunt(s) trouvé(s)", resultats.isEmpty());
        } catch (SQLException e) {
            showError("Erreur de recherche", "Une erreur est survenue", e.getMessage());
        }
    }
    
    /**
     * Filtre les emprunts en cours
     */
    @FXML
    private void filterEnCours() {
        try {
            List<Emprunt> emprunts = toggleEnCours.isSelected() ? 
                empruntService.getEmpruntsEnCours() : 
                empruntService.getAllEmprunts();
            empruntsTable.setItems(FXCollections.observableArrayList(emprunts));
            updateStatistiques();
        } catch (SQLException e) {
            showError("Erreur de filtrage", "Impossible de filtrer les emprunts", e.getMessage());
        }
    }
    
    /**
     * Gère l'ajout d'un nouvel emprunt
     */
    @FXML
    private void handleNouvelEmprunt() {
        Dialog<Emprunt> dialog = creerDialogueEmprunt();
        Optional<Emprunt> result = dialog.showAndWait();
        
        result.ifPresent(emprunt -> {
            try {
                empruntService.emprunterLivre(emprunt.getLivre().getIsbn(), 
                    emprunt.getMembre().getIdMembre());
                showSuccess("Emprunt créé", "Emprunt enregistré avec succès");
                chargerTousLesEmprunts();
            } catch (LivreIndisponibleException | MembreInactifException | LimiteEmpruntDepasseeException e) {
                showWarning("Erreur", e.getClass().getSimpleName(), e.getMessage());
            } catch (SQLException e) {
                showError("Erreur base de données", "Impossible d'enregistrer l'emprunt", e.getMessage());
            }
        });
    }
    
    /**
     * Gère le retour d'un livre emprunté
     */
    @FXML
    private void handleRetourEmprunt() {
        Emprunt empruntSelectionne = empruntsTable.getSelectionModel().getSelectedItem();
        if (empruntSelectionne == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un emprunt", null);
            return;
        }
        
        if (empruntSelectionne.getDateRetourEffective() != null) {
            showWarning("Déjà retourné", "Ce livre a déjà été retourné", null);
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Confirmer le retour ?");
        confirmation.setContentText("Retourner " + empruntSelectionne.getLivre().getTitre() + " ?");
        
        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                empruntService.retournerLivre(empruntSelectionne.getIdEmprunt());
                showSuccess("Retour enregistré", "Livre retourné avec succès");
                chargerTousLesEmprunts();
            } catch (SQLException e) {
                showError("Erreur", "Impossible d'enregistrer le retour", e.getMessage());
            }
        }
    }

    /**
     * Gère la modification d'un emprunt (dates et pénalités)
     */
    @FXML
    private void handleModifierEmprunt() {
        Emprunt empruntSelectionne = empruntsTable.getSelectionModel().getSelectedItem();
        if (empruntSelectionne == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un emprunt", null);
            return;
        }

        Dialog<Emprunt> dialog = new Dialog<>();
        dialog.setTitle("Modifier emprunt");
        dialog.setHeaderText("Modifier les dates de l'emprunt");
        ButtonType btnValider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        DatePicker dpRetourPrevue = new DatePicker(empruntSelectionne.getDateRetourPrevue());
        DatePicker dpRetourEffective = new DatePicker(empruntSelectionne.getDateRetourEffective());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(new Label("Date retour prévue:"), dpRetourPrevue,
                                      new Label("Date retour effective (laisser vide si en cours):"), dpRetourEffective);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                empruntSelectionne.setDateRetourPrevue(dpRetourPrevue.getValue());
                empruntSelectionne.setDateRetourEffective(dpRetourEffective.getValue());
                empruntSelectionne.setPenalite(empruntSelectionne.calculerPenalite());
                return empruntSelectionne;
            }
            return null;
        });

        Optional<Emprunt> result = dialog.showAndWait();
        result.ifPresent(e -> {
            try {
                empruntService.modifierEmprunt(e);
                showSuccess("Emprunt modifié", "Les modifications ont été enregistrées");
                chargerTousLesEmprunts();
            } catch (SQLException ex) {
                showError("Erreur", "Impossible de modifier l'emprunt", ex.getMessage());
            }
        });
    }

    /**
     * Gère la suppression d'un emprunt
     */
    @FXML
    private void handleSupprimerEmprunt() {
        Emprunt empruntSelectionne = empruntsTable.getSelectionModel().getSelectedItem();
        if (empruntSelectionne == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un emprunt", null);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer cet emprunt ?");
        confirmation.setContentText("Supprimer l'emprunt de \"" + empruntSelectionne.getLivre().getTitre() + "\" effectué par " + empruntSelectionne.getMembre().getNomComplet() + " ?");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                empruntService.supprimerEmprunt(empruntSelectionne.getIdEmprunt());
                showSuccess("Emprunt supprimé", "L'emprunt a été supprimé avec succès");
                chargerTousLesEmprunts();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer l'emprunt", e.getMessage());
            }
        }
    }
    
    /**
     * Crée un dialogue pour enregistrer un nouvel emprunt
     */
    private Dialog<Emprunt> creerDialogueEmprunt() {
        Dialog<Emprunt> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Emprunt");
        dialog.setHeaderText("Enregistrer un nouvel emprunt");
        
        ButtonType btnValider = new ButtonType("Emprunter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        ComboBox<Livre> livreCombo = new ComboBox<>();
        ComboBox<Membre> membreCombo = new ComboBox<>();
        
        try {
            livreCombo.setItems(FXCollections.observableArrayList(
                bibliothequeService.getLivresDisponibles()));
            membreCombo.setItems(FXCollections.observableArrayList(
                bibliothequeService.getMembresActifs()));
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les données", e.getMessage());
        }
        
        content.getChildren().addAll(
            new Label("Livre:"), livreCombo,
            new Label("Membre:"), membreCombo
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                if (livreCombo.getValue() != null && membreCombo.getValue() != null) {
                    return new Emprunt(livreCombo.getValue(), membreCombo.getValue());
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
            int total = empruntService.countAllEmprunts();
            int enCours = empruntService.countEmpruntsEnCours();
            int termines = total - enCours;
            
            lblTotalEmprunts.setText("Total: " + total);
            lblEmpruntsEnCours.setText("En cours: " + enCours);
            lblEmpruntsTerminés.setText("Terminés: " + termines);
        } catch (SQLException e) {
            lblTotalEmprunts.setText("Total: ?");
            lblEmpruntsEnCours.setText("En cours: ?");
            lblEmpruntsTerminés.setText("Terminés: ?");
        }
    }
    
    private void showStatusMessage(String message, boolean isWarning) {
        System.out.println((isWarning ? "⚠ " : "ℹ ") + message);
    }
    
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
