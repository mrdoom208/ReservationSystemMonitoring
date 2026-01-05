package com.mycompany.reservationsystem.controller.main;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.MessageRepository;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.WebsiteSyncService;
import com.mycompany.reservationsystem.transition.BorderPaneTransition;
import com.mycompany.reservationsystem.util.ComboBoxUtil;
import com.mycompany.reservationsystem.util.ToggleButtonUtil;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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

    /* ---------------- CORE ---------------- */
    private User currentUser;

    @Autowired
    private AdministratorUIController adminUIController;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private WebsiteSyncService websiteSyncService;

    @Autowired
    private MessageRepository messageRepository;

    /* ---------------- NAV ---------------- */
    @FXML private Button GeneralBtn;
    @FXML private Button CustomizeBtn;
    @FXML private Button MessageBtn;
    @FXML private Button PermissionBtn;
    @FXML private Button DatabaseBtn;

    /* ---------------- PANES ---------------- */
    @FXML private BorderPane GeneralPane;
    @FXML private BorderPane MessagePane;
    @FXML private BorderPane PermissionPane;
    @FXML private BorderPane DatabasePane;
    private BorderPane currentPane;

    @FXML private Label Section;
    /*--------------------- GENERAL ------------------------------------*/
    @FXML
    private TextField ApplicationTitle;
    @FXML
    private MFXComboBox<String> AutoCancelTime;

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
    private ScheduledExecutorService deviceMonitor;
    @FXML private MFXComboBox<Message> newReservation,cancelledReservation,confirmReservation,completeReservation;
    @FXML private MFXToggleButton newReservationtoggle,cancelledReservationtoggle,confirmReservationtoggle,completeReservationtoggle;

    /* ---------------- PERMISSIONS ---------------- */
    @FXML private TableView<Permission> permissionTable;
    private final Map<Long, Map<User.Position, Boolean>> permissionStates = new HashMap<>();

    @FXML private Button cancelBtn;
    /*--------------------- Database ------------------------------*/
    @FXML private MFXComboBox<String> DataDuration;

    /*------------------- UI RESPONSE  ------------------*/
    @FXML private Label UIresponse;
    @FXML private Button Apply;


    /* ================= INITIALIZE ================= */
    @FXML
    public void initialize() {
        currentUser = adminUIController.getCurrentUser();

        ApplicationTitle.setDisable(!permissionService.hasPermission(currentUser, "CHANGE_TITLE"));
        PermissionBtn.setManaged(permissionService.hasPermission(currentUser, "VIEW_PERMISSION"));
        DatabaseBtn.setManaged(permissionService.hasPermission(currentUser, "VIEW_DATABASE"));

        startPortMonitoring();



        showGeneral();

        setupGeneral();
        setupMessaging();
        setupDatabase();
        setupButtons();
        setupSerialCombo();

        TestButton.disableProperty().bind(messageDevicePortCombo.valueProperty().isNull());

        Platform.runLater(() -> {
            restoreSettings();
            setupPermissionTable();
            loadPermissions();
        });
    }

    /* ================= UI ================= */
    private void setupButtons() {
        setupButtonAnimation(GeneralBtn);
        setupButtonAnimation(CustomizeBtn);
        setupButtonAnimation(MessageBtn);
        setupButtonAnimation(PermissionBtn);
        setupButtonAnimation(DatabaseBtn);
    }

    @FXML private void showGeneral()    { switchPane(GeneralPane, "GENERAL", GeneralBtn); }
    @FXML private void showCustomize()  { switchPane(null, "CUSTOMIZE", CustomizeBtn); }
    @FXML private void showMessage()    { switchPane(MessagePane, "MESSAGING", MessageBtn); }
    @FXML private void showPermission() { switchPane(PermissionPane, "PERMISSION", PermissionBtn); }
    @FXML private void showDatabase()   { switchPane(DatabasePane, "DATABASE", DatabaseBtn); }

    private void switchPane(BorderPane pane, String title, Button btn) {
        Section.setText(title);
        setActive(btn);

        if (currentPane == pane) return;
        BorderPane old = currentPane;
        currentPane = pane;
        if (old != null) {
            BorderPaneTransition.animateOut(old, () -> {
                if (pane != null) BorderPaneTransition.animateIn(pane);

            });
        } else if (pane != null) {
            BorderPaneTransition.animateIn(pane);
        }
    }

    private void setActive(Button active) {
        List.of(GeneralBtn, CustomizeBtn, MessageBtn, PermissionBtn, DatabaseBtn)
                .forEach(b -> b.getStyleClass().remove("settings-nav-active"));
        active.getStyleClass().add("settings-nav-active");
    }
    /*=============================== GENERAL =========================================*/
    private void setupGeneral(){

        List<String> options = Arrays.asList("2 minutes", "5 minutes", "10 minutes", "15 minutes", "20 minutes");
        AutoCancelTime.getItems().addAll(options);

        // Optional: set default selection
        AutoCancelTime.selectItem("5 minutes");
    }
    public int getSelectedMinutes() {
        String value = AutoCancelTime.getValue(); // e.g., "10 minutes"
        if (value != null && value.matches("\\d+.*")) {
            return Integer.parseInt(value.replaceAll("\\D+", "")); // extracts the number
        }
        return 0; // default if nothing selected
    }

    /* ================= SERIAL PORTS ================= */
    private void setupSerialCombo() {
        messageDevicePortCombo.setConverter(new StringConverter<>() {
            @Override public String toString(SerialPort p) {
                return p == null ? "" : p.getSystemPortName();
            }
            @Override public SerialPort fromString(String s) { return null; }
        });
    }

    private void startPortMonitoring() {
        if (deviceMonitor == null || deviceMonitor.isShutdown() || deviceMonitor.isTerminated()) {
            deviceMonitor = Executors.newSingleThreadScheduledExecutor();
        }

        // Clear combo and populate all ports immediately
        Platform.runLater(() -> {
            messageDevicePortCombo.getItems().clear();
            SerialPort[] ports = SerialPort.getCommPorts();
            messageDevicePortCombo.getItems().addAll(ports);
        });

        // Start background polling
        deviceMonitor.scheduleAtFixedRate(this::refreshPorts, 1, 1, TimeUnit.SECONDS);

        messageDevicePortCombo.valueProperty().addListener((o, a, b) -> {
            if (b != null) AppSettings.saveSerialPort(b.getSystemPortName());
        });
    }

    private void refreshPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        Set<String> current = new HashSet<>();

        for (SerialPort p : ports) {
            String name = p.getSystemPortName();
            current.add(name);
            if (!knownPorts.contains(name)) {
                Platform.runLater(() -> {
                    if (!messageDevicePortCombo.getItems().contains(p)) {
                        messageDevicePortCombo.getItems().add(p);
                    }
                });
            }
        }


        knownPorts.removeIf(old -> !current.contains(old));

        Platform.runLater(() ->
                messageDevicePortCombo.getItems().removeIf(p -> !current.contains(p.getSystemPortName()))
        );
        Platform.runLater(() -> {
            SerialPort selected = messageDevicePortCombo.getValue();
            if (selected != null && !current.contains(selected.getSystemPortName())) {
                // Port unplugged
                messageDevicePortCombo.setValue(null);
                ControllerName.setText("Disconnected");
                ModuleName.setText("Disconnected");
                PhoneNo.setText("Disconnected");

                AppSettings.saveController("");
                AppSettings.saveModule("");
                AppSettings.savePhone("");
            }
        });

        knownPorts.clear();
        knownPorts.addAll(current);
    }


    /* ================= TEST DEVICE ================= */
    @FXML
    private void TestDevice() {
        SerialPort selected = messageDevicePortCombo.getValue();
        if (selected == null) return;

        ControllerName.setText("Searching...");
        ModuleName.setText("Searching...");
        PhoneNo.setText("Searching...");

        Task<DeviceDetectionManager.DeviceResult> task = new Task<>() {
            @Override
            protected DeviceDetectionManager.DeviceResult call() throws Exception {
                DeviceDetectionManager mgr = new DeviceDetectionManager();
                try {
                    mgr.openPort(selected.getSystemPortName(), 115200); // use port name string
                    return mgr.detectDevice();
                } finally {
                    mgr.closePort();
                }
            }
        };

        bindTask(task);

        task.setOnSucceeded(e -> {
            var r = task.getValue();
            ControllerName.setText(r.controller);
            ModuleName.setText(r.module);
            PhoneNo.setText(r.phone);

            AppSettings.saveController(r.controller);
            AppSettings.saveModule(r.module);
            AppSettings.savePhone(r.phone);
        });

        task.setOnFailed(e -> {
            ControllerName.setText("Detection failed");
            ModuleName.setText("Detection failed");
            PhoneNo.setText("Detection failed");
            task.getException().printStackTrace();
        });

        new Thread(task, "device-test").start();
    }
    private void bindTask(Task<?> t) {
        ControllerProgress.visibleProperty().bind(t.runningProperty());
        ModuleProgress.visibleProperty().bind(t.runningProperty());
        PhoneNoProgress.visibleProperty().bind(t.runningProperty());
        TestButton.disableProperty().bind(t.runningProperty());
    }

    //===================== MESSAGING =======================================
    private void setupMessaging(){

        List<Message> allMessages = messageRepository.findAll();

        // Use separate ObservableLists for each ComboBox
        newReservation.setItems(FXCollections.observableArrayList(allMessages));
        cancelledReservation.setItems(FXCollections.observableArrayList(allMessages));
        confirmReservation.setItems(FXCollections.observableArrayList(allMessages));
        completeReservation.setItems(FXCollections.observableArrayList(allMessages));

        // Format each ComboBox
        ComboBoxUtil.formatMessageComboBox(newReservation);
        ComboBoxUtil.formatMessageComboBox(cancelledReservation);
        ComboBoxUtil.formatMessageComboBox(confirmReservation);
        ComboBoxUtil.formatMessageComboBox(completeReservation);


        ComboBoxUtil.selectMessageByLabel(newReservation, AppSettings.loadMessageLabel("message.new"));
        ComboBoxUtil.selectMessageByLabel(cancelledReservation, AppSettings.loadMessageLabel("message.cancelled"));
        ComboBoxUtil.selectMessageByLabel(confirmReservation, AppSettings.loadMessageLabel("message.confirm"));
        ComboBoxUtil.selectMessageByLabel(completeReservation, AppSettings.loadMessageLabel("message.complete"));

        ToggleButtonUtil.setupToggle(newReservationtoggle, "newReservation");
        ToggleButtonUtil.setupToggle(confirmReservationtoggle, "confirmReservation");
        ToggleButtonUtil.setupToggle(cancelledReservationtoggle, "cancelledReservation");
        ToggleButtonUtil.setupToggle(completeReservationtoggle, "completeReservation");
    }






    /* ================= PERMISSIONS ================= */
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
                BooleanProperty prop = new SimpleBooleanProperty(
                        permissionStates.get(cd.getValue().getId()).get(pos));
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
        permissionStates.clear();
        var perms = permissionService.findAllPermissions();

        for (Permission p : perms) {
            Map<User.Position, Boolean> map = new EnumMap<>(User.Position.class);
            for (User.Position pos : User.Position.values()) {
                map.put(pos, permissionService.hasPermission(pos, p));
            }
            permissionStates.put(p.getId(), map);
        }
        permissionTable.setItems(FXCollections.observableArrayList(perms));
    }
    /*=================== DATABASE ===================================*/
    private void setupDatabase(){
        DataDuration.setItems(FXCollections.observableArrayList(
                "3 months", "6 months", "12 months", "18 months", "24 months"
        ));
        if(AppSettings.loadDatabaseDeleteTime().isBlank() || AppSettings.loadDatabaseDeleteTime() == null) {
            DataDuration.selectFirst();
            return;
        }
        DataDuration.selectItem(AppSettings.loadDatabaseDeleteTime());

    }

    /* ================= APPLY / CLOSE ================= */
    @FXML
    private void applyChanges() {
        Apply.setDisable(true);
        messageresponse(true, "");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(25, 25); // smaller spinner to fit nicely

        UIresponse.setGraphic(progressIndicator);
        progressIndicator.setVisible(true); // show it

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                /*------------------ General ------------------------*/
                AppSettings.saveApplicationTitle(ApplicationTitle.getText());
                websiteSyncService.sendAutoCancelTime(getSelectedMinutes());

                /*----------------- Messaging --------------------------*/
                AppSettings.saveController(ControllerName.getText());
                AppSettings.saveModule(ModuleName.getText());
                AppSettings.savePhone(PhoneNo.getText());
                AppSettings.saveCancelTime(AutoCancelTime.getSelectedItem());
                AppSettings.saveMessageLabel("message.new",newReservation.getValue().getMessageLabel());
                AppSettings.saveMessageLabel("message.cancelled",cancelledReservation.getValue().getMessageLabel());
                AppSettings.saveMessageLabel("message.confirm",confirmReservation.getValue().getMessageLabel());
                AppSettings.saveMessageLabel("message.complete",completeReservation.getValue().getMessageLabel());
                AppSettings.saveMessageEnabled("newReservation",newReservationtoggle.isSelected());
                AppSettings.saveMessageEnabled("cancelledReservation",cancelledReservationtoggle.isSelected());
                AppSettings.saveMessageEnabled("confirmReservation",confirmReservationtoggle.isSelected());
                AppSettings.saveMessageEnabled("completeReservation",completeReservationtoggle.isSelected());


                /*------------- Permission ------------*/
                permissionStates.forEach((id, map) ->
                        map.forEach((pos, enabled) ->
                                permissionService.updatePermission(pos, id, enabled)));
                /*---------------- Database --------------------------*/
                String selectedMonths = DataDuration.getSelectionModel().getSelectedItem();

                if (selectedMonths != null && !selectedMonths.isEmpty()) {
                    int months = Integer.parseInt(selectedMonths.split(" ")[0]);
                    websiteSyncService.sendAutoDeleteMonths(months);
                }
                AppSettings.saveDatabaseDeleteTime(selectedMonths);
                return null;
            }

            @Override
            protected void succeeded() {
                UIresponse.setGraphic(null); // remove spinner
                messageresponse(true, "APPLY SUCCESSFULLY");
                Apply.setDisable(false);
            }

            @Override
            protected void failed() {
                UIresponse.setGraphic(null);
                messageresponse(false, "APPLY FAILED");
                Apply.setDisable(false);
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleOK() {
        applyChanges();
        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {

        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
    /*================== RESPONSE ===========================*/
    public void messageresponse(boolean successfully,String details){
        if(!successfully){
            UIresponse.getStyleClass().removeAll("login-success", "login-message");
            UIresponse.getStyleClass().add("login-error");
            UIresponse.setText(details);
        }
        else{
            UIresponse.getStyleClass().removeAll("login-error", "login-message-hidden");
            UIresponse.getStyleClass().add("login-success");
            UIresponse.setText(details);
        }

    }

    /* ================= RESTORE ================= */
    private void restoreSettings() {
        String saved = AppSettings.loadSerialPort();
        if (saved != null) {
            messageDevicePortCombo.getItems().stream()
                    .filter(p -> p.getSystemPortName().equals(saved))
                    .findFirst()
                    .ifPresent(messageDevicePortCombo::setValue);
        }

        ApplicationTitle.setText(AppSettings.loadApplicationTitle());
        System.out.println(AppSettings.loadApplicationTitle()+"app");

        System.out.println(ApplicationTitle.getText()+"app");

        if(AppSettings.loadCancelTime() != null & !AppSettings.loadCancelTime().isBlank()){
            AutoCancelTime.selectItem(AppSettings.loadCancelTime());}


        ControllerName.setText(AppSettings.loadController());
        ModuleName.setText(AppSettings.loadModule());
        PhoneNo.setText(AppSettings.loadPhone());
    }
}
