package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Service Provider — utilise maintenant les services JDBC connectés à MySQL.
 * Pour revenir aux données en mémoire (tests), remplacer JdbcXxx par InMemoryXxx
 * et remettre le bloc static avec initData().
 */
public class ServiceProvider {

    private static final JdbcArtistService artistService = new JdbcArtistService();
    private static final JdbcArtworkService artworkService = new JdbcArtworkService();
    private static final JdbcGalleryService galleryService = new JdbcGalleryService();
    private static final JdbcWorkshopService workshopService = new JdbcWorkshopService();
    private static final JdbcCommunityService communityService = new JdbcCommunityService();

    // Plus besoin de initData() — les données viennent directement de MySQL

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}