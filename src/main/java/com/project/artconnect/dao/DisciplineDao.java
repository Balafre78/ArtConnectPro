package com.project.artconnect.dao;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;

import java.util.List;

public interface DisciplineDao {
    List<Discipline> findAll();
}
