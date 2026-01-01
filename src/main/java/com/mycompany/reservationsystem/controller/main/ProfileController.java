package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.UserRepository;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.mycompany.reservationsystem.util.FieldRestrictions.applyLettersOnly;
import static com.mycompany.reservationsystem.util.FieldValidators.markInvalid;
import static com.mycompany.reservationsystem.util.FieldValidators.validateRequired;

@Component
public class ProfileController {

    @Autowired
    private AdministratorUIController administratorUIController;

    private User currentuser;

    public void setCurrentuser(User currentuser) {
        this.currentuser = currentuser;
        position.setText(currentuser.getPosition().toString());
        firstname.setText(currentuser.getFirstname());
        lastname.setText(currentuser.getLastname());
        username.setText(currentuser.getUsername());
        password.setText(currentuser.getPassword());
    }
    private Stage DialogStage;

    public void setDialogStage(Stage stage) {
        this.DialogStage = stage;
    }

    @FXML
    private Label position;
    @FXML
    private MFXButton cancel;

    @FXML
    private MFXTextField firstname;

    @FXML
    private MFXTextField lastname;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXButton save;

    @FXML
    private MFXTextField username;

    @Autowired
    private UserRepository userRepository;

    @FXML
    private void initialize(){

        applyLettersOnly(firstname);
        applyLettersOnly(lastname);

        cancel.setOnAction(event -> DialogStage.close());

        save.setOnAction(event -> {
            if (!validateRequired(firstname)) {markInvalid(firstname); return;}
            if (!validateRequired(lastname)) {markInvalid(lastname); return;}
            if (!validateRequired(username)){markInvalid(username); return;}
            if (!validateRequired(password)){markInvalid(password); return;}

            currentuser.setFirstname(firstname.getText());
            currentuser.setLastname(lastname.getText());
            currentuser.setUsername(username.getText());
            currentuser.setPassword(password.getText());
            userRepository.save(currentuser);
            administratorUIController.setUser(currentuser);
            DialogStage.close();

        });

    }

}
