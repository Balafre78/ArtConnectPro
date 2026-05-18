package com.project.artconnect.dao;

import com.project.artconnect.model.ArtworkTag;

import java.util.List;

public interface ArtworkTagDao {
    List<ArtworkTag> findAll();
}
