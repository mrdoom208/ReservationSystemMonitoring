package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
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
                        stompSession = session;
                        System.out.println("WebSocket connected!");

                        session.subscribe("/topic/forms", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return WebUpdateDTO.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                WebUpdateDTO reservation = (WebUpdateDTO) payload;
                                for (WebSocketListener l : listeners) {
                                    Platform.runLater(() -> l.onMessage(reservation));
                                }
                            }
                        });
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
}
