package com.mycompany.reservationsystem.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for loading FXML views asynchronously in the background
 * to prevent UI blocking and improve application performance.
 */
public class BackgroundViewLoader {

    private final ConfigurableApplicationContext springContext;
    private final Map<String, Object> controllerCache = new HashMap<>();
    private final Map<String, Node> viewCache = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public BackgroundViewLoader(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    /**
     * Load view asynchronously and display when ready
     * @param fxmlFile The FXML file path to load
     * @param content The StackPane container to display the view in
     * @param onComplete Optional callback to run after loading completes
     */
    public void loadViewAsync(String fxmlFile, StackPane content, Runnable onComplete) {
        // Check cache first
        Node cachedView = viewCache.get(fxmlFile);
        if (cachedView != null) {
            content.getChildren().setAll(cachedView);
            if (onComplete != null) onComplete.run();
            return;
        }

        // Show loading indicator
        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(50, 50);
        content.getChildren().setAll(loading);

        // Load in background
        Task<ViewLoadResult> loadTask = new Task<ViewLoadResult>() {
            @Override
            protected ViewLoadResult call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                loader.setControllerFactory(springContext::getBean);

                Node view = loader.load();
                Object controller = loader.getController();

                return new ViewLoadResult(view, controller);
            }
        };

        loadTask.setOnSucceeded(event -> {
            ViewLoadResult result = loadTask.getValue();

            // Cache the result
            viewCache.put(fxmlFile, result.view);
            controllerCache.put(fxmlFile, result.controller);

            // Display the view
            content.getChildren().setAll(result.view);

            if (onComplete != null) onComplete.run();
        });

        loadTask.setOnFailed(event -> {
            Throwable error = loadTask.getException();
            error.printStackTrace();
            // Show error message to user
            content.getChildren().clear();
        });

        executor.submit(loadTask);
    }

    /**
     * Preload views in background without displaying them
     * Useful for loading views the user is likely to navigate to
     * @param fxmlFile The FXML file path to preload
     */
    public void preloadView(String fxmlFile) {
        if (viewCache.containsKey(fxmlFile)) {
            return; // Already loaded
        }

        CompletableFuture.runAsync(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                loader.setControllerFactory(springContext::getBean);

                Node view = loader.load();
                Object controller = loader.getController();

                // Cache on JavaFX thread
                Platform.runLater(() -> {
                    viewCache.put(fxmlFile, view);
                    controllerCache.put(fxmlFile, controller);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    /**
     * Preload multiple views at once
     * @param fxmlFiles Variable number of FXML file paths to preload
     */
    public void preloadViews(String... fxmlFiles) {
        for (String fxmlFile : fxmlFiles) {
            preloadView(fxmlFile);
        }
    }

    /**
     * Get a cached controller instance
     * @param fxmlFile The FXML file path
     * @return The cached controller or null if not loaded
     */
    public Object getCachedController(String fxmlFile) {
        return controllerCache.get(fxmlFile);
    }

    /**
     * Clear all cached views and controllers
     * Useful for memory management
     */
    public void clearCache() {
        viewCache.clear();
        controllerCache.clear();
    }

    /**
     * Shutdown the executor service
     * Should be called when the application closes
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Inner class to hold load results
     */
    private static class ViewLoadResult {
        final Node view;
        final Object controller;

        ViewLoadResult(Node view, Object controller) {
            this.view = view;
            this.controller = controller;
        }
    }
}