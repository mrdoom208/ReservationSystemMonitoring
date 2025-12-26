package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.controller.popup.addReservationController;
import com.mycompany.reservationsystem.service.ActivityLogService;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.repository.ReservationTableLogsRepository;
import com.mycompany.reservationsystem.service.PermissionService;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.*;

@Component
public class ReservationController {


    private final AdministratorUIController adminUIController;

    @Autowired
    private ConfigurableApplicationContext springContext;

    /* ===================== TABLE VIEWS ===================== */

    @FXML
    private TableView<ManageTables> AvailableTable;

    @FXML
    private TableView<Reservation> CustomerReservationTable, SCNReservations;

    @FXML
    private TableView<ReservationTableLogs> ReservationLogs;

    /* ===================== TABLE COLUMNS ===================== */

    // ManageTables columns
    @FXML
    private TableColumn<ManageTables, String> TableNoAT, StatusAT, LocationAT;

    @FXML
    private TableColumn<ManageTables, Integer> CapacityAT;

    // Reservation columns (String)
    @FXML
    private TableColumn<Reservation, String>
            NameCRT, PreferCRT, ReferenceCRT,phoneSCNR,
            EmailCRT, PhoneCRT, StatusCRT,refSCNR,
            customerSCNR, statusSCNR;

    // Reservation columns (Number / Time / Date)
    @FXML
    private TableColumn<Reservation, Integer> PaxCRT, paxSCNR;

    @FXML
    private TableColumn<Reservation, Long> TableNoCRT;

    @FXML
    private TableColumn<Reservation, LocalTime>
            TimeCRT, regSCNR, seatedSCNR,
            cancelSCNR, noshowSCNR, seatedRL;

    @FXML
    private TableColumn<Reservation, LocalDate> dateSCNR;

    // ReservationTableLogs columns
    @FXML
    private TableColumn<ReservationTableLogs, String> customerRL, statusRL;

    @FXML
    private TableColumn<ReservationTableLogs, LocalTime>
            pendingRL, confirmRL, completeRL;

    @FXML
    private TableColumn<ReservationTableLogs, LocalDate> dateRL;

    // Generic / unresolved columns
    @FXML
    private TableColumn<?, ?>
            refRL, paxRL,
            phoneRL,
            preferRL, tablenoRL;

    /* ===================== CONTROLS ===================== */

    @FXML
    private MFXButton Mergebtn, newcustomer, reservationrefresh;

    @FXML
    private MFXComboBox reservationfilter;

    @FXML
    private MFXTextField SearchCL;

    /* ===================== LAYOUT ===================== */

    @FXML
    private ScrollPane ReservationPane;

    @FXML
    private BorderPane reservpane;

    @FXML
    private HBox hboxCRT;

    @FXML
    private VBox hiddenTable;

    /* ===================== LABELS ===================== */

    @FXML
    private Label
            CusToTable,
            pending, confirm, seated,
            cancelled, noshow;

    /* ===================== STATE ===================== */

    private Reservation selectedReservation;
    private User currentuser;


    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ManageTablesRepository manageTablesRepository;
    @Autowired
    private ReservationTableLogsRepository RTLR;
    @Autowired
    ActivityLogService activityLogService;
    @Autowired
    PermissionService permissionService;



    @Autowired
    public ReservationController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }


    private final ObservableList<Reservation> reservationsData = FXCollections.observableArrayList();
    private final ObservableList<ManageTables> availableTables = FXCollections.observableArrayList();
    private final ObservableList<ReservationTableLogs> reservationlogsdata = FXCollections.observableArrayList();

    FilteredList<ManageTables> filteredtable;
    FilteredList<Reservation> filteredReservationList = new FilteredList<>(reservationsData);
    FilteredList<Reservation> secondfilteredReservationList = new FilteredList<>(filteredReservationList);
    FilteredList<Reservation> filteredSCNRList = new FilteredList<>(reservationsData);
    private LocalDate applyDate = LocalDate.now();


    int currentpax;
    private ManageTables selectedTable;
    private Long prevRow;
    private Long currentRow;

    ///////////////////////LOAD MASTER LIST////////////////////////////////////////
    public void loadReservationsData() {
        Platform.runLater(() -> {
            List<Reservation> data = reservationRepository.findByDateBetween(
                    applyDate,
                    LocalDate.now()
            );
            reservationsData.setAll(data);
        });

    }

    //////////////////////CUSTOMER RESERVATION TABLE////////////////////
    public void setupCustomerReservationTable() {

        CustomerReservationTable.setItems(secondfilteredReservationList);
        String[] statuses = {"Confirm", "Pending", "Show All"};
        addItemsToCombo(reservationfilter, filteredReservationList, Reservation::getStatus,statuses);

        SearchCL.textProperty().addListener((observable, oldValue, newValue) -> {
            secondfilteredReservationList.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // ----- CUSTOMER FIELDS -----
                if (item.getCustomer() != null) {

                    if (item.getCustomer().getName() != null &&
                            item.getCustomer().getName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    if (item.getCustomer().getPhone() != null &&
                            item.getCustomer().getPhone().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    if (item.getCustomer().getEmail() != null &&
                            item.getCustomer().getEmail().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                }

                if (item.getStatus() != null &&
                        item.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (item.getReference() != null &&
                        item.getReference().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (item.getPrefer() != null &&
                        item.getPrefer().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (String.valueOf(item.getPax()).contains(lowerCaseFilter)) {
                    return true;
                }

                if (item.getDate() != null &&
                        item.getDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }

                // No match
                return false;
            });
        });

        TableNoCRT.setCellFactory(col -> new TableCell<Reservation, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);

                setText(null);
                setGraphic(null);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Reservation row = getTableView().getItems().get(getIndex());

                if (item == null) {
                    Label btn = new Label("Select");
                    btn.getStyleClass().add("table-number-btn"); // CSS class
                    btn.setCursor(Cursor.HAND); // pointer cursor

                    btn.setOnMouseClicked(e -> {
                        selectedReservation = row;
                        //scrollPosition = ReservationPane.getVvalue();
                        currentpax = row.getPax();

                        hideTableList(row.getId());
                        loadAvailableTable();
                        System.out.println(row.getReference());
                    });

                    setGraphic(btn);
                } else {
                    setText(String.valueOf(item));
                    setGraphic(null);
                }

            }
        });

        applyTimeFormat(TimeCRT);
        applyStatusStyle(StatusCRT);

        ReferenceCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        NameCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.15));
        PaxCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.06));
        StatusCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        PreferCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        PhoneCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.12));
        EmailCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.15));
        TimeCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.13));
        TableNoCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.09));

        ReferenceCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getReference()));               // String
        NameCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomer().getName()));          // String
        PaxCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPax()));                          // Integer
        StatusCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStatus()));                    // String
        PreferCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPrefer()));                    // String
        PhoneCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomer().getPhone()));        // String
        EmailCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomer().getEmail()));        // String
        TimeCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getReservationPendingtime()));     // LocalTime
        TableNoCRT.setCellValueFactory(res ->
                new ReadOnlyObjectWrapper<>(res.getValue().getTable() != null ? res.getValue().getTable().getId() : null)
        );
    }

    public void loadCustomerReservationTable() {
        List<String> statuses = List.of("Pending", "Confirm");
        filteredReservationList.setPredicate(reservation -> {
            if (reservation == null || reservation.getStatus() == null) {
                return false;
            }
            return statuses.contains(reservation.getStatus());
        });

        pending.setText(String.valueOf(reservationRepository.countByStatus("Pending")));
        confirm.setText(String.valueOf(reservationRepository.countByStatus("Confirm")));


    }

    /// ////////////////AVAILABLE TABLES//////////////////////////////////////
    public void hideTableList(Long CustomerId) {

        currentRow = CustomerId;

        if (CustomerId == prevRow) {
            if (!hiddenTable.isVisible() && !hiddenTable.isManaged()) {
                hiddenTable.setVisible(true);
                hiddenTable.setManaged(true);

            } else {
                hiddenTable.setVisible(false);
                hiddenTable.setManaged(false);
            }

        } else if (CustomerId == null) {
            hiddenTable.setVisible(false);
            hiddenTable.setManaged(false);
        } else {
            hiddenTable.setVisible(true);
            hiddenTable.setManaged(true);
        }
        prevRow = CustomerId;
        if (selectedReservation == null) {
            CusToTable.setText("No Reservation Selected");

        } else if (selectedReservation == null && selectedTable == null) {
            CusToTable.setText("No Reservation and Table Selected Yet");

        } else {
            CusToTable.setText(selectedReservation.getReference());
        }
    }
    public void setupAvailableTable() {

        filteredtable = new FilteredList<>(availableTables);
        AvailableTable.setItems(filteredtable);
        applyStatusStyle(StatusAT);

        filteredtable.setPredicate(table -> table.getCapacity() > currentpax);
        AvailableTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedTable = newSelection;
                    System.out.println("Selected Table: " + newSelection);
                    Mergebtn.setDisable(newSelection == null); // update button state
                }
        );

        // Initially disable merge button
        Mergebtn.setDisable(true);

        TableColumn<?, ?>[] column = {TableNoAT, CapacityAT, StatusAT, LocationAT};
        double[] widthFactors = {0.25, 0.25, 0.25, 0.25};
        String[] namecol = {"Id", "capacity", "status", "location"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i
                        + " (expected: " + namecol[i] + ")");
                continue; // skip this iteration so it won't throw NPE
            } else {
                System.out.println("✔ Column OK: index " + i
                        + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(AvailableTable.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        AvailableTable.setPlaceholder(new Label("No Available Table yet"));

    }

    public void loadAvailableTable() {
        List<ManageTables> Data = manageTablesRepository.findByStatus("Available");
        availableTables.setAll(Data);

    }
    /// /////////////////////////SCNR RESERVATIONS///////////////////////////////////////////
    private void setupSCNReservation(){
        SCNReservations.setItems(filteredSCNRList);

        SCNReservations.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar hBar = (ScrollBar) SCNReservations.lookup(".scroll-bar:horizontal");
                if (hBar != null) {
                    hBar.setVisible(false);
                    hBar.setManaged(false); // removes layout space
                }
            }
        });
        applyStatusStyle(statusSCNR);
        applyTimeFormat(seatedSCNR);
        applyTimeFormat(cancelSCNR);
        applyTimeFormat(noshowSCNR);
        applyTimeFormat(regSCNR);

        TableColumn<?, ?>[] column = {refSCNR, customerSCNR, paxSCNR, phoneSCNR, statusSCNR, regSCNR, seatedSCNR, cancelSCNR, noshowSCNR,dateSCNR};
        double[] widthFactors = {0.1, 0.15, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,0.1};


        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i
                        + " (expected: " +")");
                continue; // skip this iteration so it won't throw NPE
            } else {
                System.out.println("✔ Column OK: index " + i
                        + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(SCNReservations.widthProperty().multiply(widthFactors[i]));

        }
        refSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReference()));

        customerSCNR.setCellValueFactory(res -> {
            Customer c = res.getValue().getCustomer();
            return new ReadOnlyObjectWrapper<>(c != null ? c.getName() : "");
        });

        paxSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getPax()));

        phoneSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getCustomer().getPhone()));


        statusSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getStatus()));

        regSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationPendingtime()));

        seatedSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationSeatedtime()));

        cancelSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationCancelledtime()));

        noshowSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationNoshowtime()));

        dateSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getDate()));

    }

    private void loadSCNReservation(){
        List<String> statuses = List.of("Seated", "Cancelled","No Show");
        filteredSCNRList.setPredicate(reservation -> {
            if (reservation == null || reservation.getStatus() == null) {
                return false;
            }
            return statuses.contains(reservation.getStatus());
        });
        seated.setText(String.valueOf(reservationRepository.countByStatus("Seated")));
        cancelled.setText(String.valueOf(reservationRepository.countByStatus("Cancelled")));
        noshow.setText(String.valueOf(reservationRepository.countByStatus("No Show")));
    }


    /// ////////////////////////RESERVATION LOGS////////////////////////////
    private void setupReservationLogs(){
        ReservationLogs.setItems(reservationlogsdata);
        applyStatusStyle(statusRL);
        applyTimeFormat(pendingRL);
        applyTimeFormat(confirmRL);
        applyTimeFormat(seatedRL);
        applyTimeFormat(completeRL);

        TableColumn<?, ?>[] column = {customerRL,paxRL,phoneRL,preferRL,statusRL,pendingRL,confirmRL,seatedRL,completeRL,tablenoRL,refRL,dateRL};
        double[] widthFactors = {0.11, 0.05, 0.1, 0.1, 0.1, 0.08, 0.08, 0.08,0.08,0.05,0.08,0.09};
        String[] namecol = {"customer", "pax", "phone", "prefer", "status", "reservationPendingtime", "reservationConfirmtime","reservationSeatedtime","reservationCompletetime","tableNo","reference","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i + " (expected: " + namecol[i] + ")");
                continue;
            } else {
                System.out.println("✔ Column OK: index " + i + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(ReservationLogs.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }
        ReservationLogs.setPlaceholder(new Label("No Complete Reservation yet "));
    }

    public void loadReservationLogs() {
        Platform.runLater(() -> {
            AvailableTable.getSelectionModel().select(0);
            AvailableTable.getFocusModel().focus(0);
        });
        List<ReservationTableLogs> Data = RTLR.findByDateBetween(applyDate,LocalDate.now());
        reservationlogsdata.setAll(Data);

    }
/// ////////////////////////////BUTTONS////////////////////////////////////
    @FXML
    private void addCustomerReservation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/addReservation.fxml"));
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
            addReservationController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait(); // wait until closed
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. Refresh UI
        loadReservationsData();
        adminUIController.getDashboardController().loadRecentReservations();
        //loadReservationReports();
        adminUIController.getDashboardController().barchart();
    }
    public void merge(ActionEvent event){
        Reservation cr = reservationRepository.findById(currentRow).orElse(null);
        System.out.print(cr);
        System.out.print(selectedTable);
        String currentStatus = selectedTable.getStatus();

        if (cr != null && selectedTable != null) {
            cr.setTable(selectedTable);
            selectedTable.setStatus("Reserved");
            selectedTable.setTablestarttime(LocalTime.now());
            System.out.print(cr);
            System.out.print(selectedTable);

            manageTablesRepository.save(selectedTable);

            reservationRepository.save(cr);

        }else{
            System.out.println(selectedTable);
            System.out.println(cr);

        }

        activityLogService.logAction(currentuser.getUsername(),String.valueOf(currentuser.getPosition()),"Table","Update Status",String.format("Changed table %d status from %s to Reserved for %s", cr.getTable().getId(),currentStatus,cr.getReference()));

        adminUIController.getDashboardController().loadTableView();
        loadAvailableTable();
        adminUIController.getTableController().loadTableManager();
        loadReservationsData();
        hiddenTable.setVisible(false);
        hiddenTable.setManaged(false);
        //ReservationPane.setVvalue(scrollPosition);
        adminUIController.getDashboardController().updateLabels();
        //TableManager.refresh();
    }

    private void applyPermissions() {
        if (currentuser == null) return;

        // Map each button to its required permission code
        Map<Button, String> buttonPermissions = Map.of(
                newcustomer,"CREATE_RESERVATION"
                );

        // Disable buttons if user doesn't have permission
        buttonPermissions.forEach((button, code) ->
                button.setManaged(permissionService.hasPermission(currentuser, code))


        );
    }



    @FXML
    private void initialize(){
        currentuser = adminUIController.getCurrentUser();
        applyPermissions();
        reservpane.minHeightProperty().bind(reservpane.widthProperty().multiply(1.456));
        hiddenTable.setVisible(false);
        hiddenTable.setManaged(false);

        loadReservationsData();
        loadCustomerReservationTable();
        loadAvailableTable();
        loadReservationLogs();
        loadSCNReservation();
        setupCustomerReservationTable();
        setupAvailableTable();
        setupSCNReservation();
        setupReservationLogs();


    }
}
