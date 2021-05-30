package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.RATPLine;

import java.util.List;

public class LinesDTO extends RATPResponse {

    private List<RATPLine> lines;

    public LinesDTO(List<RATPLine> lines) {
        super();
        this.lines = lines;
    }

    public List<RATPLine> getLines() {
        return lines;
    }

    public void setLines(List<RATPLine> lines) {
        this.lines = lines;
    }
}
