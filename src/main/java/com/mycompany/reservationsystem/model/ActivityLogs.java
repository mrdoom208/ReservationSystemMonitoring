package com.mycompany.reservationsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ActivityLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;

    LocalDateTime timestamp;
    String user;
    String action;
    String target;
    String value;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }
    public String getUser(){
            return user;
    }
    public void setUser(String User){
         user= User;
    }
    public String getTarget(){
        return target;
    }
    public void setTarget(String Target){
        target = Target;
    }
    public String getAction(){
        return action;
    }
    public void setAction(String Action){
        action = Action;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String Value){
        value = Value;
    }
    public LocalDateTime getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(LocalDateTime Timestamp){
        timestamp = Timestamp;
    }

}
