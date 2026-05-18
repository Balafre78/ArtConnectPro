package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.*;
import java.sql.Date;

/**
 * JDBC implementation for ArtistDao.
 */
public class JdbcArtistDao implements ArtistDao {

    private static final String SQL_FIND_ALL =
            "SELECT * FROM Artist";

    private static final String SQL_SAVE =
            "INSERT INTO Artist (id_artist, name, bio, birth_year, contact_email, phone, city) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE Artist SET bio=?, birth_year=?, contact_email=?, phone=?, city=? WHERE name=?";

    private static final String SQL_DELETE =
            "DELETE FROM Artist WHERE id_artist=?";

    private static final String SQL_FIND_BY_CITY =
            "SELECT id_artist, name, bio, birth_year, contact_email, phone, city FROM Artist WHERE city=?";

    private static final String SQL_FIND_BY_NAME =
            "SELECT * FROM Artist WHERE name = ?";

    private static final String SQL_FIND_DISCIPLINES =
            "SELECT name FROM Discipline d " +
            "JOIN PracticesDiscipline pd ON d.id_discipline = pd.id_discipline " +
            "WHERE pd.id_artist = ?";

    private static final String SQL_FIND_ID_BY_NAME =
            "SELECT id_artist FROM Artist WHERE name = ?";

    private static final Map<Artist, String> artistToIdMap = new HashMap<>();

    @Override
    public Artist findByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAME)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artist artist = new Artist(
                            rs.getString("name"),
                            rs.getString("bio"),
                            rs.getDate("birth_year").toLocalDate().getYear(),
                            rs.getString("contact_email"),
                            rs.getString("city")
                    );
                    artist.setPhone(rs.getString("phone"));
                    return artist;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByName for the artist named: " + name, e);
        }
        return null;
    }

    @Override
    public List<Artist> findAll(){
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            List<Artist> list = new ArrayList<>();
            while (rs.next()) {
                Artist artist = new Artist(
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getDate("birth_year").toLocalDate().getYear(),
                        rs.getString("contact_email"),
                        rs.getString("city")
                );
                artist.setPhone(rs.getString("phone"));
                // Trouver les disciplines
                List<Discipline> disciplines = new ArrayList<>();
                try (PreparedStatement ps2 = conn.prepareStatement(SQL_FIND_DISCIPLINES)) {
                    ps2.setString(1, rs.getString("id_artist"));
                    ResultSet rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        disciplines.add(new Discipline(rs2.getString("name")));
                    }
                }
                artist.setDisciplines(disciplines);

                artistToIdMap.put(artist, rs.getString("id_artist"));
                list.add(artist);
            }
            return list;
        }  catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des artistes :", e);
        }
    }

    @Override
    public void save(Artist artist) {
        String newId = "A_" + artist.getName().replaceAll("\\s+", "_");

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SAVE);) {

            ps.setString(1, newId);
            ps.setString(2, artist.getName());
            ps.setString(3, artist.getBio());
            ps.setDate(4, Date.valueOf(artist.getBirthYear() + "-01-01"));
            ps.setString(5, artist.getContactEmail());
            ps.setString(6, artist.getPhone());
            ps.setString(7, artist.getCity());
            ps.executeUpdate();
            artistToIdMap.put(artist, newId);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion d'un artiste", e);
        }
    }

    @Override
    public void update(Artist artist) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, artist.getBio());
            ps.setDate(2, Date.valueOf(artist.getBirthYear() + "-01-01"));
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getPhone());
            ps.setString(5, artist.getCity());
            ps.setString(6, artist.getName());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'artiste", e);
        }
    }

    @Override
    public void delete(Artist artist) {
        String artistId = findId(artist);
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, findId(artist));
            ps.executeUpdate();
            if (artistId != null) {
                artistToIdMap.keySet().removeIf(a -> a.equals(artist));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'artiste "+artist.getName(), e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
    List<Artist> list = new ArrayList<>();

    try (Connection conn = ConnectionManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CITY)) {

        ps.setString(1, city);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artist artist = new Artist(
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getInt("birth_year"),
                        rs.getString("contact_email"),
                        rs.getString("city")
                );
                artist.setPhone(rs.getString("phone"));
                list.add(artist);
            }
        }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par ville "+city, e);
        }
        return list;
    }

    @Override
    public String findId(Artist artist) {
        return artistToIdMap.get(artist);
    }


}