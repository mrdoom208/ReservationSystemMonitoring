package com.mycompany.reservationsystem.controller;

import com.mycompany.reservationsystem.constraint.FieldRestrictions;
import com.mycompany.reservationsystem.constraint.FieldValidators;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Validated;
import io.github.palexdev.materialfx.validation.MFXValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class addTableDialogController {

    @FXML
    private MFXTextField tableCapacityField;

    @FXML
    private MFXTextField tableNumberField;

    @FXML
    private MFXTextField tableLocationField;

    @FXML
    private MFXComboBox<String> statusComboBox;

    @FXML
    private MFXButton addButton;

    @FXML
    private MFXButton cancelButton;

    @FXML
    private Label messageLabel;

    @Autowired
    ManageTablesRepository manageTablesRepository;

    FieldValidators fieldValidators;
    FieldRestrictions fieldRestrictions;
    private Stage dialogStage;

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

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        // Optional: setup default values
        statusComboBox.getItems().addAll("Available", "Occupied");
        statusComboBox.setValue("Available");
        addButton.setDefaultButton(true);

        fieldRestrictions.applyNumbersOnly(tableCapacityField);
        fieldRestrictions.applyNumbersOnly(tableNumberField);




        cancelButton.setOnAction(e -> dialogStage.close());

        addButton.setOnAction(e -> {
            if (!fieldValidators.validateRequired(tableNumberField)) {showError("Please Insert Table Number"); return;}
            if (!fieldValidators.validateRequired(tableCapacityField)) {showError("Please Insert Table Capacity"); return;}
            if (!fieldValidators.validateRequired(tableLocationField)){showError("Please Insert Table Location"); return;}

            // handle add table logic here
            showSuccess("Table Added Successfully");
            ManageTables newTable=new ManageTables();
            newTable.setCapacity(Integer.parseInt(tableCapacityField.getText()));
            newTable.setTableNo(tableNumberField.getText());
            newTable.setId(Long.parseLong(tableNumberField.getText()));
            newTable.setLocation(tableLocationField.getText());
            newTable.setStatus(statusComboBox.getValue());
            manageTablesRepository.save(newTable);
            // close dialog after adding
            dialogStage.close();
        });
    }
}