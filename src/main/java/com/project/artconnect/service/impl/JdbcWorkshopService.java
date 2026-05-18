package com.project.artconnect.service.impl;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.WorkshopService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcWorkshopService implements WorkshopService {

    private final WorkshopDao workshopDao = new JdbcWorkshopDao();

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        return workshopDao.findAll().stream()
                .filter(w -> w.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) return;
        // La réservation est gérée en base via la table Books
        // On crée le booking en mémoire pour l'affichage immédiat
        Booking b = new Booking(workshop, member);
        member.addBooking(b);
    }

    public void createWorkshop(Workshop workshop) { workshopDao.save(workshop); }

    public void updateWorkshop(Workshop workshop) { workshopDao.update(workshop); }

    public void deleteWorkshop(Workshop workshop) { workshopDao.delete(workshop); }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) return List.of();
        return member.getBookings();
    }

    @Override
    public List<Workshop> searchWorkshop(String query, String difficulty) {
        return workshopDao.findAll().stream()
                .filter(w -> query == null || query.isEmpty()
                        || w.getTitle().toLowerCase().contains(query.toLowerCase()))
                .filter(w -> difficulty == null || difficulty.isEmpty()
                        || w.getLevel().equals(difficulty))
                .collect(Collectors.toList());
    }
}