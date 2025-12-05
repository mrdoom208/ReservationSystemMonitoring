package com.mycompany.reservationsystem.dto;

public class CustomerReportDTO {

    private String phone;
    private Long totalReservation;  // wrapper
    private Double totalRevenue;     // wrapper
    private Double averageRevenue;   // wrapper

    public CustomerReportDTO(String phone, Long totalReservation, Double totalRevenue, Double averageRevenue) {
        this.phone = phone;
        this.totalReservation = totalReservation != null ? totalReservation : 0L;
        this.totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        this.averageRevenue = averageRevenue != null ? Math.round(averageRevenue * 100.0) / 100.0 : 0.0;
    }

    public String getPhone() { return phone; }
    public Long getTotalReservation() { return totalReservation; }
    public Double getTotalRevenue() { return totalRevenue; }
    public Double getAverageRevenue() { return averageRevenue; }
}