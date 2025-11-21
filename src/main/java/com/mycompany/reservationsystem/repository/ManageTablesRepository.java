/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.model.CustomerReservation;
import com.mycompany.reservationsystem.model.ManageTables;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author formentera
 */
@Repository
public interface ManageTablesRepository extends JpaRepository<ManageTables,Long>{
   @Query("SELECT new com.mycompany.reservationsystem.dto.ManageTablesDTO(" +
        "t.tableNo, " +                      // table number
        "t.status, " +                       // table status
        "t.capacity, " +                     // table capacity
        "r.name, " +                     // customer name
        "r.pax, " +                          // number of guests
        "t.location, " +                     // table location
        "r.prefer, " +                       // customer preference
        "r.status, " +                       // reservation status
        "r.reference, " +                    // reservation reference
        "r.phone, " +                        // customer phone
        "r.email, " +                        // customer email
        "r.reservationPendingtime, " +       // pending time
        "r.reservationConfirmtime, " +       // confirm time
        "r.reservationSeatedtime, " +        // seated time
        "r.reservationCompletetime, " +      // complete time
        "t.id, " +                           // table id
        "t.tablestarttime, " +               // table start time
        "t.tableendtime, " +                 // table end time
        "r.date" +                           // reservation date
        ") " +
        "FROM ManageTables t " +
        "LEFT JOIN t.reservations r")
    List<ManageTablesDTO> getManageTablesDTO();
    List<ManageTables> findByStatus(String status);
    
    long countByStatus(String status);
    
    
    
}
