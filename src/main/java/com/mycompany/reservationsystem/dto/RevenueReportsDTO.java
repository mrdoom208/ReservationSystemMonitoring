package com.mycompany.reservationsystem.dto;

import java.time.LocalDate;

public class RevenueReportsDTO {

    private LocalDate date;
    private long totalReservation;
    private long totalCustomer;
    private double totalRevenue;

    public RevenueReportsDTO(LocalDate date, long totalReservation, long totalCustomer, double totalRevenue) {
        this.date = date;
        this.totalReservation = totalReservation;
        this.totalCustomer = totalCustomer;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTotalReservation() {
        return totalReservation;
    }

    public long getTotalCustomer() {
        return totalCustomer;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
