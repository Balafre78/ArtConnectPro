package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.dao.ArtworkTagDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {
    private static final String SQL_FIND_ALL =
            "SELECT * FROM ArtworkArtist";

    private static final String SQL_SAVE_ARTWORK =
            "INSERT INTO Artwork (id_artwork, title, price, creation_year, description, type, medium, status, id_artist) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE Artwork SET price = ?, creation_year = ?, description = ?, type = ?, medium = ?, status = ? WHERE title = ?";

    private static final String SQL_DELETE =
            "DELETE FROM Artwork WHERE id_artwork=?";

    private static final String SQL_FIND_BY_ARTIST =
            "SELECT * FROM ArtworkArtist " +
            "WHERE artist_name = ?";

    private static final Map<Artwork, String> artworkToIdMap = new HashMap<>();
    private static final JdbcArtistDao jdbcArtistDao = new JdbcArtistDao();
    private static final JdbcArtworkTagDao jdbcArtworkTagDao = new JdbcArtworkTagDao();

    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Artist artist = jdbcArtistDao.findByName(rs.getString("artist_name"));
                Artwork artwork = new Artwork(
                        rs.getString("title"),
                        rs.getDate("creation_year").toLocalDate().getYear(),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        artist
                );
                artwork.setDescription(rs.getString("description"));
                artwork.setMedium(rs.getString("medium"));

                String s = rs.getString("status");
                artwork.setStatus(Artwork.Status.valueOf(s));

                // Tags
                List<ArtworkTag> tags = new ArrayList<>();
                String tagsString = rs.getString("tags");
                if (tagsString != null && !tagsString.isEmpty()) {
                    String[] splitTags = tagsString.split(",");
                    for (String tagName : splitTags) {
                        tags.add(new ArtworkTag(tagName.trim()));
                    }
                }
                artwork.setTags(tags);

                artworkToIdMap.put(artwork, rs.getString("id_artwork"));
                artworks.add(artwork);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artworks", e);
        }

        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        String id = "W_" + artwork.getTitle().replaceAll("\\s+", "_");
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE_ARTWORK)) {
            ps.setString(1, id);
            ps.setString(2, artwork.getTitle());
            ps.setDouble(3, artwork.getPrice());
            ps.setDate(4, Date.valueOf(artwork.getCreationYear() + "-01-01"));
            ps.setString(5, artwork.getDescription());
            ps.setString(6, artwork.getType());
            ps.setString(7, artwork.getMedium());
            ps.setString(8, artwork.getStatus().toString());
            ps.setString(9, jdbcArtistDao.findId(artwork.getArtist()));
            ps.executeUpdate();

            try (PreparedStatement ps2 = conn.prepareStatement("INSERT INTO Has (id_artwork, id_artwork_tag) VALUES (?, ?)")) {
                for (ArtworkTag tag : artwork.getTags()) {
                    ps2.setString(1, id);
                    ps2.setString(2, jdbcArtworkTagDao.findId(tag));
                    ps2.addBatch();
                }
                ps2.executeBatch();
            }

            artworkToIdMap.put(artwork, id);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur save artwork", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String id = artwork.getTitle();
        if (id == null) {
            throw new IllegalStateException("L'oeuvre est introuvable.");
        }
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setDouble(1, artwork.getPrice());
            ps.setDate(2, java.sql.Date.valueOf(artwork.getCreationYear() + "-01-01"));
            ps.setString(3, artwork.getDescription());
            ps.setString(4, artwork.getType());
            ps.setString(5, artwork.getMedium());
            ps.setString(6, artwork.getStatus().toString());
            ps.setString(7, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'update de l'artwork "+artwork.getTitle(), e);
        }
    }

    @Override
    public void delete(Artwork artwork) {
        String idToRemove = artworkToIdMap.get(artwork);
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Has WHERE id_artwork = ?")) {
            ps2.setString(1, idToRemove);
            ps2.executeUpdate();
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
                ps.setString(1, idToRemove);
                ps.executeUpdate();
                if (idToRemove != null) {
                    final String id = idToRemove;
                    artworkToIdMap.entrySet().removeIf(e -> e.getValue().equals(id));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> results = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ARTIST)) {

            ps.setString(1, artistName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Artist artist = jdbcArtistDao.findByName(rs.getString("artist_name"));
                    Artwork artwork = new Artwork(
                            rs.getString("title"),
                            rs.getDate("creation_year").toLocalDate().getYear(),
                            rs.getString("type"),
                            rs.getDouble("price"),
                            artist
                    );
                    artwork.setDescription(rs.getString("description"));
                    artwork.setMedium(rs.getString("medium"));
                    String s = rs.getString("status");
                    artwork.setStatus(Artwork.Status.valueOf(s));
                    artwork.setTags(List.of());

                    results.add(artwork);
                    artworkToIdMap.put(artwork, rs.getString("id_artwork"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByArtistName", e);
        }
        return results;
    }

    public String findId(Artwork artwork) {
        return artworkToIdMap.get(artwork);
    }
}
