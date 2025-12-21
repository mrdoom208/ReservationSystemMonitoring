/* * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template */
package com.mycompany.reservationsystem.repository;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.dto.ReservationCustomerDTO;
import com.mycompany.reservationsystem.dto.RevenueReportsDTO;
import com.mycompany.reservationsystem.model.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** * * @author formentera */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    long countByStatus(String Status);
    List<Reservation> findTop15ByOrderByDateDescReservationPendingtimeDesc(Pageable pageable);
    List<Reservation> findByStatus(String status); List<Reservation> findByStatusIn(List<String> statuses);
    boolean existsByTable_Id(Long tableId); Optional<Reservation>findByReference(String reference);
    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);


    @Query("SELECT new com.mycompany.reservationsystem.dto.ReservationCustomerDTO(" + "r.id, r.pax, r.prefer, r.status, r.reference, r.date, r.revenue, "
            + "r.reservationPendingtime, r.reservationConfirmtime, r.reservationCancelledtime, " + "r.reservationSeatedtime, r.reservationCompletetime, "
            + "c.id, c.name, c.phone, c.email) " + "FROM Reservation r JOIN r.customer c "
            + "WHERE c.phone = :phone " + "AND (:from IS NULL OR r.date >= :from) "
            + "AND (:to IS NULL OR r.date <= :to)")
    List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneAndDate(
            @Param("phone") String phone, @Param("from") LocalDate from, @Param("to") LocalDate to );
///  /////////////////////////////////////////////////////////////////
@Query("""
    SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO(
        c.phone,
        COUNT(r.id),
        COALESCE(SUM(r.revenue), 0),
        COALESCE(AVG(r.revenue), 0)
    )
    FROM Reservation r
    JOIN r.customer c
    GROUP BY c.phone
""")
List<CustomerReportDTO> getAllCustomerReport(Pageable pageable);

    @Query("""
    SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO(
        c.phone, 
        COUNT(r.id),
        COALESCE(SUM(r.revenue), 0),
        COALESCE(AVG(r.revenue), 0)
    ) 
    FROM Reservation r JOIN r.customer c 
    WHERE (:from IS NULL OR r.date >= :from) 
      AND (:to IS NULL OR r.date <= :to) 
    GROUP BY c.phone
""")
    List<CustomerReportDTO> getFilteredCustomerReport(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT DISTINCT c.phone FROM Customer c")
    List<String> findAllCustomerPhones(); // for Select All by phone
    //////////////////////////////////////////////////////////////////////

    @Query("""
    SELECT new com.mycompany.reservationsystem.dto.RevenueReportsDTO(
        r.date,
        COUNT(r.id),
        COALESCE(SUM(r.pax), 0),
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

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.revenue = :revenue WHERE r.reference = :reference")
    int updateRevenueByReference(@Param("reference") String reference,
                                 @Param("revenue") BigDecimal revenue);

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = :status WHERE r.reference = :reference")
    int updateStatusByReference(@Param("reference") String reference,
                                 @Param("status") String status);



    @Query("SELECT r FROM Reservation r")
    Stream<Reservation> streamAllReservations();


}