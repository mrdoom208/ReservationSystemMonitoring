package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.UserService;
import com.mycompany.reservationsystem.util.BackgroundViewLoader;
import com.mycompany.reservationsystem.util.NotificationManager;
import com.mycompany.reservationsystem.websocket.WebSocketListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import com.mycompany.reservationsystem.websocket.WebUpdateHandlerImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.mycompany.reservationsystem.transition.ButtonTransition.setupButtonAnimation;

/**
 * FXML Controller class for Administrator UI
 * Manages navigation and view loading with background processing
 *
 * @author formentera
 */
@Component
public class AdministratorUIController implements Initializable {

    public User currentuser;
    private static BackgroundViewLoader viewLoader;
    private static DeviceDetectionManager deviceDetectionManager = new DeviceDetectionManager();




    public void setDeviceDetectionManager(DeviceDetectionManager deviceDetectionManager) {
        AdministratorUIController.deviceDetectionManager = deviceDetectionManager;
    }

    public static DeviceDetectionManager getDeviceDetectionManager() {
        return deviceDetectionManager;
    }

    public void setUser(User user) {
        this.currentuser = user;
        applyPermissions();
        System.out.println(currentuser.getPosition());
        accountname.setText(currentuser.getFirstname()+" "+currentuser.getLastname());
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
    private Label header,accountname;
    @FXML
    private StackPane content;
    /*--------Bottom Pane-------------*/
    @FXML
    private HBox HboxProgress;

    public HBox getHboxProgress(){
        return HboxProgress;
    }

    @FXML
    private Label LabelProgress;

    public Label getLabelProgress(){
        return LabelProgress;
    }

    @FXML
    private ProgressBar BarProgress;

    public ProgressBar getBarProgress(){
        return BarProgress;
    }


    /*--------------------------------------*/
    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    private Reservation selectedReservation;

    @Autowired
    private WebUpdateHandlerImpl webUpdateHandler;

    WebSocketClient wsClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize background loader

        viewLoader = new BackgroundViewLoader(springContext);

        // Preload commonly used views on startup (happens in background)
        viewLoader.preloadViews(
                "/fxml/main/Reservation.fxml",
                "/fxml/main/Table.fxml"
        );

        setupButtonAnimation(Dashboardbtn);
        setupButtonAnimation(ReservationManagementbtn);
        setupButtonAnimation(TableManagementbtn);
        setupButtonAnimation(Messagingbtn);
        setupButtonAnimation(Reportsbtn);
        setupButtonAnimation(ReservationManagementbtn);
        setupButtonAnimation(ManageStaffAndAccountsbtn);
        setupButtonAnimation(ActivityLogbtn);

        // Load dashboard initially
        Dashboardbtn.fire();

        missingDetails();


        // Setup WebSocket in separate thread
        this.wsClient = new WebSocketClient("ws://localhost:8080/ws");
        this.wsClient.addListener(webUpdateHandler);
        this.wsClient.connect();

        HboxProgress.setManaged(false);
        HboxProgress.setVisible(false);

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
                fxmlFile = "/fxml/main/Dashboard.fxml";
                break;

            case "ReservationManagementbtn":
                header.setText("Reservation Management");
                fxmlFile = "/fxml/main/Reservation.fxml";
                break;

            case "TableManagementbtn":
                header.setText("Table Management");
                fxmlFile = "/fxml/main/Table.fxml";
                break;

            case "Messagingbtn":
                header.setText("Message Management");
                fxmlFile = "/fxml/main/Messaging.fxml";
                break;

            case "ManageStaffAndAccountsbtn":
                header.setText("Account Management");
                fxmlFile = "/fxml/main/Account.fxml";
                break;

            case "Reportsbtn":
                header.setText("Reports");
                fxmlFile = "/fxml/main/Reports.fxml";
                break;

            case "ActivityLogbtn":
                header.setText("Activity Logs");
                fxmlFile = "/fxml/main/ActivityLogs.fxml";
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
    @FXML
    private void Profile(){
        Platform.runLater(() ->{
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main/Profile.fxml"));
                loader.setControllerFactory(springContext::getBean);
                Parent root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.initOwner(App.primaryStage); // mainStage is your primary stage
                dialogStage.initStyle(StageStyle.TRANSPARENT);
                dialogStage.setResizable(false);
                Scene scn = new Scene(root);
                scn.setFill(Color.TRANSPARENT);
                dialogStage.setScene(scn);

                // Link controller with dialog stage
                ProfileController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setCurrentuser(currentuser);
                dialogStage.showAndWait(); // wait until closed
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    private void Settings(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                // Load login scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main/Settings.fxml"));
                loader.setControllerFactory(springContext::getBean);
                Parent settingsRoot = loader.load();

                // Replace the scene content
                Stage settingsStage = new Stage();
                settingsStage.initStyle(StageStyle.UNDECORATED);
                settingsStage.setScene(new Scene(settingsRoot));
                settingsStage.setTitle("Settings");
                settingsStage.initModality(Modality.APPLICATION_MODAL);
                settingsStage.initOwner(App.primaryStage);
                settingsStage.setResizable(false); // optional
                settingsStage.centerOnScreen();
                settingsStage.showAndWait();





                // Optionally center window
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }




    /**
     * Preload views the user is likely to navigate to next
     * This improves perceived performance by loading views before they're needed
     */
    private void preloadAdjacentViews(String currentViewId) {
        switch (currentViewId) {
            case "Dashboardbtn":
                viewLoader.preloadView("/fxml/main/Reservation.fxml");
                //viewLoader.preloadView("/fxml/main/Settings.fxml");
                break;
            case "ReservationManagementbtn":
                viewLoader.preloadView("/fxml/main/Table.fxml");
                break;
            case "TableManagementbtn":
                viewLoader.preloadView("/fxml/main/Messaging.fxml");
                break;
            case "Messagingbtn":
                viewLoader.preloadView("/fxml/main/Account.fxml");
                break;
            case "ManageStaffAndAccountsbtn":
                viewLoader.preloadView("/fxml/main/Reports.fxml");
                break;
            case "Reportsbtn":
                viewLoader.preloadView("/fxml/main/ActivityLogs.fxml");
                break;
        }
    }

    private void applyPermissions() {
        if (currentuser == null) return;

        // Map each button to its required permission code
        Map<Button, String> buttonPermissions = Map.of(
                Dashboardbtn,"VIEW_DASHBOARD",
                ReservationManagementbtn, "VIEW_RESERVATION",
                TableManagementbtn, "VIEW_TABLES",
                Messagingbtn, "VIEW_MESSAGING",
                Reportsbtn, "VIEW_REPORTS",
                ManageStaffAndAccountsbtn, "VIEW_ACCOUNTS",
                ActivityLogbtn, "VIEW_ACTIVITY_LOGS"
        );

        // Disable buttons if user doesn't have permission
        buttonPermissions.forEach((button, code) ->
                button.setManaged(permissionService.hasPermission(currentuser, code))


        );
    }

    // Controller getters - now using cached controllers from background loader
    public static DashboardController getDashboardController() {
        return (DashboardController) viewLoader.getCachedController("/fxml/main/Dashboard.fxml");
    }

    public static ReservationController getReservationController() {
        return (ReservationController) viewLoader.getCachedController("/fxml/main/Reservation.fxml");
    }

    public static TableController getTableController() {
        return (TableController) viewLoader.getCachedController("/fxml/main/Table.fxml");
    }
    public static MessagingController getMessagingController(){
        return (MessagingController) viewLoader.getCachedController("/fxml/Message.fxml");
    }

    public static AccountController getAccountController() {
        return (AccountController) viewLoader.getCachedController("/fxml/main/Account.fxml");
    }

    public static ActivityLogsController getActivityLogsController() {
        return (ActivityLogsController) viewLoader.getCachedController("/fxml/main/ActivityLogs.fxml");
    }

    public static ReportsController getReportsController() {
        return (ReportsController) viewLoader.getCachedController("/fxml/main/Reports.fxml");
    }
    public static SettingsController getSettingsController() {
        return (SettingsController) viewLoader.getCachedController("/fxml/main/Settings.fxml");
    }


    private void missingDetails(){
        userService.createAdminIfMissing();
        messageService.createIfMissing("New Reservation");
        messageService.createIfMissing("Confirm Reservation");
        messageService.createIfMissing("Cancelled Reservation");
        messageService.createIfMissing("Complete Reservation");


        if(AppSettings.loadMessageLabel("message.new").isBlank()){
            AppSettings.saveMessageLabel("message.new","New Reservation");}

        if(AppSettings.loadMessageLabel("message.cancelled").isBlank()){
            AppSettings.saveMessageLabel("message.cancelled","Cancelled Reservation");}

        if(AppSettings.loadMessageLabel("message.confirm").isBlank()){
            AppSettings.saveMessageLabel("message.confirm","Confirm Reservation");}

        if(AppSettings.loadMessageLabel("message.complete").isBlank()){
            AppSettings.saveMessageLabel("message.complete","Complete Reservation");}
    }

    @FXML
    private void logout() {
        // 1. Stop background tasks
        shutdown();

        if (wsClient != null) {
            wsClient.disconnect();
        }

        // 2. Clear user session
        this.currentuser.setStatus("Offline");
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
                App.primaryStage = loginStage;
                loginStage.initStyle(StageStyle.TRANSPARENT);
                Scene scn = new Scene(loginRoot);
                scn.setFill(Color.TRANSPARENT);
                loginStage.setScene(scn);
                loginStage.setTitle("Login");
                loginStage.setResizable(false); // optional
                loginStage.show();
                loginStage.centerOnScreen();

                // Close current Stage (Administrator UI)
                Stage currentStage = (Stage) logoutBtn.getParentPopup().getOwnerWindow();
                currentStage.close();

                // Optionally center window
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