/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.dto;



/**
 *
 * @author formentera
 */
public class CustomerReservationDTO{
    private Long id;

    private String name;
    private int pax;
    private String prefer;
    private String phone;
    private String email;
    private String status;
    private String reference;
    
    

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
    
    
    public String toString() {
       return "CustomerReservation{name='" + name + "', phone='" + phone + "', tableCapacity=" + pax + "}";
    }   
}
