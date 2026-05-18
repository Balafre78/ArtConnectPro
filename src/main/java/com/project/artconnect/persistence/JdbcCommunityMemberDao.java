package com.project.artconnect.persistence;

import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;

import java.sql.*;
import java.util.*;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    private static final String SQL_FIND_BY_ID =
            "SELECT id_commu_member, name, email, birth_year, phone, city, membership_type " +
                    "FROM CommunityMember WHERE id_commu_member=?";

    private static final String SQL_FIND_ALL =
            "SELECT id_commu_member, name, email, birth_year, phone, city, membership_type " +
                    "FROM CommunityMember";

    private static final Map<CommunityMember, String> memberToIdMap = new HashMap<>();

    public Optional<CommunityMember> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, String.valueOf(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CommunityMember member = new CommunityMember(
                        rs.getString("name"),
                        rs.getString("email")
                    );
                    member.setBirthYear(rs.getDate("birth_year").toLocalDate().getYear());
                    member.setPhone(rs.getString("phone"));
                    member.setCity(rs.getString("city"));
                    member.setMembershipType(rs.getString("membership_type"));
                    return Optional.of(member);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById community member: " + id, e);
        }
        return Optional.empty();
    }

    public List<CommunityMember> findAll() {
        List<CommunityMember> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CommunityMember member = new CommunityMember(
                        rs.getString("name"),
                        rs.getString("email")
                );
                member.setBirthYear(rs.getDate("birth_year").toLocalDate().getYear());
                member.setPhone(rs.getString("phone"));
                member.setCity(rs.getString("city"));
                member.setMembershipType(rs.getString("membership_type"));
                memberToIdMap.put(member, rs.getString("id_commu_member"));
                list.add(member);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll community members", e);
        }
        return list;
    }
}
