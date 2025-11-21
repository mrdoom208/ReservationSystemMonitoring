package com.mycompany.reservationsystem.controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DeleteTableDialogController {

    @FXML private Button cancelBtn;
    @FXML private Button deleteBtn;

    private Runnable onDelete; // callback

    public void setOnDelete(Runnable action) {
        this.onDelete = action;
    }

    @FXML
    private void initialize() {

        cancelBtn.setOnAction(e ->
            cancelBtn.getScene().getWindow().hide()
        );

        deleteBtn.setOnAction(e -> {
            if (onDelete != null) onDelete.run();
            deleteBtn.getScene().getWindow().hide();
        });
    }
}