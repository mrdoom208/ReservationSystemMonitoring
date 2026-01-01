package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.repository.ReservationRepository;
import com.mycompany.reservationsystem.util.FieldValidators;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.mycompany.reservationsystem.util.FieldRestrictions.*;

@Component
public class editReservationController {
    private Reservation targetReservation;

    public void setTargetReservation(Reservation targetReservation) {
        this.targetReservation = targetReservation;
        CustomerName.setText(targetReservation.getCustomer().getName());
        Pax.setText(String.valueOf(targetReservation.getPax()));
        Phone.setText(targetReservation.getCustomer().getPhone());
        Email.setText(targetReservation.getCustomer().getEmail());

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
    private MFXTextField Phone;

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

    private FieldValidators fieldValidators;

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
        statusComboBox.setValue(targetReservation.getStatus());
        statusComboBox.setDisable(true);
        applyEmailRestriction(Email);
        applyNumbersOnly(Pax);
        applyNumbersOnly(Phone);
        applyLettersOnly(CustomerName);


        cancelButton.setOnAction(e -> dialogStage.close());
        Submit.setOnAction(event -> {
            if (!fieldValidators.validateRequired(CustomerName)) {showError("Please Insert Customer Name"); return;}
            if (!fieldValidators.validateRequired(Pax)) {showError("Please Insert Pax Size"); return;}
            if (!fieldValidators.isNonZeroNumeric(Pax)) {showError("Pax must be greater than zero."); return;}
            if (!fieldValidators.validateRequired(Phone)){ showError("Please Insert Phone No."); return;}
            if (!fieldValidators.startsWith09(Phone)) {showError("Please Insert Valid Phone No."); return;}
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
            targetReservation.getCustomer().setPhone(Phone.getText());
            targetReservation.getCustomer().setEmail(Email.getText());
            reservationRepository.save(targetReservation);
            // close dialog after adding
            dialogStage.close();

        });

    }
}
