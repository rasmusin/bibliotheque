package com.bibliotheque.dao.impl;

import com.bibliotheque.dao.DAO;
import com.bibliotheque.dao.EmpruntDAO;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Membre;
import com.bibliotheque.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpruntDAOImpl implements EmpruntDAO {
	private Connection connection;

	public EmpruntDAOImpl() {
		this.connection = DatabaseConnection.getInstance().getConnection();
	}

	@Override
	public void save(Emprunt emprunt) throws SQLException {
		String sql = "INSERT INTO emprunts (isbn, id_membre, date_emprunt, date_retour_prevue, date_retour_effective, penalite) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, emprunt.getLivre() != null ? emprunt.getLivre().getIsbn() : null);
			stmt.setInt(2, emprunt.getMembre() != null ? emprunt.getMembre().getIdMembre() : 0);
			stmt.setDate(3, emprunt.getDateEmprunt() != null ? Date.valueOf(emprunt.getDateEmprunt()) : Date.valueOf(LocalDate.now()));
			stmt.setDate(4, emprunt.getDateRetourPrevue() != null ? Date.valueOf(emprunt.getDateRetourPrevue()) : null);
			stmt.setDate(5, emprunt.getDateRetourEffective() != null ? Date.valueOf(emprunt.getDateRetourEffective()) : null);
			stmt.setDouble(6, emprunt.getPenalite());

			stmt.executeUpdate();

			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next()) {
					emprunt.setIdEmprunt(keys.getInt(1));
				}
			}
		}
	}

	@Override
	public Emprunt findById(Object id) throws SQLException {
		String sql = "SELECT * FROM emprunts WHERE id_emprunt = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, (Integer) id);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToEmprunt(rs);
				}
			}
		}
		return null;
	}

	@Override
	public List<Emprunt> findAll() throws SQLException {
		List<Emprunt> emprunts = new ArrayList<>();
		String sql = "SELECT * FROM emprunts ORDER BY date_emprunt DESC";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				emprunts.add(mapResultSetToEmprunt(rs));
			}
		}
		return emprunts;
	}

	@Override
	public void update(Emprunt emprunt) throws SQLException {
		String sql = "UPDATE emprunts SET isbn = ?, id_membre = ?, date_emprunt = ?, date_retour_prevue = ?, date_retour_effective = ?, penalite = ? WHERE id_emprunt = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, emprunt.getLivre() != null ? emprunt.getLivre().getIsbn() : null);
			stmt.setInt(2, emprunt.getMembre() != null ? emprunt.getMembre().getIdMembre() : 0);
			stmt.setDate(3, emprunt.getDateEmprunt() != null ? Date.valueOf(emprunt.getDateEmprunt()) : null);
			stmt.setDate(4, emprunt.getDateRetourPrevue() != null ? Date.valueOf(emprunt.getDateRetourPrevue()) : null);
			stmt.setDate(5, emprunt.getDateRetourEffective() != null ? Date.valueOf(emprunt.getDateRetourEffective()) : null);
			stmt.setDouble(6, emprunt.getPenalite());
			stmt.setInt(7, emprunt.getIdEmprunt());

			stmt.executeUpdate();
		}
	}

	@Override
	public void delete(Object id) throws SQLException {
		String sql = "DELETE FROM emprunts WHERE id_emprunt = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, (Integer) id);
			stmt.executeUpdate();
		}
	}

	// Requête utilitaire : emprunts en cours (date_retour_effective IS NULL)
	public List<Emprunt> findEnCours() throws SQLException {
		List<Emprunt> emprunts = new ArrayList<>();
		String sql = "SELECT * FROM emprunts WHERE date_retour_effective IS NULL ORDER BY date_emprunt DESC";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				emprunts.add(mapResultSetToEmprunt(rs));
			}
		}
		return emprunts;
	}

	// Requête utilitaire : trouver par membre
	public List<Emprunt> findByMembre(int membreId) throws SQLException {
		List<Emprunt> emprunts = new ArrayList<>();
		String sql = "SELECT * FROM emprunts WHERE id_membre = ? ORDER BY date_emprunt DESC";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, membreId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					emprunts.add(mapResultSetToEmprunt(rs));
				}
			}
		}
		return emprunts;
	}

	// Mapping ResultSet -> Emprunt (création d'objets Livre/Membre minimalistes)
	private Emprunt mapResultSetToEmprunt(ResultSet rs) throws SQLException {
		Emprunt emprunt = new Emprunt();
		emprunt.setIdEmprunt(rs.getInt("id_emprunt"));

		String isbn = rs.getString("isbn");
		if (isbn != null && !isbn.isEmpty()) {
			try {
				LivreDAOImpl livreDAO = new LivreDAOImpl();
				Livre livre = livreDAO.findById(isbn);
				emprunt.setLivre(livre != null ? livre : new Livre());
			} catch (SQLException e) {
				// fallback minimal object
				Livre livre = new Livre();
				livre.setIsbn(isbn);
				emprunt.setLivre(livre);
			}
		}

		int membreId = rs.getInt("id_membre");
		if (membreId > 0) {
			try {
				MembreDAOImpl membreDAO = new MembreDAOImpl();
				Membre membre = membreDAO.findById(membreId);
				emprunt.setMembre(membre != null ? membre : new Membre());
			} catch (SQLException e) {
				Membre membre = new Membre();
				membre.setIdMembre(membreId);
				emprunt.setMembre(membre);
			}
		}

		Date dEmprunt = rs.getDate("date_emprunt");
		if (dEmprunt != null) emprunt.setDateEmprunt(dEmprunt.toLocalDate());

		Date dPrevue = rs.getDate("date_retour_prevue");
		if (dPrevue != null) emprunt.setDateRetourPrevue(dPrevue.toLocalDate());

		Date dEffective = rs.getDate("date_retour_effective");
		if (dEffective != null) emprunt.setDateRetourEffective(dEffective.toLocalDate());

		emprunt.setPenalite(rs.getDouble("penalite"));

		return emprunt;
	}

	// Compte les emprunts en cours pour un membre donné
	public int countEmpruntsEnCoursByMembre(int membreId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM emprunts WHERE id_membre = ? AND date_retour_effective IS NULL";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, membreId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		return 0;
	}
}
