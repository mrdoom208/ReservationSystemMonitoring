package com.mycompany.reservationsystem.Service;

import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TablesService {

    @Autowired
    private ManageTablesRepository reservationRepository;

    public void updateStatus(Long id, String newStatus) {
        Optional<ManageTables> optional = reservationRepository.findById(id);
        if (optional.isPresent()) {
            ManageTables entity = optional.get();
            entity.setStatus(newStatus);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found with id: " + id);
        }
    }
    
}
