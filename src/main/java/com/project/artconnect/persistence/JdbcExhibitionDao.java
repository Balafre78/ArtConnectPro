package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ExhibitionDao.
 * Table: Exhibition(id_exhibition, title, start_date, end_date, description, curator_id, theme)
 * Relation galerie→exposition : table Organises(id_gallery, id_exhibition)
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    private static final String SQL_FIND_ALL =
            "SELECT id_exhibition, title, start_date, end_date, description, curator_id, theme FROM Exhibition";

    private static final String SQL_SAVE =
            "INSERT INTO Exhibition (id_exhibition, title, start_date, end_date, description, curator_id, theme) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE Exhibition SET start_date=?, end_date=?, description=?, curator_id=?, theme=? WHERE title=?";

    private static final String SQL_DELETE_EXHIBITS =
            "DELETE FROM Exhibits WHERE id_exhibition = (SELECT id_exhibition FROM Exhibition WHERE title=?)";

    private static final String SQL_DELETE_ORGANISES =
            "DELETE FROM Organises WHERE id_exhibition = (SELECT id_exhibition FROM Exhibition WHERE title=?)";

    private static final String SQL_DELETE =
            "DELETE FROM Exhibition WHERE title=?";

    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition ex = new Exhibition();
        ex.setTitle(rs.getString("title"));
        Date start = rs.getDate("start_date");
        if (start != null) ex.setStartDate(start.toLocalDate());
        Date end = rs.getDate("end_date");
        if (end != null) ex.setEndDate(end.toLocalDate());
        ex.setDescription(rs.getString("description"));
        ex.setTheme(rs.getString("theme"));
        // curator_id → on met le nom dans curatorName si besoin
        int curatorId = rs.getInt("curator_id");
        if (!rs.wasNull()) {
            ex.setCuratorName(String.valueOf(curatorId));
        }
        return ex;
    }

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll exhibitions", e);
        }
        return list;
    }

    @Override
    public void save(Exhibition exhibition) {
        String id = "E_" + exhibition.getTitle().replaceAll("\\s+", "_");
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE)) {
            ps.setString(1, id);
            ps.setString(2, exhibition.getTitle());
            ps.setDate(3, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(4, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(5, exhibition.getDescription());
            // curator_id : null si pas de curateur
            ps.setNull(6, Types.INTEGER);
            ps.setString(7, exhibition.getTheme());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save exhibition: " + exhibition.getTitle(), e);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setDate(1, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(2, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(3, exhibition.getDescription());
            ps.setNull(4, Types.INTEGER);
            ps.setString(5, exhibition.getTheme());
            ps.setString(6, exhibition.getTitle()); // WHERE
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update exhibition: " + exhibition.getTitle(), e);
        }
    }

    /**
     * TRANSACTION : supprime les liens Exhibits + Organises puis l'exposition.
     */
    @Override
    public void delete(String title) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // 1. Supprimer les liens Exhibits (œuvres ↔ exposition)
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_EXHIBITS)) {
                ps.setString(1, title);
                ps.executeUpdate();
            }

            // 2. Supprimer les liens Organises (galerie ↔ exposition)
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ORGANISES)) {
                ps.setString(1, title);
                ps.executeUpdate();
            }

            // 3. Supprimer l'exposition
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
                ps.setString(1, title);
                ps.executeUpdate();
            }

            conn.commit(); // COMMIT

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw new RuntimeException("Erreur delete exhibition (rollback): " + title, e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }
}