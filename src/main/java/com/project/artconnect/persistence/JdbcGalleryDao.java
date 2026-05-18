package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.*;

public class JdbcGalleryDao implements GalleryDao {
    private static final String SQL_FIND_BY_NAME =
            "SELECT id_gallery, name, address, owner_name, opening_hours, contact_phone, rating, website " +
            "FROM Gallery WHERE name=?";

    private static final String SQL_FIND_ALL =
            "SELECT id_gallery, name, address, owner_name, opening_hours, contact_phone, rating, website " +
            "FROM Gallery";

    private static final String SQL_FIND_ID_BY_NAME =
        "SELECT id_gallery FROM Gallery WHERE name = ?";

    private static final Map<Gallery, String> galleryToIdMap = new IdentityHashMap<>();

    @Override
    public Gallery findByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            ps.setString(1, String.valueOf(name));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Gallery gallery = new Gallery(
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getDouble("rating")
                    );
                    gallery.setOwnerName(rs.getString("owner_name"));
                    gallery.setOpeningHours(rs.getString("opening_hours"));
                    gallery.setContactPhone(rs.getString("contact_phone"));
                    gallery.setWebsite(rs.getString("website"));
                    return gallery;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById gallery: " + name, e);
        }
        return null;
    }

    @Override
    public String findIdByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAME)) {
            ps.setString(1, String.valueOf(name));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return rs.getString("id_gallery"); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById gallery: " + name, e);
        }
        return null;
    }

    public List<Gallery> findAll(){
        List<Gallery> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Gallery gallery = new Gallery(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getDouble("rating")
                );
                gallery.setOwnerName(rs.getString("owner_name"));
                gallery.setOpeningHours(rs.getString("opening_hours"));
                gallery.setContactPhone(rs.getString("contact_phone"));
                gallery.setWebsite(rs.getString("website"));
                list.add(gallery);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll galleries", e);
        }
        return list;
    }
}
