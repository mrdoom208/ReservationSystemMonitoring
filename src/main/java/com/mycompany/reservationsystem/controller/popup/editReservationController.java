package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.util.FieldValidators;
import com.mycompany.reservationsystem.util.PhoneFormatter;
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

import static com.mycompany.reservationsystem.util.FieldRestrictions.*;
import static com.mycompany.reservationsystem.util.FieldValidators.isNonZeroNumeric;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class editReservationController {
    private Reservation targetReservation;

    public void setTargetReservation(Reservation targetReservation) {
        this.targetReservation = targetReservation;
        CustomerName.setText(targetReservation.getCustomer().getName());
        Pax.setText(String.valueOf(targetReservation.getPax()));
        Phone.setText(targetReservation.getCustomer().getPhone());
        Email.setText(targetReservation.getCustomer().getEmail());
        statusComboBox.setValue(targetReservation.getStatus());
    }
    private Stage dialogStage;
    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private MFXTextField CustomerName;

    @FXML
    private MFXTextField Email;

    @FXML
    private MFXTextField Pax;

    @FXML
    private TextField Phone;

    @FXML
    private MFXButton Submit;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private Label messageLabel;

    @FXML
    private MFXComboBox<String> statusComboBox;

    @Autowired
    private ReservationRepository reservationRepository;

    private PhoneFormatter phoneFormatter;


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
    private void initialize(){
        statusComboBox.getItems().addAll("Pending", "Confirm");
        statusComboBox.setDisable(true);
        applyEmailRestriction(Email);
        applyNumbersOnly(Pax);
        phoneFormatter = new PhoneFormatter("+63",Phone);
        applyLettersOnly(CustomerName);


        cancelButton.setOnAction(e -> dialogStage.close());
        Submit.setOnAction(event -> {
            if (!validateRequired(CustomerName)) {showError("Please Insert Customer Name"); return;}
            if (!validateRequired(Pax)) {showError("Please Insert Pax Size"); return;}
            if (!isNonZeroNumeric(Pax)) {showError("Pax must be greater than zero."); return;}
            if (!validateRequired(Phone)){ showError("Please Insert Phone No."); return;}
            if (!phoneFormatter.isValid()) {showError("Please Insert Valid Phone No."); return;}
            if (Email.getText() != null && !Email.getText().trim().isEmpty()) {
                // Email has text, so validate format
                if (!isValidEmail(Email)) {
                    showError("Please Insert Valid Email");
                    return;
                }
            }
            showSuccess("Reservation Changed Successfully");
            targetReservation.getCustomer().setName(CustomerName.getText());
            targetReservation.setPax(Integer.parseInt(Pax.getText()));
            targetReservation.getCustomer().setPhone(phoneFormatter.getCleanPhone());
            targetReservation.getCustomer().setEmail(Email.getText());
            reservationRepository.save(targetReservation);
            // close dialog after adding
            dialogStage.close();

        });

    }
}
