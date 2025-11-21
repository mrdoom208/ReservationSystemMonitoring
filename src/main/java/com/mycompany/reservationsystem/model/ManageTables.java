/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author formentera
 */
@Entity
public class ManageTables {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)        
    Long id;
    
    
    String tableNo;
    int capacity;
    String status;
    String location;
    
    LocalTime tablestarttime;
    LocalTime tableendtime;
    
    
    
    
    @OneToMany(mappedBy = "table")
    private List<CustomerReservation> reservations;
    
    
    
    
    
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    
    public String getTableNo(){return tableNo;}
    public void setTableNo(String tableNo){this.tableNo = tableNo;}
    
    public String getStatus(){return status;}
    public void setStatus(String Status){this.status = Status;}
    
    
    public int getCapacity(){return capacity;}
    public void setCapacity(int Capacity){this.capacity = Capacity;}
    
    public LocalTime getTablestarttime() {return tablestarttime;}
    public void setTablestarttime(LocalTime tablestarttime) {this.tablestarttime = tablestarttime;}

    public LocalTime getTableendtime() {return tableendtime;}
    public void setTableendtime(LocalTime tableendtime) {this.tableendtime = tableendtime;}

    public String getLocation(){return location;}
    public void setLocation(String location){this.location = location;}

    
    
   
    
}
