package com.mycompany.reservationsystem.transition;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class NodeTransition {
    public static void showSmooth(Node node) {
        node.setVisible(true);
        node.setOpacity(0);
        node.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), node);
        slide.setFromY(30);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }

}
