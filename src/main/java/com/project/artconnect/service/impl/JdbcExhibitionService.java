package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.service.ExhibitionService;

import java.util.List;
import java.util.stream.Collectors;

public class JdbcExhibitionService implements ExhibitionService {
    private final ExhibitionDao exhibitionDao = new JdbcExhibitionDao();

    @Override
    public List<Exhibition> getAllExhibitions() { return exhibitionDao.findAll(); }

    @Override
    public List<String> getAllThemes() { return exhibitionDao.findAllThemes(); }

    @Override
    public void createExhibition(Exhibition exhibition) { exhibitionDao.save(exhibition); }

    @Override
    public void deleteExhibition(Exhibition exhibition) { exhibitionDao.delete(exhibition); }

    @Override
    public List<Exhibition> searchExhibition(String query, String themeName) {
        return exhibitionDao.findAll().stream()
                .filter(e -> query == null || query.isEmpty()
                        || e.getTitle().toLowerCase().contains(query.toLowerCase()))
                .filter(e -> themeName == null || themeName.isEmpty()
                        || e.getTheme().equals(themeName))
                .collect(Collectors.toList());
    }
}
