package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation for ArtistDao.
 * TODO: Students must implement this using JDBC and SQL.
 */
public class JdbcArtistDao implements ArtistDao {

    private static final Map<Artist, String> artistToIdMap = new IdentityHashMap<>();

    public static String getIdForArtist(Artist artist) {
        return artistToIdMap.get(artist);
    }

    @Override
    public List<Artist> findAll(){
        String sql = "SELECT * FROM ARTISTS";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Artist> liste = new ArrayList<>();
            while (rs.next()) {
                Artist a = new Artist(
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getDate("birthYear").toLocalDate().getYear(),
                        rs.getString("contactEmail"),
                        rs.getString("city")
                );
                artistToIdMap.put(a, rs.getString("id"));
                liste.add(a);
            }
            return liste;
        }  catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des artistes", e);
        }
    }

    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO ARTISTS (id, name, bio, birthYear, contactEmail, phone, city, isActive) VALUES (?, ?, ?, ?, ?, ?)";

        String newId = UUID.randomUUID().toString();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, newId);
            ps.setString(2, artist.getName());
            ps.setString(3, artist.getBio());
            ps.setInt(4, artist.getBirthYear());
            ps.setString(5, artist.getContactEmail());
            ps.setString(6, artist.getPhone());
            ps.setString(7, artist.getCity());
            ps.setBoolean(8, artist.isActive());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion d'un artiste", e);
        }

    }

    @Override
    public void update(Artist artist) {
        String id = artistToIdMap.get(artist);
        String sql = "UPDATE ARTISTS SET name = ?, bio = ?, birthYear = ?, contactEmail = ?, city = ? WHERE id = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBio());
            ps.setInt(3, artist.getBirthYear());
            ps.setString(4, artist.getContactEmail());
            ps.setString(5, artist.getCity());
            ps.setString(6, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'artiste", e);
        }
    }

    @Override
    public void delete(String artistName) {
        String sql = "DELETE FROM ARTISTS WHERE name = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artistName);
            int nbAffectedRows = ps.executeUpdate();

            if (nbAffectedRows > 0) {
                artistToIdMap.keySet().removeIf(artist -> artist.getName().equals(artistName));
            }

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
