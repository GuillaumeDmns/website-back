package com.gdamiens.website.controller.object;


import java.util.Map;

public class FullIDFMDTO {

    private LineDTO line;

    private Map<Integer, NextPassagesStops> calls;

    public FullIDFMDTO(LineDTO line, Map<Integer, NextPassagesStops> calls) {
        this.line = line;
        this.calls = calls;
    }

    public FullIDFMDTO() {}

    public LineDTO getLine() {
        return line;
    }

    public void setLine(LineDTO line) {
        this.line = line;
    }

    public Map<Integer, NextPassagesStops> getCalls() {
        return calls;
    }

    public void setCalls(Map<Integer, NextPassagesStops> calls) {
        this.calls = calls;
    }
}
