package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.service.ArtistService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcArtistService implements ArtistService {

    private final ArtistDao artistDao = new JdbcArtistDao();

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        return artistDao.findAll().stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(String name) {
        artistDao.delete(name);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        // Les disciplines viennent de la table Discipline en base
        // Pour l'instant on les extrait depuis les artistes chargés
        return artistDao.findAll().stream()
                .flatMap(a -> a.getDisciplines().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        return artistDao.findAll().stream()
                .filter(a -> query == null || query.isEmpty()
                        || a.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> city == null || city.isEmpty()
                        || (a.getCity() != null && a.getCity().equalsIgnoreCase(city)))
                .filter(a -> disciplineName == null || disciplineName.isEmpty()
                        || a.getDisciplines().stream().anyMatch(d -> d.getName().equals(disciplineName)))
                .collect(Collectors.toList());
    }
}