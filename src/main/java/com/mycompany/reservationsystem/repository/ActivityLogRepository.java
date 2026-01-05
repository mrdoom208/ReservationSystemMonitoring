package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {
    @Query("""
    SELECT a FROM ActivityLog a
    WHERE 
        (:dateFrom IS NULL OR a.timestamp >= :dateFrom)
        AND (:dateTo IS NULL OR a.timestamp <= :dateTo)
    ORDER BY a.timestamp DESC
    """)
    Page<ActivityLog> filterByDate(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
