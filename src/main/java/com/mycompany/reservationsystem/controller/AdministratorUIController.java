/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.model.CustomerReservation;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.repository.CustomerReservationRepository;
import com.mycompany.reservationsystem.dto.CustomerReservationDTO;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.websocket.ReservationListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * FXML Controller class
 *
 * @author formentera
 */

@Component
public class AdministratorUIController implements Initializable, ReservationListener{
    
    @FXML 
    private Button Dashboardbtn,ReservationManagementbtn,TableManagementbtn,ManageStaffAndAccountsbtn;    
    @FXML
    private ScrollPane DashboardPane, ReservationPane,TableManagementPane,ManageStaffAndAccountsPane;
    @FXML
    private Label Total_CustomerDbd,Total_Cancelled,Total_Pending;
    @FXML
    private LineChart<String, Number> myBarChart;

    @FXML
    private TableView<CustomerReservation> RecentReservationTable;
    @FXML
    private TableView<ManageTablesDTO> ManageTableView;
    @FXML
    private TableColumn<CustomerReservation,String> CustomerColm;
    @FXML
    private TableColumn<CustomerReservation, Integer> PaxColm;
    @FXML
    private TableColumn<CustomerReservation,LocalTime> TimeColm;
    @FXML
    private TableColumn<CustomerReservation,LocalDate> DateColm;
    @FXML
    private TableColumn<ManageTablesDTO,String>TableNoColum,TableCustomerColum,TableStatusColum;    
    @FXML
    private TableColumn<ManageTablesDTO,LocalTime> TableTimeColum;
    @FXML
    private TableColumn<ManageTablesDTO,Integer> TablePaxColum,TableCapacityColum;
    @FXML
    private VBox notificationArea;
    
    @FXML
    private TableView<CustomerReservation> CustomerReservationTable;
    @FXML
    private TableColumn<CustomerReservation,String> NameCRT,StatusCRT,PreferCRT,PhoneCRT,EmailCRT,ReferenceCRT;
    @FXML
    private TableColumn<CustomerReservation, Integer> PositionCRT,PaxCRT,TableNoCRT;
    
    
    
     
    
    @Autowired
    private ManageTablesRepository manageTablesRepository;
    
    
    @Autowired
    private CustomerReservationRepository customerReservationRepository;
    
    @FXML
    private void navigate(ActionEvent event){
        Button clicked = (Button) event.getSource();
        Button buttons[] = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn,ManageStaffAndAccountsbtn}; 
        
        DashboardPane.setVisible(false);
        ReservationPane.setVisible(false);
        TableManagementPane.setVisible(false);
        ManageStaffAndAccountsPane.setVisible(false);

        
        for (Button btn : buttons) {
            if (btn == null) continue;
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
    if(notificationArea.getChildren().size()>= 3){
        notificationArea.getChildren().remove(0);
    }
    
    notificationArea.getChildren().add(notification);
    }   
    
    
    
    public void updateLabels(){
        Total_CustomerDbd.setText(String.valueOf(customerReservationRepository.count()));
        Total_Pending.setText(String.valueOf(customerReservationRepository.countByStatus("Pending")));
        Total_Cancelled.setText(String.valueOf(customerReservationRepository.countByStatus("Cancelled")));
        
        
    }
    
    public void loadRecentReservations() {
        
        List<CustomerReservation> latest10 = customerReservationRepository.findTop10ByOrderByDateDescTimeDesc(PageRequest.of(0, 10));
        RecentReservationTable.getItems().setAll(latest10);
        System.out.print(RecentReservationTable.getItems().setAll(latest10));
    }
    
    public void loadTableView() {
        
        
        List<ManageTablesDTO> tables = manageTablesRepository.getManageTablesDTO();

        ManageTableView.getItems().setAll(tables);
        
        
    }
    
    
    public void setupRecentReservation(){
        
 
        TableColumn<?, ?>[] column = {CustomerColm,PaxColm,DateColm,TimeColm};
        double[] widthFactors = {0.35, 0.12, 0.29, 0.24};
        String[] namecol = {"name","pax","date","time"};
        
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
        String[] namecol = {"customer","tableNo","status","pax","capacity","time"};

        for (int i = 0; i < column.length; i++) {
        TableColumn<?, ?> col = column[i];    
        col.setResizable(false);  
        col.setReorderable(false);
        col.prefWidthProperty().bind(ManageTableView.widthProperty().multiply(widthFactors[i]));
        col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        ManageTableView.setRowFactory(tv -> new TableRow<ManageTablesDTO>() {
        @Override
        protected void updateItem(ManageTablesDTO item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setStyle("");
            } else {
                String status = item.getStatus(); // Make sure your DTO has getStatus()
                String bgColor;

                switch (status) {
                    case "Available" -> bgColor = "#4CAF50";
                    case "Occupied" -> bgColor = "#F44336";
                    case "Reserved" -> bgColor = "#FFC107";
                    default -> bgColor = "white";
                }

                setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white;");
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
    holder.setAlignment(Message,Pos.CENTER);
    
    pane.setCenter(holder);
    pane.setRight(closeBtn);
    
    return pane;
    }
    
    public void loadDashboard(){
        updateLabels();
        loadRecentReservations();
        loadTableView();
        setupRecentReservation();
        setupTableView();
        barchart();
        
    }
    

    public void barchart() {
    
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.getData().add(new XYChart.Data<>("Jan", 120));
    series.getData().add(new XYChart.Data<>("Feb", 150));
    series.getData().add(new XYChart.Data<>("Mar", 100));
    series.getData().add(new XYChart.Data<>("April", 90));
    series.getData().add(new XYChart.Data<>("June", 10));
    series.getData().add(new XYChart.Data<>("July", 50));
    series.getData().add(new XYChart.Data<>("August", 60));
    series.getData().add(new XYChart.Data<>("Septeber", 40));
    series.getData().add(new XYChart.Data<>("Octoberr", 30));
    
    myBarChart.getData().add(series);
}
    
    public void setupCustomerReservationTable(){
        
        PositionCRT.setCellFactory(Pos -> new TableCell<CustomerReservation, Integer>() {
        @Override
        protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {    
            setText(null);
        } else {
            setText(String.valueOf(getIndex() + 1));
            }
        }
        });
        
        TableColumn<?, ?>[] column = {PositionCRT,NameCRT,PaxCRT,StatusCRT,PreferCRT,PhoneCRT,EmailCRT,ReferenceCRT,TableNoCRT};
        double[] widthFactors = {0.08, 0.15, 0.06, 0.1, 0.1, 0.12,0.15,0.15,0.09};
        String[] namecol = {"","name","pax","status","prefer","phone","email","reference","table_id"};

        for (int i = 0; i < column.length; i++) {
        TableColumn<?, ?> col = column[i];    
        
        if (col == null) {
        System.out.println("❌ NULL COLUMN at index " + i + 
                           " (expected: " + namecol[i] + ")");
        continue; // skip this iteration so it won't throw NPE
        } else {
        System.out.println("✔ Column OK: index " + i + 
                           " = " + col.getText());
        }
        

        col.setResizable(false);  
        col.setReorderable(false);
        col.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(widthFactors[i]));
        col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        
    }
    
    public void loadCustomerReservationTable(){
        List<CustomerReservation> Data = customerReservationRepository.findByStatus("Pending");
        CustomerReservationTable.getItems().setAll(Data);
        
    }
    
    
    private void loadReservationManagement(){
        setupCustomerReservationTable();
        loadCustomerReservationTable();
        
        
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        Dashboardbtn.fire();
        loadDashboard();
        loadReservationManagement();
        
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
        });    
    }
    
}
