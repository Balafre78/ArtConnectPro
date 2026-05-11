package com.project.artconnect.persistence;

import static com.project.artconnect.persistence.JdbcArtistDao.getIdForArtist;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of ArtworkDao.
 * Table: Artwork(id_artwork, title, price, creation_year, description, type, medium)
 * Relation artiste→oeuvre : table Creates(id_artist, id_artwork)
 */
public class JdbcArtworkDao implements ArtworkDao {

    private static final String SQL_FIND_ALL =
            "SELECT id_artwork, title, price, creation_year, description, type, medium FROM Artwork";

    private static final String SQL_SAVE =
            "INSERT INTO Artwork (id_artwork, title, price, creation_year, description, type, medium) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE Artwork SET price=?, creation_year=?, description=?, type=?, medium=? WHERE title=?";

    private static final String SQL_DELETE_CREATES =
            "DELETE FROM Creates WHERE id_artwork = (SELECT id_artwork FROM Artwork WHERE title=?)";

    private static final String SQL_DELETE =
            "DELETE FROM Artwork WHERE title=?";

    // Jointure via la table Creates pour trouver les œuvres d'un artiste
    private static final String SQL_FIND_BY_ARTIST =
            "SELECT a.id_artwork, a.title, a.price, a.creation_year, a.description, a.type, a.medium " +
                    "FROM Artwork a " +
                    "JOIN Creates c ON a.id_artwork = c.id_artwork " +
                    "JOIN Artist ar ON c.id_artist = ar.id_artist " +
                    "WHERE ar.name = ?";

    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artwork artwork = new Artwork();
        artwork.setTitle(rs.getString("title"));
        artwork.setPrice(rs.getDouble("price"));
        Date creationDate = rs.getDate("creation_year");
        if (creationDate != null) {
            artwork.setCreationYear(creationDate.toLocalDate().getYear());
        }
        artwork.setDescription(rs.getString("description"));
        artwork.setType(rs.getString("type"));
        artwork.setMedium(rs.getString("medium"));
        return artwork;
    }

    @Override
    public List<Artwork> findAll() {
        List<Artwork> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artworks", e);
        }
        return list;
    }

    @Override
    public void save(Artwork artwork) {
        String id = "W_" + artwork.getTitle().replaceAll("\\s+", "_");
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE)) {
            ps.setString(1, id);
            ps.setString(2, artwork.getTitle());
            ps.setDouble(3, artwork.getPrice());
            if (artwork.getCreationYear() != null) {
                ps.setDate(4, Date.valueOf(artwork.getCreationYear() + "-01-01"));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, artwork.getDescription());
            ps.setString(6, artwork.getType());
            ps.setString(7, artwork.getMedium());
            ps.executeUpdate();
          
          /*
                  String sqlCreates = "INSERT INTO Creates (id_artist, id_artwork) VALUES (?, ?)";
        String newArtworkId = UUID.randomUUID().toString();

        try {
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
            */
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save artwork: " + artwork.getTitle(), e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setDouble(1, artwork.getPrice());
            if (artwork.getCreationYear() != null) {
                ps.setDate(2, Date.valueOf(artwork.getCreationYear() + "-01-01"));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, artwork.getDescription());
            ps.setString(4, artwork.getType());
            ps.setString(5, artwork.getMedium());
            ps.setString(6, artwork.getTitle()); // WHERE
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update artwork: " + artwork.getTitle(), e);
        }
    }

    /**
     * TRANSACTION : supprime le lien dans Creates puis l'œuvre.
     */
    @Override
    public void delete(String title) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // 1. Supprimer les liens dans Creates (FK)
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_CREATES)) {
                ps.setString(1, title);
                ps.executeUpdate();
            }

            // 2. Supprimer l'œuvre
            try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
                ps.setString(1, title);
                ps.executeUpdate();
            }

            conn.commit(); // COMMIT

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw new RuntimeException("Erreur delete artwork (rollback): " + title, e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ARTIST)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork artwork = mapRow(rs);
                    Artist artist = new Artist();
                    artist.setName(artistName);
                    artwork.setArtist(artist);
                    list.add(artwork);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByArtistName: " + artistName, e);
        }
        return list;
    }
}