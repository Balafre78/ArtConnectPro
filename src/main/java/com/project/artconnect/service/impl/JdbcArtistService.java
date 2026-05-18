package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.dao.DisciplineDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.persistence.JdbcDisciplineDao;
import com.project.artconnect.service.ArtistService;

import java.util.*;
import java.util.stream.Collectors;

public class JdbcArtistService implements ArtistService {

    private final ArtistDao artistDao = new JdbcArtistDao();
    private final DisciplineDao disciplineDao = new JdbcDisciplineDao();

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Artist getArtistByName(String name) { return artistDao.findByName(name); }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(Artist artist) {
        artistDao.delete(artist);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        return disciplineDao.findAll();
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName) {
        return artistDao.findAll().stream()
                .filter(a -> query == null || query.isEmpty()
                        || a.getName().toLowerCase().contains(query.toLowerCase())
                        ||a.getCity() != null && a.getCity().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> disciplineName == null || disciplineName.isEmpty()
                        || a.getDisciplines().stream().anyMatch(d -> d.getName().equals(disciplineName)))
                .collect(Collectors.toList());
    }
}