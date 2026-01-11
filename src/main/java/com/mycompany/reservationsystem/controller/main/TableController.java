package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.controller.popup.DeleteDialogController;
import com.mycompany.reservationsystem.controller.popup.addTableDialogController;
import com.mycompany.reservationsystem.controller.popup.editTableDialogController;
import com.mycompany.reservationsystem.controller.popup.setAmountPaidController;
import com.mycompany.reservationsystem.service.ActivityLogService;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.service.TablesService;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.model.ReservationTableLogs;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.repository.ReservationTableLogsRepository;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.*;


@Component
public class TableController {

    // ====================== Constructor-injected dependencies ======================
    private AdministratorUIController adminUIController;

    // ====================== Other Dependencies ======================
    public User currentuser;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    ManageTablesRepository manageTablesRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTableLogsRepository RTLR;

    @Autowired
    ActivityLogService activityLogService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    TablesService tablesService;

    @Autowired
    PermissionService permissionService;

// ====================== FXML Components ======================

    // --- TableViews ---
    @FXML
    private TableView<ManageTablesDTO> TableManager;
    @FXML
    private TableView<ReservationTableLogs> TableHistory;

    // --- TableColumns: ManageTablesDTO ---
    @FXML
    private TableColumn<ManageTablesDTO, Void> ActionTM;
    @FXML
    private TableColumn<ManageTablesDTO, Integer> CapacityTM, PaxTM;
    @FXML
    private TableColumn<ManageTablesDTO, String> CustomerTM, LocationTM, StatusTM, TablenoTM;
    @FXML
    private TableColumn<ManageTablesDTO, LocalTime> TimeUsedTM;

    // --- TableColumns: ReservationTableLogs ---
    @FXML
    private TableColumn<ReservationTableLogs, Integer> capacityTH, paxTH;
    @FXML
    private TableColumn<ReservationTableLogs, LocalTime> completeTH, occupiedTH, reservedTH;
    @FXML
    private TableColumn<ReservationTableLogs, String> customerTH, referenceTH, statusTH;
    @FXML
    private TableColumn<ReservationTableLogs, LocalDate> dateTH;
    @FXML
    private TableColumn<?, ?> tablenoTH;

    // --- Controls ---
    @FXML
    private MFXButton AddTablebtn;

    @FXML
    private MFXTextField SearchTM;
    @FXML
    private MFXComboBox tablefilter;

    // --- Layouts ---
    @FXML
    private BorderPane tablepane;
    @FXML
    private ScrollPane TableManagementPane;

    // --- Labels ---
    @FXML
    private Label totalbusy, totalfree, totaltables;

    // ====================== Local variables / ObservableLists ======================
    private final ObservableList<ManageTablesDTO> tableManagerData = FXCollections.observableArrayList();
    private final ObservableList<ReservationTableLogs> reservationlogsdata = FXCollections.observableArrayList();
    FilteredList<ManageTablesDTO> tablesfilteredData;

    private LocalDate applyDate;

    public TableController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }


    //////////////////////TABLE MANAGER////////////////////////////
    public void setupTableManager() {
        User currentuser = adminUIController.getCurrentUser();
        AddTablebtn.setDisable(!permissionService.hasPermission(currentuser,"ADD_TABLE"));
        AddTablebtn.setOnAction(e -> showAddTableDialog());
        tablesfilteredData = new FilteredList<>(tableManagerData, p -> true);
        TableManager.setItems(tablesfilteredData);
        SearchTM.textProperty().addListener((observable, oldValue, newValue) -> {
            tablesfilteredData.setPredicate(item -> {
                // If search field is empty, display all
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare name with search text
                String lowerCaseFilter = newValue.toLowerCase();

                // Check all columns
                if (item.getCustomer() != null && item.getCustomer().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getPax() != null && String.valueOf(item.getPax()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getStatus() != null && item.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getTableNo() != null && item.getTableNo().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getTablestarttime() != null && String.valueOf(item.getTablestarttime()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getCapacity() != null && String.valueOf(item.getCapacity()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getTableId() != null && String.valueOf(item.getTableId()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }



                return false; // no match
            });
        });
        String[] statuses = {"Available","Reserved","Occupied","Show All"};
        addItemsToCombo(tablefilter,tablesfilteredData,ManageTablesDTO::getStatus,statuses);
        applyStatusStyle(StatusTM);
        applyTimeFormat(TimeUsedTM);

        // --- Action Column with Buttons ---
        ActionTM.setCellFactory(col -> new TableCell<ManageTablesDTO, Void>() {
            FontIcon utensilIcon = new FontIcon(FontAwesomeSolid.UTENSILS);
            FontIcon editIcon = new FontIcon(FontAwesomeSolid.PEN_SQUARE);
            FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);
            FontIcon completeIcon = new FontIcon(FontAwesomeSolid.RECEIPT);

            private final Button btnStart = new Button("Start Service");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnComplete = new Button("Finished!");
            private final HBox hbox = new HBox(5);

            {
                // Initialize icons and buttons (same as before)
                initIconsAndButtons();
                btnEdit.setDisable(!permissionService.hasPermission(currentuser,"EDIT_TABLE"));
                btnDelete.setDisable(!permissionService.hasPermission(currentuser,"REMOVE_TABLE"));

                btnStart.setOnAction(e -> handleStart());
                btnComplete.setOnAction(e -> handleComplete());
                btnEdit.setOnAction(e -> {
                    ManageTablesDTO data = getTableView().getItems().get(getIndex());
                    handleEdit(data.getTableNo());
                });
                btnDelete.setOnAction(e -> handleDelete());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                ManageTablesDTO data = getCurrentItem();
                if (data != null) {
                    updateButtonsForStatus(data);
                    setGraphic(hbox);
                }
            }
            // ----------------update Buttons For Status---------------------------------
            private void updateButtonsForStatus(ManageTablesDTO data) {
                if ("Reserved".equals(data.getStatus())) {
                    hbox.getChildren().setAll(btnStart,btnEdit, btnDelete);

                }
                else if("Occupied".equals(data.getStatus())){
                    hbox.getChildren().setAll(btnComplete,btnEdit, btnDelete);

                }else {
                    hbox.getChildren().setAll(btnEdit, btnDelete);
                }
            }
            // ---------------- init Icons and Buttons ---------------
            private void initIconsAndButtons() {
                // Icon setup
                utensilIcon.setIconSize(12);
                utensilIcon.setIconColor(Color.web("#4A2A33"));
                editIcon.setIconSize(12);
                editIcon.setIconColor(Color.web("#000000"));
                deleteIcon.setIconSize(12);
                deleteIcon.setIconColor(Color.web("#ffffff"));
                completeIcon.setIconSize(12);
                completeIcon.setIconColor(Color.web("#ffffff"));

                // Button setup
                btnStart.setGraphic(utensilIcon);
                btnStart.setContentDisplay(ContentDisplay.LEFT);
                btnStart.getStyleClass().add("start-service");

                btnEdit.setGraphic(editIcon);
                btnEdit.setContentDisplay(ContentDisplay.LEFT);
                btnEdit.getStyleClass().add("edit");

                btnDelete.setGraphic(deleteIcon);
                btnDelete.setContentDisplay(ContentDisplay.LEFT);
                btnDelete.getStyleClass().add("delete");

                btnComplete.setGraphic(completeIcon);
                btnComplete.setContentDisplay(ContentDisplay.LEFT);
                btnComplete.getStyleClass().add("complete");

                // HBox setup
                hbox.setAlignment(Pos.CENTER);
                btnStart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnDelete.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnComplete.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                HBox.setHgrow(btnStart, Priority.ALWAYS);
                HBox.setHgrow(btnEdit, Priority.ALWAYS);
                HBox.setHgrow(btnDelete, Priority.ALWAYS);
                HBox.setHgrow(btnComplete, Priority.ALWAYS);
            }


            // ---------------- Private helper methods ----------------

            private void handleStart() {
                ManageTablesDTO data = getCurrentItem();
                if (data == null) return;

                String currentStatus = data.getStatus();
                data.setStatus("Occupied");
                updateButtonsForStatus(data);

                tablesService.updateStatus(data.getTableId(), data.getStatus());
                reservationService.updateSeatedtime(data.getReference(), LocalTime.now());
                reservationService.updateStatus(data.getReference(),"Seated");
                data.setReservationSeatedtime(LocalTime.now());

                adminUIController.getDashboardController().loadTableView();
                adminUIController.getDashboardController().updateLabels();
                adminUIController.getReservationController().loadReservationsData();
                loadTableManager();


                activityLogService.logAction(
                        currentuser.getUsername(),
                        currentuser.getPosition().toString(),
                        "Table",
                        "Update Status",
                        String.format(
                                "Changed table %d status from %s to Occupied for reservation %s",
                                data.getTableId(),
                                currentStatus,
                                data.getReference()
                        )
                );
                //loadReservationReports();
            }

            private void handleComplete() {
                ManageTablesDTO data = getCurrentItem();
                if (data == null) return;

                // Move your setAmountPaid.fxml dialog and service logic here
                try {
                    BigDecimal Amount = BigDecimal.ZERO;

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/setAmountPaid.fxml"));
                    loader.setControllerFactory(springContext::getBean);
                    Parent root = loader.load();

                    Stage dialogStage = new Stage();
                    dialogStage.initModality(Modality.APPLICATION_MODAL);
                    dialogStage.initOwner(App.primaryStage);
                    dialogStage.initStyle(StageStyle.TRANSPARENT);
                    dialogStage.setResizable(false);
                    Scene scn = new Scene(root);
                    scn.setFill(Color.TRANSPARENT);
                    dialogStage.setScene(scn);

                    setAmountPaidController controller = loader.getController();
                    controller.setDialogStage(dialogStage);
                    controller.setReference(data.getReference());
                    dialogStage.showAndWait();

                    if (controller.isCancelled()) return;

                    Amount = controller.getAmount();

                    // Update data and services
                    data.setStatus("Complete");
                    data.setDate(LocalDate.now());
                    data.setReservationCompletetime(LocalTime.now());
                    data.setRevenue(Amount);
                    ReservationTableLogs log = new ReservationTableLogs(data);
                    RTLR.save(log);
                    reservationService.updateCompletetime(data.getReference(), LocalTime.now());
                    updateButtonsForStatus(data);

                    activityLogService.logAction(
                            currentuser.getUsername(),
                            currentuser.getPosition().toString(),
                            "Table",
                            "Update Status",
                            String.format(
                                    "Changed table %s status from %s to Complete → Available",
                                    data.getTableNo(),
                                    data.getStatus()
                            )
                    );

                    // Reset table for next use
                    data.setCustomer("");
                    data.setPax(null);
                    data.setStatus("Available");
                    tablesService.updateStatus(data.getTableId(), data.getStatus());

                    adminUIController.getDashboardController().loadTableView();
                    adminUIController.getDashboardController().updateLabels();
                    adminUIController.getReservationController().loadReservationsData();
                    adminUIController.getReservationController().loadReservationLogs();
                    loadTableManager();
                    loadTableHistory();
                    //loadReports();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void handleEdit(String ref) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/editTableDialog.fxml"));
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
                    editTableDialogController controller = loader.getController();
                    controller.setDialogStage(dialogStage);
                    controller.setTargetTable(tablesService.findByNo(ref));
                    dialogStage.showAndWait(); // wait until closed
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adminUIController.getDashboardController().loadTableView();
                adminUIController.getDashboardController().updateLabels();
                loadTableManager();
                adminUIController.getReservationController().loadAvailableTable();

            }


            private void handleDelete() {
                ManageTablesDTO data = getCurrentItem();
                if (data == null) return;

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/deleteDialog.fxml"));
                    Parent root = loader.load();
                    DeleteDialogController controller = loader.getController();

                    controller.setOnDelete(() -> {
                        if (reservationRepository.existsByTable_Id(data.getTableId())) {
                            showAlert("Cannot delete: this table has active reservations");
                            return;
                        }
                        manageTablesRepository.deleteById(data.getTableId());
                        activityLogService.logAction(
                                currentuser.getUsername(),
                                currentuser.getPosition().toString(),
                                "Table",
                                "Delete Table",
                                String.format("Delete table %s", data.getTableNo())
                        );
                        //loadActivityLogs();
                        loadTableManager();
                        adminUIController.getDashboardController().loadTableView();
                        adminUIController.getDashboardController().updateLabels();
                    });

                    Stage dialog = new Stage(StageStyle.UNDECORATED);
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initStyle(StageStyle.TRANSPARENT);
                    dialog.setResizable(false);
                    Scene scn = new Scene(root);
                    scn.setFill(Color.TRANSPARENT);
                    dialog.setScene(scn);
                    dialog.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //loadReports();
            }

            // ---------------- Utility methods ----------------

            private ManageTablesDTO getCurrentItem() {
                int i = getIndex();
                if (i >= 0 && i < getTableView().getItems().size()) {
                    return getTableView().getItems().get(i);
                }
                return null;
            }
        });

        // --- Setup Other Columns ---
        TableColumn<?, ?>[] column = {TablenoTM, CustomerTM, PaxTM, StatusTM, CapacityTM, LocationTM, TimeUsedTM, ActionTM};
        double[] widthFactors = {0.08, 0.18, 0.06, 0.11, 0.08, 0.12, 0.10, 0.27};
        String[] namecol = {"tableNo", "customer", "pax", "status", "capacity", "location", "tablestarttime", ""};

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
            col.prefWidthProperty().bind(TableManager.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }
        TableManager.setPlaceholder(new Label("No Table set yet"));

    }
    public void loadTableManager(){
        List<ManageTablesDTO> tables = manageTablesRepository.getManageTablesDTO();
        tableManagerData.setAll(tables);
        totaltables.setText(String.valueOf(manageTablesRepository.count()));
        totalfree.setText(String.valueOf(manageTablesRepository.countByStatus("Available")));
        int busy = 0;
        busy += (int) manageTablesRepository.countByStatus("Reserved");
        busy += (int) manageTablesRepository.countByStatus("Occupied");
        totalbusy.setText(String.valueOf(busy));


    }
    ///////////////////////////////////////TABLE HISTORY/////////////////////////////////////
    private void setupTableHistory(){
        TableHistory.setItems(reservationlogsdata);
        applyTimeFormat(reservedTH);
        applyTimeFormat(occupiedTH);
        applyTimeFormat(completeTH);
        
        TableColumn<?, ?>[] column = {referenceTH,customerTH,paxTH,statusTH,tablenoTH,capacityTH,reservedTH,occupiedTH,completeTH,dateTH};
        double[] widthFactors = {0.11, 0.2, 0.05, 0.1, 0.05,0.05,0.11,0.11,0.11,0.11};
        String[] namecol = {"reference","customer","pax","status","tableid","tablecapacity","tablestarttime","reservationSeatedtime","reservationCompletetime","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(TableHistory.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        applyStatusStyle(statusTH);
        TableHistory.setPlaceholder(new Label("No Table set yet"));

    }
    private void loadTableHistory(){
        List<ReservationTableLogs> Data = RTLR.findByDateBetween(applyDate,LocalDate.now());
        reservationlogsdata.setAll(Data);

    }

    ////////////////////////////////////////BUTTONS////////////////////////////
     private void showAddTableDialog() {
         try {
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/addTableDialog.fxml"));
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
             addTableDialogController controller = loader.getController();
             controller.setDialogStage(dialogStage);

             dialogStage.showAndWait(); // wait until closed
         } catch (Exception e) {
             e.printStackTrace();
         }
         adminUIController.getDashboardController().loadTableView();
         adminUIController.getDashboardController().updateLabels();
         loadTableManager();
         adminUIController.getReservationController().loadAvailableTable();

     }
     //////////////////////////ALLERT///////////////////////////////////////////
     public void showAlert(String message) {
         Alert alert = new Alert(Alert.AlertType.WARNING);
         alert.setTitle("Warning");
         alert.setHeaderText(null); // no header
         alert.setContentText(message);
         alert.showAndWait();
     }





    public void initialize(){

         applyDate = LocalDate.now();
         tablepane.minHeightProperty().bind(tablepane.widthProperty().multiply(0.95));
         loadTableManager();
         loadTableHistory();
         setupTableManager();
         setupTableHistory();


    }

}
