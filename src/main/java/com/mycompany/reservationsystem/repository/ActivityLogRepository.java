package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {
    @Query("""
           SELECT a FROM ActivityLog a
           ORDER BY a.timestamp DESC
           """)
    List<ActivityLog> findAllOrderByTimestampDesc();

}
