package com.bibliotheque;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/com/bibliotheque/primary.fxml"));
		Scene scene = new Scene(root);

		// Load optional stylesheet if present
		try {
			String css = getClass().getResource("/css/style.css").toExternalForm();
			scene.getStylesheets().add(css);
		} catch (Exception ignored) {
		}

		primaryStage.setTitle("Gestion de la Bibliothèque");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
