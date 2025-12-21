/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.repository.*;
import com.mycompany.reservationsystem.websocket.ReservationListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * FXML Controller class
 *
 * @author formentera
 */
@Component
public class


AdministratorUIController implements Initializable, ReservationListener {
    private User currentuser;

    void setUser(User user) {
        this.currentuser = user;
    }

    public User getCurrentUser() {
        return currentuser;
    }

    @FXML
    private Button Dashboardbtn, ReservationManagementbtn, TableManagementbtn, Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn, Mergebtn, Reservationrpts, Customerrpts, Revenuerpts, TableUsagerpts, ApplyResrep, ApplyCusrep, ApplyRevrep, ApplyTUrep, applyAL, AddTablebtn;
    @FXML
    private ScrollPane  MessagingPane;
    @FXML
    private Label header;
    @FXML
    private StackPane content;


    @Autowired
    private ConfigurableApplicationContext springContext;
    private Reservation selectedReservation;



    private final Map<String, Object> controllerCache = new HashMap<>();
    private final Map<String, Node> viewCache = new HashMap<>();

    private void loadView(String fxmlFile) {
        try {
            Node view = viewCache.get(fxmlFile);

            if (view == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                loader.setControllerFactory(springContext::getBean);

                view = loader.load();

                viewCache.put(fxmlFile, view);
                controllerCache.put(fxmlFile, loader.getController());
            }

            content.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void navigate(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        Button[] buttons = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn, Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn};


        for (Button btn : buttons) {
            if (btn == null) {
                continue;
            }
            btn.getStyleClass().remove("navigation-btns-active");
            if (!btn.getStyleClass().contains("navigation-btns")) {
                btn.getStyleClass().add("navigation-btns");
            }
        }

        clicked.getStyleClass().remove("navigation-btns");
        if (!clicked.getStyleClass().contains("navigation-btns-active")) {
            clicked.getStyleClass().add("navigation-btns-active");
        }
        System.out.println(clicked.getId());

        String fxmlFile = null;
        switch (clicked.getId()) {
            case "Dashboardbtn":
                header.setText("Dashboard");
                fxmlFile = "/fxml/Dashboard.fxml";
                break;

            case "ReservationManagementbtn":
                header.setText("Reservation Management");
                fxmlFile = "/fxml/Reservation.fxml";
                break;

            case "TableManagementbtn":
                header.setText("Table Management");
                fxmlFile = "/fxml/Table.fxml";
                break;

            case "Messagingbtn":
                MessagingPane.setVisible(true);
                header.setText("Message Management");
                break;

            case "ManageStaffAndAccountsbtn":
                header.setText("Account Management");
                fxmlFile = "/fxml/Account.fxml";
                break;

            case "Reportsbtn":
                header.setText("Reports");
                fxmlFile = "/fxml/Reports.fxml";
                break;

            case "ActivityLogbtn":
                header.setText("Activity Logs");
                fxmlFile = "/fxml/ActivityLogs.fxml";
                break;

            default:
                break;

        }

        loadView(fxmlFile);
    }


    public DashboardController getDashboardController() {
        String key = "/fxml/Dashboard.fxml";

        if (!controllerCache.containsKey(key)) {
            loadView(key); // forces loading & caching
        }

        return (DashboardController) controllerCache.get(key);
    }

    public ReservationController getReservationController() {
        String key = "/fxml/Reservation.fxml";

        if (!controllerCache.containsKey(key)) {
            loadView(key); // forces loading & caching
        }

        return (ReservationController) controllerCache.get(key);
    }

    public TableController getTableController() {
        String key = "/fxml/Table.fxml";

        if (!controllerCache.containsKey(key)) {
            loadView(key);
        }
        return (TableController) controllerCache.get(key);
    }

    public AccountController getAccountController() {
        String key = "/fxml/Account.fxml";

        if (!controllerCache.containsKey(key)) {
            loadView(key);
        }
        return (AccountController) controllerCache.get(key);
    }

    public ActivityLogsController getActivityLogsController() {
        String key = "/fxml/ActivityLogs.fxml";

        if (!controllerCache.containsKey(key)) {
            loadView(key);
        }
        return (ActivityLogsController) controllerCache.get(key);
    }

    public ReportsController getReportsController() {
        String key = "/fxml/Reports.fxml";

        if (!controllerCache.containsKey(key)) {
            loadView(key);
        }
        return (ReportsController) controllerCache.get(key);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {


        Dashboardbtn.fire();


        WebSocketClient wsClient = new WebSocketClient();
        wsClient.addListener(this);
        new Thread(() -> {
            try {
                wsClient.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Override
    public void onNewReservation(WebupdateDTO reservation) {
        Platform.runLater(() -> {
            getDashboardController().updateLabels();
            //loadRecentReservations();
            //loadCustomerReservationTable();
            //getDashboardController().showNotification(notificationArea, "New Reservation Added", "A new Reservation has been successfully added.", "success");

        });
    }

}
