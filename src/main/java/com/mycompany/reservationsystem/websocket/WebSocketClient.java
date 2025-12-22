package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.dto.WebupdateDTO;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WebSocketClient {

    private final List<ReservationListener> listeners = new ArrayList<>();

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    private final String url = "ws://localhost:8080/ws";

    public void addListener(ReservationListener listener) {
        listeners.add(listener);
    }

    /**
     * Connects to the WebSocket server
     */
    public void connect() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Connect in a separate thread
        new Thread(() -> {
            try {
                stompClient.connect(url, new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        System.out.println("WebSocket connected!");
                        stompSession = session;

                        session.subscribe("/topic/forms", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return WebupdateDTO.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                WebupdateDTO reservation = (WebupdateDTO) payload;
                                for (ReservationListener l : listeners) {
                                    Platform.runLater(() -> l.onNewReservation(reservation));
                                }
                            }
                        });
                    }
                }).get(); // Wait for connection
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Disconnects from the WebSocket server
     */
    public void disconnect() {
        try {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.disconnect();
                System.out.println("WebSocket disconnected!");
            }
            if (stompClient != null) {
                stompClient.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
