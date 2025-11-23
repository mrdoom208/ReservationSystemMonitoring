/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.dto.CustomerReservationDTO;
import com.mycompany.reservationsystem.model.CustomerReservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author formentera
 */
@Repository
public interface CustomerReservationRepository extends JpaRepository<CustomerReservation, Long> {
    long countByStatus(String Status);

    List<CustomerReservation> findTop10ByOrderByDateDescReservationPendingtimeDesc(Pageable pageable);
    List<CustomerReservation> findByStatus(String status);
    List<CustomerReservation> findByStatusIn(List<String> statuses);

    boolean existsByTable_Id(Long tableId);
    Optional<CustomerReservation>findByReference(String reference);
    
   
    
    
    
    
}
