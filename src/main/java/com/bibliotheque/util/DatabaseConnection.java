package com.bibliotheque.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Instance unique (volatile pour thread-safety)
    private static volatile DatabaseConnection instance;
    private Connection connection;
    
    // Configuration base de données (peut être surchargée via propriétés système pour les tests)
    // Par défaut, utiliser une base H2 en mémoire pour faciliter l'exécution des tests.
    private static final String URL = System.getProperty("jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
    private static final String USER = System.getProperty("jdbc.user", "sa");
    private static final String PASSWORD = System.getProperty("jdbc.password", "");
    private static final String DRIVER = System.getProperty("jdbc.driver", "org.h2.Driver");
    
    // Constructeur privé
    private DatabaseConnection() {
        try {
            // Chargement du driver
            Class.forName(DRIVER);
            
            // Établissement de la connexion
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            System.out.println("✓ Connexion à la base de données établie");
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL introuvable", e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }
    
    // Méthode getInstance avec Double-Checked Locking (thread-safe)
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est toujours valide
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la connexion", e);
        }
        return connection;
    }
    
    // Fermeture de la connexion
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✓ Connexion à la base de données fermée");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture: " + e.getMessage());
            }
        }
    }
}
