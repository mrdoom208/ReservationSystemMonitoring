package com.mycompany.reservationsystem.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TableUsageInformationDTO {
    private String tableNo;
    private String reference;
    private Integer pax;
    private Double revenue;
    private LocalTime time;
    private LocalDate date;

    public TableUsageInformationDTO(String tableNo, String reference, Integer pax,
                                    Double revenue, LocalTime time, LocalDate date) {
        this.tableNo = tableNo;
        this.reference = reference;
        this.pax = pax;
        this.revenue = revenue;
        this.time = time;
        this.date = date;
    }

    public String getTableNo() {
        return tableNo;
    }

    public String getReference() {
        return reference;
    }

    public Integer getPax() {
        return pax;
    }

    public Double getRevenue() {
        return revenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }
}