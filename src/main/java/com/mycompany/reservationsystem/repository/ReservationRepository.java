/* * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template */
package com.mycompany.reservationsystem.repository;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.dto.ReservationCustomerDTO;
import com.mycompany.reservationsystem.dto.RevenueReportsDTO;
import com.mycompany.reservationsystem.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
/** * * @author formentera */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    long countByStatus(String Status);
    List<Reservation> findTop10ByOrderByDateDescReservationPendingtimeDesc(Pageable pageable);
    List<Reservation> findByStatus(String status); List<Reservation> findByStatusIn(List<String> statuses);
    boolean existsByTable_Id(Long tableId); Optional<Reservation>findByReference(String reference);

    @Query("SELECT new com.mycompany.reservationsystem.dto.ReservationCustomerDTO(" + "r.id, r.pax, r.prefer, r.status, r.reference, r.date, r.revenue, "
            + "r.reservationPendingtime, r.reservationConfirmtime, r.reservationCancelledtime, " + "r.reservationSeatedtime, r.reservationCompletetime, "
            + "c.id, c.name, c.phone, c.email) " + "FROM Reservation r JOIN r.customer c "
            + "WHERE c.phone = :phone " + "AND (:from IS NULL OR r.date >= :from) "
            + "AND (:to IS NULL OR r.date <= :to)")
    List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneAndDate(
            @Param("phone") String phone, @Param("from") LocalDate from, @Param("to") LocalDate to );

    @Query("""
    SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO(
        c.phone, 
        COUNT(r.id),
        COALESCE(SUM(r.revenue), 0.0), 
        COALESCE(AVG(r.revenue), 0.0)
    ) 
    FROM Reservation r JOIN r.customer c 
    WHERE (:from IS NULL OR r.date >= :from) 
      AND (:to IS NULL OR r.date <= :to) 
    GROUP BY c.phone
""")
    List<CustomerReportDTO> getCustomerReport(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
    SELECT new com.mycompany.reservationsystem.dto.RevenueReportsDTO(
        r.date,
        COUNT(r.id),
        COUNT(DISTINCT r.customer.id),
        SUM(r.revenue)
    )
    FROM Reservation r
    WHERE (:from IS NULL OR r.date >= :from)
      AND (:to IS NULL OR r.date <= :to)
    GROUP BY r.date
    ORDER BY r.date ASC
""")
    List<RevenueReportsDTO> getRevenueReports(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
    @Query("SELECT r FROM Reservation r")
    Stream<Reservation> streamAllReservations();



}