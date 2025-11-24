package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.model.ActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogsRepository extends JpaRepository<ActivityLogs,Long> {


}
