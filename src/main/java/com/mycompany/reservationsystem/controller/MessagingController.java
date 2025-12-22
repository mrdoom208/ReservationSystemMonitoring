package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.Service.MessageService;
import com.mycompany.reservationsystem.Service.ReservationService;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.model.Message;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import com.mycompany.reservationsystem.util.ComboBoxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
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
    private TextField SearchCustomerInfo,newMessageLabel;

    @FXML
    private TextArea MessageDetails;

    @FXML
    private MFXComboBox<Message> MessageLabel,MessageLabels;

    @FXML
    private MFXScrollPane MessagingPane;

    @FXML
    private TableColumn<CustomerReportDTO, Double> averagerevinfo;

    @FXML
    private MFXButton deleteCustomerinfo;

    @FXML
    private MFXButton deleteMessage,newMessage;

    @FXML
    private TableColumn<CustomerReportDTO, String> phoneinfo;

    @FXML
    private Label UIresponse;

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

    @Autowired
    ReservationService reservationService;

    @Autowired
    MessageService messageService;

    private final ObservableList<CustomerReportDTO> CustomerInformationData = FXCollections.observableArrayList();
    private final FilteredList<CustomerReportDTO> filterCustomerInfo = new FilteredList<>(CustomerInformationData, p -> true);
    private final ObservableList<Message> labelsObs = FXCollections.observableArrayList();


    private int customerInformationPage = 0;
    private int pageSize = 100;
    private boolean allDataLoaded = false;

    @FXML
    private void handleNewMessage() {
        // Add empty string to ComboBox
        Message newMessage = new Message("New Message","");


        // Add to ObservableList
        labelsObs.add(newMessage);

        // Select the new item in the ComboBox
        MessageLabel.getSelectionModel().selectItem(newMessage);

        // Clear TextArea
        newMessageLabel.setText("New Message");
        MessageDetails.clear();
    }
    @FXML
    private void handleSaveMessage() {
        // Get the currently selected Message
        Message msg = MessageLabel.getSelectionModel().getSelectedItem();

        if (msg == null || msg.getMessageLabel().isBlank()) {
            messageresponse(false,"Message Label Cannot be Empty");
            return;
        }

        messageService.saveMessage(
                msg.getMessageLabel(),
                msg.getMessageDetails()
        );

        messageresponse(true,msg.getMessageLabel()+" Message saved Successfully");
        newMessageLabel.clear();
        MessageDetails.clear();
        loadMessages();
    }

    @FXML
    private void handleDeleteMessage(){
        Message msg = MessageLabel.getSelectionModel().getSelectedItem();
        messageService.deleteMessage(msg.getId());

        newMessageLabel.clear();
        MessageDetails.clear();
        loadMessages();
        messageresponse(false,msg.getMessageLabel()+" Message Removed Successfully");
    }


    public void setupMessages(){
        MessageLabels.setItems(labelsObs);
        MessageLabel.setItems(labelsObs);
        ComboBoxUtil.MFXComboboxMessageFormat(MessageLabel);
        ComboBoxUtil.MFXComboboxMessageFormat(MessageLabels);
        MessageLabel.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldMsg, newMsg) -> {
                    if (newMsg != null) {
                        newMessageLabel.setText(newMsg.getMessageLabel());
                        MessageDetails.setText(newMsg.getMessageDetails());
                    }
                });
        newMessageLabel.textProperty().addListener((obs, old, text) -> {
            Message selected = MessageLabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setMessageLabel(text);

            }
        });
        MessageDetails.textProperty().addListener((obs, old, text) -> {
            Message selected = MessageLabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setMessageDetails(text);
            }
        });


    }
    public void loadMessages(){
        List<Message> allMessages = messageService.getAllMessages(); // or getAllMessageLabels()
        MessageLabel.getSelectionModel().clearSelection();
        MessageLabels.getSelectionModel().clearSelection();

        labelsObs.setAll(allMessages);
        javafx.application.Platform.runLater(() -> {
            ComboBoxUtil.selectTopItem(MessageLabel);
            ComboBoxUtil.selectTopItem(MessageLabels);
        });

    }



    public void setupCustomerInformation(){
        selectAllCustomerInfo.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            for (CustomerReportDTO customer : CustomerInfo.getItems()) {
                customer.setSelected(isSelected);
            }
        });
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
        // Add listener to each row
        CustomerInfo.getItems().forEach(customer ->
                customer.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        // If any row is unchecked, uncheck "Select All"
                        selectAllCustomerInfo.setSelected(false);
                    } else {
                        // If all rows are selected, check "Select All"
                        boolean allSelected = CustomerInfo.getItems().stream()
                                .allMatch(CustomerReportDTO::isSelected);
                        selectAllCustomerInfo.setSelected(allSelected);
                    }
                })
        );



        selectInfo.setEditable(true);
        CustomerInfo.setEditable(true);
        selectInfo.setCellValueFactory(cellData ->
                cellData.getValue().selectedProperty()
        );
        selectInfo.setCellFactory(CheckBoxTableCell.forTableColumn(selectInfo));

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

    @FXML
    private void initialize(){

        loadMessages();
        loadCustomerInformation();
        setupMessages();
        setupCustomerInformation();

    }
}
