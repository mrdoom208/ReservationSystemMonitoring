package com.mycompany.reservationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 * JavaFX + Spring Boot Integrated App
 */
@SpringBootApplication
public class App extends Application {

    private static Scene scene;
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Start the Spring Boot context when the app launches
        springContext = new SpringApplicationBuilder(App.class).run();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/Login.fxml"));

        // Make controllers managed by Spring
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();

        scene = new Scene(root);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        stage.setScene(scene);
        stage.setTitle("Reservation System");
        stage.show();
    }

    @Override
    public void stop() {
        // Close Spring context when JavaFX stops
        springContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
