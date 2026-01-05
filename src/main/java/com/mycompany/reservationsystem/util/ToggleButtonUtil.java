package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.config.AppSettings;
import io.github.palexdev.materialfx.controls.MFXToggleButton;

public class ToggleButtonUtil {


    public static void setupToggle(MFXToggleButton toggle, String key) {
        // Load saved state
        boolean state = AppSettings.loadMessageEnabled(key);
        System.out.println(state + key);
        toggle.setSelected(state);
        toggle.setText(state ? "ON" : "OFF");

        toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggle.setText(newVal ? "ON" : "OFF");
        });

    }
}
