package com.project.artconnect.dao;

import com.project.artconnect.model.Artist;
import java.util.List;

/**
 * Data Access Object for Artist entity.
 */
public interface ArtistDao {
    Artist findByName(String name);

    List<Artist> findAll();

    void save(Artist artist);

    void update(Artist artist);

    void delete(Artist artist);

    String findId(Artist artist);

    List<Artist> findByCity(String city);
}
