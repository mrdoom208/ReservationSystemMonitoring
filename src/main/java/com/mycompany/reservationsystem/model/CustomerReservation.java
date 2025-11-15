/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author formentera
 */
@Entity
public class CustomerReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int pax;
    private String prefer;
    private String phone;
    private String email;
    private String status;
    private String reference;
    private LocalDate date;
    private LocalTime time;
    
    @ManyToOne
    @JoinColumn(name="table_id")
    private ManageTables table;
    
    
    
    
    

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPax() { return pax; }
    public void setPax(int pax) { this.pax = pax; }

    public String getPrefer() { return prefer; }
    public void setPrefer(String prefer) { this.prefer = prefer; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    
    public Long getTableId() {
    return table != null ? table.getId() : null;
    
    
}
    
    
    
}
