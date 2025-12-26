-- Suppression de la base si elle existe
DROP DATABASE IF EXISTS bib;

-- Création de la base de données
CREATE DATABASE bibliotheque CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bib;

-- Table des livres
CREATE TABLE livres (
    isbn VARCHAR(20) PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    auteur VARCHAR(100) NOT NULL,
    annee_publication INT,
    prix DECIMAL(10,2),
    disponible BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des membres
CREATE TABLE membres (
    id_membre INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    date_inscription DATE NOT NULL,
    actif BOOLEAN DEFAULT TRUE
);

-- Table des emprunts
CREATE TABLE emprunts (
    id_emprunt INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL,
    id_membre INT NOT NULL,
    date_emprunt DATE NOT NULL,
    date_retour_prevue DATE NOT NULL,
    date_retour_effective DATE NULL,
    penalite DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (isbn) REFERENCES livres(isbn) ON DELETE CASCADE,
    FOREIGN KEY (id_membre) REFERENCES membres(id_membre) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_livres_auteur ON livres(auteur);
CREATE INDEX idx_livres_titre ON livres(titre);
CREATE INDEX idx_emprunts_membre ON emprunts(id_membre);
CREATE INDEX idx_emprunts_livre ON emprunts(isbn);
CREATE INDEX idx_emprunts_dates ON emprunts(date_emprunt, date_retour_effective);

-- Données de test
INSERT INTO livres (isbn, titre, auteur, annee_publication, prix, disponible) VALUES
('978-2-1234-5680-3', 'Java pour les nuls', 'Barry Burd', 2020, 29.99, TRUE),
('978-2-2122-5681-7', 'Design Patterns en Java', 'Eric Freeman', 2019, 45.50, TRUE),
('978-2-3122-5682-1', 'Clean Code', 'Robert Martin', 2018, 39.99, FALSE),
('978-2-4122-5683-5', 'Effective Java', 'Joshua Bloch', 2017, 42.00, TRUE),
('978-2-5122-5684-9', 'Head First Java', 'Kathy Sierra', 2021, 35.00, TRUE);

INSERT INTO membres (nom, prenom, email, date_inscription, actif) VALUES
('DUPONT', 'Jean', 'jean.dupont@email.com', '2024-01-15', TRUE),
('MARTIN', 'Sophie', 'sophie.martin@email.com', '2024-02-20', TRUE),
('BERNARD', 'Pierre', 'pierre.bernard@email.com', '2024-03-10', TRUE),
('DUBOIS', 'Marie', 'marie.dubois@email.com', '2024-04-05', FALSE);

INSERT INTO emprunts (isbn, id_membre, date_emprunt, date_retour_prevue, date_retour_effective, penalite) VALUES
('978-2-3122-5682-1', 1, '2024-11-01', '2024-11-15', NULL, 0.00),
('978-2-1234-5680-3', 2, '2024-10-20', '2024-11-03', '2024-11-05', 1.00);

-- Vérification
SELECT * FROM livres;
SELECT * FROM membres;
SELECT * FROM emprunts;