package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.LineDTO;
import com.gdamiens.website.controller.object.StopsByLineDTO;
import com.gdamiens.website.model.IDFMStopGtfs;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopGtfsService;
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

    private final IDFMStopGtfsService idfmStopGtfsService;

    private final IDFMLineService idfmLineService;

    public IDFMStopController(IDFMStopGtfsService idfmStopGtfsService, IDFMLineService idfmLineService) {
        this.idfmStopGtfsService = idfmStopGtfsService;
        this.idfmLineService = idfmLineService;
    }

    @GetMapping("/stops-by-line")
    @Operation(summary = "Get list of stops by their line ID", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<StopsByLineDTO> getStopsByLineId(String lineId) {
        try {
            LineDTO requestedLine = this.idfmLineService.getLine(lineId);

            if (requestedLine == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<IDFMStopGtfs> idfmStops;

            switch (requestedLine.getTransportMode()) {
                case BUS:
                case METRO:
                case NOCTILIEN:
                case TER:
                case TRAM:
                    idfmStops = this.idfmStopGtfsService.getParentStopsFromLineId(lineId); // with parents
                    break;
                case RER:
                case TRANSILIEN:
                default:
                    idfmStops = this.idfmStopGtfsService.getStopsFromLineId(lineId);
                    break;
            }

            return new ResponseEntity<>(new StopsByLineDTO(idfmStops, this.idfmLineService.getLineShapeAsGeoJson(lineId)), HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM get stops by lineId request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
