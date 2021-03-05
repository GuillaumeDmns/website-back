package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Line;

import java.util.List;

public class LinesDTO extends RATPResponse {

    private List<Line> lines;

    public LinesDTO(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
