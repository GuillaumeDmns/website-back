package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.Content;
import com.gdamiens.website.idfm.InfoChannelRef;
import com.gdamiens.website.idfm.InfoMessage;
import com.gdamiens.website.idfm.LineRef;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InfoMessageCustom {

    private String infoChannelRef;

    private String validUntilTime;

    private List<MessageCustom> message;

    private String line;


    public InfoMessageCustom(InfoMessage infoMessage) {
        infoChannelRef = Optional.ofNullable(infoMessage.getInfoChannelRef()).map(InfoChannelRef::getValue).orElse(null);
        validUntilTime = infoMessage.getValidUntilTime();
        message = Optional.ofNullable(infoMessage.getContent())
            .map(Content::getMessage)
            .map(messages -> messages
                .stream()
                .map(MessageCustom::new)
                .collect(Collectors.toList()))
            .orElse(null);
        line = Optional.ofNullable(infoMessage.getContent())
            .map(Content::getLineRef)
            .map(lineRefs -> lineRefs.get(0))
            .map(LineRef::getValue)
            .orElse(null);
    }

    public String getInfoChannelRef() {
        return infoChannelRef;
    }

    public void setInfoChannelRef(String infoChannelRef) {
        this.infoChannelRef = infoChannelRef;
    }

    public String getValidUntilTime() {
        return validUntilTime;
    }

    public void setValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public List<MessageCustom> getMessage() {
        return message;
    }

    public void setMessage(List<MessageCustom> message) {
        this.message = message;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
