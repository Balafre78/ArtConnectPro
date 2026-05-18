package com.project.artconnect.dao;

import com.project.artconnect.model.Gallery;
import java.util.List;
import java.util.Optional;

public interface GalleryDao {
    Gallery findByName(String name);

    String findIdByName(String Name);

    List<Gallery> findAll();
}
