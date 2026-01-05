package com.mycompany.reservationsystem.util.dialog;

import com.mycompany.reservationsystem.controller.popup.ConfirmationDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class ConfirmationDialog {
    public static void show(String message, Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ConfirmationDialog.class.getResource(
                            "/fxml/popup/confirmationDialog.fxml"
                    )
            );

            Parent root = loader.load();
            ConfirmationDialogController controller = loader.getController();

            controller.setMessage(message);
            controller.setOnConfirm(onConfirm);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scn = new Scene(root);
            scn.setFill(Color.TRANSPARENT);
            stage.setScene(scn);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
