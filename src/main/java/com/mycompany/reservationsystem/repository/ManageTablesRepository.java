/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;
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
           "t.tableNo, " +
           "t.status, " +
           "t.capacity, " +
           "r.name, " +
           "r.pax, " +
           "t.time)"+
           "FROM ManageTables t " +
           "LEFT JOIN t.reservations r ")
    List<ManageTablesDTO> getManageTablesDTO();
}
