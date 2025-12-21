package com.mycompany.reservationsystem.Service;

import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.model.ManageTables;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public void updateStatus(String Ref, String newStatus) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setStatus(newStatus);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found with id: " + Ref);
        }
    }
    public void updateTableId(String Ref, ManageTables table) {
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setTable(table);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found with id table: " + Ref);
        }
    }
    public void updateSeatedtime(String Ref, LocalTime newtime){
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setReservationSeatedtime(newtime);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found" + Ref);
        }
    }
    public void updateCompletetime(String Ref, LocalTime newtime){
        Optional<Reservation> optional = reservationRepository.findByReference(Ref);
        if (optional.isPresent()) {
            Reservation entity = optional.get();
            entity.setReservationCompletetime(newtime);   // update the status
            reservationRepository.save(entity);  // save back to DB
        } else {
            throw new RuntimeException("Reservation not found" + Ref);
        }
    }

    public boolean setRevenueForReference(String reference, BigDecimal revenue) {
        int updatedRows = reservationRepository.updateRevenueByReference(reference, revenue);
        return updatedRows > 0; // returns true if at least one row was updated
    }
    public boolean setStatusForReference(String reference, String status) {
        int updatedRows = reservationRepository.updateStatusByReference(reference, status);
        return updatedRows > 0; // returns true if at least one row was updated
    }
    @Transactional(readOnly = true)
    public Stream<Reservation> streamAllReservations() {
        return reservationRepository.streamAllReservations();
    }

    public List<CustomerReportDTO> loadPage(int page, int pageSize) {
        return reservationRepository.getAllCustomerReport(PageRequest.of(page, pageSize));
    }

    public List<CustomerReportDTO> loadByDate(LocalDate from, LocalDate to) {
        return reservationRepository.getFilteredCustomerReport(from, to);
    }
    public List<String> getAllCustomerPhones() {
        return reservationRepository.findAllCustomerPhones();
    }


}
