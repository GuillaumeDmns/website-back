package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Line;

import java.util.List;
import java.util.stream.Collectors;

public class LinesDTO extends RATPResponse {

    private List<LineCustom> lines;

    public LinesDTO(List<Line> lines) {
        super();
        this.lines = lines.stream().map(LineCustom::new).collect(Collectors.toList());
    }

    public List<LineCustom> getLines() {
        return lines;
    }

    public void setLines(List<LineCustom> lines) {
        this.lines = lines;
    }
}
