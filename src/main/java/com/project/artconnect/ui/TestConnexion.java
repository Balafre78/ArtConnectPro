package com.project.artconnect.ui;

import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.model.Artist;
import java.sql.Connection;
import java.util.List;

public class TestConnexion {
    public static void main(String[] args) {

        // Test 1 : connexion brute
        System.out.println("=== Test connexion ===");
        try (Connection conn = ConnectionManager.getConnection()) {
            System.out.println("✅ Connexion OK - Base : " + conn.getCatalog());
        } catch (Exception e) {
            System.out.println("❌ Connexion échouée : " + e.getMessage());
            return; // inutile de continuer
        }

        // Test 2 : vrai appel JDBC sur la table Artist
        System.out.println("\n=== Test JdbcArtistDao.findAll() ===");
        try {
            JdbcArtistDao dao = new JdbcArtistDao();
            List<Artist> artists = dao.findAll();
            System.out.println("✅ " + artists.size() + " artiste(s) trouvé(s)");
            for (Artist a : artists) {
                System.out.println("  → " + a.getName() + " | " + a.getCity());
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur DAO : " + e.getMessage());
        }
    }
}