package com.bibliotheque.dao;

import com.bibliotheque.model.Livre;
import java.sql.SQLException;
import java.util.List;

public interface LivreDAO extends DAO<Livre> {
    @Override
    void save(Livre livre) throws SQLException;
    
    @Override
    Livre findById(Object isbn) throws SQLException;
    
    @Override
    List<Livre> findAll() throws SQLException;
    
    @Override
    void update(Livre livre) throws SQLException;
    
    @Override
    void delete(Object isbn) throws SQLException;
    
    // Méthodes spécifiques
    List<Livre> findByAuteur(String auteur) throws SQLException;
    List<Livre> findByTitre(String titre) throws SQLException;
    List<Livre> findDisponibles() throws SQLException;
}
