/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.repository;


import com.mycompany.reservationsystem.model.ReservationTableLogs;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author formentera
 */
public interface ReservationTableLogsRepository extends JpaRepository<ReservationTableLogs,Long>{
    
}
