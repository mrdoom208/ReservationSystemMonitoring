/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.Service.ReservationService;
import com.mycompany.reservationsystem.controller.DeleteTableDialogController;
import com.mycompany.reservationsystem.Service.TablesService;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.repository.*;
import com.mycompany.reservationsystem.dto.CustomerReservationDTO;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.websocket.ReservationListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;



/**
 * FXML Controller class
 *
 * @author formentera
 */
@Component
public class AdministratorUIController implements Initializable, ReservationListener {
    private User currentuser;

    void  setUser(User user) {
        this.currentuser = user;
    }

    @FXML
    private Button Dashboardbtn, ReservationManagementbtn, TableManagementbtn, ManageStaffAndAccountsbtn,Mergebtn,AddTablebtn;
    @FXML
    private ScrollPane DashboardPane, ReservationPane, TableManagementPane, ManageStaffAndAccountsPane;
    @FXML
    private Label Total_CustomerDbd, activetable,Total_Cancelled, Total_Pending, CusToTable,totaltables,totalfree,totalbusy,pending,confirm;
    @FXML
    private LineChart<String, Number> myBarChart;
    @FXML
    private TextField SearchCL,SearchTM;

    @FXML
    private TableView<CustomerReservation> RecentReservationTable,SCNReservations;
    @FXML
    private TableView<ManageTablesDTO> ManageTableView,TableManager;
    @FXML
    private TableColumn<CustomerReservation, String> CustomerColm,customerSCNR,phoneSCNR,statusSCNR,refSCNR;
    @FXML
    private TableColumn<CustomerReservation, Integer> PaxColm,paxSCNR;
    @FXML
    private TableColumn<CustomerReservation, LocalTime> TimeColm,regSCNR,seatedSCNR,cancelSCNR,noshowSCNR;
    @FXML
    private TableColumn<CustomerReservation, LocalDate> dateSCNR;
    @FXML
    private TableColumn<ManageTablesDTO, String> TableNoColum, TableCustomerColum, TableStatusColum, TablenoTM,StatusTM,CustomerTM,LocationTM;
    @FXML
    private TableColumn<ManageTablesDTO, LocalTime> TableTimeColum,TimeUsedTM;
    @FXML
    private TableColumn<ManageTablesDTO, Integer> TablePaxColum, TableCapacityColum;
    @FXML
    private VBox notificationArea, hiddenTable;
    @FXML
    private TableView<CustomerReservation> CustomerReservationTable;
    @FXML
    private TableColumn<CustomerReservation, String> NameCRT, StatusCRT, PreferCRT, PhoneCRT, EmailCRT, ReferenceCRT;
    @FXML
    private TableColumn<CustomerReservation, Integer> PaxCRT;
    @FXML
    private TableColumn<CustomerReservation, Long> TableNoCRT;
    @FXML
    private TableColumn<CustomerReservation, LocalTime> TimeCRT;
    @FXML
    private TableView<User> AccountTable;
    @FXML
    private TableColumn<User,String> usernameAT,firstnameAT,lastnameAT,positionAT,statusAT;

    
    private CustomerReservation selectedReservation;
    
    private final ObservableList<CustomerReservation> recentReservations = FXCollections.observableArrayList();
    private final ObservableList<CustomerReservation> pendingReservations = FXCollections.observableArrayList();
    private final ObservableList<CustomerReservation> canceledReservations = FXCollections.observableArrayList();
    private final ObservableList<ReservationTableLogs> reservationlogsdata = FXCollections.observableArrayList();
    private final ObservableList<ManageTablesDTO> manageTablesData = FXCollections.observableArrayList();
    private final ObservableList<ManageTablesDTO> tableManagerData = FXCollections.observableArrayList();
    private final ObservableList<ManageTables> availableTables = FXCollections.observableArrayList();
    private final ObservableList<User> UserData = FXCollections.observableArrayList();

    @FXML
    private TableView<ManageTables> AvailableTable;
    @FXML
    private TableColumn<ManageTables, String> StatusAT, LocationAT, TableNoAT;
    @FXML
    private TableColumn<ManageTables, Integer> CapacityAT,PaxTM,CapacityTM;
    @FXML
    private TableColumn<ManageTablesDTO, Void> ActionTM;
    @FXML
    private TableColumn<User, Void> actionAT;
    @FXML 
    private TableView<ReservationTableLogs> ReservationLogs,TableHistory;
    @FXML
    private TableColumn<ReservationTableLogs,String>customerRL,phoneRL,preferRL,statusRL,tablenoRL,refRL,customerTH,referenceTH,statusTH,tablenoTH;
    @FXML
    private TableColumn<ReservationTableLogs,Integer>paxRL,capacityTH,paxTH;
    @FXML
    private TableColumn<ReservationTableLogs,LocalTime>pendingRL,confirmRL,seatedRL,completeRL,reservedTH,occupiedTH,completeTH;
    @FXML
    private TableColumn<ReservationTableLogs,LocalDate>dateRL,dateTH;

    @Autowired
    private ManageTablesRepository manageTablesRepository;

    @Autowired
    private CustomerReservationRepository customerReservationRepository;
    
    @Autowired
    private ReservationTableLogsRepository RTLR;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TablesService tablesService;
    
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ActivityLogsRepository activityLogsRepository;
    
    
    private Long prevRow;
    private Long currentRow;
    private String tableno;
    private ManageTables selectedTable;
    
    double scrollPosition;


    @FXML
    private void navigate(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        Button buttons[] = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn, ManageStaffAndAccountsbtn};

        DashboardPane.setVisible(false);
        ReservationPane.setVisible(false);
        TableManagementPane.setVisible(false);
        ManageStaffAndAccountsPane.setVisible(false);

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

        switch (clicked.getId()) {
            case "Dashboardbtn":
                DashboardPane.setVisible(true);
                break;
            case "ReservationManagementbtn":
                ReservationPane.setVisible(true);
                break;
            case "TableManagementbtn":
                TableManagementPane.setVisible(true);
                break;
            case "ManageStaffAndAccountsbtn":
                ManageStaffAndAccountsPane.setVisible(true);
            default:
                break;

        }
        System.out.println(clicked.getId());

    }

    @FXML
    private void handleClick() {
        BorderPane notification = createNotification("New Reservation Added!");
        if (notificationArea.getChildren().size() >= 3) {
            notificationArea.getChildren().remove(0);
        }

        notificationArea.getChildren().add(notification);
    }

    public void updateLabels() {
        activetable.setText(String.valueOf(manageTablesRepository.countByStatus("Occupied"))+"/"+String.valueOf(manageTablesRepository.countByStatus("Reserved"))+"/"+String.valueOf(manageTablesRepository.count()));
        Total_CustomerDbd.setText(String.valueOf(customerReservationRepository.count()));
        Total_Pending.setText(String.valueOf(customerReservationRepository.countByStatus("Pending")));
        Total_Cancelled.setText(String.valueOf(customerReservationRepository.countByStatus("Cancelled")));

    }

    public void loadRecentReservations() {

        List<CustomerReservation> latest10 = customerReservationRepository.findTop10ByOrderByDateDescReservationPendingtimeDesc(PageRequest.of(0, 10));
        recentReservations.setAll(latest10);
        System.out.print(RecentReservationTable.getItems().setAll(latest10));
    }

    public void loadTableView() {

        List<ManageTablesDTO> tables = manageTablesRepository.getManageTablesDTO();

        manageTablesData.setAll(tables);

    }

    public void setupRecentReservation() {

        TableColumn<?, ?>[] column = {CustomerColm, PaxColm, TimeColm};
        double[] widthFactors = {0.4, 0.20, 0.4};
        String[] namecol = {"name","pax","reservationPendingtime"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(true);
            col.setReorderable(false);
            col.prefWidthProperty().bind(RecentReservationTable.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }

        int maxRows = 10;
        double tableHeight = RecentReservationTable.getPrefHeight()-32; 
        double rowHeight = tableHeight / maxRows;
        RecentReservationTable.setFixedCellSize(rowHeight);
        RecentReservationTable.setPlaceholder(new Label("No Customer Reservation yet"));

        
    }

    public void setupTableView(){

        TableColumn<?, ?>[] column = {TableCustomerColum,TableNoColum,TableStatusColum,TablePaxColum,TableCapacityColum,TableTimeColum};
        double[] widthFactors = {0.2, 0.15, 0.15, 0.1, 0.2, 0.2};
        String[] namecol = {"customer","tableNo","status","pax","capacity","tablestarttime"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(ManageTableView.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        TableStatusColum.setCellFactory(tv -> new TableCell<ManageTablesDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    setText(item); // Make sure your DTO has getStatus()
                    String bgColor;

                    switch (item) {
                        case "Available" ->
                            bgColor = "#2a4d2a";
                        case "Occupied" ->
                            bgColor = "#5a1e1e";
                        case "Reserved" ->
                            bgColor = "#7A5A00";
                        default ->
                            bgColor = "white";
                    }

                    setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 5;");
                    
                }
            }
        });

        ManageTableView.setPlaceholder(new Label("No Table set yet"));

    }

    private BorderPane createNotification(String message) {
        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("notification-pane");

        StackPane holder = new StackPane();

        Label title = new Label("Notication");
        title.getStyleClass().add("notification-title");
        Label Message = new Label(message);
        Message.getStyleClass().add("notification-message");
                    
        Button closeBtn = new Button("X");
        closeBtn.getStyleClass().add("close-notif-btn");
        closeBtn.setOnAction(e -> notificationArea.getChildren().remove(pane));

        holder.getChildren().add(title);
        holder.setAlignment(title, Pos.TOP_CENTER);
        holder.getChildren().add(Message);
        holder.setAlignment(Message, Pos.CENTER);

        pane.setCenter(holder);
        pane.setRight(closeBtn);

        return pane;
    }

    public void loadDashboard() {
        updateLabels();
        loadRecentReservations();
        loadTableView();
        setupRecentReservation();
        setupTableView();
        barchart();

    }

    public void barchart() {
        myBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        List<String> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            last7Days.add(today.minusDays(i).format(dateformat));
        }
        LocalDate SevenDaysAgo = today.minusDays(6);
        List<CustomerReservation> reservations = customerReservationRepository.findAll()
                .stream()
                .filter(r -> r.getDate() !=null)
                .filter(r -> !r.getDate().isBefore(SevenDaysAgo)&&!r.getDate().isAfter(today))
                .toList();
        for (String day : last7Days) {
            long count = reservations.stream()
                    .filter(r -> r.getDate().format(dateformat).equals(day))
                    .count();
            series.getData().add(new XYChart.Data<>(day, count));
        }


        myBarChart.getData().add(series);
    }
    FilteredList<ManageTablesDTO> tablesfilteredData;
    FilteredList<CustomerReservation> filteredData;
    private int currentpax;
    FilteredList<ManageTables> filteredtable;


    public void setupCustomerReservationTable() {
        filteredData = new FilteredList<>(pendingReservations, p -> true);

        SearchCL.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                // If search field is empty, display all
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare name with search text
                String lowerCaseFilter = newValue.toLowerCase();

                // Check all columns
                if (item.getName() != null && item.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getPhone() != null && item.getPhone().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getEmail() != null && item.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getStatus() != null && item.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getReference() != null && item.getReference().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getPrefer() != null && item.getPrefer().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getPhone() != null && item.getPhone().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }


                // You can add other fields similarly
                return false; // no match
            });
        });

        TableNoCRT.setCellFactory(col -> new TableCell<CustomerReservation, Long>() {
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

                CustomerReservation row = getTableView().getItems().get(getIndex());

                if (item == null) {
                    Label btn = new Label("Select");
                    btn.getStyleClass().add("table-number-btn"); // CSS class
                    btn.setCursor(Cursor.HAND); // pointer cursor

                    btn.setOnMouseClicked(e -> {
                        selectedReservation = row;
                        scrollPosition = ReservationPane.getVvalue();
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

        TimeCRT.setCellFactory(col -> new TableCell<CustomerReservation, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                }
            }
        });
        StatusCRT.setCellFactory(tv -> new TableCell<CustomerReservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item); // Make sure your DTO has getStatus()
                    String bgColor;
                    String ftColor;

                    switch (item) {
                        case "Confirm" ->
                            bgColor = "#2196F3";
                        case "Pending" ->
                            bgColor = "#455A64";

                        default ->
                            bgColor = "transparent";
                    }

                    setStyle("-fx-background-color: " + bgColor + ";");
                    
                }
            }
        });

        TableColumn<?, ?>[] column = {ReferenceCRT, NameCRT, PaxCRT, StatusCRT, PreferCRT, PhoneCRT, EmailCRT, TimeCRT, TableNoCRT};
        double[] widthFactors = {0.1, 0.15, 0.06, 0.1, 0.1, 0.12, 0.15, 0.13, 0.09};
        String[] namecol = {"reference", "name", "pax", "status", "prefer", "phone", "email", "reservationPendingtime", "tableId"};

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
            col.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }


    }
    
    public void loadCustomerReservationTable() {
        List<String> statuses = List.of("Pending", "Confirm");
        List<CustomerReservation> Data = customerReservationRepository.findByStatusIn(statuses);
        pendingReservations.setAll(Data);
        pending.setText(String.valueOf(customerReservationRepository.countByStatus("Pending")));
        confirm.setText(String.valueOf(customerReservationRepository.countByStatus("Confirm")));


    }
    
    public void setupAvailableTable() {
        filteredtable = new FilteredList<>(availableTables);
        filteredtable.setPredicate(table -> table.getCapacity() > currentpax);
        AvailableTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        tableno = newSelection.getTableNo();
                        Long id = newSelection.getId();// ← get the row ID
                        selectedTable = newSelection;
                        System.out.println("Selected ID: " + tableno);
                    }
                }
        );

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
        Platform.runLater(() -> {
            AvailableTable.getSelectionModel().select(0);
            AvailableTable.getFocusModel().focus(0);
        });
        List<ManageTables> Data = manageTablesRepository.findByStatus("Available");
        availableTables.setAll(Data);
        
    }
    public void hideTableList(Long CustomerId) {

        currentRow = CustomerId;

        if (CustomerId == prevRow) {
            if (!hiddenTable.isVisible() && !hiddenTable.isManaged()) {
                AvailableTable.getSelectionModel().select(0);
                AvailableTable.scrollTo(0);
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
        if(selectedTable == null){
            CusToTable.setText("No Available Table");
        }
        else if(selectedReservation == null){
            CusToTable.setText("No Reservation Selected");
            
        }else if(selectedReservation == null && selectedTable == null){
            CusToTable.setText("No Reservation and Table Selected Yet");
            
        }else{
        CusToTable.setText(selectedReservation.getReference());
        }
                        

        

    }
    
    public void merge(ActionEvent event){
        CustomerReservation cr = customerReservationRepository.findById(currentRow).orElse(null);
        System.out.print(cr);
        System.out.print(selectedTable);
            
        if (cr != null && selectedTable != null) {
            cr.setTable(selectedTable);
            selectedTable.setStatus("Reserved");
            selectedTable.setTablestarttime(LocalTime.now());
            System.out.print(cr);
            System.out.print(selectedTable);
            
            manageTablesRepository.save(selectedTable);
            
            customerReservationRepository.save(cr);

        }else{
            System.out.println(selectedTable);
            System.out.println(cr);
            
        }
        ActivityLogs logs = new ActivityLogs();
        logs.setTimestamp(LocalDateTime.now());
        logs.setAction("Reserved");
        logs.setTarget("Table No. "+cr.getTableId());
        logs.setValue(cr.getReference());
        logs.setUser(currentuser.getUsername());
        activityLogsRepository.save(logs);


       loadTableView();
       loadAvailableTable();
       loadTableManager();
       loadCustomerReservationTable();
       hiddenTable.setVisible(false);
       hiddenTable.setManaged(false);
       ReservationPane.setVvalue(scrollPosition);
       updateLabels();
       TableManager.refresh();
    }
    
    private void editTablerow(String tableno,String customer,int pax,String status,int capacity,String location){
     
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Edit Table");
    dialog.initModality(Modality.WINDOW_MODAL);


        // Create fields
    TextField tableNoField = new TextField(tableno);
    TextField capacityField = new TextField(String.valueOf(capacity));
    TextField LocationField = new TextField(location);
    Label customerField = new Label(customer);
    Label paxField = new Label(String.valueOf(pax));
    Label StatusField = new Label(status);
    
    makeNumeric(tableNoField);
    makeNumeric(capacityField);
    makeAlphaNumericWithSpace(LocationField);
    

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Table No:"), 0, 0);
    grid.add(tableNoField, 1, 0);
    
    grid.add(new Label("Customer:"), 0, 1);
    grid.add(customerField, 1, 1);

    grid.add(new Label("Pax:"), 0, 2);
    grid.add(paxField, 1, 2);
    
    grid.add(new Label("Status:"), 0, 3);
    grid.add(StatusField, 1, 3);
    
    
    grid.add(new Label("Capacity:"), 0, 4);
    grid.add(capacityField, 1, 4);
    
    grid.add(new Label("Location:"), 0, 5);
    grid.add(LocationField, 1, 5);
    

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.addEventFilter(ActionEvent.ACTION, e -> {

        boolean invalid = false;

        if (tableNoField.getText().trim().isEmpty()) {
            tableNoField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { tableNoField.setStyle(null); }

        if (capacityField.getText().trim().isEmpty()) {
            capacityField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { capacityField.setStyle(null); }

        if (LocationField.getText().trim().isEmpty()) {
            LocationField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { LocationField.setStyle(null); }

        
        if (invalid) {
            e.consume(); // STOP closing the dialog
        }
    });
    DialogPane pane = dialog.getDialogPane();
    pane.setPrefSize(280, 210);

    dialog.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            ManageTables row = new ManageTables();
            row.setId(Long.parseLong(tableNoField.getText()));
            row.setCapacity(Integer.parseInt(capacityField.getText()));
            row.setLocation(LocationField.getText());
            row.setStatus(StatusField.getText());
            manageTablesRepository.save(row);
            ActivityLogs logs = new ActivityLogs();
            logs.setTimestamp(LocalDateTime.now());
            logs.setAction("Edit");
            logs.setTarget("Table no."+ row.getId());
            logs.setValue("Table No: "+row.getId()+"Capacity: "+row.getCapacity()+"Location"+row.getLocation());
            logs.setUser(currentuser.getUsername());

            activityLogsRepository.save(logs);
            loadTableManager();
            loadTableView();
            updateLabels();
            loadTableManager();
            loadAvailableTable();
            loadTableView();
        }
    });
        
    }
    
    @FXML 
    private void addCustomerReservation(ActionEvent event){
        LocalTime currenttime = LocalTime.now();
        String formatted = currenttime.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        LocalDate currentdate = LocalDate.now();
        
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Customer Reservation");
        dialog.initModality(Modality.WINDOW_MODAL);
    

    // Create fields
    TextField customerField = new TextField();
    TextField paxField = new TextField();
    TextField preferField = new TextField();
    TextField phoneField = new TextField();
    TextField emailField = new TextField();
    Label pendingtimeField = new Label(formatted);
    
    makeNumeric(paxField);
    makeAlphaNumericWithSpace(customerField);
    makeAlphaNumericWithSpace(preferField);
    

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Customer:"), 0, 0);
    grid.add(customerField, 1, 0);
    
    grid.add(new Label("Pax:"), 0, 1);
    grid.add(paxField, 1, 1);

    grid.add(new Label("Status:"), 0, 2);
    grid.add(new Label("Pending"), 1, 2);
    
    grid.add(new Label("Prefer:"), 0, 3);
    grid.add(preferField, 1, 3);
    
    
    grid.add(new Label("Phone:"), 0, 4);
    grid.add(phoneField, 1, 4);
    
    grid.add(new Label("Email:"), 0, 5);
    grid.add(emailField, 1, 5);
    
    grid.add(new Label("Time Registered:"), 0,6);
    grid.add(pendingtimeField, 1, 6);
    

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.addEventFilter(ActionEvent.ACTION, e -> {

        boolean invalid = false;

        if (customerField.getText().trim().isEmpty()) {
            customerField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { customerField.setStyle(null); }

        if (paxField.getText().trim().isEmpty()) {
            paxField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { paxField.setStyle(null); }

        if (preferField.getText().trim().isEmpty()) {
            preferField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { preferField.setStyle(null); }
        
        if (phoneField.getText().trim().isEmpty()) {
            phoneField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { phoneField.setStyle(null); }

        if (emailField.getText().trim().isEmpty()) {
            emailField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { emailField.setStyle(null); }

        
        if (invalid) {
            e.consume();
        }
    });
    DialogPane pane = dialog.getDialogPane();
    pane.setPrefWidth(230);
    

    dialog.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            CustomerReservation row = new CustomerReservation();
            row.setName(customerField.getText());
            row.setPax(Integer.parseInt(paxField.getText()));
            row.setStatus("Pending");
            row.setPrefer(preferField.getText());
            row.setEmail(emailField.getText());
            row.setDate(currentdate);
            row.setPhone(phoneField.getText());
            row.setReservationPendingtime(LocalTime.parse(pendingtimeField.getText(),DateTimeFormatter.ofPattern("hh:mm:ss a")));
            row.setReference(String.format("RSV-%05d", customerReservationRepository.count()+1));

            ActivityLogs logs = new ActivityLogs();
            logs.setTimestamp(LocalDateTime.now());
            logs.setAction("Add Reservation");
            logs.setTarget(row.getReference());
            logs.setValue(row.getName());
            logs.setUser(currentuser.getUsername());

            activityLogsRepository.save(logs);
            customerReservationRepository.save(row);
            loadCustomerReservationTable();
            loadRecentReservations();
            barchart();
            
        }
    });
    }
    
    @FXML
    private void onAddTableClicked(ActionEvent event) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add Table");
    dialog.initModality(Modality.WINDOW_MODAL);



        // Create fields
    TextField tableNoField = new TextField();
    TextField capacityField = new TextField();
    TextField LocationField = new TextField();
    Label StatusField = new Label("Available");
    
    makeNumeric(tableNoField);
    makeNumeric(capacityField);
    makeAlphaNumericWithSpace(LocationField);
    

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Table No:"), 0, 0);
    grid.add(tableNoField, 1, 0);

    grid.add(new Label("Capacity:"), 0, 1);
    grid.add(capacityField, 1, 1);
    
    grid.add(new Label("Location:"), 0, 2);
    grid.add(LocationField, 1, 2);
    
    grid.add(new Label("Status:"), 0, 3);
    grid.add(StatusField, 1, 3);
    

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.addEventFilter(ActionEvent.ACTION, e -> {

        boolean invalid = false;

        if (tableNoField.getText().trim().isEmpty()) {
            tableNoField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { tableNoField.setStyle(null); }

        if (capacityField.getText().trim().isEmpty()) {
            capacityField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { capacityField.setStyle(null); }

        if (LocationField.getText().trim().isEmpty()) {
            LocationField.setStyle("-fx-border-color: red; -fx-border-width:0.5");
            invalid = true;
        } else { LocationField.setStyle(null); }

        
        if (invalid) {
            e.consume(); // STOP closing the dialog
        }
    });
    DialogPane pane = dialog.getDialogPane();
    pane.setPrefSize(280, 210);

    dialog.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            ManageTables row = new ManageTables();
            row.setTableNo(tableNoField.getText());
            row.setCapacity(Integer.parseInt(capacityField.getText()));
            row.setLocation(LocationField.getText());
            row.setStatus(StatusField.getText());
            manageTablesRepository.save(row);

            ActivityLogs logs = new ActivityLogs();
            logs.setTimestamp(LocalDateTime.now());
            logs.setAction("Add Table");
            logs.setTarget("Table no."+ row.getTableNo());
            logs.setValue("Capacity: "+row.getCapacity()+" Location: "+row.getLocation());
            logs.setUser(currentuser.getUsername());
            activityLogsRepository.save(logs);
            loadTableManager();
            loadTableView();
            updateLabels();

            loadTableManager();
            loadAvailableTable();
            loadTableView();
        }
    });
    }
    
    private void makeNumeric(TextField field) {
    field.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) {
            field.setText(newValue.replaceAll("[^\\d]", ""));
        }
    });
    }
    
    private void makeLetterOnly(TextField field) {
    field.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-Z]*")) {
            field.setText(newValue.replaceAll("[^a-zA-Z]", ""));
        }
    });
    }
    
    private void makeAlphaNumericWithSpace(TextField field) {
    field.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-Z0-9 ]*")) {
            field.setText(newValue.replaceAll("[^a-zA-Z0-9 ]", ""));
        }
    });
    }
    
    private void setupSCNReservation(){
        SCNReservations.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        if (newSkin != null) {
        ScrollBar hBar = (ScrollBar) SCNReservations.lookup(".scroll-bar:horizontal");
        if (hBar != null) {
            hBar.setVisible(false);
            hBar.setManaged(false); // removes layout space
        }
        }
        });
        statusSCNR.setCellFactory(tv -> new TableCell<CustomerReservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(item); // Make sure your DTO has getStatus()
                    String bgColor;

                    switch (item) {
                        case "Seated" ->
                            bgColor = "#2E7D32";
                        case "Cancelled" ->
                            bgColor = "#D32F2F";
                        case "No Show" ->
                            bgColor = "#9C27B0";
                        default ->
                            bgColor = "transparent";
                    }

                    setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 5;");
                    
                }
            }
        });
        TableColumn<?, ?>[] column = {refSCNR, customerSCNR, paxSCNR, phoneSCNR, statusSCNR, regSCNR, seatedSCNR, cancelSCNR, noshowSCNR,dateSCNR};
        double[] widthFactors = {0.1, 0.15, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,0.1};
        String[] namecol = {"reference", "name", "pax", "phone", "status", "reservationPendingtime", "reservationSeatedtime", "reservationCompletetime", "reservationCompletetime","date"};

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
            col.prefWidthProperty().bind(SCNReservations.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            
        }
        SCNReservations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        
    }
    
    private void loadSCNReservation(){
        List<String> statuses = List.of("Seated", "Cancelled","No Show");
        List<CustomerReservation> tables = customerReservationRepository.findByStatusIn(statuses);
        canceledReservations.setAll(tables);
    }


    private void loadReservationManagement() {
        loadCustomerReservationTable();
        loadAvailableTable();
        loadReservationLogs();

        loadSCNReservation();
        setupSCNReservation();
        setupReservationLogs();
        setupAvailableTable();
        setupCustomerReservationTable();


        hiddenTable.setVisible(false);
        hiddenTable.setManaged(false);

    }
    
    
    public void setupTableManager() {
        tablesfilteredData = new FilteredList<>(manageTablesData, p -> true);

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

        StatusTM.setCellFactory(tv -> new TableCell<ManageTablesDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item); // Make sure your DTO has getStatus()
                    String bgColor;

                    switch (item) {
                        case "Available" ->
                            bgColor = "#2a4d2a";
                        case "Occupied" ->
                            bgColor = "#5a1e1e";
                        case "Reserved" ->
                            bgColor = "#7A5A00";
                        default ->
                            bgColor = "white";
                    }

                    setStyle("-fx-background-color: " + bgColor + "; -fx-margin: 8 16 8 16;");
                    
                }
            }
        });


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
                utensilIcon.setIconSize(12);
                utensilIcon.setIconColor(Color.web("#4A2A33"));
                editIcon.setIconSize(12);
                editIcon.setIconColor(Color.web("#000000"));
                deleteIcon.setIconSize(12);
                deleteIcon.setIconColor(Color.web("#ffffff"));
                completeIcon.setIconSize(12);
                completeIcon.setIconColor(Color.web("#ffffff"));
                
                
                
                
                btnStart.setGraphic(utensilIcon);
                btnStart.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                btnStart.getStyleClass().add("start-service");
                
                btnEdit.setGraphic(editIcon);
                btnEdit.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                btnEdit.getStyleClass().add("edit");
                
                btnDelete.setGraphic(deleteIcon);
                btnDelete.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                btnDelete.getStyleClass().add("delete");
                
                btnComplete.setGraphic(completeIcon);
                btnComplete.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                btnComplete.getStyleClass().add("complete");


            
                hbox.setAlignment(Pos.CENTER);
                btnStart.setMaxWidth(Double.MAX_VALUE);
                btnEdit.setMaxWidth(Double.MAX_VALUE);
                btnDelete.setMaxWidth(Double.MAX_VALUE);
                btnStart.setMaxHeight(Double.MAX_VALUE);
                btnEdit.setMaxHeight(Double.MAX_VALUE);
                btnDelete.setMaxHeight(Double.MAX_VALUE);
                
                HBox.setHgrow(btnComplete, Priority.ALWAYS);
                HBox.setHgrow(btnStart, Priority.ALWAYS);
                HBox.setHgrow(btnEdit, Priority.ALWAYS);
                HBox.setHgrow(btnDelete, Priority.ALWAYS);

                btnStart.setOnAction(event -> {
                    ManageTablesDTO data = getCurrentItem();
                    if (data != null) {
                        data.setStatus("Occupied");
                        updateButtonsForStatus(data);

                        tablesService.updateStatus(data.getTableId(), data.getStatus());
                        reservationService.updateSeatedtime(data.getReference(), LocalTime.now());
                        reservationService.updateStatus(data.getReference(),"Seated");
                        data.setReservationSeatedtime(LocalTime.now());
                        loadTableView();
                        loadTableManager();
                        updateLabels();
                        getTableView().refresh();
                        loadSCNReservation();

                        ActivityLogs logs = new ActivityLogs();
                        logs.setTimestamp(LocalDateTime.now());
                        logs.setAction("Occupied");
                        logs.setTarget("Table No." + data.getTableId());
                        logs.setValue(data.getCustomer());
                        logs.setUser(currentuser.getUsername());

                        activityLogsRepository.save(logs);


                    }
                });
                btnComplete.setOnAction(event -> {
                    ManageTablesDTO data = getCurrentItem();
                    if (data != null) {
                        data.setStatus("Complete");
                        data.setDate(LocalDate.now());
                        data.setReservationCompletetime(LocalTime.now());
                        updateButtonsForStatus(data);

                        ActivityLogs logs = new ActivityLogs();
                        logs.setTimestamp(LocalDateTime.now());
                        logs.setAction("Complete");
                        logs.setTarget("Table no."+ data.getTableId());
                        logs.setValue(data.getReference());
                        logs.setUser(currentuser.getUsername());
                        activityLogsRepository.save(logs);
                        
                        ReservationTableLogs reservationtablelogs = new ReservationTableLogs(data);
                        RTLR.save(reservationtablelogs);
                        reservationService.updateStatus(data.getReference(),data.getStatus());
                        reservationService.updateTableId(data.getReference(),null);

                       
                        data.setCustomer("");
                        data.setPax(null);
                        data.setStatus("Available");
                        tablesService.updateStatus(data.getTableId(),data.getStatus());




                        System.out.println("Service started for: " + data.getStatus());
                    }
                    loadTableView();
                    loadCustomerReservationTable();
                    loadReservationLogs();
                    loadTableManager();
                    updateLabels();
                    getTableView().refresh();

                });

                btnEdit.setOnAction(event -> {
                    ManageTablesDTO data = getCurrentItem();
                        if (data != null) {
                            int pax;
                            String customer;
                            if(data.getPax()==null && data.getCustomer()==null){
                                pax = 0;
                                customer = "No Customer";
                            }
                            else{pax = data.getPax(); customer = data.getCustomer();}
                            
                            
                            editTablerow(String.valueOf(data.getTableId()),customer,pax,data.getStatus(),data.getCapacity(),data.getLocation());
                            
                         }
                    });

                    btnDelete.setOnAction(event -> {
                        ManageTablesDTO data = getCurrentItem();
                        if (data != null) {
                                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/deleteTableDialog.fxml"));
                    Parent root = loader.load();

                    // Get controller to handle callback
                    DeleteTableDialogController controller = loader.getController();
                    controller.setOnDelete(() -> {
                        if(customerReservationRepository.existsByTable_Id(data.getTableId())) {
                            showAlert("Cannot delete: this table has active reservations");
                            return;
                        }
                        manageTablesRepository.deleteById(data.getTableId());
                        ActivityLogs logs = new ActivityLogs();
                        logs.setTimestamp(LocalDateTime.now());
                        logs.setAction("Delete");
                        logs.setTarget("Table no."+ data.getTableId());
                        logs.setValue("");
                        logs.setUser(currentuser.getUsername());

                        activityLogsRepository.save(logs);
                        loadTableManager();
                        loadTableView();
                        updateLabels();
                    });

                    // Create & show dialog
                    Stage dialog = new Stage(StageStyle.UNDECORATED);
                    dialog.initModality(Modality.WINDOW_MODAL);
                    dialog.initStyle(StageStyle.TRANSPARENT);
                    dialog.setResizable(false);
                    Scene scn = new Scene(root);
                    scn.setFill(Color.TRANSPARENT);
                    dialog.setScene(scn);
                    dialog.showAndWait();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                    }
                });
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
        String[] namecol = {"tableId", "customer", "pax", "status", "capacity", "location", "tablestarttime", ""};

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

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null); // no header
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void loadTableManager(){
        List<ManageTablesDTO> tables = manageTablesRepository.getManageTablesDTO();
        tableManagerData.setAll(tables);
        totaltables.setText(String.valueOf(manageTablesRepository.count()));
        totalfree.setText(String.valueOf(manageTablesRepository.countByStatus("Available")));
        int busy = 0;
        busy += manageTablesRepository.countByStatus("Reserved");
        busy += manageTablesRepository.countByStatus("Occupied");
        totalbusy.setText(String.valueOf(busy));
        
        
    }
    private void setupTableHistory(){
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
        statusTH.setCellFactory(tv -> new TableCell<ReservationTableLogs, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    setText(item); // Make sure your DTO has getStatus()
                    String bgColor;

                    switch (item) {
                        case "Complete" ->
                                bgColor = "#2a4d2a";
                        default ->
                                bgColor = "white";
                    }

                    setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 5;");

                }
            }
        });

        TableHistory.setPlaceholder(new Label("No Table set yet"));

    }
    private void loadTableHistory(){
        List<ReservationTableLogs> tables = RTLR.findAll();
        reservationlogsdata.setAll(tables);

    }


    
    
    private void loadTableManagement(){
        setupTableManager();        
        loadTableManager();
        setupTableHistory();
    }
    
    private void setupReservationLogs(){
        ReservationLogs.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        if (newSkin != null) {
        ScrollBar hBar = (ScrollBar) ReservationLogs.lookup(".scroll-bar:horizontal");
        if (hBar != null) {
            hBar.setVisible(false);
            hBar.setManaged(false); // removes layout space
        }
        }
        });
        statusRL.setCellFactory(tv -> new TableCell<ReservationTableLogs, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {

                        setText(null);
                        setGraphic(null);

                } else {
                    setText(item); // Make sure your DTO has getStatus()
                    String bgColor;

                    switch (item) {
                        case "Complete" ->
                            bgColor = "#4CAF50";
                       
                        default ->
                            bgColor = "transparent";
                    }

                    setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 5;");
                    
                }
            }
        });

        TableColumn<?, ?>[] column = {customerRL,paxRL,phoneRL,preferRL,statusRL,pendingRL,confirmRL,seatedRL,completeRL,tablenoRL,refRL,dateRL};
        double[] widthFactors = {0.13, 0.05, 0.1, 0.1, 0.1, 0.07, 0.07, 0.07,0.07,0.05,0.08,0.11};
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
        List<ReservationTableLogs> Data = RTLR.findAll();
        reservationlogsdata.setAll(Data);
        
    }

    public void setupAccountTable(){
        actionAT.setCellFactory(col -> new TableCell<User, Void>() {
                    FontIcon editIcon = new FontIcon(FontAwesomeSolid.PEN_SQUARE);
                    FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);

                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox hbox = new HBox(5);

                    {
                        editIcon.setIconSize(12);
                        editIcon.setIconColor(Color.web("#000000"));
                        deleteIcon.setIconSize(12);
                        deleteIcon.setIconColor(Color.web("#ffffff"));

                        btnEdit.setGraphic(editIcon);
                        btnEdit.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                        btnEdit.getStyleClass().add("edit");

                        btnDelete.setGraphic(deleteIcon);
                        btnDelete.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
                        btnDelete.getStyleClass().add("delete");


                        hbox.setAlignment(Pos.CENTER);
                        hbox.getChildren().addAll(btnEdit, btnDelete);
                        btnEdit.setMaxWidth(Double.MAX_VALUE);
                        btnDelete.setMaxWidth(Double.MAX_VALUE);
                        btnEdit.setMaxHeight(Double.MAX_VALUE);
                        btnDelete.setMaxHeight(Double.MAX_VALUE);

                        HBox.setHgrow(btnEdit, Priority.ALWAYS);
                        HBox.setHgrow(btnDelete, Priority.ALWAYS);


                        btnEdit.setOnAction(event -> {
                            User data = getTableView().getItems().get(getIndex());
                            editAccount(data);



                        });

                        btnDelete.setOnAction(event -> {
                            User data = getTableView().getItems().get(getIndex());

                            if (data != null) {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/deleteTableDialog.fxml"));
                                    Parent root = loader.load();

                                    // Get controller to handle callback
                                    DeleteTableDialogController controller = loader.getController();
                                    controller.setOnDelete(() -> {
                                        if ("Active".equals(data.getStatus())) {
                                            showAlert("This account cannot be deleted because it is currently in use");
                                            return;
                                        }else{
                                            userRepository.deleteById(data.getId());
                                            loadAccountTable();

                                        }

                                    });

                                    // Create & show dialog
                                    Stage dialog = new Stage(StageStyle.UNDECORATED);
                                    dialog.initModality(Modality.WINDOW_MODAL);
                                    dialog.initStyle(StageStyle.TRANSPARENT);
                                    dialog.setResizable(false);
                                    Scene scn = new Scene(root);
                                    scn.setFill(Color.TRANSPARENT);
                                    dialog.setScene(scn);
                                    dialog.showAndWait();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                            setText(null);
                            setGraphic(null);

                        }else{
                            setText(null);       // clear text
                            setGraphic(hbox);
                        }
                    }
                private User getCurrentItem() {
                int i = getIndex();
                if (i >= 0 && i < getTableView().getItems().size()) {
                    return getTableView().getItems().get(i);
                }
                return null;
            }

                });

        TableColumn<?, ?>[] column = {usernameAT,firstnameAT,lastnameAT,positionAT,statusAT,actionAT};
        double[] widthFactors = {0.15, 0.15, 0.15, 0.15, 0.15,0.25};
        String[] namecol = {"username", "firstname", "lastname", "position", "status",""};

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
            col.prefWidthProperty().bind(AccountTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        AccountTable.setPlaceholder(new Label("No Account yet "));
    }
    public void loadAccountTable(){
        List<User> data = userRepository.findAll();
        UserData.setAll(data);

    }
    public void loadAccountManagement(){
        loadAccountTable();
        setupAccountTable();
    }

    public void editAccount(User item) {
        // Create UI elements
        TextField usernameField = createTextField("Username");
        TextField firstnameField = createTextField("First Name");
        TextField lastnameField = createTextField("Last Name");

        ComboBox<String> positionField = new ComboBox<>();
        positionField.getItems().addAll("ADMINISTRATOR", "MANAGER");
        positionField.setPromptText("Position");
        positionField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0;");

        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisibleField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0;");

        Button togglePasswordButton = new Button("👁");
        togglePasswordButton.setFocusTraversable(false);
        togglePasswordButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        togglePasswordButton.setOnAction(e -> {
            if (passwordVisibleField.isVisible()) {
                passwordVisibleField.setVisible(false);
                passwordVisibleField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
            } else {
                passwordVisibleField.setVisible(true);
                passwordVisibleField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
            }
        });

        StackPane passwordPane = new StackPane(passwordField, passwordVisibleField, togglePasswordButton);
        StackPane.setAlignment(togglePasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(togglePasswordButton, new Insets(0, 5, 0, 0));

        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("Active", "Inactive");
        statusField.setPromptText("Status");
        statusField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0;");

        // Buttons
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        okButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Container for all fields
        VBox editAccountPane = new VBox(10);
        editAccountPane.setPadding(new Insets(15));
        editAccountPane.setStyle("-fx-background-color: #2b2b2b; -fx-border-radius: 10; -fx-background-radius: 10;");
        editAccountPane.getChildren().addAll(usernameField, firstnameField, lastnameField, positionField, passwordPane, statusField, buttonBox);

        usernameField.setText(item.getUsername());
        firstnameField.setText(item.getFirstname());
        lastnameField.setText(item.getLastname());
        passwordField.setText(item.getPassword());


        // Stage / Dialog
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        Scene scene = new Scene(editAccountPane);
        stage.setScene(scene);
        stage.setTitle("Edit Account");

        // Button actions
        okButton.setOnAction(e -> {
            String username = usernameField.getText();
            String firstname = firstnameField.getText();
            String lastname = lastnameField.getText();
            String position = positionField.getValue();
            String password = passwordField.getText();
            String status = statusField.getValue();
            User updatedUser = new User();
            updatedUser.setId(item.getId());
            updatedUser.setUsername(username);
            updatedUser.setFirstname(firstname);
            updatedUser.setLastname(lastname);
            updatedUser.setPosition(User.Position.valueOf(position));
            updatedUser.setPassword(password);
            updatedUser.setStatus(status);


            // TODO: Save/update user in your repository
            userRepository.save(updatedUser);

            stage.close();
            loadAccountTable();
        });

        cancelButton.setOnAction(e -> stage.close());

        stage.showAndWait();
    }


    // Helper method
    private TextField createTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0;");
        return tf;
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Dashboardbtn.fire();
        loadDashboard();
        loadReservationManagement();
        loadTableManagement();
        loadAccountManagement();
        
        
        RecentReservationTable.setItems(recentReservations);
        CustomerReservationTable.setItems(filteredData);
        ManageTableView.setItems(manageTablesData);
        TableManager.setItems(tablesfilteredData);
        AvailableTable.setItems(filteredtable);
        ReservationLogs.setItems(reservationlogsdata);
        SCNReservations.setItems(canceledReservations);
        TableHistory.setItems(reservationlogsdata);
        AccountTable.setItems(UserData);
        

        handleClick();

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
    public void onNewReservation(CustomerReservationDTO reservation) {
        Platform.runLater(() -> {
            updateLabels();
            handleClick();
            loadRecentReservations();
            loadCustomerReservationTable();
        });
    }

}
