package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.TransportMode;

import java.util.List;
import java.util.Map;

public class LinesDTO {

    private Map<TransportMode, List<LineDTO>> lines;

    private Map<TransportMode, Integer> count;

    public LinesDTO(Map<TransportMode, List<LineDTO>> lines, Map<TransportMode, Integer> count) {
        this.lines = lines;
        this.count = count;
    }

    public Map<TransportMode, List<LineDTO>> getLines() {
        return lines;
    }

    public void setLines(Map<TransportMode, List<LineDTO>> lines) {
        this.lines = lines;
    }

    public Map<TransportMode, Integer> getCount() {
        return count;
    }

    public void setCount(Map<TransportMode, Integer> count) {
        this.count = count;
    }
}
