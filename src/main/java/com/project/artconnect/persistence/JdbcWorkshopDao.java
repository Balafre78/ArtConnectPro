package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import javax.sound.sampled.SourceDataLine;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcWorkshopDao implements WorkshopDao {

    private static final String SQL_FIND_ALL =
            "SELECT w.id_workshop, w.title, w.level, w.description, w.date, w.duration_minutes, w.max_participants, w.price, w.location, ar.name as instructor_name " +
                    "FROM Workshop w " +
                    "JOIN Artist ar ON w.id_instructor = ar.id_artist";

    private static final String SQL_SAVE =
            "INSERT INTO Workshop (id_workshop, title, level, description, date, duration_minutes, max_participants, price, location, id_instructor) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_DELETE =
            "DELETE FROM Workshop WHERE id_workshop = ?";

    private static final String SQL_UPDATE =
            "UPDATE Workshop SET title = ?, level = ?, description = ?, date = ?, duration_minutes = ?, max_participants = ?, price = ?, location = ?, id_instructor = ? where id_workshop = ?";

    private static final Map<Workshop, String> workshopToIdMap = new HashMap<>();
    private static final JdbcArtistDao jdbcArtistDao = new JdbcArtistDao();

    @Override
    public List<Workshop> findAll() {
        List<Workshop> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artist instructor = jdbcArtistDao.findByName(rs.getString("instructor_name"));
                Workshop workshop = new Workshop(
                        rs.getString("title"),
                        rs.getDate("date").toLocalDate().atStartOfDay(),
                        instructor,
                        rs.getDouble("price")
                );
                workshop.setDurationMinutes(rs.getInt("duration_minutes"));
                workshop.setMaxParticipants(rs.getInt("max_participants"));
                workshop.setLocation(rs.getString("location"));
                workshop.setDescription(rs.getString("description"));
                workshop.setLevel(rs.getString("level"));

                workshopToIdMap.put(workshop, rs.getString("id_workshop"));
                list.add(workshop);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll workshops", e);
        }
        return list;
    }

    @Override
    public void save(Workshop workshop) {
        String id = "W_" + workshop.getTitle().replaceAll("\\s+", "_");
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE);) {
            ps.setString(1, id);
            ps.setString(2, workshop.getTitle());
            ps.setString(3, workshop.getLevel());
            ps.setString(4, workshop.getDescription());
            ps.setDate(5, workshop.getDate()!= null ? Date.valueOf(workshop.getDate().toLocalDate()) : null);
            ps.setInt(6, workshop.getDurationMinutes());
            ps.setInt(7, workshop.getMaxParticipants());
            ps.setDouble(8, workshop.getPrice());
            ps.setString(9, workshop.getLocation());
            ps.setString(10, jdbcArtistDao.findId(workshop.getInstructor()));
            ps.executeUpdate();
            workshopToIdMap.put(workshop, id);
        }  catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde d'une activité :", e);
        }
    }

    @Override
    public void update(Workshop workshop) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, workshop.getTitle());
            ps.setString(2, workshop.getLevel());
            ps.setString(3, workshop.getDescription());
            ps.setDate(4, workshop.getDate()!= null ? Date.valueOf(workshop.getDate().toLocalDate()) : null);
            ps.setInt(5, workshop.getDurationMinutes());
            ps.setInt(6, workshop.getMaxParticipants());
            ps.setDouble(7, workshop.getPrice());
            ps.setString(8, workshop.getLocation());
            ps.setString(9, jdbcArtistDao.findId(workshop.getInstructor()));
            ps.setString(10, findId(workshop));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'artiste", e);
        }
    }

    @Override
    public void delete(Workshop workshop) {
        String workshopId = findId(workshop);
        if (workshopId == null) {
            throw new RuntimeException("Workshop introuvable : " + workshop.getTitle());
        }
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, workshopId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du workshop "+ workshop.getTitle(),e);
        }
    }


    private String findId(Workshop workshop) {
        String workshopId;
        for (Map.Entry<Workshop, String> entry : workshopToIdMap.entrySet()) {
            if (entry.getKey().getTitle().equals(workshop.getTitle())) {
                workshopId = entry.getValue();
                return workshopId;
            }
        }
        return null;
    }
}
