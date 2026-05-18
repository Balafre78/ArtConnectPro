package com.project.artconnect.service;

import com.project.artconnect.model.Exhibition;

import java.util.List;

public interface ExhibitionService {
    List<Exhibition> getAllExhibitions();

    List<String> getAllThemes();

    void createExhibition(Exhibition exhibition);

    void deleteExhibition(Exhibition exhibition);

    List<Exhibition> searchExhibition(String query, String theme);
}
