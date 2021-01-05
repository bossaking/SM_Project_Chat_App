package com.example.sm_project.Models;

public class Message {
    private String sender, messageContext, messageTime;

    public Message(String sender, String messageContext, String messageTime){
        this.sender = sender;
        this.messageContext = messageContext;
        this.messageTime = messageTime;
    }

    public Message(){

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(String messageContext) {
        this.messageContext = messageContext;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}
