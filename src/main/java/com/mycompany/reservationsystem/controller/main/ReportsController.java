package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.service.ReservationService;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.mycompany.reservationsystem.transition.NodeTransition.showSmooth;
import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.*;

@Component
public class ReportsController {

    // ====================== Constants ======================
    private static final int PAGE_SIZE = 100;
    private static final int MAX_CHART_TICKS = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private static final Map<String, String> STATUS_COLORS = Map.of(
            "Pending", "#455A64",
            "Confirm", "#2196F3",
            "Cancelled", "#D32F2F",
            "Seated", "#2E7D32",
            "Complete", "#4CAF50",
            "No Show", "#9C27B0"
    );

    // ====================== FXML Controls ======================
    @FXML private MFXButton ApplyCusrep, ApplyResrep, ApplyRevrep, ApplyTUrep;
    @FXML private MFXButton Customerrpts, Reservationrpts, Revenuerpts, TableUsagerpts;
    @FXML private MFXComboBox<String> StatusfilterResrep;

    @FXML private MFXDatePicker dateFromCusrep, dateToCusrep;
    @FXML private MFXDatePicker dateFromResrep, dateToResrep;
    @FXML private MFXDatePicker dateFromRevrep, dateToRevrep;
    @FXML private MFXDatePicker dateFromTUrep, dateToTUrep;

    @FXML private ScrollPane ReportsPane;
    @FXML private HBox CustomerReport, ReservationReport, RevenueReport;
    @FXML private GridPane TableUsageReport;
    @FXML private VBox rootVBox, rootVBoxTableUsage, tableInfopane;

    @FXML private PieChart reservationPieChart;
    @FXML private BarChart<String, Number> totalCustomerChart, totalReservationChart, totalRevenueChart;
    @FXML private BarChart<String, Number> totalCustomerChartTableUsage, totalReservationChartTableUsage, totalRevenueChartTableUsage;

    // ====================== TableViews ======================
    @FXML private TableView<CustomerReportDTO> CusRepTable;
    @FXML private TableView<Reservation> ResRepTable;
    @FXML private TableView<ReservationCustomerDTO> ResInCusRep;
    @FXML private TableView<RevenueReportsDTO> RevRepTable;
    @FXML private TableView<TableUsageReportDTO> TableUseRep;
    @FXML private TableView<TableUsageInformationDTO> TableinfoTUrep;

    // ====================== TableColumns ======================
    @FXML private TableColumn<CustomerReportDTO, Double> averageCusrep;
    @FXML private TableColumn<CustomerReportDTO, BigDecimal> totalrevenueCusrep, revenueResInCusRep;
    @FXML private TableColumn<CustomerReportDTO, ?> phoneCusrep, totalreservationCusrep;

    @FXML private TableColumn<?, ?> dateResrep, paxResrep, referenceResrep, totalreservationRevrep;
    @FXML private TableColumn<?, String> statusResrep;
    @FXML private TableColumn<?, LocalTime> timeResrep;

    @FXML private TableColumn<?, ?> nameResInCusRep, phoneResInCusRep, referenceResInCusRep, dateResInCusRep;
    @FXML private TableColumn<?, String> statusResInCusRep;
    @FXML private TableColumn<?, LocalTime> timeResInCusRep;

    @FXML private TableColumn<RevenueReportsDTO, BigDecimal> totalrevenueRevrep;
    @FXML private TableColumn<?, ?> dateRevrep, totalcustomerRevrep;

    @FXML private TableColumn<TableUsageReportDTO, BigDecimal> totalrevenueTableUseRep;
    @FXML private TableColumn<?, ?> tablenoTableUseRep, totalcusotmerTableUseRep, totalreservationTableUseRep;

    @FXML private TableColumn<?, ?> tablenoTableinfo, paxTableinfo, dateTableinfo, referenceTableinfo, revenueTableinfo;
    @FXML private TableColumn<?, LocalTime> timeTableinfo;

    // ====================== Spring Dependencies ======================
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationTableLogsRepository RTLR;
    @Autowired private ReservationService reservationService;

    // ====================== Data Models ======================
    private final ObservableList<Reservation> reservationReports = FXCollections.observableArrayList();
    private final ObservableList<CustomerReportDTO> customerReports = FXCollections.observableArrayList();
    private final ObservableList<ReservationCustomerDTO> reservationCustomerDTOS = FXCollections.observableArrayList();
    private final ObservableList<RevenueReportsDTO> revenueReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageReportDTO> tableUsageReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageInformationDTO> tableUsageInformationDTOS = FXCollections.observableArrayList();

    // ====================== Filters ======================
    private FilteredList<Reservation> filterReservationReports =
            new FilteredList<>(reservationReports, p -> true);
    private final FilteredList<CustomerReportDTO> filterCustomerReports =
            new FilteredList<>(customerReports, p -> true);

    // ====================== Utilities ======================
    private final ChartsTransition chartsTransition = new ChartsTransition();

    // ====================== State Management ======================
    private boolean reservationLoaded, customerLoaded, revenueLoaded, tableUsageLoaded;
    private int customerReportPage = 0;
    private boolean allDataLoaded = false;

    // ====================== Initialization ======================
    @FXML
    private void initialize() {
        Reservationrpts.fire();

        setupAllReports();
        setupChartTransitions();
    }

    private void setupAllReports() {
        setupReservationReports();
        setupCustomerReports();
        setupReservationInformation();
        setupRevenueReports();
        setupTableUsageReport();
        setupTableUsageInfo();
    }

    private void setupChartTransitions() {
        chartsTransition.setupHoverExpand(totalReservationChart, rootVBox);
        chartsTransition.setupHoverExpand(totalCustomerChart, rootVBox);
        chartsTransition.setupHoverExpand(totalRevenueChart, rootVBox);
        chartsTransition.setupHoverExpand(totalReservationChartTableUsage, rootVBoxTableUsage);
        chartsTransition.setupHoverExpand(totalCustomerChartTableUsage, rootVBoxTableUsage);
        chartsTransition.setupHoverExpand(totalRevenueChartTableUsage, rootVBoxTableUsage);
    }

    // ====================== Navigation ======================
    @FXML
    private void navigateReports(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        hideAllReports();
        updateNavigationButtons(clicked);
        showSelectedReport(clicked.getId());
    }

    private void hideAllReports() {
        ReservationReport.setVisible(false);
        TableUsageReport.setVisible(false);
        RevenueReport.setVisible(false);
        CustomerReport.setVisible(false);
    }

    private void updateNavigationButtons(Button activeButton) {
        Button[] buttons = {Reservationrpts, Customerrpts, Revenuerpts, TableUsagerpts};

        for (Button btn : buttons) {
            if (btn == null) continue;

            btn.getStyleClass().removeAll("navigation-report-btns-active");
            if (!btn.getStyleClass().contains("navigation-report-btns")) {
                btn.getStyleClass().add("navigation-report-btns");
            }
        }

        activeButton.getStyleClass().removeAll("navigation-report-btns");
        if (!activeButton.getStyleClass().contains("navigation-report-btns-active")) {
            activeButton.getStyleClass().add("navigation-report-btns-active");
        }
    }

    private void showSelectedReport(String reportId) {
        switch (reportId) {
            case "Reservationrpts":
                showSmooth(ReservationReport);
                loadReportIfNeeded(() -> loadReservationReports(), () -> reservationLoaded, flag -> reservationLoaded = flag);
                break;

            case "Customerrpts":
                showSmooth(CustomerReport);
                loadReportIfNeeded(() -> loadCustomerReport(), () -> customerLoaded, flag -> customerLoaded = flag);
                break;

            case "Revenuerpts":
                showSmooth(RevenueReport);
                loadReportIfNeeded(() -> loadRevenueReport(), () -> revenueLoaded, flag -> revenueLoaded = flag);
                break;

            case "TableUsagerpts":
                showSmooth(TableUsageReport);
                loadReportIfNeeded(() -> loadTableUsageReport(), () -> tableUsageLoaded, flag -> tableUsageLoaded = flag);
                break;
        }
    }

    private void loadReportIfNeeded(Runnable loader, java.util.function.Supplier<Boolean> isLoaded,
                                    java.util.function.Consumer<Boolean> setLoaded) {
        if (!isLoaded.get()) {
            loader.run();
            setLoaded.accept(true);
        }
    }

    // ====================== Reservation Reports ======================
    private void setupReservationReports() {
        StatusfilterResrep.getItems().addAll("Complete", "Cancelled", "Pending", "No Show", "Seated", "Show All");
        StatusfilterResrep.setText("Show All");

        applyStatusStyle(statusResrep);
        applyTimeFormat(timeResrep);

        setupTableColumns(ResRepTable,
                new TableColumn[]{referenceResrep, paxResrep, statusResrep, timeResrep, dateResrep},
                new double[]{0.2, 0.2, 0.2, 0.2, 0.21},
                new String[]{"reference", "pax", "status", "reservationPendingtime", "date"});

        ResRepTable.setItems(filterReservationReports);

        ApplyResrep.setOnAction(e -> applyReservationFilters());
    }

    private void applyReservationFilters() {
        loadReservationReports();

        LocalDate from = dateFromResrep.getValue();
        LocalDate to = dateToResrep.getValue();
        String selectedStatus = StatusfilterResrep.getText();

        filterReservationReports.setPredicate(item ->
                isDateInRange(item.getDate(), from, to) &&
                        matchesStatus(item.getStatus(), selectedStatus)
        );

        updateReservationPieChart();
    }

    private boolean isDateInRange(LocalDate date, LocalDate from, LocalDate to) {
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private boolean matchesStatus(String itemStatus, String selectedStatus) {
        if ("Show All".equals(selectedStatus)) return true;
        return itemStatus != null && itemStatus.equalsIgnoreCase(selectedStatus);
    }

    private void loadReservationReports() {
        List<Reservation> reservations = reservationRepository.findAll();
        reservationReports.setAll(reservations);
        updateReservationPieChart();
    }

    private void updateReservationPieChart() {
        Map<String, Long> statusCounts = filterReservationReports.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        statusCounts.forEach((status, count) -> pieChartData.add(new PieChart.Data(status, count)));

        reservationPieChart.setData(pieChartData);
        applyPieChartColors(statusCounts, total);
    }

    private void applyPieChartColors(Map<String, Long> statusCounts, long total) {
        for (PieChart.Data data : reservationPieChart.getData()) {
            String color = STATUS_COLORS.getOrDefault(data.getName(), "#bdc3c7");
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }

        Platform.runLater(() -> updatePieChartLegend(statusCounts, total));
    }

    private void updatePieChartLegend(Map<String, Long> statusCounts, long total) {
        for (Node node : reservationPieChart.lookupAll(".chart-legend-item")) {
            if (node instanceof Label label) {
                String status = label.getText();
                long count = statusCounts.getOrDefault(status, 0L);
                double percent = total == 0 ? 0 : (count * 100.0 / total);

                label.setText(String.format("%s (%.1f%% | %d)", status, percent, count));

                String color = STATUS_COLORS.getOrDefault(status, "#bdc3c7");
                applyColorToLegendSymbol(label.getGraphic(), color);
            }
        }
    }

    private void applyColorToLegendSymbol(Node graphic, String color) {
        if (graphic == null) return;

        if (graphic instanceof Region region) {
            region.setStyle("-fx-background-color: " + color + ";");
        } else if (graphic instanceof Shape shape) {
            shape.setFill(Paint.valueOf(color));
            shape.setStroke(Paint.valueOf(color));
        } else {
            graphic.setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
        }
    }

    // ====================== Customer Reports ======================
    private void setupCustomerReports() {
        applyDecimalFormat(totalrevenueCusrep, 2);
        applyDecimalFormat(averageCusrep, 2);

        setupTableColumns(CusRepTable,
                new TableColumn[]{phoneCusrep, totalreservationCusrep, totalrevenueCusrep, averageCusrep},
                new double[]{0.25, 0.25, 0.25, 0.25},
                new String[]{"phone", "totalReservation", "totalRevenue", "averageRevenue"});

        CusRepTable.setItems(filterCustomerReports);
        ApplyCusrep.setOnAction(e -> loadCustomerReport());

        CusRepTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadReservationInformation(newSelection.getPhone());
            }
        });
    }

    private void loadCustomerReport() {
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();

        if (from == null && to == null) {
            resetPagination();
            loadCustomerReportPage();
        } else {
            loadCustomerReportFiltered(from, to);
        }
    }

    private void resetPagination() {
        customerReportPage = 0;
        allDataLoaded = false;
        customerReports.clear();
        CusRepTable.setItems(filterCustomerReports);
    }

    private void loadCustomerReportPage() {
        if (allDataLoaded) return;

        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return reservationService.loadPage(customerReportPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            List<CustomerReportDTO> results = task.getValue();
            if (results.isEmpty()) {
                allDataLoaded = true;
                return;
            }
            customerReports.addAll(results);
            customerReportPage++;
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
            CusRepTable.setItems(FXCollections.observableArrayList(task.getValue()));
            allDataLoaded = true;
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    // ====================== Reservation Information ======================
    private void setupReservationInformation() {
        applyStatusStyle(statusResInCusRep);
        applyTimeFormat(timeResInCusRep);
        applyDecimalFormat(revenueResInCusRep, 2);

        setupTableColumns(ResInCusRep,
                new TableColumn[]{referenceResInCusRep, nameResInCusRep, phoneResInCusRep, statusResInCusRep,
                        revenueResInCusRep, timeResInCusRep, dateResInCusRep},
                new double[]{0.2, 0.25, 0.2, 0.18, 0.15, 0.2, 0.2},
                new String[]{"reference", "customerName", "customerPhone", "status", "revenue",
                        "reservationPendingtime", "date"});

        ResInCusRep.setItems(reservationCustomerDTOS);
    }

    private void loadReservationInformation(String phone) {
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();

        List<ReservationCustomerDTO> results =
                reservationRepository.getReservationCustomerDTOByPhoneAndDate(phone, from, to);
        reservationCustomerDTOS.setAll(results);
    }

    // ====================== Revenue Reports ======================
    private void setupRevenueReports() {
        applyDecimalFormat(totalrevenueRevrep, 2);

        setupTableColumns(RevRepTable,
                new TableColumn[]{dateRevrep, totalreservationRevrep, totalcustomerRevrep, totalrevenueRevrep},
                new double[]{0.25, 0.25, 0.25, 0.25},
                new String[]{"date", "totalReservation", "totalCustomer", "totalRevenue"});

        RevRepTable.setItems(revenueReportDTOS);
        ApplyRevrep.setOnAction(e -> loadRevenueReport());
    }

    private void loadRevenueReport() {
        LocalDate from = dateFromRevrep.getValue();
        LocalDate to = dateToRevrep.getValue();

        List<RevenueReportsDTO> results = reservationRepository.getRevenueReports(from, to);
        revenueReportDTOS.setAll(results);
        loadRevenueBarCharts(results, from, to);
    }

    private void loadRevenueBarCharts(List<RevenueReportsDTO> data, LocalDate dateFrom, LocalDate dateTo) {
        clearCharts(totalReservationChart, totalCustomerChart, totalRevenueChart);

        if (data == null || data.isEmpty()) return;

        DateRange dateRange = calculateDateRange(data, dateFrom, dateTo);
        List<LocalDate> allDates = generateDateRange(dateRange.from, dateRange.to);
        Map<LocalDate, RevenueReportsDTO> dataMap = createDataMap(data);

        int step = Math.max(1, allDates.size() / MAX_CHART_TICKS);

        XYChart.Series<String, Number> reservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> customerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();

        for (int i = 0; i < allDates.size(); i++) {
            LocalDate date = allDates.get(i);
            RevenueReportsDTO dto = dataMap.getOrDefault(date, createEmptyRevenueDTO(date));
            String dateStr = (i % step == 0) ? date.format(DATE_FORMATTER) : "";

            reservationSeries.getData().add(new XYChart.Data<>(dateStr, dto.getTotalReservation()));
            customerSeries.getData().add(new XYChart.Data<>(dateStr, dto.getTotalCustomer()));
            revenueSeries.getData().add(new XYChart.Data<>(dateStr, dto.getTotalRevenue()));
        }

        populateCharts(totalReservationChart, totalCustomerChart, totalRevenueChart,
                reservationSeries, customerSeries, revenueSeries);
    }

    // ====================== Table Usage Reports ======================
    private void setupTableUsageReport() {
        setupTableColumns(TableUseRep,
                new TableColumn[]{tablenoTableUseRep, totalreservationTableUseRep,
                        totalcusotmerTableUseRep, totalrevenueTableUseRep},
                new double[]{0.25, 0.25, 0.25, 0.25},
                new String[]{"tableNo", "totalReservation", "totalCustomer", "totalRevenue"});

        TableUseRep.setItems(tableUsageReportDTOS);
        ApplyTUrep.setOnAction(e -> loadTableUsageReport());

        TableUseRep.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTableUsageInfo(newVal.getTableNo());
                showTableInfoPane();
            }
        });
    }

    private void showTableInfoPane() {
        if (!tableInfopane.isManaged() && !tableInfopane.isVisible()) {
            tableInfopane.setManaged(true);
            tableInfopane.setVisible(true);
        }
    }

    private void loadTableUsageReport() {
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();

        List<TableUsageReportDTO> results = RTLR.getTableUsageReport(from, to);
        tableUsageReportDTOS.setAll(results);
        loadTableUsageBarCharts(results);
    }

    private void setupTableUsageInfo() {
        applyTimeFormat(timeTableinfo);

        setupTableColumns(TableinfoTUrep,
                new TableColumn[]{tablenoTableinfo, referenceTableinfo, paxTableinfo,
                        revenueTableinfo, timeTableinfo, dateTableinfo},
                new double[]{0.10, 0.25, 0.10, 0.15, 0.20, 0.20},
                new String[]{"tableNo", "reference", "pax", "revenue", "time", "date"});

        TableinfoTUrep.setItems(tableUsageInformationDTOS);
    }

    private void loadTableUsageInfo(String tableNo) {
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();

        List<TableUsageInformationDTO> results = RTLR.getTableUsageInfo(from, to, tableNo);
        tableUsageInformationDTOS.setAll(results);
    }

    private void loadTableUsageBarCharts(List<TableUsageReportDTO> data) {
        clearCharts(totalReservationChartTableUsage, totalCustomerChartTableUsage, totalRevenueChartTableUsage);

        if (data == null || data.isEmpty()) return;

        XYChart.Series<String, Number> reservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> customerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();

        for (TableUsageReportDTO dto : data) {
            String tableNo = dto.getTableNo();
            reservationSeries.getData().add(new XYChart.Data<>(tableNo, dto.getTotalReservation()));
            customerSeries.getData().add(new XYChart.Data<>(tableNo, dto.getTotalCustomer()));
            revenueSeries.getData().add(new XYChart.Data<>(tableNo, dto.getTotalRevenue()));
        }

        populateCharts(totalReservationChartTableUsage, totalCustomerChartTableUsage,
                totalRevenueChartTableUsage, reservationSeries, customerSeries, revenueSeries);
    }

    // ====================== Utility Methods ======================
    private void setupTableColumns(TableView<?> table, TableColumn<?, ?>[] columns,
                                   double[] widthFactors, String[] propertyNames) {
        for (int i = 0; i < columns.length; i++) {
            TableColumn<?, ?> col = columns[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(table.widthProperty().multiply(widthFactors[i]));

            if (i < propertyNames.length && !propertyNames[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(propertyNames[i]));
            }
        }
    }

    private void clearCharts(BarChart<String, Number>... charts) {
        for (BarChart<String, Number> chart : charts) {
            chart.getData().clear();
        }
    }

    private void populateCharts(BarChart<String, Number> chart1, BarChart<String, Number> chart2,
                                BarChart<String, Number> chart3, XYChart.Series<String, Number> series1,
                                XYChart.Series<String, Number> series2, XYChart.Series<String, Number> series3) {
        chart1.setLegendVisible(false);
        chart2.setLegendVisible(false);
        chart3.setLegendVisible(false);

        chart1.getData().add(series1);
        chart2.getData().add(series2);
        chart3.getData().add(series3);

        ((javafx.scene.chart.CategoryAxis) chart1.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) chart2.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) chart3.getXAxis()).setTickLabelRotation(45);
    }

    private DateRange calculateDateRange(List<RevenueReportsDTO> data, LocalDate from, LocalDate to) {
        LocalDate minDate = data.stream().map(RevenueReportsDTO::getDate)
                .min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = data.stream().map(RevenueReportsDTO::getDate)
                .max(LocalDate::compareTo).orElse(LocalDate.now());

        return new DateRange(from != null ? from : minDate, to != null ? to : maxDate);
    }

    private List<LocalDate> generateDateRange(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private Map<LocalDate, RevenueReportsDTO> createDataMap(List<RevenueReportsDTO> data) {
        Map<LocalDate, RevenueReportsDTO> map = new HashMap<>();
        for (RevenueReportsDTO dto : data) {
            map.put(dto.getDate(), dto);
        }
        return map;
    }

    private RevenueReportsDTO createEmptyRevenueDTO(LocalDate date) {
        return new RevenueReportsDTO(date, 0L, 0L, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    // ====================== Helper Classes ======================
    private static class DateRange {
        final LocalDate from;
        final LocalDate to;

        DateRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }
    }
}