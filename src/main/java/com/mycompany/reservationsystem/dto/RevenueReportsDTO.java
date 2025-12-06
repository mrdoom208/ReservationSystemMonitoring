package com.mycompany.reservationsystem.dto;

import java.time.LocalDate;

public class RevenueReportsDTO {

    private LocalDate date;
    private Long totalReservation;
    private Long totalCustomer;
    private Double totalRevenue;

    public RevenueReportsDTO(LocalDate date, Long totalReservation, Long totalCustomer, Double totalRevenue) {
        this.date = date;
        this.totalReservation = totalReservation;
        this.totalCustomer = totalCustomer;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTotalReservation() {
        return totalReservation;
    }

    public Long getTotalCustomer() {
        return totalCustomer;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }
}
