package com.mycompany.reservationsystem.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Modern Message Sender with minimizable progress tracking UI
 * Handles batch message sending with visual progress feedback
 */
public class MessageSenderUI {

    private final VBox container;
    private final VBox progressContainer;
    private final ScrollPane scrollPane;
    private final List<MessageProgressCard> activeCards;
    private BiConsumer<String, String> sendMessageFunction;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm:ss a");

    public MessageSenderUI() {
        this.activeCards = new ArrayList<>();
        this.container = createMainContainer();
        this.progressContainer = createProgressContainer();
        this.scrollPane = createScrollPane();
    }

    private VBox createMainContainer() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.TOP_CENTER);
        box.getStyleClass().add("message-sender-container");
        return box;
    }

    private VBox createProgressContainer() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));
        return box;
    }

    private ScrollPane createScrollPane() {
        ScrollPane scroll = new ScrollPane(progressContainer);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(400);
        scroll.getStyleClass().add("message-progress-scroll");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    /**
     * Set the function that will be called to send each message
     * @param sendFunction (phoneNumber, message) -> void
     */
    public void setSendMessageFunction(BiConsumer<String, String> sendFunction) {
        this.sendMessageFunction = sendFunction;
    }

    /**
     * Send messages to multiple recipients with progress tracking
     * @param phoneNumbers List of recipient phone numbers
     * @param message Message content to send
     * @param messageLabel Label/title for the message batch
     */
    public void sendMessages(List<String> phoneNumbers, String message, String messageLabel) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            showNotification("No recipients selected", false);
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            showNotification("Message content is empty", false);
            return;
        }

        // Create progress card for this batch
        MessageProgressCard card = new MessageProgressCard(
                phoneNumbers.size(),
                messageLabel,
                message
        );

        activeCards.add(card);
        Platform.runLater(() -> {
            progressContainer.getChildren().add(0, card.getRoot());
            if (!container.getChildren().contains(scrollPane)) {
                container.getChildren().add(scrollPane);
            }
        });

        // Send messages asynchronously
        CompletableFuture.runAsync(() -> sendBatchMessages(phoneNumbers, message, card));
    }

    private void sendBatchMessages(List<String> phoneNumbers, String message, MessageProgressCard card) {
        int total = phoneNumbers.size();
        int successful = 0;
        int failed = 0;

        for (int i = 0; i < phoneNumbers.size(); i++) {
            String phone = phoneNumbers.get(i);
            int currentIndex = i;

            try {
                // Call the actual send function
                if (sendMessageFunction != null) {
                    sendMessageFunction.accept(phone, message);
                }

                successful++;
                final int successCount = successful;
                Platform.runLater(() -> {
                    card.updateProgress(currentIndex + 1, total, successCount, failed);
                    card.addRecipient(phone, true);
                });

                // Small delay between messages to prevent overload
                Thread.sleep(100);

            } catch (Exception e) {
                failed++;
                final int failCount = failed;
                Platform.runLater(() -> {
                    card.updateProgress(currentIndex + 1, total, successful, failCount);
                    card.addRecipient(phone, false);
                });
            }
        }

        final int finalSuccess = successful;
        final int finalFailed = failed;

        Platform.runLater(() -> {
            card.markComplete(finalSuccess, finalFailed);
            if (finalFailed == 0) {
                showNotification("All messages sent successfully!", true);
            } else {
                showNotification(
                        String.format("Sent: %d, Failed: %d", finalSuccess, finalFailed),
                        false
                );
            }
        });
    }

    private void showNotification(String message, boolean success) {
        // This can be integrated with your existing notification system
        System.out.println((success ? "✓ " : "✗ ") + message);
    }

    /**
     * Get the main container to add to your UI
     */
    public VBox getContainer() {
        return container;
    }

    /**
     * Clear all completed message cards
     */
    public void clearCompleted() {
        activeCards.removeIf(card -> {
            if (card.isComplete()) {
                Platform.runLater(() -> progressContainer.getChildren().remove(card.getRoot()));
                return true;
            }
            return false;
        });
    }

    /**
     * Individual message batch progress card with minimize functionality
     */
    private class MessageProgressCard {
        private final VBox root;
        private final HBox header;
        private final VBox content;
        private final ProgressBar progressBar;
        private final Label statusLabel;
        private final Label progressLabel;
        private final Label timeLabel;
        private final VBox recipientList;
        private final Button minimizeBtn;
        private final Button closeBtn;

        private boolean minimized = false;
        private boolean complete = false;
        private final int totalRecipients;
        private final String messageLabel;
        private final LocalDateTime startTime;

        public MessageProgressCard(int totalRecipients, String messageLabel, String message) {
            this.totalRecipients = totalRecipients;
            this.messageLabel = messageLabel;
            this.startTime = LocalDateTime.now();

            this.root = new VBox(0);
            this.header = createHeader();
            this.content = createContent();
            this.progressBar = createProgressBar();
            this.statusLabel = createLabel("Sending messages...", "status-label");
            this.progressLabel = createLabel("0 / " + totalRecipients, "progress-label");
            this.timeLabel = createLabel(startTime.format(TIME_FORMATTER), "time-label");
            this.recipientList = new VBox(4);
            this.minimizeBtn = createMinimizeButton();
            this.closeBtn = createCloseButton();

            setupCard();
        }

        private void setupCard() {
            root.getStyleClass().add("message-progress-card");
            root.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1e293b, #334155);" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);" +
                            "-fx-padding: 0;"
            );

            // Setup header
            Label titleLabel = createLabel(messageLabel, "title-label");
            titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

            Circle statusCircle = new Circle(6);
            statusCircle.setFill(Color.web("#3b82f6"));
            statusCircle.getStyleClass().add("status-circle-animated");

            HBox leftHeader = new HBox(10, statusCircle, titleLabel, timeLabel);
            leftHeader.setAlignment(Pos.CENTER_LEFT);

            HBox rightHeader = new HBox(5, minimizeBtn, closeBtn);
            rightHeader.setAlignment(Pos.CENTER_RIGHT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(leftHeader, spacer, rightHeader);
            header.setStyle(
                    "-fx-background-color: rgba(30, 41, 59, 0.8);" +
                            "-fx-padding: 12;" +
                            "-fx-background-radius: 12 12 0 0;"
            );

            // Setup content
            VBox progressSection = new VBox(8);
            progressSection.setPadding(new Insets(15));

            HBox statusRow = new HBox(10, statusLabel, progressLabel);
            statusRow.setAlignment(Pos.CENTER_LEFT);

            progressSection.getChildren().addAll(statusRow, progressBar);

            ScrollPane recipientScroll = new ScrollPane(recipientList);
            recipientScroll.setFitToWidth(true);
            recipientScroll.setMaxHeight(150);
            recipientScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            recipientScroll.setPadding(new Insets(0, 15, 15, 15));

            content.getChildren().addAll(progressSection, recipientScroll);

            root.getChildren().addAll(header, content);
        }

        private HBox createHeader() {
            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER_LEFT);
            return box;
        }

        private VBox createContent() {
            VBox box = new VBox(0);
            return box;
        }

        private ProgressBar createProgressBar() {
            ProgressBar bar = new ProgressBar(0);
            bar.setPrefWidth(Double.MAX_VALUE);
            bar.setPrefHeight(8);
            bar.setStyle(
                    "-fx-accent: linear-gradient(to right, #3b82f6, #8b5cf6);" +
                            "-fx-background-radius: 4;" +
                            "-fx-background-insets: 0;"
            );
            return bar;
        }

        private Label createLabel(String text, String styleClass) {
            Label label = new Label(text);
            label.getStyleClass().add(styleClass);
            label.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
            return label;
        }

        private Button createMinimizeButton() {
            Button btn = new Button();
            FontIcon icon = new FontIcon(MaterialDesignC.CHEVRON_UP);
            icon.setIconColor(Color.web("#94a3b8"));
            icon.setIconSize(18);
            btn.setGraphic(icon);
            btn.getStyleClass().add("icon-button");
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 4;"
            );
            btn.setOnAction(e -> toggleMinimize());

            btn.setOnMouseEntered(e ->
                    btn.setStyle("-fx-background-color: rgba(148, 163, 184, 0.2); -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;")
            );
            btn.setOnMouseExited(e ->
                    btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;")
            );

            return btn;
        }

        private Button createCloseButton() {
            Button btn = new Button();
            FontIcon icon = new FontIcon(MaterialDesignC.CLOSE);
            icon.setIconColor(Color.web("#94a3b8"));
            icon.setIconSize(18);
            btn.setGraphic(icon);
            btn.getStyleClass().add("icon-button");
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 4;"
            );
            btn.setDisable(true); // Enable only when complete
            btn.setOnAction(e -> removeCard());

            btn.setOnMouseEntered(e -> {
                if (!btn.isDisabled()) {
                    btn.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;");
                }
            });
            btn.setOnMouseExited(e ->
                    btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;")
            );

            return btn;
        }

        private void toggleMinimize() {
            minimized = !minimized;

            FontIcon icon = (FontIcon) minimizeBtn.getGraphic();
            icon.setIconCode(minimized ?
                    MaterialDesignC.CHEVRON_DOWN :
                    MaterialDesignC.CHEVRON_UP
            );

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(content.prefHeightProperty(), minimized ? 0 : 200),
                            new KeyValue(content.opacityProperty(), minimized ? 0 : 1)
                    )
            );

            content.setVisible(!minimized);
            content.setManaged(!minimized);
            timeline.play();
        }

        public void updateProgress(int current, int total, int successful, int failed) {
            double progress = (double) current / total;
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%d / %d", current, total));
            statusLabel.setText(String.format("Sending... (✓ %d | ✗ %d)", successful, failed));
        }

        public void addRecipient(String phone, boolean success) {
            HBox recipientRow = new HBox(8);
            recipientRow.setAlignment(Pos.CENTER_LEFT);
            recipientRow.setPadding(new Insets(4));
            recipientRow.setStyle(
                    "-fx-background-color: rgba(51, 65, 85, 0.5);" +
                            "-fx-background-radius: 6;"
            );

            FontIcon icon = new FontIcon(success ?
                    MaterialDesignC.CHECK_CIRCLE :
                    MaterialDesignA.ALERT_CIRCLE
            );
            icon.setIconColor(success ? Color.web("#10b981") : Color.web("#ef4444"));
            icon.setIconSize(14);

            Label phoneLabel = new Label(phone);
            phoneLabel.setStyle(
                    "-fx-text-fill: " + (success ? "#d1fae5" : "#fecaca") + ";" +
                            "-fx-font-size: 11px;"
            );

            recipientRow.getChildren().addAll(icon, phoneLabel);
            recipientList.getChildren().add(0, recipientRow);
        }

        public void markComplete(int successful, int failed) {
            complete = true;
            closeBtn.setDisable(false);

            boolean allSuccess = failed == 0;
            statusLabel.setText(allSuccess ? "All messages sent!" : "Completed with errors");
            statusLabel.setStyle(
                    "-fx-text-fill: " + (allSuccess ? "#10b981" : "#f59e0b") + ";" +
                            "-fx-font-weight: bold; -fx-font-size: 12px;"
            );

            progressLabel.setText(String.format("✓ %d | ✗ %d", successful, failed));

            // Update status circle
            Circle statusCircle = (Circle) ((HBox) header.getChildren().get(0)).getChildren().get(0);
            statusCircle.setFill(allSuccess ? Color.web("#10b981") : Color.web("#f59e0b"));
            statusCircle.getStyleClass().remove("status-circle-animated");
        }

        private void removeCard() {
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(root.opacityProperty(), 0),
                            new KeyValue(root.scaleXProperty(), 0.8),
                            new KeyValue(root.scaleYProperty(), 0.8)
                    )
            );

            fadeOut.setOnFinished(e -> {
                progressContainer.getChildren().remove(root);
                activeCards.remove(this);
            });

            fadeOut.play();
        }

        public VBox getRoot() {
            return root;
        }

        public boolean isComplete() {
            return complete;
        }
    }
}