package com.bibliotheque.dao;

import com.bibliotheque.model.Emprunt;
import java.sql.SQLException;
import java.util.List;

public interface EmpruntDAO extends DAO<Emprunt> {
	List<Emprunt> findEnCours() throws SQLException;
	List<Emprunt> findByMembre(int membreId) throws SQLException;
}
