package com.mycompany.reservationsystem.controller.main;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.transition.BorderPaneTransition;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mycompany.reservationsystem.transition.ButtonTransition.setupButtonAnimation;

@Component
public class SettingsController {

    private User currentUser;

    @Autowired
    private AdministratorUIController adminUIController;

    @Autowired
    private PermissionService permissionService;

    /* ---------------- NAV BUTTONS ---------------- */
    @FXML private Button GeneralBtn;
    @FXML private Button CustomizeBtn;
    @FXML private Button MessageBtn;
    @FXML private Button PermissionBtn;
    @FXML private Button DatabaseBtn;

    /* ---------------- CONTENT PANES ---------------- */
    @FXML private BorderPane GeneralPane;
    @FXML private BorderPane MessagePane;
    @FXML private BorderPane DatabasePane;
    @FXML private BorderPane PermissionPane;

    /* ---------------- HEADER ---------------- */
    @FXML private Label Section;

    /* ---------------- STATE ---------------- */
    private BorderPane currentPane;

    /* ---------------- MESSAGING ---------------- */
    @FXML private MFXComboBox<String> messageDevicePortCombo;
    @FXML private Label ControllerName;
    @FXML private Label ModuleName;
    @FXML private Label PhoneNo;
    @FXML private Button TestButton;
    @FXML private ProgressIndicator ControllerProgress;
    @FXML private ProgressIndicator ModuleProgress;
    @FXML private ProgressIndicator PhoneNoProgress;

    private final Set<String> knownPorts = new HashSet<>();
    private final ScheduledExecutorService deviceMonitor = Executors.newSingleThreadScheduledExecutor();

    /* ---------------- PERMISSION ---------------- */
    @FXML private TableView<Permission> permissionTable;
    private Map<Long, Map<User.Position, Boolean>> permissionStates = new HashMap<>();

    @FXML private Button cancelBtn;

    /* ---------------- INITIALIZE ---------------- */
    @FXML
    public void initialize() {
        currentUser = adminUIController.getCurrentUser();

        // Set nav button visibility based on permissions
        PermissionBtn.setManaged(permissionService.hasPermission(currentUser, "VIEW_PERMISSION"));
        DatabaseBtn.setManaged(permissionService.hasPermission(currentUser, "VIEW_DATABASE"));

        showGeneral();

        setupButtonAnimation(GeneralBtn);
        setupButtonAnimation(CustomizeBtn);
        setupButtonAnimation(MessageBtn);
        setupButtonAnimation(PermissionBtn);
        setupButtonAnimation(DatabaseBtn);

        startDeviceMonitoring();

        Platform.runLater(() -> {
            setupPermissionTable();
            loadPermissions();
        });
    }

    /* ---------------- NAV ACTIONS ---------------- */
    @FXML private void showGeneral()   { switchPane(GeneralPane, "GENERAL", GeneralBtn); }
    @FXML private void showCustomize() { switchPane(null, "CUSTOMIZE", CustomizeBtn); }
    @FXML private void showMessage()   { switchPane(MessagePane, "MESSAGING", MessageBtn); }
    @FXML private void showPermission(){ switchPane(PermissionPane, "PERMISSION", PermissionBtn); }
    @FXML private void showDatabase()  { switchPane(DatabasePane, "DATABASE", DatabaseBtn); }

    private void switchPane(BorderPane targetPane, String title, Button activeBtn) {
        Section.setText(title);
        setActiveButton(activeBtn);

        if (currentPane == targetPane) return;

        BorderPane oldPane = currentPane;
        currentPane = targetPane;

        if (oldPane != null) {
            BorderPaneTransition.animateOut(oldPane, () -> {
                if (targetPane != null) BorderPaneTransition.animateIn(targetPane);
            });
        } else if (targetPane != null) {
            BorderPaneTransition.animateIn(targetPane);
        }
    }

    private void setActiveButton(Button active) {
        List<Button> buttons = List.of(GeneralBtn, CustomizeBtn, MessageBtn, PermissionBtn, DatabaseBtn);
        buttons.forEach(btn -> btn.getStyleClass().remove("settings-nav-active"));
        if (!active.getStyleClass().contains("settings-nav-active")) active.getStyleClass().add("settings-nav-active");
    }

    /* ---------------- MESSAGING DEVICE MONITOR ---------------- */
    private void startDeviceMonitoring() {
        deviceMonitor.scheduleAtFixedRate(this::updateMessagingDevices, 0, 1, TimeUnit.SECONDS);
    }

    private void updateMessagingDevices() {
        try {
            SerialPort[] ports = SerialPort.getCommPorts();
            Set<String> currentPorts = new HashSet<>();

            for (SerialPort port : ports) {
                String name = port.getSystemPortName();
                currentPorts.add(name);

                if (!knownPorts.contains(name)) {
                    Platform.runLater(() -> {
                        if (!messageDevicePortCombo.getItems().contains(name)) {
                            messageDevicePortCombo.getItems().add(name);
                            System.out.println("Added: " + name);
                        }
                    });
                }
            }

            // Remove disconnected ports
            for (String oldPort : new HashSet<>(knownPorts)) {
                if (!currentPorts.contains(oldPort)) {
                    Platform.runLater(() -> {
                        messageDevicePortCombo.getItems().remove(oldPort);
                        if (messageDevicePortCombo.getItems().isEmpty()) {
                            messageDevicePortCombo.setValue("No device found");
                        }
                        System.out.println("Removed: " + oldPort);
                    });
                }
            }

            synchronized (knownPorts) {
                knownPorts.clear();
                knownPorts.addAll(currentPorts);
            }

            // Set default value if ComboBox has items
            Platform.runLater(() -> {
                if (!messageDevicePortCombo.getItems().isEmpty() && messageDevicePortCombo.getValue() == null) {
                    messageDevicePortCombo.setValue(messageDevicePortCombo.getItems().get(0));
                }
            });

        } catch (Exception ex) {
            System.err.println("Error scanning ports: " + ex.getMessage());
        }
    }

    /* ---------------- TEST DEVICE ---------------- */
    @FXML
    private void TestDevice(ActionEvent e) {
        ControllerName.setText("Searching...");
        ModuleName.setText("Searching...");
        PhoneNo.setText("Searching...");

        DeviceDetectionManager manager = new DeviceDetectionManager();
        Task<DeviceDetectionManager.DeviceResult> task = manager.createDetectionTask();

        ControllerProgress.visibleProperty().bind(task.runningProperty());
        ModuleProgress.visibleProperty().bind(task.runningProperty());
        PhoneNoProgress.visibleProperty().bind(task.runningProperty());
        TestButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(ev -> {
            DeviceDetectionManager.DeviceResult result = task.getValue();
            ControllerName.setText(result.controller);
            ModuleName.setText(result.module);
            PhoneNo.setText(DeviceDetectionManager.cleanPhoneResponse(result.phone));
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            ControllerName.setText("Error");
            ModuleName.setText("Error");
            PhoneNo.setText(ex.getMessage() != null ? ex.getMessage() : "Unknown error");
        });

        new Thread(task).start();
    }

    /* ---------------- PERMISSIONS ---------------- */
    private void setupPermissionTable() {
        permissionTable.getColumns().clear();

        // ---------------- Permission code column ----------------
        TableColumn<Permission, String> codeCol = new TableColumn<>("Permission");
        codeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCode())
        );
        codeCol.prefWidthProperty().bind(permissionTable.widthProperty().multiply(0.4));
        permissionTable.getColumns().add(codeCol);

        // ---------------- Role columns dynamically ----------------
        for (User.Position position : User.Position.values()) {
            TableColumn<Permission, Boolean> roleCol = new TableColumn<>(position.name());
            roleCol.prefWidthProperty().bind(permissionTable.widthProperty().multiply(0.2));

            // Only read/write from permissionStates, no DB access here
            roleCol.setCellValueFactory(cellData -> {
                Permission permission = cellData.getValue();
                Map<User.Position, Boolean> states = permissionStates.get(permission.getId());

                // Create a BooleanProperty that reads/writes directly to the map
                BooleanProperty prop = new SimpleBooleanProperty(states.get(position));
                prop.addListener((obs, oldVal, newVal) -> states.put(position, newVal));
                return prop;
            });

            roleCol.setCellFactory(CheckBoxTableCell.forTableColumn(roleCol));
            roleCol.setEditable(true);

            // Update only the in-memory map
            roleCol.setOnEditCommit(event -> {
                Permission permission = event.getRowValue();
                Map<User.Position, Boolean> states = permissionStates.get(permission.getId());
                states.put(position, event.getNewValue());
            });

            permissionTable.getColumns().add(roleCol);
        }

        permissionTable.setEditable(true);
    }

    private void loadPermissions() {
        List<Permission> permissions = permissionService.findAllPermissions();

        // ---------------- Populate permissionStates from DB ----------------
        permissionStates.clear();
        for (Permission p : permissions) {
            Map<User.Position, Boolean> map = new HashMap<>();
            for (User.Position pos : User.Position.values()) {
                map.put(pos, permissionService.hasPermission(pos, p));
            }
            permissionStates.put(p.getId(), map);
        }

        ObservableList<Permission> rows = FXCollections.observableArrayList(permissions);
        permissionTable.setItems(rows);
    }

    @FXML
    private void applyChanges() {
        // Apply permission changes
        permissionStates.forEach((permissionId, roleMap) -> roleMap.forEach(
                (position, enabled) -> permissionService.updatePermission(position, permissionId, enabled)
        ));

        loadPermissions();

    }

    /* ---------------- CANCEL ---------------- */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    /* ---------------- SHUTDOWN ---------------- */
    public void stopController() {
        deviceMonitor.shutdownNow();
    }
}
