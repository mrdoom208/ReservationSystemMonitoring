/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.dto.WebupdateDTO;

/**
 *
 * @author formentera
 */
public interface ReservationListener {
    void onNewReservation(WebupdateDTO reservation);
}