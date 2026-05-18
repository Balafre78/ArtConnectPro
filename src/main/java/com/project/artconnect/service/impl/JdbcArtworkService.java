package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.dao.ArtworkTagDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.persistence.JdbcArtworkTagDao;
import com.project.artconnect.service.ArtworkService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcArtworkService implements ArtworkService {

    private final ArtworkDao artworkDao = new JdbcArtworkDao();
    private final ArtworkTagDao artworkTagDao = new JdbcArtworkTagDao();

    @Override
    public List<Artwork> getAllArtworks() {
        return artworkDao.findAll();
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        return artworkDao.findAll().stream()
                .filter(a -> a.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null) return List.of();
        return artworkDao.findByArtistName(artist.getName());
    }

    @Override
    public void createArtwork(Artwork artwork) {
        artworkDao.save(artwork);
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        artworkDao.update(artwork);
    }

    @Override
    public void deleteArtwork(Artwork artwork) {
        artworkDao.delete(artwork);
    }

    @Override
    public List<ArtworkTag> getAllTags() {
        return artworkTagDao.findAll();
    }

    @Override
    public List<Artwork> searchArtwork(String query, String tagName) {
        return artworkDao.findAll().stream()
                .filter(a -> query == null || query.isEmpty()
                        || a.getTitle().toLowerCase().contains(query.toLowerCase())
                        || a.getArtist() != null && a.getArtist().getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> tagName == null || tagName.isEmpty()
                        || a.getTags().stream().anyMatch(tag -> tag.getName().equals(tagName)))
                .collect(Collectors.toList());
    }
}