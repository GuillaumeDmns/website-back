package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMLine;

import java.util.Map;

public class FullIDFMDTO {

    private IDFMLine line;

    private Map<Integer, NextPassagesStops> calls;

    public FullIDFMDTO(IDFMLine line, Map<Integer, NextPassagesStops> calls) {
        this.line = line;
        this.calls = calls;
    }

    public FullIDFMDTO() {}

    public IDFMLine getLine() {
        return line;
    }

    public void setLine(IDFMLine line) {
        this.line = line;
    }

    public Map<Integer, NextPassagesStops> getCalls() {
        return calls;
    }

    public void setCalls(Map<Integer, NextPassagesStops> calls) {
        this.calls = calls;
    }
}
