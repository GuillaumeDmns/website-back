package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.CallUnit;
import com.gdamiens.website.controller.object.FullIDFMDTO;
import com.gdamiens.website.controller.object.NextPassagesStops;
import com.gdamiens.website.controller.object.UnitIDFMDTO;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopAreaService;
import com.gdamiens.website.service.IDFMStopService;
import com.gdamiens.website.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class IDFMTimetableController {

    private static final Logger log = LoggerFactory.getLogger(IDFMTimetableController.class);

    private final IDFMLineService idfmLineService;

    private final IDFMStopService idfmStopService;

    private final IDFMStopAreaService idfmStopAreaService;

    public IDFMTimetableController(IDFMLineService idfmLineService,
                          IDFMStopService idfmStopService,
                          IDFMStopAreaService idfmStopAreaService
    ) {
        this.idfmLineService = idfmLineService;
        this.idfmStopService = idfmStopService;
        this.idfmStopAreaService = idfmStopAreaService;
    }

    @GetMapping("/full-idfm")
    @Operation(summary = "Get full next passages of all IDFM stops by line", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<FullIDFMDTO> getAllStopsByLine(String lineId) {
        try {
            Map<Integer, NextPassagesStops> calls = this.idfmLineService.getAllStopsByLine(lineId, Constants.IDFM_ESTIMATED_TIMETABLE_URL);

            IDFMLine idfmLine = this.idfmLineService.getLine(lineId);

            log.info("success requesting IDFM next passages");

            return new ResponseEntity<>(new FullIDFMDTO(idfmLine, calls), HttpStatus.OK);
        } catch (CustomException e) {
            log.info(e.getMessage());

            return new ResponseEntity<>(e.getHttpStatus());
        }
        catch (Exception e) {
            log.info("error during IDFM next passages request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/get-stop-next-passages")
    @Operation(summary = "Get next passages of a IDFM stop", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<UnitIDFMDTO> getStopInformation(@NotNull Integer stopId) {
        try {
            List<CallUnit> calls = this.idfmStopService.getStopNextPassage(stopId, Constants.IDFM_STOP_MONITORING_URL);

            IDFMStop idfmStop = this.idfmStopService.getStop(stopId);

            if (Optional.ofNullable(idfmStop).isEmpty()) {
                IDFMStopArea idfmStopArea = this.idfmStopAreaService.getStop(stopId);

                return new ResponseEntity<>(new UnitIDFMDTO(idfmStopArea, calls), HttpStatus.OK);
            }

            return new ResponseEntity<>(new UnitIDFMDTO(idfmStop, calls), HttpStatus.OK);
        } catch (CustomException e) {
            log.info(e.getMessage());

            return new ResponseEntity<>(e.getHttpStatus());
        } catch (Exception e) {
            log.info("error during IDFM stop information request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
