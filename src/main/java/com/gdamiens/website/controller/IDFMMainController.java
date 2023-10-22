package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.StopsByLineDTO;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.model.Test;
import com.gdamiens.website.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api")
public class IDFMMainController {

    private static final Logger log = LoggerFactory.getLogger(IDFMMainController.class);

    private final IDFMMainService idfmMainService;
    public IDFMMainController(IDFMMainService idfmMainService) {
        this.idfmMainService = idfmMainService;
    }

    @PostMapping("/gtfs")
    @Operation(summary = "Update all data with GTFS", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateGTFS() {
        try {
            String GTFSlink = this.idfmMainService.getGTFSlink();

            if (GTFSlink == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            ZipReader zipReader = new ZipReader();
            ZipInputStream zipInputStream = zipReader.readFromUrl(GTFSlink);

            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                log.info(fileName);
                new InputStreamReader(zipInputStream);
                InputStreamReader reader = new InputStreamReader(zipInputStream);
                zipEntry = zipInputStream.getNextEntry();
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during GTFS update");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
