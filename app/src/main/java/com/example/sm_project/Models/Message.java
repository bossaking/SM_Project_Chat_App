package com.example.sm_project.Models;

public class Message {
    private String sender, receiver, messageContext, messageTime;

    public Message(String sender, String receiver, String messageContext, String messageTime){
        this.sender = sender;
        this.receiver = receiver;
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
