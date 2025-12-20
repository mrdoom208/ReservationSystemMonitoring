package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ManageTablesDTO {

    private String tableNo;
    private String status;
    private Integer capacity;
    private String customer;
    private Integer pax;
    
    private String location;
    private String prefer;
    private String customerStatus;
    private String phone;                // <-- added
    private String email;                // <-- added
    
    private String reference;
    private BigDecimal revenue;
    private LocalTime reservationPendingtime;
    private LocalTime reservationConfirmtime;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;
    
    private Long tableId;
    private LocalTime tablestarttime;
    private LocalTime tableendtime;
    private LocalDate date;

     public ManageTablesDTO(
            String tableNo,
            String status,
            Integer capacity,
            String customer,
            Integer pax,
            BigDecimal revenue,
            
            String location,
            String prefer,
            String customerStatus,   
            String reference,// <-- added
            String phone,
            
            String email,
            LocalTime reservationPendingtime,
            LocalTime reservationConfirmtime,
            LocalTime reservationSeatedtime,
            LocalTime reservationCompletetime,
            
            Long tableId,
            LocalTime tablestarttime,
            LocalTime tableendtime,
            LocalDate date
    ) {
        this.tableNo = tableNo;
        this.status = status;
        this.capacity = capacity;
        this.customer = customer;
        this.pax = pax;
        this.location = location;
        this.revenue = revenue;

        this.prefer = prefer;
        this.customerStatus = customerStatus;  // <-- added
        this.phone = phone;
        this.email = email;
        this.reference = reference;
        this.reservationPendingtime = reservationPendingtime;
        this.reservationConfirmtime = reservationConfirmtime;
        this.reservationSeatedtime = reservationSeatedtime;
        this.reservationCompletetime = reservationCompletetime;
        this.tableId = tableId;
        this.tablestarttime = tablestarttime;
        this.tableendtime = tableendtime;
        this.date = date;
    }

    // Getters
    public String getTableNo() { return tableNo; }
    public String getStatus() { return status; }
    public Integer getCapacity() { return capacity; }
    public String getCustomer() { return customer; }
    public Integer getPax() { return pax; }
    public String getLocation(){ return location; }
    public BigDecimal getRevenue(){return revenue;}
    public String getPrefer() { return prefer; }
    public String getCustomerStatus() { return customerStatus; } // <-- added
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getReference() { return reference; }
    public LocalTime getReservationPendingtime() { return reservationPendingtime; }
    public LocalTime getReservationConfirmtime() { return reservationConfirmtime; }
    public LocalTime getReservationSeatedtime() { return reservationSeatedtime; }
    public LocalTime getReservationCompletetime() { return reservationCompletetime; }
    public Long getTableId() { return tableId; }
    public LocalTime getTablestarttime() { return tablestarttime; }
    public LocalTime getTableendtime() { return tableendtime; }
    public LocalDate getDate() { return date; }

    // Setters
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }
    public void setStatus(String status) { this.status = status; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setCustomer(String customer) { this.customer = customer; }
    public void setPax(Integer pax) { this.pax = pax; }
    public void setLocation(String location) { this.location = location; }
    public void setRevenue(BigDecimal revenue){this.revenue = revenue;}
    public void setPrefer(String prefer) { this.prefer = prefer; }
    public void setCustomerStatus(String customerStatus) { this.customerStatus = customerStatus; } // <-- added
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setReference(String reference) { this.reference = reference; }
    public void setReservationPendingtime(LocalTime reservationPendingtime) { this.reservationPendingtime = reservationPendingtime; }
    public void setReservationConfirmtime(LocalTime reservationConfirmtime) { this.reservationConfirmtime = reservationConfirmtime; }
    public void setReservationSeatedtime(LocalTime reservationSeatedtime) { this.reservationSeatedtime = reservationSeatedtime; }
    public void setReservationCompletetime(LocalTime reservationCompletetime) { this.reservationCompletetime = reservationCompletetime; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public void setTablestarttime(LocalTime tablestarttime) { this.tablestarttime = tablestarttime; }
    public void setTableendtime(LocalTime tableendtime) { this.tableendtime = tableendtime; }
    public void setDate(LocalDate date) { this.date = date; }
}