package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtistDao.
 * TODO: Students must implement this using JDBC and SQL.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll(){
        String sql = "SELECT * FROM ARTISTS";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Artist> liste = new ArrayList<>();
            while (rs.next()) {
                liste.add(new Artist(
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getInt("birthYear"),
                        rs.getString("contactEmail"),
                        rs.getString("city")
                ));
            }
            return liste;
        }  catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des artistes", e);
        }
    }

    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO ARTISTS (name, bio, birthYear, contactEmail, city, isActive) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBio());
            ps.setInt(3, artist.getBirthYear());
            ps.setString(4, artist.getContactEmail());
            ps.setString(5, artist.getCity());
            ps.setBoolean(6, artist.isActive());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion d'un artiste", e);
        }

    }

    @Override
    public void update(Artist artist) {
        // TODO: Implement UPDATE artist SET ... WHERE name = ?
        String sql = "UPDATE ARTISTS SET bio = ?, birthYear = ?, contactEmail = ?, city = ?, isActive = ? WHERE name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artist.getBio());
            ps.setInt(2, artist.getBirthYear());
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getCity());
            ps.setBoolean(5, artist.isActive());
            ps.setString(6, artist.getName());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'artiste", e);
        }
    }

    @Override
    public void delete(String artistName) {
        // TODO: Implement DELETE FROM artist WHERE name = ?
        String sql = "DELETE FROM ARTISTS WHERE name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artistName);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'artiste", e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        // TODO: Implement SELECT * FROM artist WHERE city = ?
        String sql = "SELECT * FROM ARTISTS WHERE city = ?";
        List<Artist> liste = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Artist(
                            rs.getString("name"),
                            rs.getString("bio"),
                            rs.getInt("birthYear"),
                            rs.getString("contactEmail"),
                            rs.getString("city")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par ville", e);
        }
        return liste;
        }
    }
