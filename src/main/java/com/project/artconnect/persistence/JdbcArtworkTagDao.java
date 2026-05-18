package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.dao.ArtworkTagDao;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation for ArtworkTagDao.
 */
public class JdbcArtworkTagDao implements ArtworkTagDao {

    private static final String SQL_FIND_ALL =
            "SELECT * FROM ArtworkTag";

    private static final String SQL_FIND_FOR_ARTWORK =
            "SELECT at.name_tag FROM ArtworkTag at " +
            "JOIN Has h on at.id_artwork_tag = h.id_artwork_tag " +
            "WHERE h.id_artwork = ?";

    private static final Map<ArtworkTag, String> artworkToIdMap = new HashMap<>();

    public List<ArtworkTag> findAll() {
        List<ArtworkTag> list = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ArtworkTag tag = new ArtworkTag(rs.getString("name_tag"));
                list.add(tag);
                artworkToIdMap.put(tag, rs.getString("id_artwork_tag"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des tags", e);
        }
        return list;
    }

    public List<ArtworkTag> findForArtist(String idArtwork) {
        List<ArtworkTag> list = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_FOR_ARTWORK)) {

            ps.setString(1, idArtwork);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new ArtworkTag(rs.getString("name_tag")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des tags de l'artwork "+idArtwork, e);
        }
        return list;
    }

    public String findId(ArtworkTag tag) {
        return artworkToIdMap.get(tag);
    }
}
