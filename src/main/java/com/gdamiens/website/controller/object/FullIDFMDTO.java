package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMLine;

import java.util.List;
import java.util.Map;

public class FullIDFMDTO {

    private IDFMLine line;

    private Map<String, Map<String, List<Call>>> calls;

    public FullIDFMDTO(IDFMLine line, Map<String, Map<String, List<Call>>> calls) {
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

    public Map<String, Map<String, List<Call>>> getCalls() {
        return calls;
    }

    public void setCalls(Map<String, Map<String, List<Call>>> calls) {
        this.calls = calls;
    }
}
