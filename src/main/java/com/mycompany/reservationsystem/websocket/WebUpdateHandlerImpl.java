package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import com.mycompany.reservationsystem.controller.main.DashboardController;
import com.mycompany.reservationsystem.controller.main.ReservationController;
import com.mycompany.reservationsystem.controller.main.TableController;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.util.NotificationManager;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebUpdateHandlerImpl implements WebSocketListener {

    private final DeviceDetectionManager deviceDetectionManager =
            AdministratorUIController.getDeviceDetectionManager();

    @Autowired
    private MessageService messageService;

    @Override
    public void onMessage(WebUpdateDTO dto) {

        DashboardController dashboardController = AdministratorUIController.getDashboardController();
        ReservationController reservationController = AdministratorUIController.getReservationController();
        TableController tableController = AdministratorUIController.getTableController();

        if (dashboardController == null) return;

        String port = AppSettings.loadSerialPort();
        if (port == null || port.isBlank()) {
            throw new IllegalStateException("No serial port selected");
        }

        /* ================= UI THREAD ================= */
        Platform.runLater(() -> {

            System.out.println("WS EVENT: " + dto.getCode());

            switch (dto.getCode()) {

                case "NEW_RESERVATION" -> NotificationManager.show(
                        "New Reservation Added",
                        dto.getMessage(),
                        NotificationManager.NotificationType.SUCCESS
                );

                case "CANCELLED_RESERVATION" -> NotificationManager.show(
                        "Reservation Cancelled",
                        dto.getMessage(),
                        NotificationManager.NotificationType.ERROR
                );

                case "CHANGED_RESERVATION" -> NotificationManager.show(
                        "Reservation Updated",
                        dto.getMessage(),
                        NotificationManager.NotificationType.CHANGE
                );
            }

            dashboardController.loadRecentReservations();
            dashboardController.updateLabels();
            reservationController.loadReservationsData();
            tableController.loadTableManager();
        });

        /* ================= SERIAL THREAD ================= */
        new Thread(() -> {

            try {
                deviceDetectionManager.openPort(port, 115200);

                switch (dto.getCode()) {

                    case "NEW_RESERVATION" -> {
                        String smsMessage =
                                "Hello " + dto.getCustomerName() + ", your reservation has been successfully made.\n" +
                                        "Reference: " + dto.getReference() + "\n" +
                                        "Party Size: " + dto.getPax() + "\n" +
                                        "You can view or manage your reservation directly here: " + dto.getLink() + "\n" +
                                        "We look forward to welcoming you!";

                        deviceDetectionManager.sendMessage(dto.getPhone(), smsMessage);

                        String key = "message.new";
                        if (AppSettings.loadMessageEnabled(key)) {
                            messageService
                                    .findByLabel(AppSettings.loadMessageLabel(key))
                                    .ifPresent(msg -> sendSafe(dto.getPhone(), msg.getMessageDetails()));
                        }
                    }

                    case "CANCELLED_RESERVATION" -> {
                        String key = "message.cancelled";
                        if (AppSettings.loadMessageEnabled(key)) {
                            messageService
                                    .findByLabel(AppSettings.loadMessageLabel(key))
                                    .ifPresent(msg -> sendSafe(dto.getPhone(), msg.getMessageDetails()));
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                deviceDetectionManager.closePort();
            }

        }, "GSM-SERIAL-THREAD").start();
    }

    /* ================= SAFE SEND ================= */
    private void sendSafe(String phone, String message) {
        try {
            deviceDetectionManager.sendMessage(phone, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
