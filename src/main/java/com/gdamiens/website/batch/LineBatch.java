package com.gdamiens.website.batch;

import com.gdamiens.website.service.RATPLineService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LineBatch {

    private final RATPLineService ratpLineService;

    public LineBatch(RATPLineService ratpLineService) {
        this.ratpLineService = ratpLineService;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void launchLineBatch() {
        System.out.println("Start cron : Refreshing lines");
        ratpLineService.refreshLines(true);
        System.out.println("End cron : Refreshing lines");
    }
}
