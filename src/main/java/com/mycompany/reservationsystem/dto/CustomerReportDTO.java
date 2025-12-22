package com.mycompany.reservationsystem.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomerReportDTO {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private String phone;
    private Long totalReservation;  // wrapper
    private BigDecimal totalRevenue;     // wrapper
    private Double averageRevenue;   // wrapper

    public CustomerReportDTO(String phone, Long totalReservation, BigDecimal totalRevenue, Double averageRevenue) {
        this.phone = phone;
        this.totalReservation = totalReservation != null ? totalReservation : 0L;
        this.totalRevenue = totalRevenue != null
                ? new BigDecimal(totalRevenue.toString()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        this.averageRevenue = averageRevenue != null
                ? Math.round(averageRevenue.doubleValue() * 100.0) / 100.0
                : 0.00;}

    public BooleanProperty selectedProperty() {
        return selected;
    }
    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }
    public String getPhone() { return phone; }
    public Long getTotalReservation() { return totalReservation; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public Double getAverageRevenue() { return averageRevenue; }
}