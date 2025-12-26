package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyStatusStyle;
import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyTimeFormat;

@Component
public class DashboardController {
    @FXML
    private TableColumn<Reservation, String> CustomerColm;

    @FXML
    private ScrollPane DashboardPane;

    @FXML
    private TableView<ManageTablesDTO> ManageTableView;

    @FXML
    private TableColumn<Reservation, Integer> PaxColm;

    @FXML
    private TableView<Reservation> RecentReservationTable;

    @FXML
    private TableColumn<ManageTablesDTO, Integer> TableCapacityColum;

    @FXML
    private TableColumn<ManageTablesDTO,String> TableCustomerColum;

    @FXML
    private TableColumn<ManageTablesDTO, String> TableNoColum;

    @FXML
    private TableColumn<ManageTablesDTO,Integer> TablePaxColum;

    @FXML
    private TableColumn<ManageTablesDTO, String> TableStatusColum;

    @FXML
    private TableColumn<ManageTablesDTO, LocalTime> TableTimeColum;

    @FXML
    private TableColumn<Reservation, LocalTime> TimeColm;

    @FXML
    private Label Total_Cancelled;

    @FXML
    private Label Total_CustomerDbd;

    @FXML
    private Label Total_Pending;

    @FXML
    private Label activetable;

    @FXML
    private BorderPane dashpane;

    @FXML
    private LineChart<String, Number> myBarChart;

    @FXML
    private VBox notificationArea;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ManageTablesRepository manageTablesRepository;

    private final ObservableList<Reservation> recentReservations = FXCollections.observableArrayList();
    private final ObservableList<ManageTablesDTO> manageTablesData = FXCollections.observableArrayList();

    public void updateLabels() {
        activetable.setText(String.valueOf(manageTablesRepository.countByStatus("Occupied"))+"/"+String.valueOf(manageTablesRepository.countByStatus("Reserved"))+"/"+String.valueOf(manageTablesRepository.count()));
        Total_CustomerDbd.setText(String.valueOf(reservationRepository.count()));
        Total_Pending.setText(String.valueOf(reservationRepository.countByStatus("Pending")));
        Total_Cancelled.setText(String.valueOf(reservationRepository.countByStatus("Cancelled")));

    }


    public void barchart() {
        myBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        List<String> last7Days = new ArrayList<>();
        for (int i = 30; i >= 0; i--) {
            last7Days.add(today.minusDays(i).format(dateformat));
        }
        LocalDate SevenDaysAgo = today.minusDays(30);
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
    public void setupRecentReservation() {

        RecentReservationTable.setItems(recentReservations);
        applyTimeFormat(TimeColm);


        TableColumn<?, ?>[] column = {CustomerColm, PaxColm, TimeColm};
        double[] widthFactors = {0.4, 0.2, 0.4};
        String[] namecol = {"name","pax","reservationPendingtime"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(true);
            col.setReorderable(false);
            col.prefWidthProperty().bind(
                    RecentReservationTable.widthProperty().multiply(widthFactors[i])
            );
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

        applyStatusStyle(TableStatusColum);
        applyTimeFormat(TableTimeColum);


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

        ManageTableView.setPlaceholder(new Label("No Table set yet"));

    }


    public void loadRecentReservations() {

        List<Reservation> latest10 = reservationRepository.findTop15ByOrderByDateDescReservationPendingtimeDesc(PageRequest.of(0, 15));
        recentReservations.setAll(latest10);
    }
    public void loadTableView() {

        List<ManageTablesDTO> tables = manageTablesRepository.getManageTablesDTO();
        manageTablesData.setAll(tables);

    }
    public void showNotification(String title, String message, String type) {
        VBox container = notificationArea;

        // Build notification UI
        System.out.println("notificationArea: " + notificationArea);
        HBox root = new HBox(15);
        root.getStyleClass().add("notification-root");

        if (type.equals("success"))
            root.getStyleClass().add("success-border");
        else
            root.getStyleClass().add("error-border");

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("icon-circle");

        if (type.equals("success"))
            iconCircle.getStyleClass().add("success-icon");
        else
            iconCircle.getStyleClass().add("error-icon");

        Label icon = new Label(type.equals("success") ? "✓" : "✕");
        icon.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        iconCircle.getChildren().add(icon);

        // Texts
        VBox texts = new VBox(5);
        Label t = new Label(title);
        t.getStyleClass().add("notification-title");

        Label m = new Label(message);
        m.getStyleClass().add("notification-message");
        texts.getChildren().addAll(t, m);

        root.getChildren().addAll(iconCircle, texts);

        // Add to VBox (NEWEST ON TOP)
        if (container.getChildren().size() >= 5) {
            container.getChildren().remove(container.getChildren().size() - 1); // remove last (oldest)
        }

        container.getChildren().add(0, root);
        root.setMaxWidth(Double.MAX_VALUE);
        root.prefWidthProperty().bind(container.widthProperty());
        // ---- ANIMATION ----
        root.setOpacity(0);
        root.setTranslateY(-20);

        Timeline showAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.opacityProperty(), 0),
                        new KeyValue(root.translateYProperty(), -20)
                ),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(root.opacityProperty(), 1),
                        new KeyValue(root.translateYProperty(), 0)
                )
        );

        // Auto close after 3 sec
        Timeline wait = new Timeline(new KeyFrame(Duration.seconds(60)));

        // Fade out and remove
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(root.opacityProperty(), 0),
                        new KeyValue(root.translateYProperty(), -20)
                )
        );

        wait.setOnFinished(e -> fadeOut.play());

        fadeOut.setOnFinished(e -> container.getChildren().remove(root));

        showAnim.play();
        wait.play();
    }

    @FXML
    public void initialize(){
        dashpane.minHeightProperty().bind(dashpane.widthProperty().multiply(0.965));
        updateLabels();
        loadRecentReservations();
        loadTableView();
        barchart();
        setupRecentReservation();
        setupTableView();

        showNotification("New Reservation Added","A new Reservation has been successfully added.","success");

        showNotification(
                "Reservation Cancelled",
                "The reservation has been cancelled.",
                "error"
        );





    }

}
