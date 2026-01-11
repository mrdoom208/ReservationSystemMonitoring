package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.model.Reservation;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WebSocketClient {

    private final List<WebSocketListener> listeners = new ArrayList<>();
    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    private final String url; // configurable

    public WebSocketClient(String websocketUrl) {
        this.url = websocketUrl;
    }

    public void addListener(WebSocketListener listener) {
        listeners.add(listener);
    }

    public void connect() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

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
                                System.out.println("Websocket is ready!222");
                                return WebUpdateDTO.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                System.out.println("Websocket is ready!123");
                                WebUpdateDTO reservation = (WebUpdateDTO) payload;
                                reservation.setLink(generateLoginLink(reservation));


                                for (WebSocketListener l : listeners) {
                                    Platform.runLater(() -> l.onMessage(reservation));
                                }
                            }

                        });
                        System.out.println("Websocket is ready!");
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        System.err.println("WebSocket transport error: " + exception.getMessage());
                    }
                }).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.disconnect();
            }
            if (stompClient != null) {
                stompClient.stop();
            }
            System.out.println("WebSocket disconnected!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void send(Object payload) {
        String destination ="/app/forms";
        System.out.println("SENDSENSEND ");
        if (stompSession != null && stompSession.isConnected()) {
            System.out.println(stompSession);
            stompSession.send(destination, payload);
        } else {
            System.err.println("WebSocket not connected, cannot send message");
        }
    }

    public String generateLoginLink(WebUpdateDTO reservation) {
        String baseUrl = AppSettings.loadApplicationUrl(); // e.g., https://myrestaurant.com
        String phone = URLEncoder.encode(reservation.getPhone(), StandardCharsets.UTF_8);
        String reference = URLEncoder.encode(reservation.getReference(), StandardCharsets.UTF_8);

        return baseUrl + "/login?phone=" + phone + "&reference=" + reference;
    }
}
