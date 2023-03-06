package com.shahardror.wetrip;

public class ChatMessage {

    String message;
    String sender;
    String receiver;
    String time_sent;

    public ChatMessage(String message, String sender, String receiver, String time_sent) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.time_sent = time_sent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getTime_sent() {
        return time_sent;
    }

    public void setTime_sent(String time_sent) {
        this.time_sent = time_sent;
    }
}
