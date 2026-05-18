package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.*;
import com.project.artconnect.util.ConnectionManager;
import javafx.beans.property.SetProperty;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class JdbcExhibitionDao implements ExhibitionDao {
    private static final String SQL_FIND_ALL =
            "SELECT e.id_exhibition, e.title, e.start_date, e.end_date, e.description, e.curator_name, e.theme, g.name, g.address, g.rating " +
            "FROM Exhibition e " +
            "JOIN Organises o ON e.id_exhibition = o.id_exhibition " +
            "JOIN Gallery g ON o.id_gallery = g.id_gallery ";

    private static final String SQL_SAVE_EXHIBITION =
            "INSERT INTO Exhibition (id_exhibition, title, start_date, end_date, description, curator_name, theme) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SAVE_ORGANISES =
            "INSERT INTO Organises (id_gallery, id_exhibition) " +
            "VALUES (?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE Exhibition SET start_date=?, end_date=?, description=?, curator_name=?, theme=? WHERE title=?";

    private static final String SQL_DELETE =
            "DELETE FROM Exhibition WHERE id_exhibition=?";

    private static final Map<Exhibition, String> exhibitionToIdMap = new IdentityHashMap<>();
    private static final JdbcGalleryDao jdbcGalleryDao = new JdbcGalleryDao();

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Gallery gallery = new Gallery(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getDouble("rating")
                );
                Exhibition exhibition = new Exhibition(
                        rs.getString("title"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        gallery
                );
                exhibition.setTheme(rs.getString("theme"));
                exhibition.setDescription(rs.getString("description"));
                exhibition.setCuratorName(rs.getString("curator_name"));

                exhibitionToIdMap.put(exhibition, rs.getString("id_exhibition"));
                list.add(exhibition);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll exhibitions", e);
        }
    }

    @Override
    public void save(Exhibition exhibition) {
        String id = "E_" + exhibition.getTitle().replaceAll("\\s+", "_");
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE_EXHIBITION);
            PreparedStatement psOrganises  = conn.prepareStatement(SQL_SAVE_ORGANISES)) {

            ps.setString(1, id);
            ps.setString(2, exhibition.getTitle());
            ps.setDate(3, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(4, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(5, exhibition.getDescription());
            ps.setString(6, exhibition.getCuratorName());
            ps.setString(7, exhibition.getTheme());
            ps.executeUpdate();

            psOrganises.setString(1, jdbcGalleryDao.findIdByName(exhibition.getGallery().getName()));
            psOrganises.setString(2, id);
            psOrganises.executeUpdate();

            exhibitionToIdMap.put(exhibition, id);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur save exhibition: " + exhibition.getTitle(), e);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setDate(1, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(2, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(3, exhibition.getDescription());
            ps.setString(4, exhibition.getCuratorName());
            ps.setString(5, exhibition.getTheme());
            ps.setString(6, exhibition.getTitle());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur update exhibition: " + exhibition.getTitle(), e);
        }
    }

    @Override
    public void delete(Exhibition exhibition) {
        String id = findId(exhibition);
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, id);
            ps.executeUpdate();

            if (id != null) {
                exhibitionToIdMap.keySet().removeIf(e -> e.equals(exhibition));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'exposition "+exhibition.getTitle(), e);
        }
    }

    @Override
    public List<String> findAllThemes() {
        Set<String> set = new HashSet<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                set.add(rs.getString("theme"));
            }
            return new ArrayList<>(set);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur finding les themes : ", e);
        }
    }

    public List<String> findAllCurators() {
        Set<String> set = new HashSet<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                set.add(rs.getString("curator_name"));
            }
            return new ArrayList<>(set);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur finding les themes : ", e);
        }
    }

    public String findId(Exhibition exhibition) {
        return exhibitionToIdMap.get(exhibition);
    }
}
