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
import javafx.util.StringConverter;
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

    private DeviceDetectionManager manager;

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

    private BorderPane currentPane;

    /* ---------------- MESSAGING ---------------- */
    @FXML private MFXComboBox<SerialPort> messageDevicePortCombo;
    @FXML private Label ControllerName;
    @FXML private Label ModuleName;
    @FXML private Label PhoneNo;
    @FXML private Button TestButton;
    @FXML private ProgressIndicator ControllerProgress;
    @FXML private ProgressIndicator ModuleProgress;
    @FXML private ProgressIndicator PhoneNoProgress;

    private final Set<String> knownPorts = new HashSet<>();
    private final ScheduledExecutorService deviceMonitor =
            Executors.newSingleThreadScheduledExecutor();

    /* ---------------- PERMISSION ---------------- */
    @FXML private TableView<Permission> permissionTable;
    private final Map<Long, Map<User.Position, Boolean>> permissionStates = new HashMap<>();

    @FXML private Button cancelBtn;

    /* ---------------- INITIALIZE ---------------- */
    @FXML
    public void initialize() {
        currentUser = adminUIController.getCurrentUser();

        PermissionBtn.setManaged(permissionService.hasPermission(currentUser, "VIEW_PERMISSION"));
        DatabaseBtn.setManaged(permissionService.hasPermission(currentUser, "VIEW_DATABASE"));

        showGeneral();

        setupButtonAnimation(GeneralBtn);
        setupButtonAnimation(CustomizeBtn);
        setupButtonAnimation(MessageBtn);
        setupButtonAnimation(PermissionBtn);
        setupButtonAnimation(DatabaseBtn);

        setupSerialPortCombo();
        startDeviceMonitoring();

        Platform.runLater(() -> {
            setupPermissionTable();
            loadPermissions();
        });
    }

    /* ---------------- SERIAL PORT COMBO ---------------- */
    private void setupSerialPortCombo() {
        messageDevicePortCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SerialPort port) {
                return port == null ? "" : port.getSystemPortName();
            }

            @Override
            public SerialPort fromString(String string) {
                return null;
            }
        });
    }

    /* ---------------- NAV ACTIONS ---------------- */
    @FXML private void showGeneral()    { switchPane(GeneralPane, "GENERAL", GeneralBtn); }
    @FXML private void showCustomize()  { switchPane(null, "CUSTOMIZE", CustomizeBtn); }
    @FXML private void showMessage()    { switchPane(MessagePane, "MESSAGING", MessageBtn); }
    @FXML private void showPermission() { switchPane(PermissionPane, "PERMISSION", PermissionBtn); }
    @FXML private void showDatabase()   { switchPane(DatabasePane, "DATABASE", DatabaseBtn); }

    private void switchPane(BorderPane targetPane, String title, Button activeBtn) {
        Section.setText(title);
        setActiveButton(activeBtn);

        if (currentPane == targetPane) return;

        BorderPane oldPane = currentPane;
        currentPane = targetPane;

        if (oldPane != null) {
            BorderPaneTransition.animateOut(oldPane, () -> {
                if (targetPane != null) {
                    BorderPaneTransition.animateIn(targetPane);
                }
            });
        } else if (targetPane != null) {
            BorderPaneTransition.animateIn(targetPane);
        }
    }

    private void setActiveButton(Button active) {
        List<Button> buttons =
                List.of(GeneralBtn, CustomizeBtn, MessageBtn, PermissionBtn, DatabaseBtn);
        buttons.forEach(b -> b.getStyleClass().remove("settings-nav-active"));
        active.getStyleClass().add("settings-nav-active");
    }

    /* ---------------- DEVICE MONITOR ---------------- */
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
                        messageDevicePortCombo.getItems().add(port);
                        System.out.println("Added: " + name);
                    });
                }
            }

            for (String oldPort : new HashSet<>(knownPorts)) {
                if (!currentPorts.contains(oldPort)) {
                    Platform.runLater(() -> {
                        messageDevicePortCombo.getItems()
                                .removeIf(p -> p.getSystemPortName().equals(oldPort));
                        System.out.println("Removed: " + oldPort);
                    });
                }
            }

            knownPorts.clear();
            knownPorts.addAll(currentPorts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------- TEST DEVICE ---------------- */
    @FXML
    private void TestDevice(ActionEvent e) {
        SerialPort selected = messageDevicePortCombo.getValue();
        if (selected == null) return;

        ControllerName.setText("Searching...");
        ModuleName.setText("Searching...");
        PhoneNo.setText("Searching...");

        if (manager == null) {
            manager = new DeviceDetectionManager();
        }

        Task<DeviceDetectionManager.DeviceResult> task = new Task<>() {
            @Override
            protected DeviceDetectionManager.DeviceResult call() throws Exception {
                manager.openPort(selected, 115200); // or user-selected baud
                return manager.detectDevice();
            }
        };

        ControllerProgress.visibleProperty().bind(task.runningProperty());
        ModuleProgress.visibleProperty().bind(task.runningProperty());
        PhoneNoProgress.visibleProperty().bind(task.runningProperty());
        TestButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(ev -> {
            var r = task.getValue();
            ControllerName.setText(r.controller);
            ModuleName.setText(r.module);
            PhoneNo.setText(r.phone);
            //PhoneNo.setText(DeviceDetectionManager.cleanPhoneResponse(r.phone));
        });

        task.setOnFailed(ev -> {
            ControllerName.setText("Error");
            ModuleName.setText("Error");
            PhoneNo.setText("Detection failed");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    /* ---------------- PERMISSIONS ---------------- */
    private void setupPermissionTable() {
        permissionTable.getColumns().clear();

        TableColumn<Permission, String> codeCol = new TableColumn<>("Permission");
        codeCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCode()));
        codeCol.prefWidthProperty().bind(permissionTable.widthProperty().multiply(0.4));
        permissionTable.getColumns().add(codeCol);

        for (User.Position pos : User.Position.values()) {
            TableColumn<Permission, Boolean> col = new TableColumn<>(pos.name());
            col.prefWidthProperty().bind(permissionTable.widthProperty().multiply(0.2));

            col.setCellValueFactory(cd -> {
                BooleanProperty prop =
                        new SimpleBooleanProperty(permissionStates
                                .get(cd.getValue().getId()).get(pos));
                prop.addListener((o, a, b) ->
                        permissionStates.get(cd.getValue().getId()).put(pos, b));
                return prop;
            });

            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            permissionTable.getColumns().add(col);
        }

        permissionTable.setEditable(true);
    }

    private void loadPermissions() {
        List<Permission> permissions = permissionService.findAllPermissions();
        permissionStates.clear();

        for (Permission p : permissions) {
            Map<User.Position, Boolean> map = new HashMap<>();
            for (User.Position pos : User.Position.values()) {
                map.put(pos, permissionService.hasPermission(pos, p));
            }
            permissionStates.put(p.getId(), map);
        }

        permissionTable.setItems(FXCollections.observableArrayList(permissions));
    }

    /* ---------------- APPLY / CANCEL ---------------- */
    @FXML
    private void applyChanges() {
        permissionStates.forEach((id, map) ->
                map.forEach((pos, enabled) ->
                        permissionService.updatePermission(pos, id, enabled)));
        loadPermissions();
        if (manager != null && manager.isPortOpen()) {
            adminUIController.setDeviceDetectionManager(manager);
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }

    @FXML
    private void handleOK() {
        applyChanges();
        handleCancel();
    }

    /* ---------------- SHUTDOWN ---------------- */
    public void stopController() {
        deviceMonitor.shutdownNow();
    }
}
