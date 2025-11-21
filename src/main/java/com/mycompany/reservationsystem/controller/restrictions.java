/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.controller;

import javafx.scene.control.TextField;

/**
 *
 * @author formentera
 */
public class restrictions {
    
    public void makeNumeric(TextField field) {
    field.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) {
            field.setText(newValue.replaceAll("[^\\d]", ""));
        }
    });
    }
    
    private void makeLetterOnly(TextField field) {
    field.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-Z]*")) {
            field.setText(newValue.replaceAll("[^a-zA-Z]", ""));
        }
    });
    }
    
    private void makeAlphaNumericWithSpace(TextField field) {
    field.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-Z0-9 ]*")) {
            field.setText(newValue.replaceAll("[^a-zA-Z0-9 ]", ""));
        }
    });
    }
    
}
