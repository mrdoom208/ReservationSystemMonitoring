package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.Service.ReservationService;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyDecimalFormat;

@Component
public class MessagingController {
    @FXML
    private TableView<CustomerReportDTO> CustomerInfo;

    @FXML
    private TextField SearchCustomerInfo;

    @FXML
    private TextArea MessageDetails;

    @FXML
    private MFXComboBox MessageLabel;

    @FXML
    private MFXScrollPane MessagingPane;

    @FXML
    private TableColumn<CustomerReportDTO, Double> averagerevinfo;

    @FXML
    private MFXButton deleteCustomerinfo;

    @FXML
    private MFXButton deleteMessage;

    @FXML
    private TableColumn<CustomerReportDTO, String> phoneinfo;

    @FXML
    private MFXButton saveMessage;

    @FXML
    private MFXCheckbox selectAllCustomerInfo;

    @FXML
    private TableColumn<CustomerReportDTO, Boolean> selectInfo;

    @FXML
    private MFXButton sendMessage;

    @FXML
    private TableColumn<CustomerReportDTO, Integer> totalresinfo;

    @FXML
    private TableColumn<CustomerReportDTO, BigDecimal> totalrevinfo;

    private final ObservableList<CustomerReportDTO> CustomerInformationData = FXCollections.observableArrayList();
    private final FilteredList<CustomerReportDTO> filterCustomerInfo = new FilteredList<>(CustomerInformationData, p -> true);


    @Autowired
    ReservationService reservationService;

    private int customerInformationPage = 0;
    private int pageSize = 100;
    private boolean allDataLoaded = false;


    public void setupCustomerInformation(){
        SearchCustomerInfo.textProperty().addListener((obs, oldValue, newValue) -> {
            String lowerCaseFilter = newValue.toLowerCase();

            filterCustomerInfo.setPredicate(customer -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // show all
                }

                // Customize searchable columns
                if (customer.getPhone().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getTotalReservation() != null &&
                        String.valueOf(customer.getTotalReservation()).contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getTotalRevenue() != null &&
                        customer.getTotalRevenue().toString().contains(lowerCaseFilter)) {
                    return true;
                }
                // add more fields if needed

                return false; // no match
            });
        });


        selectInfo.setEditable(true);
        CustomerInfo.setEditable(true);
        selectInfo.setCellValueFactory(cellData ->
                cellData.getValue().selectedProperty()
        );
        selectInfo.setCellFactory(col -> new CheckBoxTableCell<>());

        applyDecimalFormat(averagerevinfo,2);

        TableColumn<?, ?>[] column = {selectInfo,phoneinfo,totalresinfo,totalrevinfo,averagerevinfo};
        double[] widthFactors = {0.12,0.22,0.22,0.22,0.22};
        String[] namecol = {"","phone", "totalReservation", "totalRevenue", "averageRevenue"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(CustomerInfo.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }
        CustomerInfo.setItems(filterCustomerInfo);


    }
    public void loadCustomerInformation(){
        customerInformationPage = 0;
        allDataLoaded = false;
        CustomerInformationData.clear();

        loadCustomerReportPage();


    }
    private void loadCustomerReportPage() {
        if (allDataLoaded) return;
        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return reservationService.loadPage(customerInformationPage, pageSize);
            }
        };
        task.setOnSucceeded(e -> {
            List<CustomerReportDTO> results = task.getValue();

            if (results.isEmpty()) {
                allDataLoaded = true;
                return;
            }

            CustomerInformationData.addAll(results);
            customerInformationPage++; // move to next page
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }




    @FXML
    private void initialize(){
        loadCustomerInformation();
        setupCustomerInformation();

    }
}
