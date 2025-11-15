package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.dto.CustomerReservationDTO;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class WebSocketClient {
    
    private final List<ReservationListener> listeners = new ArrayList<>();

    public void addListener(ReservationListener listener) {
        listeners.add(listener);
    }
    public void connect() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("WebSocket connected!");
                session.subscribe("/topic/forms", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return CustomerReservationDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        CustomerReservationDTO reservation = (CustomerReservationDTO) payload;
                        System.out.println("New reservation: " + reservation);
                        
                        for (ReservationListener l : listeners) {
                            Platform.runLater(() -> l.onNewReservation(reservation));
                        }
                        
                    }
                });
            }
        });

        
    }

    
}
