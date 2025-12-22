package com.mycompany.reservationsystem.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageLabel;


    private String messageDetails;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    //Constructor
    public Message() {
    }

    public Message(String messageLabel, String messageDetails) {
        this.messageLabel = messageLabel;
        this.messageDetails = messageDetails;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }




    // getters and setters
    public Long getId() {
        return id;
    }

    public String getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(String messageLabel) {
        this.messageLabel = messageLabel;
    }

    public String getMessageDetails() {
        return messageDetails;
    }

    public void setMessageDetails(String messageDetails) {
        this.messageDetails = messageDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}