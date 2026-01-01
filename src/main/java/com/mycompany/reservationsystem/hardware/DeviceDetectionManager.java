package com.mycompany.reservationsystem.hardware;

import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class DeviceDetectionManager {

    private SerialPort port;
    private BufferedReader in;
    private OutputStream out;

    // ================= PORT LIFECYCLE =================

    public synchronized void openPort(SerialPort selectedPort, int baudRate) throws Exception {
        if (selectedPort == null) throw new IllegalArgumentException("No serial port selected");

        if (isPortOpen()) return;

        port = selectedPort;
        port.setBaudRate(baudRate);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);

        if (!port.openPort()) throw new IllegalStateException("Unable to open serial port");

        in = new BufferedReader(new InputStreamReader(port.getInputStream(), StandardCharsets.US_ASCII));
        out = port.getOutputStream();

        // Wait a moment for Pico to boot and print READY
        TimeUnit.MILLISECONDS.sleep(1500);
    }

    public synchronized boolean isPortOpen() {
        return port != null && port.isOpen();
    }

    public synchronized void closePort() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (port != null && port.isOpen()) port.closePort();
        } catch (Exception ignored) {
        } finally {
            in = null;
            out = null;
            port = null;
        }
    }

    // ================= DEVICE DETECTION =================

    public DeviceResult detectDevice() throws Exception {
        ensureOpen();

        // Wait for READY from Pico (or retry handshake)
        String ready = readLine(5000);
        if (ready == null || (!ready.contains("READY") && !ready.contains("PICO_READY"))) {
            sendCommand("PING");
            String pong = readLine(2000);
            if (pong == null || !pong.equals("PONG")) {
                throw new IllegalStateException("Handshake failed: no response from Pico");
            }
        }

        String module = detect("GET_MODULE");
        String phone = detect("GET_PHONE");
        String controller = detect("GET_CONTROLLER");

        return new DeviceResult(module, phone, controller);
    }

    private String detect(String command) {
        try {
            flushInput();
            sendCommand(command);
            return readLine(2000);
        } catch (Exception e) {
            return null;
        }
    }

    // ================= SMS =================

    public synchronized void sendMessage(String phone, String message) throws Exception {
        ensureOpen();

        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("Invalid phone number");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message cannot be empty");

        flushInput();

        // MicroPython expects: SMS <phone> <message>
        sendCommand("SMS " + phone + " " + message);

        // Wait for Pico confirmation (+CMGS:<id>)
        String response = readLine(10000);
        if (response == null || !response.contains("+CMGS")) {
            throw new IllegalStateException("SMS failed: no confirmation from Pico");
        }
    }

    // ================= LOW LEVEL =================

    private void sendCommand(String cmd) throws Exception {
        if (out == null) throw new IllegalStateException("Output stream not initialized");
        out.write((cmd + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private String readLine(long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (in != null && in.ready()) {
                String line = in.readLine();
                if (line != null) {
                    line = line.trim();
                    if (!line.isEmpty()) return line;
                }
            }
            TimeUnit.MILLISECONDS.sleep(10);
        }
        return null;
    }

    private void flushInput() throws Exception {
        while (in != null && in.ready()) {
            in.readLine(); // discard any leftover lines
        }
    }

    private void ensureOpen() {
        if (!isPortOpen()) throw new IllegalStateException("Serial port not open");
    }

    // ================= RESULT =================

    public static class DeviceResult {
        public final String module;
        public final String phone;
        public final String controller;

        public DeviceResult(String module, String phone, String controller) {
            this.module = module;
            this.phone = phone;
            this.controller = controller;
        }

        @Override
        public String toString() {
            return "DeviceResult{" +
                    "module='" + module + '\'' +
                    ", phone='" + phone + '\'' +
                    ", controller='" + controller + '\'' +
                    '}';
        }
    }
}
