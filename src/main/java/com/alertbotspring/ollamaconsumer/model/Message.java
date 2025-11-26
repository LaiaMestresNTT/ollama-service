package com.alertbotspring.ollamaconsumer.model;

import lombok.Data;

@Data
public class Message {

    private String role; // "user", "assistant", o "system"
    private String content;

    public Message() {
    }

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

}