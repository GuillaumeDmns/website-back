package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.StopsByLineDTO;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IDFMStopController {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopController.class);

    private final IDFMStopAreaService idfmStopAreaService;

    private final IDFMLineService idfmLineService;

    public IDFMStopController(IDFMStopAreaService idfmStopAreaService, IDFMLineService idfmLineService) {
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmLineService = idfmLineService;

    }

    @GetMapping("/stops")
    @Operation(summary = "Get list of stops by their line ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopsByLineDTO> getStopsByLineId(String lineId) {
        try {
            IDFMLine requestedLine = this.idfmLineService.getLine(lineId);

            if (requestedLine == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<IDFMStopArea> idfmStopAreas = this.idfmStopAreaService.getStopAreasFromLineId(lineId);

            return new ResponseEntity<>(new StopsByLineDTO(idfmStopAreas), HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM get stops by lineId request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
