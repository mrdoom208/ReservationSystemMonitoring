package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.repository.ManageTablesRepository;
import com.mycompany.reservationsystem.util.FieldRestrictions;
import com.mycompany.reservationsystem.util.FieldValidators;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class editTableDialogController {
    @FXML
    private ManageTables targetTable;

    public void setTargetTable(ManageTables TargetTable){
        this.targetTable = TargetTable;
        tableNumberField.setText(targetTable.getTableNo());
        tableCapacityField.setText(String.valueOf(targetTable.getCapacity()));
        tableLocationField.setText(targetTable.getLocation());
        statusComboBox.setValue(targetTable.getStatus());
    }
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

        tableNumberField.setDisable(true);

        cancelButton.setOnAction(e -> dialogStage.close());

        addButton.setOnAction(e -> {
            if (!fieldValidators.validateRequired(tableNumberField)) {showError("Please Insert Table Number"); return;}
            if (!fieldValidators.validateRequired(tableCapacityField)) {showError("Please Insert Table Capacity"); return;}
            if (!fieldValidators.validateRequired(tableLocationField)){showError("Please Insert Table Location"); return;}

            // handle add table logic here
            showSuccess("Table Changed Successfully");

            targetTable.setCapacity(Integer.parseInt(tableCapacityField.getText()));
            targetTable.setTableNo(tableNumberField.getText());
            targetTable.setLocation(tableLocationField.getText());
            targetTable.setStatus(statusComboBox.getValue());
            manageTablesRepository.save(targetTable);
            // close dialog after adding
            dialogStage.close();
        });
    }
}
