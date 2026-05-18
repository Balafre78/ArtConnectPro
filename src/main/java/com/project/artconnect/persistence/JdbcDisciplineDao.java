package com.project.artconnect.persistence;

import com.project.artconnect.dao.DisciplineDao;
import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.*;
import java.sql.Date;

public class JdbcDisciplineDao implements DisciplineDao {

    private static final String SQL_FIND_ALL =
            "SELECT name FROM Discipline";

    @Override
    public List<Discipline> findAll() {
        List<Discipline> list = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Discipline(rs.getString("name")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur load disciplines", e);
        }

        return list;
    }
}
