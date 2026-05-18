package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 */
public class ServiceProvider {
    private static final JdbcArtistService artistService = new JdbcArtistService();
    private static final JdbcArtworkService artworkService = new JdbcArtworkService();
    private static final JdbcGalleryService galleryService = new JdbcGalleryService();
    private static final JdbcWorkshopService workshopService = new JdbcWorkshopService();
    private static final JdbcCommunityService communityService = new JdbcCommunityService();
    private static final JdbcExhibitionService exhibitionService = new JdbcExhibitionService();

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

    public static ExhibitionService getExhibitionService() {
        return exhibitionService;
    }
}
