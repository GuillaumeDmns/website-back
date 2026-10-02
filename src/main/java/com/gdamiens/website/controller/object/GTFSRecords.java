package com.gdamiens.website.controller.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GTFSRecords {

    private List<GTFSRecord> results;

    public List<GTFSRecord> getResults() {
        return results;
    }

    public void setResults(List<GTFSRecord> results) {
        this.results = results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GTFSRecord {

        private String filename;

        private GTFSFile url;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public GTFSFile getUrl() {
            return url;
        }

        public void setUrl(GTFSFile url) {
            this.url = url;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GTFSFile {

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
