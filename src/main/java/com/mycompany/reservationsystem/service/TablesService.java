package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TablesService {

    @Autowired
    private ManageTablesRepository manageTablesRepository;

    public void updateStatus(Long id, String newStatus) {
        Optional<ManageTables> optional = manageTablesRepository.findById(id);
        if (optional.isPresent()) {
            ManageTables entity = optional.get();
            entity.setStatus(newStatus);   // update the status
            manageTablesRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found with id: " + id);
        }
    }

    public ManageTables findByNo(String TableNo){
        Optional<ManageTables> optional = manageTablesRepository.findByTableNo(TableNo);
        ManageTables entity = null;
        if(optional.isPresent()){
            entity = optional.get();
            }else {
            throw new RuntimeException();
        }
        return entity;

    }
    
}
