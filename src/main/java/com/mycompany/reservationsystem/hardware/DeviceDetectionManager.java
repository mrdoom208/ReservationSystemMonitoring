package com.mycompany.reservationsystem.hardware;

import com.fazecast.jSerialComm.SerialPort;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Device detection manager using JavaFX Task for async UI-friendly updates.
 */
public class DeviceDetectionManager {

    private SerialPort port;
    private BufferedReader in;
    private OutputStream out;

    public DeviceDetectionManager() {}

    /**
     * Creates a Task that performs device detection.
     * @return Task<DeviceResult>
     */
    public Task<DeviceResult> createDetectionTask() {
        return new Task<>() {
            @Override
            protected DeviceResult call() throws Exception {

                for (SerialPort p : SerialPort.getCommPorts()) {
                    try {
                        if (!p.openPort()) continue;

                        port = p;
                        port.setBaudRate(115200);
                        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);

                        in = new BufferedReader(new InputStreamReader(port.getInputStream()));
                        out = port.getOutputStream();

                        // Handshake
                        String ready = readLine(2000);
                        if (!"PICO_READY".equals(ready)) {
                            send("PING");
                            if (!"PONG".equals(readLine(1000))) {
                                port.closePort();
                                continue;
                            }
                        }

                        // Initialize results as null
                        String module = null;
                        String phone = null;
                        String controller = null;

                        // Flush input before each command
                        flushInput();

                        // Module detection
                        try {
                            send("GET_MODULE");
                            module = readLine(1500);
                            if (module.isEmpty()) module = null;
                        } catch (Exception ex) {
                            module = null;
                        }

                        flushInput();

                        // Phone detection
                        try {
                            send("GET_PHONE");
                            phone = readLine(1500);
                            if (phone.isEmpty()) phone = null;
                        } catch (Exception ex) {
                            phone = null;
                        }

                        flushInput();

                        // Controller detection
                        try {
                            send("GET_CONTROLLER");
                            controller = readLine(1500);
                            if (controller.isEmpty()) controller = null;
                        } catch (Exception ex) {
                            controller = null;
                        }

                        port.closePort();

                        // Return partial results (null = not detected)
                        return new DeviceResult(module, phone, controller);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                throw new RuntimeException("No compatible device found.");
            }
        };
    }

    // Helper to clear leftover serial data
    private void flushInput() {
        try {
            while (in.ready()) in.readLine();
        } catch (Exception ignored) {}
    }


    // --------------------------------------------------
    private void send(String cmd) throws Exception {

        out.write((cmd + "\n").getBytes());
        out.flush();
    }

    private String readLine(long timeoutMs) {
        long start = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() - start < timeoutMs) {
                if (in.ready()) {
                    return in.readLine().trim();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    public static String cleanPhoneResponse(String raw) {
        if (raw == null || raw.isEmpty()) return "NO_RESPONSE";
        String[] tokens = raw.split(",");
        String phone = null;
        for (String token : tokens) {
            token = token.replace("\"", "").trim();
            if (token.startsWith("+63")) {
                phone = token;
                break;
            }
        }
        boolean ok = raw.toUpperCase().contains("OK");
        if (phone == null) phone = "NO SIMCARD DETECTED";
        return ok ? phone + " OK" : phone;
    }

    // ----------------- Result holder -----------------
    public static class DeviceResult {
        public final String module;
        public final String phone;
        public final String controller;

        public DeviceResult(String module, String phone, String controller) {
            this.module = module;
            this.phone = phone;
            this.controller = controller;
        }
    }

}
