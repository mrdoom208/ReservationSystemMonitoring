package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Log a user action into the activity_logs table.
     */
    public void logAction(String user, String position, String module, String action, String description) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setPosition(position); // set the position
        log.setModule(module);
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());

        activityLogRepository.save(log);
    }

    /**
     * Return all logs (newest first)
     */

    /**
     * Optional: clear logs (useful for maintenance)
     */
    public void clearLogs() {
        activityLogRepository.deleteAll();
    }
}
