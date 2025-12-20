package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.constraint.FieldRestrictions;
import com.mycompany.reservationsystem.constraint.FieldValidators;
import com.mycompany.reservationsystem.constraint.PhoneNumberRestriction;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.CustomerRepository;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class addReservationController {

    @FXML
    private MFXTextField CustomerName;

    @FXML
    private MFXTextField Email;

    @FXML
    private MFXTextField Pax;

    @FXML
    private MFXTextField Phone;

    @FXML
    private MFXButton addButton;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private MFXComboBox<String> statusComboBox;

    @FXML
    private Label messageLabel;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ReservationRepository reservationRepository;

    private Stage dialogStage;

    private FieldRestrictions fieldRestrictions;

    private FieldValidators fieldValidators;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    private void showError(String message) {
        messageLabel.getStyleClass().removeAll("popup-success", "popup-message-hidden");
        messageLabel.getStyleClass().add("popup-error");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("popup-error", "popup-message-hidden");
        messageLabel.getStyleClass().add("popup-success");
        messageLabel.setText(message);
    }

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("Pending", "Confirm");
        statusComboBox.setValue("Pending");

        fieldRestrictions.applyEmailRestriction(Email);
        fieldRestrictions.applyNumbersOnly(Pax);
        fieldRestrictions.applyNumbersOnly(Phone);
        fieldRestrictions.applyLettersOnly(CustomerName);






        cancelButton.setOnAction(e -> dialogStage.close());

        addButton.setOnAction(e -> {
            if (!fieldValidators.validateRequired(CustomerName)) {showError("Please Insert Customer Name"); return;}
            if (!fieldValidators.validateRequired(Pax)) {showError("Please Insert Pax Size"); return;}
            if (!fieldValidators.isNonZeroNumeric(Pax)) {showError("Pax must be greater than zero."); return;}
            if (!fieldValidators.validateRequired(Phone)){ showError("Please Insert Phone No."); return;}
            if (!fieldValidators.startsWith09(Phone)) {showError("Please Insert Valid Phone No."); return;}
            if (Email.getText() != null && !Email.getText().trim().isEmpty()) {
                // Email has text, so validate format
                if (!fieldRestrictions.isValidEmail(Email)) {
                    showError("Please Insert Valid Email");
                         return;
                }
            }
            showSuccess("Reservation Added Successfully");
            Customer newCustomer = new Customer();
            newCustomer.setName(CustomerName.getText());
            newCustomer.setEmail(Email.getText());
            newCustomer.setPhone(Phone.getText());
            customerRepository.save(newCustomer);

            Reservation newReservation = new Reservation();
            newReservation.setCustomer(newCustomer);
            newReservation.setPax(Integer.parseInt(Pax.getText()));
            newReservation.setStatus(statusComboBox.getValue());
            newReservation.setReservationPendingtime(LocalTime.now());
            newReservation.setDate(LocalDate.now());
            newReservation.setReference(String.format("RSV-%05d", reservationRepository.count() + 1));
            reservationRepository.save(newReservation);

            // close dialog after adding
            dialogStage.close();
        });
    }

}
