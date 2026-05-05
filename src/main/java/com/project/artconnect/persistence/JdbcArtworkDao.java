package com.project.artconnect.persistence;

import static com.project.artconnect.persistence.JdbcArtistDao.getIdForArtist;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;


import java.sql.*;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    // Lier l'instance d'objet à son id dans la base
    private static final Map<Artwork, String> objectToIdMap = new IdentityHashMap<>();

    @Override
    public List<Artwork> findAll() {
        String sql = "SELECT aw.*, ar.name as artist_name, ar.bio as artist_bio, ar.birthYear as artist_birth, ar.contactEmail as artist_email, ar.city as artist_city FROM Artwork aw JOIN ARTISTS ar ON aw.artist_id = ar.id";

        List<Artwork> artworks = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Artist artist = new Artist(
                        rs.getString("artist_name"),
                        rs.getString("artist_bio"),
                        rs.getInt("artist_birth"),
                        rs.getString("artist_email"),
                        rs.getString("artist_city")
                );

                Artwork artwork = new Artwork(
                        rs.getString("title"),
                        rs.getInt("creation_year"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        artist
                );
                artwork.setDescription(rs.getString("description"));
                artwork.setMedium(rs.getString("medium"));

                objectToIdMap.put(artwork, rs.getString("id_artwork"));

                artworks.add(artwork);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artworks", e);
        }
        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        String sqlArtwork = "INSERT INTO Artwork (id_artwork, title, price, creation_year, description, type, medium, artist_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCreates = "INSERT INTO Creates (id_artist, id_artwork) VALUES (?, ?)";

        String newArtworkId = UUID.randomUUID().toString();

        try {
            Connection conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // 1. insertion dans Artworks
            try (PreparedStatement psArt = conn.prepareStatement(sqlArtwork)) {
                psArt.setString(1, newArtworkId);
                psArt.setString(2, artwork.getTitle());
                psArt.setDouble(3, artwork.getPrice());
                // Conversion Integer -> Date SQL (si ta colonne est DATE)
                psArt.setDate(4, Date.valueOf(artwork.getCreationYear() + "-01-01"));
                psArt.setString(5, artwork.getDescription());
                psArt.setString(6, artwork.getType());
                psArt.setString(7, artwork.getMedium());
                psArt.executeUpdate();
            }
            // 2.insertion dans Creates
            try (PreparedStatement psLink = conn.prepareStatement(sqlCreates)) {
                String artistId = JdbcArtistDao.getIdForArtist(artwork.getArtist());

                if (artistId == null) {
                    throw new SQLException("Lien impossible : L'id de l'artiste est introuvable dans le registre.");
                }
                psLink.setString(1, artistId);
                psLink.setString(2, newArtworkId);
                psLink.executeUpdate();
            }

            conn.commit();
            objectToIdMap.put(artwork, newArtworkId);
            conn.setAutoCommit(true);
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur save", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String id = objectToIdMap.get(artwork);
        if (id == null) {
            throw new IllegalStateException("L'oeuvre introuvable.");
        }

        String sql = "UPDATE Artwork SET title = ?, price = ?, creation_year = ?, description = ?, type = ?, medium = ? WHERE id_artwork = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artwork.getTitle());
            ps.setDouble(2, artwork.getPrice());
            ps.setDate(3, java.sql.Date.valueOf(artwork.getCreationYear() + "-01-01"));
            ps.setString(4, artwork.getDescription());
            ps.setString(5, artwork.getType());
            ps.setString(6, artwork.getMedium());
            ps.setString(7, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'update de l'artwork", e);
        }
    }

    @Override
    public void delete(String title) {
        String idToRemove = null;
        for (Map.Entry<Artwork, String> entry : objectToIdMap.entrySet()) {
            if (entry.getKey().getTitle().equals(title)) {
                idToRemove = entry.getValue();
                break;
            }
        }

        // 2. Supprimer en cascade (si ta DB n'a pas ON DELETE CASCADE)
        String sqlLink = "DELETE FROM Creates WHERE id_artwork = (SELECT id_artwork FROM Artwork WHERE title = ?)";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlLink)) {

                ps1.setString(1, title);
                ps1.executeUpdate();

                conn.commit();

                if (idToRemove != null) {
                    String finalId = idToRemove;
                    objectToIdMap.entrySet().removeIf(e -> e.getValue().equals(finalId));
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> results = new ArrayList<>();
        String sql = "SELECT a.*, r.* FROM Artwork a JOIN Creates c ON a.id_artwork = c.id_artwork JOIN Artist r ON c.id_artist = r.id_artist WHERE r.name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, artistName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artist artist = new Artist(
                            rs.getString("name"),
                            rs.getString("bio"),
                            rs.getInt("birthYear"),
                            rs.getString("contactEmail"),
                            rs.getString("city")
                    );

                    Artwork artwork = new Artwork(
                            rs.getString("title"),
                            rs.getDate("creation_year").toLocalDate().getYear(),
                            rs.getString("type"),
                            rs.getDouble("price"),
                            artist
                    );
                    artwork.setDescription(rs.getString("description"));
                    artwork.setMedium(rs.getString("medium"));

                    objectToIdMap.put(artwork, rs.getString("id_artwork"));

                    results.add(artwork);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByArtistName", e);
        }
        return results;
    }
}
