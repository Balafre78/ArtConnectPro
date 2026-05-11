package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.GalleryService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcGalleryService implements GalleryService {

    private final GalleryDao galleryDao = new JdbcGalleryDao();
    private final ExhibitionDao exhibitionDao = new JdbcExhibitionDao();

    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return galleryDao.findAll().stream()
                .filter(g -> g.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) return List.of();
        // On filtre les expositions dont la galerie correspond
        return exhibitionDao.findAll().stream()
                .filter(e -> e.getGallery() != null
                        && e.getGallery().getName().equalsIgnoreCase(gallery.getName()))
                .collect(Collectors.toList());
    }
}