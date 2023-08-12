package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.StopsByLineDTO;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.model.Test;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopAreaService;
import com.gdamiens.website.service.IDFMStopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
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

import java.util.List;

@RestController
@RequestMapping("/api")
public class IDFMStopController {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopController.class);

    private final IDFMStopAreaService idfmStopAreaService;
    private final IDFMStopService idfmStopService;

    private final IDFMLineService idfmLineService;

    public IDFMStopController(IDFMStopAreaService idfmStopAreaService, IDFMStopService idfmStopService, IDFMLineService idfmLineService) {
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmStopService = idfmStopService;
        this.idfmLineService = idfmLineService;

    }

    @GetMapping("/stops-by-line")
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

    @PostMapping("/test")
    @Operation(summary = "Test", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Coordinate[]> createOrUpdate(Double lat, Double lon) {
        try {
            Test test = new Test();
            test.setId(3);
            GeometryFactory geometryFactory = new GeometryFactory();
            Coordinate coordinate = new Coordinate(lat, lon);
            Point point = geometryFactory.createPoint(coordinate);
            test.setGeog(point);

            Test createdTest = this.idfmStopService.createOrUpdate(test);

            return new ResponseEntity<>(createdTest.getGeog().getCoordinates(), HttpStatus.OK);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/test")
    @Operation(summary = "Test a", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Coordinate[]> get(Integer id) {
        try {
            Test test = this.idfmStopService.get(id);

            return new ResponseEntity<>(test.getGeog().getCoordinates(), HttpStatus.OK);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
