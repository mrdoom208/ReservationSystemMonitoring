/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.dto;

import java.time.LocalTime;

/**
 *
 * @author formentera
 */
public class ManageTablesDTO {

    private String tableNo;
    private String status;
    private Integer capacity;
    private String customer;
    private Integer pax;
    private LocalTime time;

    public ManageTablesDTO(String tableNo, String status, Integer capacity,
                                String customer, Integer pax, LocalTime time) {
        this.tableNo = tableNo;
        this.status = status;
        this.capacity = capacity;
        this.customer = customer;
        this.pax = pax;
        this.time = time;
    }

    public String getTableNo() { return tableNo; }
    public String getStatus() { return status; }
    public Integer getCapacity() { return capacity; }
    public String getCustomer() { return customer; }
    public Integer getPax() { return pax; }
    public LocalTime getTime() { return time; }
    
    
     @Override
    public String toString() {
        return "TableNo: " + tableNo +
               ", Status: " + status +
               ", Capacity: " + capacity +
               ", Name: " + customer +
               ", Pax: " + pax +
               ", Time: " + time;
    }
}
