package com.bibliotheque.dao;


import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {
    /**
     * Sauvegarde une entité dans la base de données
     */
    void save(T entity) throws SQLException;
    
    /**
     * Récupère une entité par son identifiant
     */
    T findById(Object id) throws SQLException;
    
    /**
     * Récupère toutes les entités
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Met à jour une entité existante
     */
    void update(T entity) throws SQLException;
    
    /**
     * Supprime une entité
     */
    void delete(Object id) throws SQLException;
}