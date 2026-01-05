/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.controller;
import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import com.mycompany.reservationsystem.service.ActivityLogService;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.ActivityLogRepository;
import com.mycompany.reservationsystem.repository.UserRepository;
import java.io.IOException;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;


/**
 *
 * @author formentera
 */

@Component
public class LoginController {
    
    @FXML
    private Button Staff,Admin,Submit,activeButton,closebtn;
    @FXML
    private MFXTextField usernamefield;
    @FXML
    private MFXPasswordField passwordfield;
    @FXML
    private Label messageLabel;
    @FXML
    private StackPane dragArea;

    private ProgressIndicator buttonSpinner;
    private String originalButtonText;
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConfigurableApplicationContext springContext;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private ActivityLogService activityLogService;
    private boolean manager = false;
    private double xOffset = 0;
    private double yOffset = 0;
    
    
    @FXML
    public void Colorchange(ActionEvent event){
        Button clicked = (Button) event.getSource();
        Button buttons[] = {Admin, Staff}; 
        
        for (Button btn : buttons) {
            if (btn == null) continue;
            btn.getStyleClass().remove("login-button-active");
            if (!btn.getStyleClass().contains("login-button")) {
                btn.getStyleClass().add("login-button");
                }   
        }

        clicked.getStyleClass().remove("login-button");
        if (!clicked.getStyleClass().contains("login-button-active")) {
            clicked.getStyleClass().add("login-button-active");
        }

            
    }

    
    @FXML
    private void initialize() {
        // Set default active button
        Submit.setDefaultButton(true);

        dragArea.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        dragArea.setOnMouseDragged(event -> {
            App.primaryStage.setX(event.getScreenX() - xOffset);
            App.primaryStage.setY(event.getScreenY() - yOffset);
        });




        
        
    }
    public void closeApp(ActionEvent event) {
        Platform.exit();     // cleanly shuts down JavaFX
        System.exit(0);      // ensures JVM exits
    }

    @FXML
    public void SubmitButton(ActionEvent event) {
        String userf = usernamefield.getText();
        String passf = passwordfield.getText();

        showButtonLoading();

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return userRepository.findByUsernameAndPassword(userf, passf);
            }
        };

        loginTask.setOnSucceeded(e -> {
            hideButtonLoading();

            User found = loginTask.getValue();
            if (found == null) {
                showError("Wrong Username or Password");
                return;
            }

            try {
                showSuccess("Login Successfully, Welcome Back!");

                found.setStatus("Active");
                userRepository.save(found);

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/main/AdministratorUI.fxml")
                );
                loader.setControllerFactory(springContext::getBean);
                Parent root = loader.load();

                AdministratorUIController controller = loader.getController();
                controller.setUser(found);

                Stage stage = new Stage();
                stage.setTitle(AppSettings.loadApplicationTitle());
                Scene scene = new Scene(root);
                stage.setScene(scene);
                root.styleProperty().bind(
                        Bindings.createStringBinding(() -> {
                            double referenceWidth = 1600;   // base width
                            double referenceHeight = 900;  // base height
                            double scale = Math.min(scene.getWidth() / referenceWidth, scene.getHeight() / referenceHeight);

                            double fontSize = Math.min(32, Math.max(14, 16 * scale)); // 16 is base font size
                            return "-fx-font-size: " + fontSize + "px;";
                        }, scene.widthProperty(), scene.heightProperty())
                );
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setMaximized(true);
                stage.show();

                ((Node) event.getSource()).getScene().getWindow().hide();

            } catch (IOException ex) {
                ex.printStackTrace();
                showError("Failed to load dashboard");
            }
        });

        loginTask.setOnFailed(e -> {
            hideButtonLoading();
            showError("Login failed");
            loginTask.getException().printStackTrace();
        });

        new Thread(loginTask, "login-task").start();
    }

    private void showButtonLoading() {
        originalButtonText = Submit.getText();

        if (buttonSpinner == null) {
            buttonSpinner = new ProgressIndicator();
            buttonSpinner.setMaxSize(18, 18);
        }

        Submit.setText("");
        Submit.setGraphic(buttonSpinner);
        Submit.setDisable(true);
    }

    private void hideButtonLoading() {
        Submit.setGraphic(null);
        Submit.setText(originalButtonText);
        Submit.setDisable(false);
    }

    private void showError(String message) {
        messageLabel.getStyleClass().removeAll("login-success", "login-message-hidden");
        messageLabel.getStyleClass().add("login-error");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("login-error", "login-message-hidden");
        messageLabel.getStyleClass().add("login-success");
        messageLabel.setText(message);
    }

    private void hideMessage() {
        messageLabel.getStyleClass().add("login-message-hidden");
        messageLabel.setText("");
    }
}
    
    
    

