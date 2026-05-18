package com.project.artconnect.service;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.ArtworkTag;

import java.util.List;
import java.util.Optional;

public interface ArtworkService {
    List<Artwork> getAllArtworks();

    Optional<Artwork> getArtworkByTitle(String title);

    List<Artwork> getArtworksByArtist(Artist artist);

    void createArtwork(Artwork artwork);

    void updateArtwork(Artwork artwork);

    void deleteArtwork(Artwork artwork);

    List<ArtworkTag> getAllTags();

    List<Artwork> searchArtwork(String query, String tagName);
}
