package com.mycompany.reservationsystem.controller.popup;

import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.UserRepository;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.mycompany.reservationsystem.util.FieldRestrictions.applyLettersOnly;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class editAccountController {
    private User editUser;

    public void setEdituser(User edituser){
        this.editUser = edituser;
        firstname.setText(editUser.getFirstname());
        lastname.setText(editUser.getLastname());
        username.setText(editUser.getUsername());
        password.setText(editUser.getPassword());
        position.setValue(editUser.getPosition());
    }

    @FXML
    private MFXButton submit;

    @FXML
    private MFXButton cancel;

    @FXML
    private MFXTextField firstname;

    @FXML
    private MFXTextField lastname;

    @FXML
    private Label messageLabel;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXComboBox<User.Position> position;

    @FXML
    private MFXTextField username;

    private Stage dialogStage;

    @Autowired
    private UserRepository userRepository;

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
    private void initialize(){
        cancel.setOnAction(e ->dialogStage.close());

        applyLettersOnly(firstname);
        applyLettersOnly(lastname);
        position.setItems(FXCollections.observableArrayList(User.Position.values()));



        submit.setOnAction(e ->{
            if (!validateRequired(firstname)) {showError("Please Insert Firstname"); return;}
            if (!validateRequired(lastname)) {showError("Please Insert Lastname"); return;}
            if (!validateRequired(username)){showError("Please Insert Username"); return;}
            if (!validateRequired(password)){showError("Please Insert Password"); return;}
            showSuccess("Account Added Successfully");
            editUser.setFirstname(firstname.getText());
            editUser.setLastname(lastname.getText());
            editUser.setUsername(username.getText());
            editUser.setPassword(password.getText());
            editUser.setPosition(position.getValue());
            userRepository.save(editUser);
            dialogStage.close();

        });


    }

}
