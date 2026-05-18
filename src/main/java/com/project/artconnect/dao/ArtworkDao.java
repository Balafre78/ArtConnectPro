package com.project.artconnect.dao;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import java.util.List;

public interface ArtworkDao {
    List<Artwork> findAll();

    void save(Artwork artwork);

    void update(Artwork artwork);

    void delete(Artwork artwork);

    List<Artwork> findByArtistName(String artistName);
}
