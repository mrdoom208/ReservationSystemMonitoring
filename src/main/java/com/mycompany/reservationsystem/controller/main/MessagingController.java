package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.util.dialog.ConfirmationDialog;
import com.mycompany.reservationsystem.util.dialog.DeleteDialog;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import com.mycompany.reservationsystem.util.ComboBoxUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyDecimalFormat;

@Component
public class MessagingController {
    private AdministratorUIController adminUIController;

    public MessagingController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }
    private DeviceDetectionManager deviceDetectionManager;

    private User currentuser;
    @FXML
    private TableView<CustomerReportDTO> CustomerInfo;

    @FXML
    private TextField SearchCustomerInfo,newMessageLabel;

    @FXML
    private TextArea MessageDetails;

    @FXML
    private MFXComboBox<Message> MessageLabel,MessageLabels;

    @FXML
    private MFXComboBox<String> SortCustomer;

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
    private Label UIresponse,sendmessageresponse;

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

    private HBox HboxProgress;
    private Label LabelProgress;
    private ProgressBar BarProgress;


    @Autowired
    ReservationService reservationService;

    @Autowired
    MessageService messageService;

    @Autowired
    PermissionService permissionService;

    private final ObservableList<CustomerReportDTO> CustomerInformationData = FXCollections.observableArrayList();
    private final FilteredList<CustomerReportDTO> filterCustomerInfo = new FilteredList<>(CustomerInformationData, p -> true);
    private final ObservableList<Message> labelsObs = FXCollections.observableArrayList();


    private int customerInformationPage = 0;
    private int pageSize = 100;
    private boolean allDataLoaded = false;

    @FXML
    private void handleNewMessage() {
        // Add empty string to ComboBox
        Message newMessage = new Message("New Message","",false);


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
                msg.getId(),
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
        if (msg == null) {
            messageresponse(false, "Please select a message to delete.");
            return;
        }

        DeleteDialog.show(
                "Are you sure you want to delete this " + msg.getMessageLabel() + " message?",
                () -> {
                    try {
                        messageService.deleteMessage(msg.getId());
                        newMessageLabel.clear();
                        MessageDetails.clear();
                        loadMessages();

                        messageresponse(true, msg.getMessageLabel() + " Message Removed Successfully");
                    } catch (IllegalArgumentException ex) {
                        // This is called if it's the default message
                        messageresponse(false, ex.getMessage());
                    }
                });
    }


    public void setupMessages(){
        MessageLabels.setItems(labelsObs);
        MessageLabel.setItems(labelsObs);
        ComboBoxUtil.formatMessageComboBox(MessageLabel);
        ComboBoxUtil.formatMessageComboBox(MessageLabels);
        MessageLabel.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldMsg, newMsg) -> {
                    if (newMsg != null) {
                        newMessageLabel.setText(newMsg.getMessageLabel());
                        if(newMsg.isDefault()){
                            newMessageLabel.setDisable(true);
                        }else{
                            newMessageLabel.setDisable(false);
                        }
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
    @FXML
    private void SendMessage() {
        // 1. Get selected customers
        List<CustomerReportDTO> selectedCustomers = CustomerInfo.getItems()
                .stream()
                .filter(CustomerReportDTO::isSelected)
                .toList();

        if (selectedCustomers.isEmpty()) {
            sendresponse(false, "Please select at least one customer");
            return;
        }

        // 2. Get selected message
        Message selectedMessage = MessageLabels.getSelectionModel().getSelectedItem();
        if (selectedMessage == null) {
            sendresponse(false, "Please select a message");
            return;
        }

        String message = selectedMessage.getMessageDetails();
        if (message == null || message.isBlank()) {
            sendresponse(false, "Message content is empty");
            return;
        }

        // 3. Show confirmation dialog
        ConfirmationDialog.show(
                "Are you sure you want to send this message to "
                        + selectedCustomers.size() + " customer(s)?",
                () -> {
                    // Only proceed if user clicks Yes

                    // 4. Prepare progress bar
                    HboxProgress.setManaged(true);
                    HboxProgress.setVisible(true);
                    BarProgress.setProgress(0);
                    LabelProgress.setText("Preparing to send messages...");

                    int total = selectedCustomers.size();

                    // 5. Background task for sending messages
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            String port = AppSettings.loadSerialPort();
                            if (port == null || port.isBlank()) {
                                throw new IllegalStateException("No serial port selected");
                            }

                            // Open port if not already open
                            if (!deviceDetectionManager.isPortOpen()) {
                                deviceDetectionManager.openPort(port, 115200);
                            }

                            updateProgress(0, total);
                            updateMessage("Starting to send messages...");

                            for (int i = 0; i < total; i++) {
                                CustomerReportDTO customer = selectedCustomers.get(i);
                                String phone = customer.getPhone();
                                if (phone == null || phone.isBlank()) continue;

                                // --- SEND SMS HERE ---
                                deviceDetectionManager.sendMessage(phone, message); // your method

                                // Update progress
                                updateProgress(i + 1, total);
                                updateMessage("Sending message " + (i + 1) + " of " + total);
                            }

                            return null;
                        }
                    };

                    // Bind progress bar and label
                    BarProgress.progressProperty().bind(task.progressProperty());
                    LabelProgress.textProperty().bind(task.messageProperty());

                    task.setOnSucceeded(e -> {
                        deviceDetectionManager.closePort();
                        BarProgress.progressProperty().unbind();
                        LabelProgress.textProperty().unbind();
                        BarProgress.setProgress(1);
                        LabelProgress.setText("Messages sent successfully!");
                        sendresponse(true, "Message sent to " + total + " customer(s)");
                        HboxProgress.setManaged(false);
                        HboxProgress.setVisible(false);
                    });

                    task.setOnFailed(e -> {
                        deviceDetectionManager.closePort();
                        BarProgress.progressProperty().unbind();
                        LabelProgress.textProperty().unbind();
                        LabelProgress.setText("Failed to send messages");
                        sendresponse(false, task.getException().getMessage());
                        HboxProgress.setManaged(false);
                        HboxProgress.setVisible(false);
                    });

                    new Thread(task, "sms-sender").start();
                }
        );
    }





    public void setupCustomerInformation(){
        SortCustomer.getItems().addAll(
                "Reservation (↓)",
                "Reservation (↑)",
                "Revenue (↓)",
                "Revenue (↑)",
                "Average Spend (↓)",
                "Average Spend (↑)"
        );

        SortedList<CustomerReportDTO> sortedCustomerInfo =
                new SortedList<>(filterCustomerInfo);

        SortCustomer.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            Comparator<CustomerReportDTO> comparator = null;

            switch (newVal) {

                // -------- TOTAL RESERVATION --------
                case "Reservation (↓)":
                    comparator = (a, b) -> {
                        Long x = a.getTotalReservation();
                        Long y = b.getTotalReservation();
                        if (x == null && y == null) return 0;
                        if (x == null) return 1;
                        if (y == null) return -1;
                        return Long.compare(y, x);
                    };
                    break;

                case "Reservation (↑)":
                    comparator = (a, b) -> {
                        Long x = a.getTotalReservation();
                        Long y = b.getTotalReservation();
                        if (x == null && y == null) return 0;
                        if (x == null) return 1;
                        if (y == null) return -1;
                        return Long.compare(x, y);
                    };
                    break;

                // -------- TOTAL REVENUE --------
                case "Revenue (↓)":
                    comparator = (a, b) -> {
                        BigDecimal x = a.getTotalRevenue();
                        BigDecimal y = b.getTotalRevenue();
                        if (x == null && y == null) return 0;
                        if (x == null) return 1;
                        if (y == null) return -1;
                        return y.compareTo(x);
                    };
                    break;

                case "Revenue (↑)":
                    comparator = (a, b) -> {
                        BigDecimal x = a.getTotalRevenue();
                        BigDecimal y = b.getTotalRevenue();
                        if (x == null && y == null) return 0;
                        if (x == null) return 1;
                        if (y == null) return -1;
                        return x.compareTo(y);
                    };
                    break;

                // -------- AVERAGE SPEND --------
                case "Average Spend (↓)":
                    comparator = (a, b) -> {
                        BigDecimal x = BigDecimal.valueOf(a.getAverageRevenue());
                        BigDecimal y = BigDecimal.valueOf(b.getAverageRevenue());
                        if (x == null && y == null) return 0;
                        if (x == null) return 1;
                        if (y == null) return -1;
                        return y.compareTo(x);
                    };
                    break;

                case "Average Spend (↑)":
                    comparator = (a, b) -> {
                        BigDecimal x = BigDecimal.valueOf(a.getAverageRevenue());
                        BigDecimal y = BigDecimal.valueOf(b.getAverageRevenue());
                        if (x == null && y == null) return 0;
                        if (x == null) return 1;
                        if (y == null) return -1;
                        return x.compareTo(y);
                    };
                    break;
            }
            // Prevent TableColumn sorting conflicts
            CustomerInfo.getSortOrder().clear();
            sortedCustomerInfo.setComparator(comparator);
        });




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
        CustomerInfo.setItems(sortedCustomerInfo);
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
    public void sendresponse(boolean successfully,String details){
        if(!successfully){
            sendmessageresponse.getStyleClass().removeAll("login-success", "login-message");
            sendmessageresponse.getStyleClass().add("login-error");
            sendmessageresponse.setText(details);
        }
        else{
            sendmessageresponse.getStyleClass().removeAll("login-error", "login-message-hidden");
            sendmessageresponse.getStyleClass().add("login-success");
            sendmessageresponse.setText(details);
        }

    }
    private void applyPermissions() {
        if (currentuser == null) return;

        // Map each button to its required permission code
        Map<Button, String> buttonPermissions = Map.of(
                sendMessage,"SEND_MESSAGE",
                newMessage, "CREATE_MESSAGE",
                deleteMessage, "DELETE_MESSAGE"
        );
        Map<TextArea, String> TextPermissions = Map.of(
                MessageDetails,"EDIT_MESSAGE"
                );


                // Disable buttons if user doesn't have permission
        buttonPermissions.forEach((button, code) ->
                button.setDisable(!permissionService.hasPermission(currentuser, code))
        );
        TextPermissions.forEach((TextArea,code)->
                TextArea.setDisable(!permissionService.hasPermission(currentuser,code))
        );
    }


    @FXML
    private void initialize(){
        currentuser = adminUIController.getCurrentUser();
        deviceDetectionManager = adminUIController.getDeviceDetectionManager();

        HboxProgress = adminUIController.getHboxProgress();
        LabelProgress = adminUIController.getLabelProgress();
        BarProgress = adminUIController.getBarProgress();

        applyPermissions();
        Platform.runLater(this::loadMessages);
        loadCustomerInformation();
        setupMessages();
        setupCustomerInformation();

    }
}
