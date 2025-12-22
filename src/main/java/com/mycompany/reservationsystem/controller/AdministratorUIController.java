package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.util.BackgroundViewLoader;
import com.mycompany.reservationsystem.websocket.ReservationListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * FXML Controller class for Administrator UI
 * Manages navigation and view loading with background processing
 *
 * @author formentera
 */
@Component
public class AdministratorUIController implements Initializable, ReservationListener {

    private User currentuser;
    private BackgroundViewLoader viewLoader;

    void setUser(User user) {
        this.currentuser = user;
    }

    public User getCurrentUser() {
        return currentuser;
    }

    @FXML
    private Button Dashboardbtn, ReservationManagementbtn, TableManagementbtn,
            Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn;
    @FXML
    private MenuItem logoutBtn;
    @FXML
    private ScrollPane MessagingPane;
    @FXML
    private Label header;
    @FXML
    private StackPane content;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private Reservation selectedReservation;

    WebSocketClient wsClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize background loader
        viewLoader = new BackgroundViewLoader(springContext);

        // Preload commonly used views on startup (happens in background)
        viewLoader.preloadViews(
                "/fxml/Reservation.fxml",
                "/fxml/Table.fxml"
        );

        // Load dashboard initially
        Dashboardbtn.fire();

        // Setup WebSocket in separate thread
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

    @FXML
    private void navigate(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        Button[] buttons = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn,
                Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn};

        // Update button styles
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
                return; // No view to load

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

        if (fxmlFile != null) {
            final String viewToLoad = fxmlFile;
            // Load view asynchronously with loading indicator
            viewLoader.loadViewAsync(viewToLoad, content, () -> {
                // After view loads, preload adjacent views for faster navigation
                preloadAdjacentViews(clicked.getId());
            });
        }
    }

    /**
     * Preload views the user is likely to navigate to next
     * This improves perceived performance by loading views before they're needed
     */
    private void preloadAdjacentViews(String currentViewId) {
        switch (currentViewId) {
            case "Dashboardbtn":
                viewLoader.preloadView("/fxml/Reservation.fxml");
                break;
            case "ReservationManagementbtn":
                viewLoader.preloadView("/fxml/Table.fxml");
                break;
            case "TableManagementbtn":
                viewLoader.preloadView("/fxml/Account.fxml");
                break;
            case "ManageStaffAndAccountsbtn":
                viewLoader.preloadView("/fxml/Reports.fxml");
                break;
            case "Reportsbtn":
                viewLoader.preloadView("/fxml/ActivityLogs.fxml");
                break;
        }
    }

    // Controller getters - now using cached controllers from background loader
    public DashboardController getDashboardController() {
        return (DashboardController) viewLoader.getCachedController("/fxml/Dashboard.fxml");
    }

    public ReservationController getReservationController() {
        return (ReservationController) viewLoader.getCachedController("/fxml/Reservation.fxml");
    }

    public TableController getTableController() {
        return (TableController) viewLoader.getCachedController("/fxml/Table.fxml");
    }

    public AccountController getAccountController() {
        return (AccountController) viewLoader.getCachedController("/fxml/Account.fxml");
    }

    public ActivityLogsController getActivityLogsController() {
        return (ActivityLogsController) viewLoader.getCachedController("/fxml/ActivityLogs.fxml");
    }

    public ReportsController getReportsController() {
        return (ReportsController) viewLoader.getCachedController("/fxml/Reports.fxml");
    }

    @Override
    public void onNewReservation(WebupdateDTO reservation) {
        // Only update if Dashboard controller is already loaded (not forced loading)
        DashboardController controller = getDashboardController();
        if (controller != null) {
            // Update in background to avoid blocking the UI thread
            CompletableFuture.runAsync(() -> {
                Platform.runLater(() -> {
                    controller.updateLabels();
                });
            });
        }
    }
    @FXML
    private void logout(ActionEvent event) {
        // 1. Stop background tasks
        shutdown();

        if (wsClient != null) {
            wsClient.disconnect();
        }

        // 2. Clear user session
        this.currentuser = null;

        // 3. Close current stage or remove current scene
        Platform.runLater(() -> {
            try {
                // Load login scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                loader.setControllerFactory(springContext::getBean);
                Parent loginRoot = loader.load();

                // Replace the scene content
                Stage loginStage = new Stage();
                loginStage.initStyle(StageStyle.UNDECORATED);
                loginStage.setScene(new Scene(loginRoot));
                loginStage.setTitle("Login");
                loginStage.setResizable(false); // optional
                loginStage.show();
                loginStage.centerOnScreen();

                // Close current Stage (Administrator UI)
                Stage currentStage = (Stage) logoutBtn.getParentPopup().getOwnerWindow();
                currentStage.close();

                // Optionally center window
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Clean shutdown - call this when closing the application
     */
    public void shutdown() {
        if (viewLoader != null) {
            viewLoader.shutdown();
        }
    }
}