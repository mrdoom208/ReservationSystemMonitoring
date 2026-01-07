package com.mycompany.reservationsystem;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import com.mycompany.reservationsystem.util.BackgroundViewLoader;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import io.github.palexdev.materialfx.MFXResourcesLoader;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import io.github.palexdev.materialfx.theming.base.Theme;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.springframework.beans.factory.annotation.Autowired;
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
    public BackgroundViewLoader backgroundLoader;
    public String ApplicationTitle;
    public WebSocketClient wsClient;




    @Override
    public void init() {
        // Start the Spring Boot context when the app launches
        springContext = new SpringApplicationBuilder(App.class).run();

    }


    @Override
    public void start(Stage stage) throws IOException {

        this.ApplicationTitle = AppSettings.loadApplicationTitle();
        // Either global for entire app
        App.primaryStage = stage;

        UserAgentBuilder.builder()

                .themes(JavaFXThemes.MODENA)                // keep default JavaFX theme too
                .themes(MaterialFXStylesheets.forAssemble(true)) // add MFX styles
                .setDeploy(true)     // if you have assets (fonts, images)
                .setResolveAssets(true)
                .build()
                .setGlobal();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/Login.fxml"));

        // Make controllers managed by Spring
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Regular.ttf"),14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lora-Bold.ttf"),14);

        scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        root.styleProperty().bind(
                Bindings.createStringBinding(() -> {
                    double referenceWidth = 1600;   // base width
                    double referenceHeight = 900;  // base height
                    double scale = Math.min(scene.getWidth() / referenceWidth, scene.getHeight() / referenceHeight);

                    double fontSize = Math.min(32, Math.max(14, 16 * scale)); // 16 is base font size
                    return "-fx-font-size: " + fontSize + "px;";
                }, scene.widthProperty(), scene.heightProperty())
        );

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle(ApplicationTitle);
        stage.show();
    }
    @Override
    public void stop() throws Exception{
        super.stop();
        springContext.close();

        if(backgroundLoader != null){
        backgroundLoader.shutdown();
        }
        if (wsClient != null) {
            wsClient.disconnect();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "true"); // enable LCD text smoothing
        System.setProperty("prism.text", "t2k");    // use TrueType text renderer
        launch(args);
    }
}
