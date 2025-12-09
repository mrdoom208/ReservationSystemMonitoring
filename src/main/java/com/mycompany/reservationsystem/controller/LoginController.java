/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.controller;
import com.mycompany.reservationsystem.Service.ActivityLogService;
import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.ActivityLogRepository;
import com.mycompany.reservationsystem.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.*;
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
    private Button Staff,Admin,Submit,activeButton;
    @FXML
    private MFXTextField usernamefield;
    @FXML
    private MFXPasswordField passwordfield;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConfigurableApplicationContext springContext;
    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private ActivityLogService activityLogService;
    private boolean manager = false;
    
    
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
        
        if(clicked == Staff){

            manager = false;
            
            
        }else{

            manager = true;
            
        }
            
    }
    
    @FXML
    private void initialize() {
        // Set default active button
        activeButton = Admin;
        Admin.fire();
        
        Submit.setDefaultButton(true);
        
        
    }
    
    @FXML
    public void SubmitButton(ActionEvent event){
        Button clicked = Submit;
        String defaultstyle = "-fx-background-color: linear-gradient(to bottom, #FF0000, #FF3333);";
        clicked.setStyle("-fx-background-color: rgba(255, 0, 0, 0.2);");
        PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
        pause.setOnFinished(e -> clicked.setStyle(defaultstyle));
        pause.play();
            
        String userf = usernamefield.getText();
        String passf = passwordfield.getText();
        
        User found = userRepository.findByUsernameAndPassword(userf, passf);
        System.out.println();
        if (found != null) {
            
            try {
                
                System.out.println("Login Successfully");
                    found.setStatus("Active");
                    userRepository.save(found);
                    String fxmlFile = "fxml/AdministratorUI.fxml";    
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../"+fxmlFile));
                    loader.setControllerFactory(springContext::getBean);
                    Parent root = loader.load();
                    AdministratorUIController controller = loader.getController();
                    controller.setUser(found);

                activityLogService.logAction(
                        found.getUsername(),                    // username
                        found.getPosition().toString(),        // position/role
                        "Account",                                     // module
                        "Login",                             // action
                        String.format(
                                "User %s %s signed in",
                                found.getFirstname(),
                                found.getLastname()                 // old table status
                        )
                );




                    Stage stage = new Stage();
         

                    Scene scene = new Scene(root);

                root.styleProperty().bind(
                        Bindings.createStringBinding(() -> {
                            double referenceWidth = 1600;   // base width
                            double referenceHeight = 900;  // base height

                            // Scale factor based on BOTH width and height
                            double scale = Math.min(scene.getWidth() / referenceWidth, scene.getHeight() / referenceHeight);

                            // Apply scaling to a base font size
                            double fontSize = Math.min(32, Math.max(14, 16 * scale)); // 16 is base font size

                            return "-fx-font-size: " + fontSize + "px;";
                        }, scene.widthProperty(), scene.heightProperty())
                );
                    stage.setScene(scene);
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setMaximized(true);

                    stage.setResizable(true);
                    stage.setMinWidth(1300);
                    stage.setMinHeight(720);
                    stage.centerOnScreen();
                   

                    stage.show();
                     ((Node) event.getSource()).getScene().getWindow().hide();

                }catch (IOException e) {
                    e.printStackTrace();
                }    
                }else{
                    System.out.println("Wrong Username or Password");
                    User user = new User();
                    user.setUsername(userf);
                    user.setPassword(passf);
                    userRepository.save(user);
                }

            }
        }
    
    
    

