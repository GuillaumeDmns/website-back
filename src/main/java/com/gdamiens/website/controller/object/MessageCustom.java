package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.Message;
import com.gdamiens.website.idfm.MessageText;

import java.util.Optional;

public class MessageCustom {

    private String messageType;

    private String messageText;

    public MessageCustom(Message message) {
        messageType = message.getMessageType();
        messageText = Optional.ofNullable(message.getMessageText()).map(MessageText::getValue).orElse(null);
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
