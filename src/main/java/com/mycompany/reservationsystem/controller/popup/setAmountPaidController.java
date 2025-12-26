package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.util.FieldRestrictions;
import com.mycompany.reservationsystem.util.FieldValidators;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component
public class setAmountPaidController {
    @FXML
    private MFXTextField AmountPaid;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private Label messageLabel;

    @FXML
    private MFXTextField reference;

    @FXML
    private MFXButton setAmount;

    private Stage dialogStage;

    private boolean cancelled = true;
    private FieldValidators fieldValidators;
    private FieldRestrictions fieldRestrictions;
    @Autowired
    private ReservationService reservationService;

    public boolean isCancelled() {
        return cancelled;
    }
    public BigDecimal getAmount(){ return new BigDecimal(AmountPaid.getText());}

    public void setReference(String Reference){
        reference.setText(Reference);
    }
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

        FieldRestrictions.applyNumericDecimalNonZeroFilter(AmountPaid);

        cancelButton.setOnAction(e -> {
            cancelled = true;
            dialogStage.close();
        });
        setAmount.setOnAction(event -> {

            if(!FieldValidators.validateRequired(AmountPaid)) {showError("Please Insert Amount Paid"); return;}
            try {
                // Convert to BigDecimal
                BigDecimal amount = new BigDecimal(AmountPaid.getText());

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Amount must be greater than zero");
                    return;
                }

                // Format to 2 decimals
                DecimalFormat df = new DecimalFormat("0.00");
                String formatted = df.format(amount);

                // Optionally, overwrite the field with formatted value
                AmountPaid.setText(formatted);
                reservationService.setRevenueForReference(reference.getText(),amount);
                reservationService.setStatusForReference(reference.getText(),"Complete");
                reservationService.updateTableId(reference.getText(),null);


                showSuccess("Amount Paid Inserted Successfully");
                cancelled = false;
                dialogStage.close();

            } catch (NumberFormatException ex) {
                showError("Invalid amount format");
            }

        });

    }

}
