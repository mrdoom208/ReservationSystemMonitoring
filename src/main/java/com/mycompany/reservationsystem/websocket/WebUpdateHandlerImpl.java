package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import com.mycompany.reservationsystem.controller.main.DashboardController;
import com.mycompany.reservationsystem.controller.main.ReservationController;
import com.mycompany.reservationsystem.controller.main.TableController;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.util.NotificationManager;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WebUpdateHandlerImpl implements WebSocketListener{

    private final DeviceDetectionManager deviceDetectionManager = AdministratorUIController.getDeviceDetectionManager();

    @Autowired
    private MessageService messageService;

    @Override
    public void onMessage(WebUpdateDTO dto) {
        DashboardController dashboardController = AdministratorUIController.getDashboardController();
        ReservationController reservationController = AdministratorUIController.getReservationController();
        TableController tableController = AdministratorUIController.getTableController();


        String port = AppSettings.loadSerialPort();
        if (port == null || port.isBlank()) {
            throw new IllegalStateException("No serial port selected");
        }
        try {
            deviceDetectionManager.openPort(port, 115200);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        if (dashboardController == null) return;

        Platform.runLater(() -> {

            System.out.println("WS EVENT: " + dto.getCode());

            switch (dto.getCode()) {

                case "NEW_RESERVATION" -> {
                    NotificationManager.show(
                            "New Reservation Added",
                            dto.getMessage(),
                            NotificationManager.NotificationType.SUCCESS
                    );

                    String key = "message.new";
                    if(AppSettings.loadMessageEnabled(key)){
                        messageService.findByLabel(AppSettings.loadMessageLabel(key)).ifPresent(msg -> {
                            try {
                                deviceDetectionManager.sendMessage(dto.getPhone(), msg.getMessageDetails());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }

                case "CANCELLED_RESERVATION" -> {
                    NotificationManager.show(
                            "Reservation Cancelled",
                            dto.getMessage(),
                            NotificationManager.NotificationType.ERROR);

                    String key = "message.cancelled";
                    if(AppSettings.loadMessageEnabled(key)){
                        messageService.findByLabel(AppSettings.loadMessageLabel(key)).ifPresent(msg -> {
                            try {
                                deviceDetectionManager.sendMessage(dto.getPhone(), msg.getMessageDetails());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }

                case "CHANGED_RESERVATION" -> {
                    NotificationManager.show(
                            "Reservation Updated",
                            dto.getMessage(),
                            NotificationManager.NotificationType.CHANGE
                    );

                }
            }
            deviceDetectionManager.closePort();
            dashboardController.loadRecentReservations();
            dashboardController.updateLabels();
            reservationController.loadReservationsData();
            tableController.loadTableManager();
            System.out.println("Start");



        });

    }
}
