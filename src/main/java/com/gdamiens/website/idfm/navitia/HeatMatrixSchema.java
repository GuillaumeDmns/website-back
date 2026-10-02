package com.gdamiens.website.idfm.navitia;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class HeatMatrixSchema {

    private List<LineHeadersSchema> lineHeaders;

    @JsonProperty("lines")
    private List<LinesSchema> lines;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("lineHeaders")
    public List<LineHeadersSchema> getLineHeaders() {
        return lineHeaders;
    }

    @JsonProperty("line_headers")
    public void setLineHeaders(List<LineHeadersSchema> lineHeaders) {
        this.lineHeaders = lineHeaders;
    }

    @JsonProperty("lines")
    public List<LinesSchema> getLines() {
        return lines;
    }

    @JsonProperty("lines")
    public void setLines(List<LinesSchema> lines) {
        this.lines = lines;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}