package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of WorkshopDao.
 * Table: Workshop(id_workshop, title, duration_minutes, max_participants, price, location_id)
 * Relation artiste→atelier : table Animates(id_artist, id_workshop)
 */
public class JdbcWorkshopDao implements WorkshopDao {

    private static final String SQL_FIND_BY_ID =
            "SELECT w.id_workshop, w.title, w.duration_minutes, w.max_participants, w.price, " +
                    "ar.name as instructor_name " +
                    "FROM Workshop w " +
                    "LEFT JOIN Animates a ON w.id_workshop = a.id_workshop " +
                    "LEFT JOIN Artist ar ON a.id_artist = ar.id_artist " +
                    "WHERE w.id_workshop=?";

    private static final String SQL_FIND_ALL =
            "SELECT w.id_workshop, w.title, w.duration_minutes, w.max_participants, w.price, " +
                    "ar.name as instructor_name " +
                    "FROM Workshop w " +
                    "LEFT JOIN Animates a ON w.id_workshop = a.id_workshop " +
                    "LEFT JOIN Artist ar ON a.id_artist = ar.id_artist";

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setTitle(rs.getString("title"));
        workshop.setDurationMinutes(rs.getInt("duration_minutes"));
        workshop.setMaxParticipants(rs.getInt("max_participants"));
        workshop.setPrice(rs.getDouble("price"));
        String instructorName = rs.getString("instructor_name");
        if (instructorName != null) {
            Artist instructor = new Artist();
            instructor.setName(instructorName);
            workshop.setInstructor(instructor);
        }
        return workshop;
    }

    /**
     * findById prend un Long mais notre PK est un VARCHAR (ex: "W1").
     * On convertit le Long en String pour la requête.
     */
    @Override
    public Optional<Workshop> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, String.valueOf(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById workshop: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll workshops", e);
        }
        return list;
    }
}