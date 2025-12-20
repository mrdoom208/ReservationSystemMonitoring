package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class RevenueReportsDTO {

    private LocalDate date;
    private Long totalReservation;
    private Long totalCustomer;
    private BigDecimal totalRevenue;

    public RevenueReportsDTO(LocalDate date, Long totalReservation, Long totalCustomer, BigDecimal totalRevenue) {
        this.date = date;
        this.totalReservation = totalReservation;
        this.totalCustomer = totalCustomer;
        this.totalRevenue = (totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
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

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
