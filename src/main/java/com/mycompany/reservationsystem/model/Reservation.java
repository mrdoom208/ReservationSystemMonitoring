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
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pax;
    private String prefer;
    private String status;
    private String reference;
    private LocalDate date;
    private double revenue;

    private LocalTime reservationPendingtime;
    private LocalTime reservationConfirmtime;
    private LocalTime reservationCancelledtime;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = true)
    private ManageTables table;

    // ============================
    //       GETTERS & SETTERS
    // ============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPax() {
        return pax;
    }

    public void setPax(int pax) {
        this.pax = pax;
    }

    public String getPrefer() {
        return prefer;
    }

    public void setPrefer(String prefer) {
        this.prefer = prefer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRevenue(){ return revenue;}
    public void setRevenue(double revenue){this.revenue = revenue;}

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getReservationPendingtime() {
        return reservationPendingtime;
    }

    public void setReservationPendingtime(LocalTime reservationPendingtime) {
        this.reservationPendingtime = reservationPendingtime;
    }

    public LocalTime getReservationConfirmtime() {
        return reservationConfirmtime;
    }

    public void setReservationConfirmtime(LocalTime reservationConfirmtime) {
        this.reservationConfirmtime = reservationConfirmtime;
    }

    public LocalTime getReservationCancelledtime() {
        return reservationCancelledtime;
    }

    public void setReservationCancelledtime(LocalTime reservationCancelledtime) {
        this.reservationCancelledtime = reservationCancelledtime;
    }

    public LocalTime getReservationSeatedtime() {
        return reservationSeatedtime;
    }

    public void setReservationSeatedtime(LocalTime reservationSeatedtime) {
        this.reservationSeatedtime = reservationSeatedtime;
    }

    public LocalTime getReservationCompletetime() {
        return reservationCompletetime;
    }

    public void setReservationCompletetime(LocalTime reservationCompletetime) {
        this.reservationCompletetime = reservationCompletetime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ManageTables getTable() {
        return table;
    }

    public void setTable(ManageTables table) {
        this.table = table;
    }
}