package com.mycompany.reservationsystem.config;

import java.util.prefs.Preferences;

public final class AppSettings {

    private static final Preferences PREFS =
            Preferences.userRoot().node("reservation-system");

    private AppSettings() {}
    //------------- GENERAL ---------------
    public static void saveApplicationTitle(String title) {
        String defaultTitle = "Romantic Baboy Reservation System";
        PREFS.put("ApplicationTitle", (title != null && !title.isBlank()) ? title : defaultTitle);
    }

    public static String loadApplicationTitle() {
        return PREFS.get("ApplicationTitle", "");
    }





    // -------- SERIAL --------
    public static void saveSerialPort(String portName) {
        PREFS.put("serial.port", portName != null ? portName : "");
    }

    public static String loadSerialPort() {
        return PREFS.get("serial.port", null);
    }

    // -------- DEVICE INFO --------
    public static void saveController(String value) {
        PREFS.put("device.controller", value != null ? value : "");
    }
    public static void saveCancelTime(String minute){PREFS.put("cancelTime", minute != null ? minute : "");}

    public static void saveModule(String value) {
        PREFS.put("device.module", value != null ? value : "");
    }

    public static void savePhone(String value) {
        PREFS.put("device.phone", value != null ? value : "");
    }

    public static String loadCancelTime() {
        return PREFS.get("cancelTime", "");
    }


    public static String loadController() {
        return PREFS.get("device.controller", "");
    }

    public static String loadModule() {
        return PREFS.get("device.module", "");
    }

    public static String loadPhone() {
        return PREFS.get("device.phone", "");
    }
    //----------------- MESSAGING --------------------------------
    public static void saveMessageLabel(String key, String value) {
        PREFS.put(key, value != null ? value : "");
    }

    // Load selected message label for a given reservation type
    public static String loadMessageLabel(String key) {
        return PREFS.get(key, "");
    }

    // Optional: helper keys
    public static final String NEW_RESERVATION_KEY = "message.new";
    public static final String CANCELLED_RESERVATION_KEY = "message.cancelled";
    public static final String CONFIRM_RESERVATION_KEY = "message.confirm";
    public static final String COMPLETE_RESERVATION_KEY = "message.complete";



    //--------------- DATABASE INFO -----------------
    public static void saveDatabaseDeleteTime(String value) {
        PREFS.put("database.deleteTime", value != null ? value : "");
    }

    public static String loadDatabaseDeleteTime() {
        return PREFS.get("database.deleteTime", "");
    }

}
