package com.mycompany.reservationsystem.Service;

import com.mycompany.reservationsystem.model.CustomerReservation;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.repository.CustomerReservationRepository;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private CustomerReservationRepository reservationRepository;

    public void updateStatus(String Ref, String newStatus) {
        Optional<CustomerReservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            CustomerReservation entity = optional.get();
            entity.setStatus(newStatus);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found with id: " + Ref);
        }
    }
    public void updateTableId(String Ref, ManageTables table) {
        Optional<CustomerReservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            CustomerReservation entity = optional.get();
            entity.setTable(table);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found with id table: " + Ref);
        }
    }
    public void updateSeatedtime(String Ref, LocalTime newtime){
        Optional<CustomerReservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            CustomerReservation entity = optional.get();
            entity.setReservationSeatedtime(newtime);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found" + Ref);
        }
    }
    public void updateCompletetime(String Ref, LocalTime newtime){
        Optional<CustomerReservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            CustomerReservation entity = optional.get();
            entity.setReservationCompletetime(newtime);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found" + Ref);
        }
    }
}
