package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of ArtistDao.
 * Table: Artist(id_artist, name, bio, birth_year, contact_email, phone, city)
 */
public class JdbcArtistDao implements ArtistDao {
    private static final String SQL_FIND_ALL =
            "SELECT id_artist, name, bio, birth_year, contact_email, phone, city FROM Artist";

    private static final String SQL_SAVE =
            "INSERT INTO Artist (id_artist, name, bio, birth_year, contact_email, phone, city) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE Artist SET bio=?, birth_year=?, contact_email=?, phone=?, city=? WHERE name=?";

    private static final String SQL_DELETE =
            "DELETE FROM Artist WHERE name=?";

    private static final String SQL_FIND_BY_CITY =
            "SELECT id_artist, name, bio, birth_year, contact_email, phone, city FROM Artist WHERE city=?";
    private Artist mapRow(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        artist.setName(rs.getString("name"));
        artist.setBio(rs.getString("bio"));
        // birth_year est une DATE en SQL, on récupère juste l'année
        Date birthDate = rs.getDate("birth_year");
        if (birthDate != null) {
            artist.setBirthYear(birthDate.toLocalDate().getYear());
        }
        artist.setContactEmail(rs.getString("contact_email"));
        artist.setPhone(rs.getString("phone"));
        artist.setCity(rs.getString("city"));
        return artist;
    }

    @Override
    public List<Artist> findAll() {
        List<Artist> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artists", e);
        }
        return list;
    }

    @Override
    public void save(Artist artist) {
        // On génère un id simple basé sur le nom (ex: "A_Picasso")
        String id = "A_" + artist.getName().replaceAll("\\s+", "_");
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE)) {
            ps.setString(1, id);
            ps.setString(2, artist.getName());
            ps.setString(3, artist.getBio());
            // Convertir l'année en date (1er janvier de l'année)
            if (artist.getBirthYear() != null) {
                ps.setDate(4, Date.valueOf(artist.getBirthYear() + "-01-01"));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, artist.getContactEmail());
            ps.setString(6, artist.getPhone());
            ps.setString(7, artist.getCity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save artist: " + artist.getName(), e);
        }
    }

    @Override
    public void update(Artist artist) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, artist.getBio());
            if (artist.getBirthYear() != null) {
                ps.setDate(2, Date.valueOf(artist.getBirthYear() + "-01-01"));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getPhone());
            ps.setString(5, artist.getCity());
            ps.setString(6, artist.getName()); // WHERE
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update artist: " + artist.getName(), e);
        }
    }

    @Override
    public void delete(String artistName) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, artistName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete artist: " + artistName, e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CITY)) {
            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByCity: " + city, e);
        }
        return list;
    }
}
