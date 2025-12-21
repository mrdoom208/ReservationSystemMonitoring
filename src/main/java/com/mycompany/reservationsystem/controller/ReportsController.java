package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.Service.ReservationService;
import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.repository.ReservationTableLogsRepository;
import com.mycompany.reservationsystem.transition.ChartsTransition;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyStatusStyle;

@Component
public class ReportsController {
    // ====================== FXML Controls ======================

    // --- Buttons ---
    @FXML
    private MFXButton ApplyCusrep, ApplyResrep, ApplyRevrep, ApplyTUrep,
            Customerrpts, Reservationrpts, Revenuerpts, TableUsagerpts;

    // --- ComboBoxes ---
    @FXML
    private MFXComboBox StatusfilterResrep;

    // --- DatePickers ---
    @FXML
    private MFXDatePicker dateFromCusrep, dateToCusrep,
            dateFromResrep, dateToResrep,
            dateFromRevrep, dateToRevrep,
            dateFromTUrep, dateToTUrep;

    // --- Layouts ---
    @FXML
    private ScrollPane ReportsPane;
    @FXML
    private HBox CustomerReport, ReservationReport, RevenueReport;
    @FXML
    private GridPane TableUsageReport;
    @FXML
    private VBox rootVBox, rootVBoxTableUsage, tableInfopane;

    // --- Charts ---
    @FXML
    private PieChart reservationPieChart;
    @FXML
    private BarChart<String, Number> totalCustomerChart, totalReservationChart, totalRevenueChart,
            totalCustomerChartTableUsage, totalReservationChartTableUsage, totalRevenueChartTableUsage;

// ====================== TableViews ======================

    @FXML
    private TableView<CustomerReportDTO> CusRepTable;
    @FXML
    private TableView<Reservation> ResRepTable;
    @FXML
    private TableView<ReservationCustomerDTO> ResInCusRep;
    @FXML
    private TableView<RevenueReportsDTO> RevRepTable;
    @FXML
    private TableView<TableUsageReportDTO> TableUseRep;
    @FXML
    private TableView<TableUsageInformationDTO> TableinfoTUrep;

// ====================== TableColumns ======================

    // --- Customer Report ---
    @FXML
    private TableColumn<CustomerReportDTO, Double> averageCusrep;
    @FXML
    private TableColumn<CustomerReportDTO, BigDecimal> totalrevenueCusrep, revenueResInCusRep;
    @FXML
    private TableColumn<?, ?> phoneCusrep, totalreservationCusrep;

    // --- Reservation Report ---
    @FXML
    private TableColumn<?, ?> dateResrep, timeResrep, paxResrep,
            referenceResrep, totalreservationRevrep;
    @FXML
    private TableColumn<?, String> statusResrep;

    // --- Reservation in Customer Report ---
    @FXML
    private TableColumn<?, ?> nameResInCusRep, phoneResInCusRep,
            referenceResInCusRep, dateResInCusRep, timeResInCusRep;
    @FXML
    private TableColumn<?, String> statusResInCusRep;

    // --- Revenue Report ---
    @FXML
    private TableColumn<RevenueReportsDTO, BigDecimal> totalrevenueRevrep;
    @FXML
    private TableColumn<?, ?> dateRevrep, totalcustomerRevrep;

    // --- Table Usage Report ---
    @FXML
    private TableColumn<TableUsageReportDTO, BigDecimal> totalrevenueTableUseRep;
    @FXML
    private TableColumn<?, ?> tablenoTableUseRep, totalcusotmerTableUseRep,
            totalreservationTableUseRep;

    // --- Table Usage Info ---
    @FXML
    private TableColumn<?, ?> tablenoTableinfo, paxTableinfo, dateTableinfo,
            timeTableinfo, referenceTableinfo, revenueTableinfo;

// ====================== Spring Dependencies ======================

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTableLogsRepository RTLR;
    @Autowired
    private ReservationService reservationService;

// ====================== Data Models ======================

    private final ObservableList<Reservation> reservationreports = FXCollections.observableArrayList();
    private final ObservableList<CustomerReportDTO> customerreports = FXCollections.observableArrayList();
    private final ObservableList<ReservationCustomerDTO> reservationCustomerDTOS = FXCollections.observableArrayList();
    private final ObservableList<RevenueReportsDTO> RevenueReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageReportDTO> TableUsageReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageInformationDTO> TableUsageInformationDTOS = FXCollections.observableArrayList();

// ====================== Filters ======================

    private FilteredList<Reservation> filterReservationReports =
            new FilteredList<>(reservationreports, p -> true);

    private final FilteredList<CustomerReportDTO> filterCustomerReports =
            new FilteredList<>(customerreports, p -> true);

// ====================== Utilities ======================

    private final ChartsTransition chartsTransition = new ChartsTransition();

// ====================== Load Flags (Caching) ======================

    private boolean reservationLoaded,
            customerLoaded,
            revenueLoaded,
            tableUsageLoaded;



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
                if (!reservationLoaded) {
                    loadReservationReports();
                    reservationLoaded = true;
                }
                break;

            case "Customerrpts":
                CustomerReport.setVisible(true);
                if (!customerLoaded) {
                    loadCustomerReport();
                    customerLoaded = true;
                }
                break;

            case "Revenuerpts":
                RevenueReport.setVisible(true);
                if (!revenueLoaded) {
                    loadRevenueReport();
                    revenueLoaded = true;
                }
                break;

            case "TableUsagerpts":
                TableUsageReport.setVisible(true);
                if (!tableUsageLoaded) {
                    loadTableUsageReport();
                    tableUsageLoaded = true;
                }
                break;

            default:
                break;
        }


    }

    private void updateReservationPieChart() {
        Map<String, Long> statusCounts = filterReservationReports.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        Map<String, String> statusColors = Map.of(
                "Pending", "#455A64",
                "Confirm", "#2196F3",
                "Cancelled", "#D32F2F",
                "Seated", "#2E7D32",
                "Complete", "#4CAF50",
                "No Show","#9C27B0"
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
        addItems(StatusfilterResrep,"Complete","Cancelled","Pending","No Show","Seated","Show All");

        StatusfilterResrep.setText("Show All");
        applyStatusStyle(statusResrep);
        TableColumn<?, ?>[] column = {referenceResrep,paxResrep,statusResrep,timeResrep,dateResrep};
        double[] widthFactors = {0.2,0.2,0.2,0.2,0.21};
        String[] namecol = {"reference", "pax", "status", "reservationPendingtime","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(ResRepTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        // Wrap list in FilteredList
        filterReservationReports = new FilteredList<>(reservationreports, p -> true);
        ResRepTable.setItems(filterReservationReports);

        // Apply button filters
        ApplyResrep.setOnAction(e -> {
            loadReservationReports();
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

        totalrevenueCusrep.setCellFactory(col -> new TableCell<CustomerReportDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
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
        applyStatusStyle(statusResInCusRep);
        revenueResInCusRep.setCellFactory(col -> new TableCell<CustomerReportDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        ResInCusRep.setItems(reservationCustomerDTOS);
        TableColumn<?, ?>[] column = {referenceResInCusRep,nameResInCusRep,phoneResInCusRep,statusResInCusRep,revenueResInCusRep,timeResInCusRep,dateResInCusRep};
        double[] widthFactors = {0.2,0.25,0.2,0.18,0.15,0.2,0.2};
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
        totalrevenueRevrep.setCellFactory(col -> new TableCell<RevenueReportsDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
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


    private int customerReportPage = 0;
    private int pageSize = 100;
    private boolean allDataLoaded = false;

    private void loadCustomerReport() {
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();

        if (from == null && to == null) {
            // Pageable mode → reset ONCE
            customerReportPage = 0;
            allDataLoaded = false;
            customerreports.clear();
            CusRepTable.setItems(filterCustomerReports);

            loadCustomerReportPage();   // 👈 call page loader
        } else {
            loadCustomerReportFiltered(from, to);
        }
    }

    private void loadCustomerReportPage() {
        if (allDataLoaded) return;
        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return reservationService.loadPage(customerReportPage, pageSize);
            }
        };
        task.setOnSucceeded(e -> {
            List<CustomerReportDTO> results = task.getValue();

            if (results.isEmpty()) {
                allDataLoaded = true;
                return;
            }

            customerreports.addAll(results);
            customerReportPage++; // move to next page
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }

    private void loadCustomerReportFiltered(LocalDate from, LocalDate to) {

        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return reservationService.loadByDate(from, to);
            }
        };

        task.setOnSucceeded(e -> {
            CusRepTable.setItems(
                    FXCollections.observableArrayList(task.getValue())
            );
            allDataLoaded = true; // disable paging
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
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

        XYChart.Series<String, Number> totalReservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> totalCustomerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> totalRevenueSeries = new XYChart.Series<>();

        for (int i = 0; i < allDates.size(); i++) {
            LocalDate d = allDates.get(i);
            RevenueReportsDTO r = dataMap.getOrDefault(d, new RevenueReportsDTO(d, 0L, 0L, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));

            String dateStr = (i % step == 0) ? d.format(formatter) : "";

            totalReservationSeries.getData().add(new XYChart.Data<>(dateStr, r.getTotalReservation()));
            totalCustomerSeries.getData().add(new XYChart.Data<>(dateStr, r.getTotalCustomer()));
            totalRevenueSeries.getData().add(new XYChart.Data<>(dateStr, r.getTotalRevenue()));
        }

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


    @FXML
    private void initialize(){
        Reservationrpts.fire();

        setupreservationreports();
        setupCustomerReports();
        setupReservatioInformation();
        setupRevenueReports();
        setupTableUsageReport();
        setupTableUsageInfo();

        /*----------------Transition-------------------------*/
        chartsTransition.setupHoverExpand(totalReservationChart,rootVBox);
        chartsTransition.setupHoverExpand(totalCustomerChart,rootVBox);
        chartsTransition.setupHoverExpand(totalRevenueChart,rootVBox);
        chartsTransition.setupHoverExpand(totalReservationChartTableUsage,rootVBoxTableUsage);
        chartsTransition.setupHoverExpand(totalCustomerChartTableUsage,rootVBoxTableUsage);
        chartsTransition.setupHoverExpand(totalRevenueChartTableUsage,rootVBoxTableUsage);



    }
}
