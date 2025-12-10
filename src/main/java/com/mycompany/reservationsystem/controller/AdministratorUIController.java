/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.Service.ActivityLogService;
import com.mycompany.reservationsystem.Service.ReservationService;
import com.mycompany.reservationsystem.Service.TablesService;
import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.repository.*;
import com.mycompany.reservationsystem.websocket.ReservationListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;

import java.net.URL;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
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
public class


AdministratorUIController implements Initializable, ReservationListener {
    private User currentuser;

    void  setUser(User user) {
        this.currentuser = user;
    }

    @FXML
    private Button Dashboardbtn, ReservationManagementbtn, TableManagementbtn, ManageStaffAndAccountsbtn,Reportsbtn,ActivityLogbtn,Mergebtn,Reservationrpts, Customerrpts, Revenuerpts, TableUsagerpts,ApplyResrep,ApplyCusrep,ApplyRevrep,ApplyTUrep,applyAL;
    @FXML
    private ScrollPane DashboardPane, ReservationPane, TableManagementPane, ManageStaffAndAccountsPane,ReportsPane,ActivityLogPane;
    @FXML
    private Label Total_CustomerDbd, activetable,Total_Cancelled, Total_Pending, CusToTable,totaltables,totalfree,totalbusy,pending,confirm,header,seated,cancelled,noshow;
    @FXML
    private LineChart<String, Number> myBarChart;
    @FXML
    private BarChart<String, Number> totalReservationChart,totalCustomerChart,totalRevenueChart,totalReservationChartTableUsage,totalCustomerChartTableUsage,totalRevenueChartTableUsage;
    @FXML
    private TextField SearchCL,SearchTM,searchAL;
    @FXML
    private BorderPane dashpane,reservpane,tablepane,accountpane;
    @FXML
    private TableView<Reservation> RecentReservationTable,SCNReservations,ResRepTable;
    @FXML
    private TableView<CustomerReportDTO> CusRepTable;
    @FXML
    private TableColumn<CustomerReportDTO, Integer>totalreservationCusrep;
    @FXML
    private TableColumn<CustomerReportDTO, Double> averageCusrep,totalrevenueCusrep;
    @FXML
    private TableColumn<CustomerReportDTO, String> phoneCusrep;
    @FXML
    private TableView<ReservationCustomerDTO> ResInCusRep;
    @FXML
    private TableColumn<CustomerReportDTO, LocalTime>timeResInCusRep;
    @FXML
    private TableColumn<CustomerReportDTO, LocalDate>dateResInCusRep;
    @FXML
    private TableColumn<CustomerReportDTO, Double> revenueResInCusRep;
    @FXML
    private TableColumn<CustomerReportDTO, String> referenceResInCusRep,nameResInCusRep,phoneResInCusRep,statusResInCusRep;
    @FXML
    private TableView<RevenueReportsDTO>RevRepTable;
    @FXML
    private TableColumn<RevenueReportsDTO, Long>totalcustomerRevrep,totalreservationRevrep;
    @FXML
    private TableColumn<RevenueReportsDTO, LocalDate>dateRevrep;
    @FXML
    private TableColumn<RevenueReportsDTO, Double>totalrevenueRevrep;
    @FXML
    private TableView<ManageTablesDTO> ManageTableView,TableManager;
    @FXML
    private TableColumn<Reservation, String> CustomerColm,customerSCNR,phoneSCNR,statusSCNR,refSCNR,referenceResrep,statusResrep;
    @FXML
    private TableColumn<Reservation, Integer> PaxColm,paxSCNR,paxResrep;
    @FXML
    private TableColumn<Reservation, LocalTime> TimeColm,regSCNR,seatedSCNR,cancelSCNR,noshowSCNR,timeResrep;
    @FXML
    private TableColumn<Reservation, LocalDate> dateSCNR,dateResrep;
    @FXML
    private TableColumn<ManageTablesDTO, String> TableNoColum, TableCustomerColum, TableStatusColum, TablenoTM,StatusTM,CustomerTM,LocationTM;
    @FXML
    private TableColumn<ManageTablesDTO, LocalTime> TableTimeColum,TimeUsedTM;
    @FXML
    private TableColumn<ManageTablesDTO, Integer> TablePaxColum, TableCapacityColum;
    @FXML
    private VBox notificationArea, hiddenTable,rootVBox,rootVBoxTableUsage,tableInfopane;
    @FXML
    private TableView<Reservation> CustomerReservationTable;
    @FXML
    private TableView<ActivityLog> ActivityLogsTable;
    @FXML
    private TableColumn<ActivityLog,String> userAL,positionAL,moduleAL,actionAL,descriptionAL;
    @FXML
    private TableColumn<ActivityLog,LocalDateTime>timestampsAL;
    @FXML
    private TableColumn<Reservation, String> NameCRT, StatusCRT, PreferCRT, PhoneCRT, EmailCRT, ReferenceCRT;
    @FXML
    private TableColumn<Reservation, Integer> PaxCRT;
    @FXML
    private TableColumn<Reservation, Long> TableNoCRT;
    @FXML
    private TableColumn<Reservation, LocalTime> TimeCRT;
    @FXML
    private TableView<User> AccountTable;
    @FXML
    private TableColumn<User,String> usernameAT,firstnameAT,lastnameAT,positionAT,statusAT;
    @FXML
    private TableView<TableUsageReportDTO>TableUseRep;
    @FXML
    private TableColumn<TableUsageReportDTO, String>tablenoTableUseRep;
    @FXML
    private TableColumn<TableUsageReportDTO, Integer>totalreservationTableUseRep,totalcusotmerTableUseRep;
    @FXML
    private TableColumn<TableUsageReportDTO, Double>totalrevenueTableUseRep;
    @FXML
    private TableView<TableUsageInformationDTO>TableinfoTUrep;
    @FXML
    private TableColumn<TableUsageInformationDTO,String> tablenoTableinfo,referenceTableinfo;
    @FXML
    private TableColumn<TableUsageInformationDTO,Integer> paxTableinfo;
    @FXML
    private TableColumn<TableUsageInformationDTO,Double> revenueTableinfo;
    @FXML
    private TableColumn<TableUsageInformationDTO,LocalTime> timeTableinfo;
    @FXML
    private TableColumn<TableUsageInformationDTO,LocalDate> dateTableinfo;
    @FXML
    private MFXDatePicker dateFromResrep,dateToResrep,dateFromCusrep,dateToCusrep,dateFromRevrep,dateToRevrep,dateFromTUrep,dateToTUrep,fromAL,toAL;


    private Reservation selectedReservation;
    
    private final ObservableList<Reservation> recentReservations = FXCollections.observableArrayList();
    private final ObservableList<Reservation> pendingReservations = FXCollections.observableArrayList();
    private final ObservableList<Reservation> canceledReservations = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservationreports = FXCollections.observableArrayList();
    private final ObservableList<CustomerReportDTO> customerreports = FXCollections.observableArrayList();
    private final ObservableList<ReservationTableLogs> reservationlogsdata = FXCollections.observableArrayList();
    private final ObservableList<ManageTablesDTO> manageTablesData = FXCollections.observableArrayList();
    private final ObservableList<ManageTablesDTO> tableManagerData = FXCollections.observableArrayList();
    private final ObservableList<ManageTables> availableTables = FXCollections.observableArrayList();
    private final ObservableList<User> UserData = FXCollections.observableArrayList();
    private final ObservableList<ActivityLog> activitylogsdata = FXCollections.observableArrayList();
    private final ObservableList<ReservationCustomerDTO> reservationCustomerDTOS = FXCollections.observableArrayList();
    private final ObservableList<RevenueReportsDTO> RevenueReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageReportDTO> TableUsageReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageInformationDTO> TableUsageInformationDTOS = FXCollections.observableArrayList();

    private FilteredList<Reservation> filterReservationReports = new FilteredList<>(reservationreports, p -> true);;
    private FilteredList<CustomerReportDTO> filterCustomerReports = new FilteredList<>(customerreports, p -> true);;
    private FilteredList<ActivityLog> filteredActivityLogs = new FilteredList<>(activitylogsdata, p -> true);

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
    @FXML
    private HBox ReservationReport,CustomerReport,ActivityLogsReport,RevenueReport;

    @FXML
    private GridPane TableUsageReport;
    @FXML
    private MFXComboBox tablefilter,reservationfilter,StatusfilterResrep;
    @FXML
    private PieChart reservationPieChart;

    @Autowired
    private ManageTablesRepository manageTablesRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ReservationTableLogsRepository RTLR;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private TablesService tablesService;
    
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private ActivityLogService activityLogService;
    private Long prevRow;
    private Long currentRow;
    private ManageTables selectedTable;
    double scrollPosition;
    boolean DashboardDataLoaded = false;
    boolean ReservationDataLoaded = false;
    boolean TableDataLoaded = false;
    boolean AccountDataLoaded = false;
    boolean ReportsDataLoaded = false;
    boolean ActivityLogLoaded = false;



    @FXML
    private void navigate(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        Button buttons[] = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn, ManageStaffAndAccountsbtn,Reportsbtn,ActivityLogbtn};

        DashboardPane.setVisible(false);
        ReservationPane.setVisible(false);
        TableManagementPane.setVisible(false);
        ManageStaffAndAccountsPane.setVisible(false);
        ReportsPane.setVisible(false);
        ActivityLogPane.setVisible(false);

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

        switch (clicked.getId()) {
            case "Dashboardbtn":
                DashboardPane.setVisible(true);
                header.setText("Dashboard");
                if (!DashboardDataLoaded) {
                    loadDashboard();
                    DashboardDataLoaded = true;
                }

                break;
            case "ReservationManagementbtn":
                ReservationPane.setVisible(true);
                header.setText("Reservation Management");
                if (!ReservationDataLoaded) {
                    loadReservationManagement();
                    ReservationDataLoaded = true;
                }
                break;
            case "TableManagementbtn":
                TableManagementPane.setVisible(true);
                header.setText("Table Management");
                if (!TableDataLoaded) {
                    loadTableManagement();
                   TableDataLoaded = true;
                }

                break;
            case "ManageStaffAndAccountsbtn":
                ManageStaffAndAccountsPane.setVisible(true);
                header.setText("Account Management");
                if (!AccountDataLoaded) {
                    loadAccountManagement();
                    AccountDataLoaded = true;
                }

                break;
            case "Reportsbtn":
                ReportsPane.setVisible(true);
                header.setText("Reports");
                if (!ReportsDataLoaded) {
                    loadReports();
                    ReportsDataLoaded = true;
                }
                break;
            case "ActivityLogbtn":
                ActivityLogPane.setVisible(true);
                header.setText("Activity Logs");
                //if (!ActivityLogLoaded) {
                    loadActivityLogs();
                 //   ActivityLogLoaded = true;
                //}
                break;
            default:
                break;

        }
    }

    @FXML
    private void navigateReports(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        Button[] buttons = {Reservationrpts, Customerrpts, Revenuerpts, TableUsagerpts};

        ReservationReport.setVisible(false);
        TableUsageReport.setVisible(false);
        RevenueReport.setVisible(false);
        CustomerReport.setVisible(false);


        for (Button btn : buttons) {
            if (btn == null) {
                continue;
            }
            btn.getStyleClass().remove("navigation-report-btns-active");
            if (!btn.getStyleClass().contains("navigation-report-btns")) {
                btn.getStyleClass().add("navigation-report-btns");
            }
        }

        clicked.getStyleClass().remove("navigation-report-btns");
        if (!clicked.getStyleClass().contains("navigation-report-btns-active")) {
            clicked.getStyleClass().add("navigation-report-btns-active");
        }

        switch (clicked.getId()) {
            case "Reservationrpts":
                ReservationReport.setVisible(true);
                break;
            case "Customerrpts":
                CustomerReport.setVisible(true);
                break;
            case "Revenuerpts":
                RevenueReport.setVisible(true);
                break;
            case "TableUsagerpts":
                TableUsageReport.setVisible(true);
                break;

            default:
                break;

        }


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
        Total_CustomerDbd.setText(String.valueOf(reservationRepository.count()));
        Total_Pending.setText(String.valueOf(reservationRepository.countByStatus("Pending")));
        Total_Cancelled.setText(String.valueOf(reservationRepository.countByStatus("Cancelled")));

    }

    public void loadRecentReservations() {

        List<Reservation> latest10 = reservationRepository.findTop15ByOrderByDateDescReservationPendingtimeDesc(PageRequest.of(0, 15));
        recentReservations.setAll(latest10);
    }

    public void loadTableView() {

        List<ManageTablesDTO> tables = manageTablesRepository.getManageTablesDTO();
        manageTablesData.setAll(tables);

    }

    public void setupRecentReservation() {

        RecentReservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        RecentReservationTable.setItems(recentReservations);

        TableColumn<?, ?>[] column = {CustomerColm, PaxColm, TimeColm};
        double[] widthFactors = {0.4, 0.10, 0.55};
        String[] namecol = {"name","pax","reservationPendingtime"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(true);
            col.setReorderable(false);
            if (namecol[i].equals("name")) {
                ((TableColumn<Reservation, String>) col).setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getCustomer().getName())
                );
            } else {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
            }
        RecentReservationTable.setPlaceholder(new Label("No Customer Reservation yet"));

    }

    public void setupTableView(){
        ManageTableView.setItems(manageTablesData);


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
                    setGraphic(null);
                    setText("");
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
        List<Reservation> reservations = reservationRepository.findAll()
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
    private <T> void addItemsToCombo(
            MFXComboBox<String> combo,
            FilteredList<T> filteredData,
            Function<T, String> propertyExtractor,
            String... statuses) {

        combo.getItems().addAll(statuses);

        combo.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int i = newIndex.intValue();

            if (i >= 0 && i < statuses.length) {
                String selectedStatus = statuses[i];

                if (selectedStatus.equalsIgnoreCase("Show All")) {
                    filteredData.setPredicate(item -> true); // no filter
                } else {
                    filteredData.setPredicate(item ->
                            propertyExtractor.apply(item) != null &&
                                    propertyExtractor.apply(item).equalsIgnoreCase(selectedStatus)
                    );
                }

                combo.setText(selectedStatus);
            }
        });
    }








    FilteredList<ManageTablesDTO> tablesfilteredData;
    FilteredList<Reservation> filteredData;
    private int currentpax;
    FilteredList<ManageTables> filteredtable;



    public void setupCustomerReservationTable() {
        filteredData = new FilteredList<>(pendingReservations, p -> true);
        CustomerReservationTable.setItems(filteredData);
        String[] statuses = {"Confirm", "Pending", "Show All"};
        addItemsToCombo(reservationfilter,filteredData,Reservation::getStatus,statuses);



        SearchCL.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
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

        TimeCRT.setCellFactory(col -> new TableCell<Reservation, LocalTime>() {
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
        StatusCRT.setCellFactory(tv -> new TableCell<Reservation, String>() {
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

        ReferenceCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        NameCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.15));
        PaxCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.06));
        StatusCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        PreferCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        PhoneCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.12));
        EmailCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.15));
        TimeCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.13));
        TableNoCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.09));

// Set cell value factories manually with proper types
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
        List<Reservation> Data = reservationRepository.findByStatusIn(statuses);
        pendingReservations.setAll(Data);



        pending.setText(String.valueOf(reservationRepository.countByStatus("Pending")));
        confirm.setText(String.valueOf(reservationRepository.countByStatus("Confirm")));


    }
    
    public void setupAvailableTable() {

        filteredtable = new FilteredList<>(availableTables);
        AvailableTable.setItems(filteredtable);
        AvailableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
    dialog.initModality(Modality.APPLICATION_MODAL);


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

            activityLogService.logAction(currentuser.getUsername(),String.valueOf(currentuser.getPosition()),"Table","Edit Table",
                    String.format("Change Table # %s → %s : Capacity %d → %d, Location %s → %s",
                            tableno,String.valueOf(row.getId()),capacity,row.getCapacity(),location,row.getLocation()));

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
        dialog.initModality(Modality.APPLICATION_MODAL);
    

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

            // 1. Create CUSTOMER object
            Customer customer = new Customer();
            customer.setName(customerField.getText());
            customer.setPhone(phoneField.getText());
            customer.setEmail(emailField.getText());

            customerRepository.save(customer);

            // 2. Create RESERVATION object
            Reservation row = new Reservation();
            row.setCustomer(customer);   // <-- IMPORTANT
            row.setPrefer(preferField.getText());
            row.setPax(Integer.parseInt(paxField.getText()));

            row.setStatus("Pending");
            row.setDate(currentdate);
            row.setReference(String.format("RSV-%05d", reservationRepository.count() + 1));

            row.setReservationPendingtime(
                    LocalTime.parse(pendingtimeField.getText(), DateTimeFormatter.ofPattern("hh:mm:ss a"))
            );

            // table is optional: row.setTable(selectedTable);

            // 3. Save RESERVATION
            reservationRepository.save(row);

            // 4. Create Activity Log
            activityLogService.logAction(
                    currentuser.getUsername(),                    // username
                    currentuser.getPosition().toString(),        // position/role
                    "Reservation",                                     // module
                    "Add",                             // action
                    String.format(
                            "Add Reservation Reference %s",
                            row.getReference()                // reservation reference
                    )
            );


            // 5. Refresh UI
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
    dialog.initModality(Modality.APPLICATION_MODAL);



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
            row.setId(Long.parseLong(tableNoField.getText()));
            row.setTableNo(tableNoField.getText());
            row.setCapacity(Integer.parseInt(capacityField.getText()));
            row.setLocation(LocationField.getText());
            row.setStatus(StatusField.getText());
            manageTablesRepository.save(row);

            activityLogService.logAction(
                    currentuser.getUsername(),                    // username
                    currentuser.getPosition().toString(),        // position/role
                    "Table",                                     // module
                    "Add Table",                             // action
                    String.format(
                            "Add Table #%s Capacity %d Location %s",
                            row.getTableNo(),                   // table ID
                            row.getCapacity(),                           // old table status
                            row.getLocation()                        // reservation reference
                    )
            );


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
        SCNReservations.setItems(canceledReservations);
        SCNReservations.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        if (newSkin != null) {
        ScrollBar hBar = (ScrollBar) SCNReservations.lookup(".scroll-bar:horizontal");
        if (hBar != null) {
            hBar.setVisible(false);
            hBar.setManaged(false); // removes layout space
        }
        }
        });
        statusSCNR.setCellFactory(tv -> new TableCell<Reservation, String>() {
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
            switch (col.getText()) {
                case "Reference":
                    ((TableColumn<Reservation, String>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReference()));
                    break;

                case "Customer":
                    ((TableColumn<Reservation, String>) col)
                            .setCellValueFactory(res -> {
                                Customer c = res.getValue().getCustomer();
                                return new ReadOnlyObjectWrapper<>(c != null ? c.getName() : "");
                            });
                    break;
                case "Pax":
                    ((TableColumn<Reservation, Integer>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getPax()));
                    break;

                case "Phone no":
                    ((TableColumn<Reservation, String>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getCustomer().getPhone()));

                    break;

                case "Status":
                    ((TableColumn<Reservation, String>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getStatus()));
                    break;

                case "Registered Time":
                    ((TableColumn<Reservation, LocalTime>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationPendingtime()));
                    break;

                case "Seated Time":
                    ((TableColumn<Reservation, LocalTime>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationSeatedtime()));
                    break;

                case "Cancelled Time":
                    ((TableColumn<Reservation, LocalTime>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationCancelledtime()));
                    break;
                case "NoShow Time":
                    ((TableColumn<Reservation, LocalTime>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationCompletetime()));
                    break;

                case "Date":
                    ((TableColumn<Reservation, LocalDate>) col)
                            .setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getDate()));
                    break;

                default:
                    System.out.println("⚠ Unknown column: " + col.getText());
                    break;
            }

        }
        SCNReservations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        
    }
    
    private void loadSCNReservation(){
        List<String> statuses = List.of("Seated", "Cancelled","No Show");
        List<Reservation> tables = reservationRepository.findByStatusIn(statuses);
        canceledReservations.setAll(tables);
        seated.setText(String.valueOf(reservationRepository.countByStatus("Seated")));
        cancelled.setText(String.valueOf(reservationRepository.countByStatus("Cancelled")));
        noshow.setText(String.valueOf(reservationRepository.countByStatus("No Show")));
    }


    private void loadReservationManagement() {
        loadCustomerReservationTable();
        loadAvailableTable();
        loadReservationLogs();
        loadSCNReservation();


        hiddenTable.setVisible(false);
        hiddenTable.setManaged(false);

    }
    
    
    public void setupTableManager() {
        tablesfilteredData = new FilteredList<>(manageTablesData, p -> true);
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
                    String currentStatus = data.getStatus();
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
                        loadCustomerReservationTable();

                        activityLogService.logAction(
                                currentuser.getUsername(),                    // username
                                currentuser.getPosition().toString(),        // position/role
                                "Table",                                     // module
                                "Update Status",                             // action
                                String.format(
                                        "Changed table %d status from %s to Occupied for reservation %s",
                                        data.getTableId(),                   // table ID
                                        currentStatus,                           // old table status
                                        data.getReference()                        // reservation reference
                                )
                        );


                    }
                });
                btnComplete.setOnAction(event -> {
                    ManageTablesDTO data = getCurrentItem();
                    String currentStatus = data.getStatus();
                    if (data != null) {
                        data.setStatus("Complete");
                        data.setDate(LocalDate.now());
                        data.setReservationCompletetime(LocalTime.now());

                        updateButtonsForStatus(data);

                        activityLogService.logAction(
                                currentuser.getUsername(),                    // username
                                currentuser.getPosition().toString(),        // position/role
                                "Table",                                     // module
                                "Update Status",                             // action
                                String.format(
                                        "Changed table %s status from %s to Complete → Available",
                                        data.getTableNo(),                   // table ID
                                        currentStatus                           // old table status
                                        )
                        );


                        
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
                        if(reservationRepository.existsByTable_Id(data.getTableId())) {
                            showAlert("Cannot delete: this table has active reservations");
                            return;
                        }
                        manageTablesRepository.deleteById(data.getTableId());

                        activityLogService.logAction(
                                currentuser.getUsername(),                    // username
                                currentuser.getPosition().toString(),        // position/role
                                "Table",                                     // module
                                "Delete Table",                             // action
                                String.format(
                                        "Delete table %s",
                                        data.getTableNo()                  // table ID
                                                                  // old table status
                                )
                        );


                        loadTableManager();
                        loadTableView();
                        updateLabels();
                    });

                    // Create & show dialog
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
        TableManager.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        TableHistory.setItems(reservationlogsdata);
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
                    setGraphic(null);
                    setText("");
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
        TableHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableHistory.setPlaceholder(new Label("No Table set yet"));

    }
    private void loadTableHistory(){
        List<ReservationTableLogs> tables = RTLR.findAll();
        reservationlogsdata.setAll(tables);

    }


    
    
    private void loadTableManagement(){
        loadTableManager();
        loadTableHistory();
        }
    
    private void setupReservationLogs(){
        ReservationLogs.setItems(reservationlogsdata);
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
                        setStyle(null);
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
        ReservationLogs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


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
        AccountTable.setItems(UserData);
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
        stage.initModality(Modality.APPLICATION_MODAL);
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
    private void setupActivityLogs(){
        applyAL.setOnAction(e -> loadActivityLogs());
        searchAL.textProperty().addListener((obs, oldValue, newValue) -> {
            String search = (newValue == null) ? "" : newValue.toLowerCase();

            filteredActivityLogs.setPredicate(log -> {
                if (search.isEmpty()) return true;

                return (log.getUser() != null && log.getUser().toLowerCase().contains(search))
                        || (log.getPosition() != null && log.getPosition().toLowerCase().contains(search))
                        || (log.getModule() != null && log.getModule().toLowerCase().contains(search))
                        || (log.getAction() != null && log.getAction().toLowerCase().contains(search))
                        || (log.getDescription() != null && log.getDescription().toLowerCase().contains(search));
            });
        });
        timestampsAL.setCellFactory(column -> new TableCell<ActivityLog, LocalDateTime>() {

            private final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        descriptionAL.setCellFactory(column -> new TableCell<ActivityLog, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Left align the text
                setStyle("-fx-alignment: CENTER-LEFT;");

                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                    setStyle(null);
                } else {
                    setText(item);
                }
            }
        });

        ActivityLogsTable.setItems(filteredActivityLogs);
        TableColumn<?, ?>[] column = {userAL,positionAL,moduleAL,actionAL,descriptionAL,timestampsAL};
        double[] widthFactors = {0.15, 0.15, 0.1, 0.1, 0.35,0.15};
        String[] namecol = {"user", "position", "module", "action", "description","timestamp"};

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
            col.prefWidthProperty().bind(ActivityLogsTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        ActivityLogsTable.setPlaceholder(new Label("No Activity Data "));
    }
    private void loadActivityLogs(){
        LocalDate from = fromAL.getValue();
        LocalDateTime startDateTime = (from != null) ? from.atStartOfDay() : null;

        LocalDate to = toAL.getValue();
        LocalDateTime endDateTime = (to != null) ? to.atTime(23, 59, 59) : null;


        List<ActivityLog> data = activityLogRepository.filterByDate(startDateTime,endDateTime);
        activitylogsdata.setAll(data);

    }

    private void updateReservationPieChart() {
        Map<String, Long> statusCounts = filterReservationReports.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        Map<String, String> statusColors = Map.of(
                "Pending", "#3498db",    // blue
                "Confirmed", "#2ecc71",  // green
                "Cancelled", "#e74c3c",  // red
                "Seated", "#f1c40f",     // yellow
                "Completed", "#9b59b6"   // purple
        );

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        statusCounts.forEach((status, count) -> {
            pieChartData.add(new PieChart.Data(status, count));
        });

        reservationPieChart.setData(pieChartData);

        for (PieChart.Data data : reservationPieChart.getData()) {
            String color = statusColors.getOrDefault(data.getName(), "#bdc3c7"); // default gray
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
        Platform.runLater(() -> {
            for (Node node : reservationPieChart.lookupAll(".chart-legend-item")) {

                if (node instanceof Label label) {

                    // Legend text
                    String status = label.getText();
                    long count = statusCounts.getOrDefault(status, 0L);
                    double percent = total == 0 ? 0 : (count * 100.0 / total);

                    // Set legend text: e.g., "Pending (33.3% | 10)"
                    label.setText(String.format("%s (%.1f%% | %d)", status, percent, count));

                    String color = statusColors.getOrDefault(status, "#bdc3c7");

                    // The symbol is stored as label.getGraphic()
                    Node graphic = label.getGraphic();
                    if (graphic != null) {

                        // Case 1: Region (most common)
                        if (graphic instanceof Region region) {
                            region.setStyle("-fx-background-color: " + color + ";");
                        }

                        // Case 2: Shape (rare)
                        else if (graphic instanceof Shape shape) {
                            shape.setFill(Paint.valueOf(color));
                            shape.setStroke(Paint.valueOf(color));
                        }

                        // Case 3: Fallback
                        else {
                            graphic.setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
                        }
                    }
                }
            }
        });

    }
    public void addItems(MFXComboBox<String> combo, String... items) {
        combo.getItems().addAll(items);
    }

    public void setupreservationreports() {
       addItems(StatusfilterResrep,"Reserved","Complete","Cancelled","Pending","No Show","Seated");

        StatusfilterResrep.setText("Show All");

        referenceResrep.setCellValueFactory(new PropertyValueFactory<>("reference"));
        paxResrep.setCellValueFactory(new PropertyValueFactory<>("pax"));
        statusResrep.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeResrep.setCellValueFactory(new PropertyValueFactory<>("reservationPendingtime"));
        dateResrep.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Wrap list in FilteredList
        filterReservationReports = new FilteredList<>(reservationreports, p -> true);
        ResRepTable.setItems(filterReservationReports);

        // Apply button filters
        ApplyResrep.setOnAction(e -> {
            LocalDate from = dateFromResrep.getValue();
            LocalDate to = dateToResrep.getValue();
            String selectedStatus = StatusfilterResrep.getText();

            filterReservationReports.setPredicate(item -> {

                // Date filtering
                if (from != null && item.getDate().isBefore(from))
                    return false;
                if (to != null && item.getDate().isAfter(to))
                    return false;

                // Status filtering
                if (!"Show All".equals(selectedStatus)) {
                    if (item.getStatus() == null ||
                            !item.getStatus().equalsIgnoreCase(selectedStatus))
                        return false;
                }

                return true;
            });
            updateReservationPieChart();

        });

    }

    public void setupCustomerReports() {

        totalrevenueCusrep.setCellFactory(col -> new TableCell<CustomerReportDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
        averageCusrep.setCellFactory(col -> new TableCell<CustomerReportDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });



        TableColumn<?, ?>[] column = {phoneCusrep,totalreservationCusrep,totalrevenueCusrep,averageCusrep};
        double[] widthFactors = {0.25,0.25,0.25,0.25};
        String[] namecol = {"phone", "totalReservation", "totalRevenue", "averageRevenue"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(CusRepTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }


        CustomerReservationTable.setPlaceholder(new Label("No Customer Yet"));


        // Wrap list in FilteredList
        CusRepTable.setItems(filterCustomerReports);

        // Apply button filters
        ApplyCusrep.setOnAction(e -> loadCustomerReport());

        CusRepTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCustomer) -> {
            if (selectedCustomer != null) {
                String phone = selectedCustomer.getPhone();
                loadReservationInformation(phone);
            }
        });

    }

    public void setupReservatioInformation(){

        revenueResInCusRep.setCellFactory(col -> new TableCell<CustomerReportDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        ResInCusRep.setItems(reservationCustomerDTOS);
        TableColumn<?, ?>[] column = {referenceResInCusRep,nameResInCusRep,phoneResInCusRep,statusResInCusRep,revenueResInCusRep,timeResInCusRep,dateResInCusRep};
        double[] widthFactors = {0.2,0.25,0.2,0.15,0.15,0.2,0.2};
        String[] namecol = {"reference", "customerName", "customerPhone", "status", "revenue", "reservationPendingtime","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(ResInCusRep.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

    }

    public void setupRevenueReports(){
       totalrevenueRevrep.setCellFactory(col -> new TableCell<RevenueReportsDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        RevRepTable.setItems(RevenueReportDTOS);
        TableColumn<?, ?>[] column = {dateRevrep,totalreservationRevrep,totalcustomerRevrep,totalrevenueRevrep};
        double[] widthFactors = {0.25,0.25,0.25,0.25};
        String[] namecol = {"date", "totalReservation", "totalCustomer", "totalRevenue"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(RevRepTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        ApplyRevrep.setOnAction(e -> {
            loadRevenueReport(); ;
        });

    }
    public void setupTableUsageReport(){

        TableUseRep.setItems(TableUsageReportDTOS);
        TableColumn<?, ?>[] column = {tablenoTableUseRep,totalreservationTableUseRep,totalcusotmerTableUseRep,totalrevenueTableUseRep};
        double[] widthFactors = {0.25,0.25,0.25,0.25};
        String[] namecol = {"tableNo", "totalReservation", "totalCustomer", "totalRevenue"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(TableUseRep.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        TableUseRep.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ApplyTUrep.setOnAction(e -> {
            loadTableUsageReport(); ;
        });
        TableUseRep.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadTableUsageInfo(newValue.getTableNo());
                System.out.println("newvalue");

                if(!tableInfopane.isManaged()&& !tableInfopane.isVisible()) {
                    tableInfopane.setManaged(true);
                    tableInfopane.setVisible(true);

                }

            }
        });



    }

    private void loadReservationReports() {
        // Fetch all reservations (or filtered ones if you want)
        List<Reservation> reservations = reservationRepository.findAll();

        // Convert to ObservableList for JavaFX TableView
        reservationreports.setAll(reservations);
        updateReservationPieChart();

    }

    private void loadCustomerReport() {
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();

        List<CustomerReportDTO> results = reservationRepository.getCustomerReport(from, to);

        filterCustomerReports.setPredicate(null);
        customerreports.setAll(results);
    }
    private void loadReservationInformation(String phone){
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();

        List<ReservationCustomerDTO> results = reservationRepository.getReservationCustomerDTOByPhoneAndDate(phone,from, to);

        reservationCustomerDTOS.setAll(results);

    }

    private void loadRevenueReport(){
        LocalDate from = dateFromRevrep.getValue();
        LocalDate to = dateToRevrep.getValue();

        List<RevenueReportsDTO> results = reservationRepository.getRevenueReports(from, to);

        RevenueReportDTOS.setAll(results);
        loadRevenueBarCharts(results,from,to);

    }
    public void loadRevenueBarCharts(List<RevenueReportsDTO> data, LocalDate dateFrom, LocalDate dateTo) {

        // Clear previous chart data
        totalReservationChart.getData().clear();
        totalCustomerChart.getData().clear();
        totalRevenueChart.getData().clear();

        if (data == null || data.isEmpty()) {
            return; // nothing to show
        }

        // Determine date range
        LocalDate minDate = data.stream().map(RevenueReportsDTO::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = data.stream().map(RevenueReportsDTO::getDate).max(LocalDate::compareTo).orElse(LocalDate.now());

        if (dateFrom == null) dateFrom = minDate;
        if (dateTo == null) dateTo = maxDate;

        // Generate all dates in range
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate current = dateFrom;
        while (!current.isAfter(dateTo)) {
            allDates.add(current);
            current = current.plusDays(1);
        }

        // Map DTOs by date for fast lookup
        Map<LocalDate, RevenueReportsDTO> dataMap = new HashMap<>();
        for (RevenueReportsDTO r : data) {
            dataMap.put(r.getDate(), r);
        }

        // Formatter for X-axis labels
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // Optional: limit number of tick labels for readability
        int maxTicks = 20;
        int step = Math.max(1, allDates.size() / maxTicks);

        // Prepare chart series
        XYChart.Series<String, Number> totalReservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> totalCustomerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> totalRevenueSeries = new XYChart.Series<>();

        for (int i = 0; i < allDates.size(); i++) {
            LocalDate d = allDates.get(i);
            RevenueReportsDTO r = dataMap.getOrDefault(d, new RevenueReportsDTO(d, 0L, 0L, 0.0));

            // Format date label
            String dateStr = (i % step == 0) ? d.format(formatter) : ""; // only show some labels

            totalReservationSeries.getData().add(new XYChart.Data<>(dateStr, r.getTotalReservation()));
            totalCustomerSeries.getData().add(new XYChart.Data<>(dateStr, r.getTotalCustomer()));
            totalRevenueSeries.getData().add(new XYChart.Data<>(dateStr, r.getTotalRevenue()));
        }

        // Add series to charts
        totalReservationChart.getData().clear();
        totalCustomerChart.getData().forEach(series -> series.getData().clear());
        totalRevenueChart.getData().clear();

        totalReservationChart.setLegendVisible(false);
        totalCustomerChart.setLegendVisible(false);
        totalRevenueChart.setLegendVisible(false);

        totalReservationChart.getData().add(totalReservationSeries);
        totalCustomerChart.getData().add(totalCustomerSeries);
        totalRevenueChart.getData().add(totalRevenueSeries);

        // Optional: rotate X-axis labels for better readability
        ((javafx.scene.chart.CategoryAxis) totalReservationChart.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) totalCustomerChart.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) totalRevenueChart.getXAxis()).setTickLabelRotation(45);
    }
    public void loadTableUsageReport(){
        /*if(dateFromTUrep.getValue() == null){
            dateFromTUrep.setValue(LocalDate.now());
        }
        if(dateToTUrep.getValue() == null){
            dateToTUrep.setValue(LocalDate.now());
        }*/
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();
        List<TableUsageReportDTO> results = RTLR.getTableUsageReport(from,to);
        TableUsageReportDTOS.setAll(results);
        loadTableUsageBarCharts(results);
    }

    public void setupTableUsageInfo(){

        TableinfoTUrep.setItems(TableUsageInformationDTOS);
        TableColumn<?, ?>[] column = {tablenoTableinfo,referenceTableinfo,paxTableinfo,revenueTableinfo,timeTableinfo,dateTableinfo};
        double[] widthFactors = {0.10,0.25,0.10,0.15,0.20,0.20};
        String[] namecol = {"tableNo", "reference", "pax", "revenue","time","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(TableinfoTUrep.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

    }

    public void loadTableUsageInfo(String tableno){
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();
        List<TableUsageInformationDTO> results = RTLR.getTableUsageInfo(from,to,tableno);
        TableUsageInformationDTOS.setAll(results);


    }

    public void loadTableUsageBarCharts(List<TableUsageReportDTO> data) {

        // Clear previous chart data
        totalReservationChartTableUsage.getData().clear();
        totalCustomerChartTableUsage.getData().clear();
        totalRevenueChartTableUsage.getData().clear();

        if (data == null || data.isEmpty()) {
            return; // nothing to show
        }

        // Prepare chart series
        XYChart.Series<String, Number> totalReservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> totalCustomerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> totalRevenueSeries = new XYChart.Series<>();

        for (TableUsageReportDTO r : data) {
            String tableNo = r.getTableNo(); // X-axis: table number

            totalReservationSeries.getData().add(new XYChart.Data<>(tableNo, r.getTotalReservation()));
            totalCustomerSeries.getData().add(new XYChart.Data<>(tableNo, r.getTotalCustomer()));
            totalRevenueSeries.getData().add(new XYChart.Data<>(tableNo, r.getTotalRevenue()));
        }


        totalReservationChartTableUsage.setLegendVisible(false);
        totalCustomerChartTableUsage.setLegendVisible(false);
        totalRevenueChartTableUsage.setLegendVisible(false);

        totalReservationChartTableUsage.getData().add(totalReservationSeries);
        totalCustomerChartTableUsage.getData().add(totalCustomerSeries);
        totalRevenueChartTableUsage.getData().add(totalRevenueSeries);

        // Optional: rotate X-axis labels for better readability
        ((javafx.scene.chart.CategoryAxis) totalReservationChart.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) totalCustomerChart.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) totalRevenueChart.getXAxis()).setTickLabelRotation(45);
    }

    public void loadReports(){
        Reservationrpts.fire();
        loadReservationReports();
        loadCustomerReport();
        loadRevenueReport();
        loadTableUsageReport();
        tableInfopane.setManaged(false);
        tableInfopane.setVisible(false);
    }


    private void expand(BarChart<?, ?> target, VBox rootVBox) {
        // Heights and animation duration defined inside the method
        double normalHeight = 200;
        double hoverHeight = 400;
        double shrinkHeight = 120;
        Duration animationDuration = Duration.millis(300);

        for (Node node : rootVBox.getChildren()) {
            if (node instanceof BarChart) {
                BarChart<?, ?> chart = (BarChart<?, ?>) node;
                double targetHeight = (chart == target) ? hoverHeight : shrinkHeight;

                Timeline timeline = new Timeline(
                        new KeyFrame(animationDuration,
                                new KeyValue(chart.prefHeightProperty(), targetHeight))
                );
                timeline.play();

                VBox.setVgrow(chart, (chart == target) ? Priority.ALWAYS : Priority.NEVER);
            }
        }
    }

    private void resetSizes(VBox rootVBox) {
        // Heights and animation duration defined inside the method
        double normalHeight = 200;
        Duration animationDuration = Duration.millis(300);

        for (Node node : rootVBox.getChildren()) {
            if (node instanceof BarChart) {
                BarChart<?, ?> chart = (BarChart<?, ?>) node;

                Timeline timeline = new Timeline(
                        new KeyFrame(animationDuration,
                                new KeyValue(chart.prefHeightProperty(), normalHeight))
                );
                timeline.play();

                VBox.setVgrow(chart, Priority.ALWAYS);
            }
        }
    }

    private void setupHoverExpand(BarChart<?, ?> chart, VBox root) {
        chart.setOnMouseEntered(e -> expand(chart,root));
        chart.setOnMouseExited(e -> resetSizes(root));

    }





    @Override
    public void initialize(URL location, ResourceBundle resources) {


        dashpane.minHeightProperty().bind(dashpane.widthProperty().multiply(0.965));
        reservpane.minHeightProperty().bind(reservpane.widthProperty().multiply(1.456));
        tablepane.minHeightProperty().bind(tablepane.widthProperty().multiply(0.95));
        //accountpane.minHeightProperty().bind(accountpane.widthProperty().multiply());
        Dashboardbtn.fire();

        setupRecentReservation();
        setupTableView();
        setupCustomerReservationTable();
        setupTableManager();
        setupAvailableTable();
        setupReservationLogs();
        setupSCNReservation();
        setupTableHistory();
        setupAccountTable();
        setupreservationreports();
        setupCustomerReports();
        setupReservatioInformation();
        setupRevenueReports();
        setupTableUsageReport();
        setupTableUsageInfo();
        setupActivityLogs();
        //ActivityLogsTable.setItems(activitylogsdata);
        setupHoverExpand(totalReservationChart,rootVBox);
        setupHoverExpand(totalCustomerChart,rootVBox);
        setupHoverExpand(totalRevenueChart,rootVBox);
        setupHoverExpand(totalReservationChartTableUsage,rootVBoxTableUsage);
        setupHoverExpand(totalCustomerChartTableUsage,rootVBoxTableUsage);
        setupHoverExpand(totalRevenueChartTableUsage,rootVBoxTableUsage);




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
    public void onNewReservation(WebupdateDTO reservation) {
        Platform.runLater(() -> {
            updateLabels();
            handleClick();
            loadRecentReservations();
            loadCustomerReservationTable();
        });
    }

}
