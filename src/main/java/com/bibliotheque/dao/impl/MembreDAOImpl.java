package com.bibliotheque.dao.impl;

import com.bibliotheque.dao.MembreDAO;
import com.bibliotheque.model.Membre;
import com.bibliotheque.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MembreDAOImpl implements MembreDAO {
    private Connection connection;

    public MembreDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void save(Membre membre) throws SQLException {
        String sql = "INSERT INTO membres (nom, prenom, email, date_inscription, actif) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, membre.getNom());
            stmt.setString(2, membre.getPrenom());
            stmt.setString(3, membre.getEmail());
            stmt.setDate(4, Date.valueOf(membre.getDateInscription()));
            stmt.setBoolean(5, membre.isActif());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    membre.setIdMembre(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public Membre findById(Object id) throws SQLException {
        String sql = "SELECT * FROM membres WHERE id_membre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, (Integer) id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToMembre(rs);
            }
        }
        return null;
    }

    @Override
    public List<Membre> findAll() throws SQLException {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT * FROM membres ORDER BY nom, prenom";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) membres.add(mapResultSetToMembre(rs));
        }
        return membres;
    }

    @Override
    public void update(Membre membre) throws SQLException {
        String sql = "UPDATE membres SET nom = ?, prenom = ?, email = ?, date_inscription = ?, actif = ? WHERE id_membre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, membre.getNom());
            stmt.setString(2, membre.getPrenom());
            stmt.setString(3, membre.getEmail());
            stmt.setDate(4, Date.valueOf(membre.getDateInscription()));
            stmt.setBoolean(5, membre.isActif());
            stmt.setInt(6, membre.getIdMembre());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Object id) throws SQLException {
        String sql = "DELETE FROM membres WHERE id_membre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, (Integer) id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Membre findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM membres WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToMembre(rs);
            }
        }
        return null;
    }

    @Override
    public List<Membre> findActifs() throws SQLException {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT * FROM membres WHERE actif = TRUE ORDER BY nom, prenom";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) membres.add(mapResultSetToMembre(rs));
        }
        return membres;
    }

    @Override
    public List<Membre> findByNom(String nom) throws SQLException {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT * FROM membres WHERE nom LIKE ? ORDER BY nom, prenom";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) membres.add(mapResultSetToMembre(rs));
            }
        }
        return membres;
    }

    private Membre mapResultSetToMembre(ResultSet rs) throws SQLException {
        Membre m = new Membre();
        m.setIdMembre(rs.getInt("id_membre"));
        m.setNom(rs.getString("nom"));
        m.setPrenom(rs.getString("prenom"));
        m.setEmail(rs.getString("email"));
        Date d = rs.getDate("date_inscription");
        if (d != null) m.setDateInscription(d.toLocalDate());
        m.setActif(rs.getBoolean("actif"));
        return m;
    }
}

