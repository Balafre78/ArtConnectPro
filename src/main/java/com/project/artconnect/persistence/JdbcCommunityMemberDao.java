package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CommunityMemberDao.
 * Table: CommunityMember(id_commu_member, name, email, birth_year, phone, city, membership_type)
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    private static final String SQL_FIND_BY_ID =
            "SELECT id_commu_member, name, email, birth_year, phone, city, membership_type " +
                    "FROM CommunityMember WHERE id_commu_member=?";

    private static final String SQL_FIND_ALL =
            "SELECT id_commu_member, name, email, birth_year, phone, city, membership_type " +
                    "FROM CommunityMember";

    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember member = new CommunityMember();
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        Date birthDate = rs.getDate("birth_year");
        if (birthDate != null) {
            member.setBirthYear(birthDate.toLocalDate().getYear());
        }
        member.setPhone(rs.getString("phone"));
        member.setCity(rs.getString("city"));
        member.setMembershipType(rs.getString("membership_type"));
        return member;
    }

    /**
     * findById prend un Long mais notre PK est un VARCHAR (ex: "C1").
     * On convertit le Long en String pour la requête.
     */
    @Override
    public Optional<CommunityMember> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, String.valueOf(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById community member: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll community members", e);
        }
        return list;
    }
}