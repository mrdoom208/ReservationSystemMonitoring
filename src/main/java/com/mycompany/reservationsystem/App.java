package com.mycompany.reservationsystem;

import io.github.palexdev.materialfx.MFXResourcesLoader;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import io.github.palexdev.materialfx.theming.base.Theme;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.IOException;
import java.util.Set;

/**
 * JavaFX + Spring Boot Integrated App
 */
@SpringBootApplication
public class App extends Application {
    public static Stage primaryStage;
    private static Scene scene;
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Start the Spring Boot context when the app launches
        springContext = new SpringApplicationBuilder(App.class).run();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Either global for entire app
        App.primaryStage = stage;

        UserAgentBuilder.builder()

                .themes(JavaFXThemes.MODENA)                // keep default JavaFX theme too
                .themes(MaterialFXStylesheets.forAssemble(true)) // add MFX styles
                .setDeploy(true)     // if you have assets (fonts, images)
                .setResolveAssets(true)
                .build()
                .setGlobal();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/Login.fxml"));

        // Make controllers managed by Spring
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"),14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"),14);

        scene = new Scene(root);
        root.styleProperty().bind(
                javafx.beans.binding.Bindings.concat(
                        "-fx-font-size: ", scene.widthProperty().divide(100), "px;"
                )
        );

        stage.initStyle(StageStyle.UNDECORATED);
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
        System.setProperty("prism.lcdtext", "true"); // enable LCD text smoothing
        System.setProperty("prism.text", "t2k");    // use TrueType text renderer
        launch(args);
    }
}
