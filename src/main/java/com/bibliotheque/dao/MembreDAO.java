package com.bibliotheque.dao;

import com.bibliotheque.model.Membre;
import java.sql.SQLException;
import java.util.List;

public interface MembreDAO extends DAO<Membre> {
	Membre findByEmail(String email) throws SQLException;
	List<Membre> findActifs() throws SQLException;
	List<Membre> findByNom(String nom) throws SQLException;
}
