package com.gdamiens.website.controller.object;

import java.time.LocalDateTime;

public abstract class RATPResponse {

    private LocalDateTime currentDate;

    public RATPResponse() {
        this.currentDate = LocalDateTime.now();
    }

    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDateTime currentDate) {
        this.currentDate = currentDate;
    }

}
