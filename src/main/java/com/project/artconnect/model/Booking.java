package com.project.artconnect.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Booking {
    private Workshop workshop;
    private CommunityMember member;
    private LocalDateTime bookingDate;
    private String paymentStatus; // PENDING, PAID, CANCELLED

    public Booking() {
    }

    public Booking(Workshop workshop, CommunityMember member) {
        this.workshop = workshop;
        this.member = member;
        this.bookingDate = LocalDateTime.now();
        this.paymentStatus = "PENDING";
    }

    public Workshop getWorkshop() {
        return workshop;
    }

    public void setWorkshop(Workshop workshop) {
        this.workshop = workshop;
    }

    public CommunityMember getMember() {
        return member;
    }

    public void setMember(CommunityMember member) {
        this.member = member;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Booking booking)) {
            return false;
        }
        return Objects.equals(workshop, booking.workshop)
                && Objects.equals(member, booking.member)
                && Objects.equals(bookingDate, booking.bookingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workshop, member, bookingDate);
    }
}
