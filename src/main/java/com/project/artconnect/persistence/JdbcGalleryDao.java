package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of GalleryDao.
 * Table: Gallery(id_gallery, name, address, owner_name, opening_hours, contact_phone, rating, website)
 */
public class JdbcGalleryDao implements GalleryDao {

    private static final String SQL_FIND_BY_ID =
            "SELECT id_gallery, name, address, owner_name, opening_hours, contact_phone, rating, website " +
                    "FROM Gallery WHERE id_gallery=?";

    private static final String SQL_FIND_ALL =
            "SELECT id_gallery, name, address, owner_name, opening_hours, contact_phone, rating, website " +
                    "FROM Gallery";

    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("name"));
        gallery.setAddress(rs.getString("address"));
        gallery.setOwnerName(rs.getString("owner_name"));
        gallery.setOpeningHours(rs.getString("opening_hours"));
        gallery.setContactPhone(rs.getString("contact_phone"));
        gallery.setRating(rs.getDouble("rating"));
        gallery.setWebsite(rs.getString("website"));
        return gallery;
    }

    /**
     * findById prend un Long mais notre PK est un VARCHAR (ex: "G1").
     * On convertit le Long en String pour la requête.
     */
    @Override
    public Optional<Gallery> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, String.valueOf(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById gallery: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        List<Gallery> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll galleries", e);
        }
        return list;
    }
}