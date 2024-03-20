package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.*;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStopGtfs;
import com.gdamiens.website.service.IDFMGeneralMessageService;
import com.gdamiens.website.service.IDFMLineService;
import com.gdamiens.website.service.IDFMStopGtfsService;
import com.gdamiens.website.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IDFMTimetableController {

    private static final Logger log = LoggerFactory.getLogger(IDFMTimetableController.class);

    private final IDFMLineService idfmLineService;

    private final IDFMStopGtfsService idfmStopGtfsService;

    private final IDFMGeneralMessageService idfmGeneralMessageService;

    public IDFMTimetableController(IDFMLineService idfmLineService,
                                   IDFMStopGtfsService idfmStopGtfsService,
                          IDFMGeneralMessageService idfmGeneralMessageService
    ) {
        this.idfmLineService = idfmLineService;
        this.idfmStopGtfsService = idfmStopGtfsService;
        this.idfmGeneralMessageService = idfmGeneralMessageService;
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
    public ResponseEntity<UnitIDFMDTO> getStopInformation(@NotNull String stopId, String lineId) {
        try {
            IDFMStopGtfs idfmStopGtfs = this.idfmStopGtfsService.getStop(stopId);

            List<CallUnit> calls = this.idfmStopGtfsService.getStopNextPassage(stopId, lineId, Constants.IDFM_STOP_MONITORING_URL);

            return new ResponseEntity<>(new UnitIDFMDTO(idfmStopGtfs, calls), HttpStatus.OK);
        } catch (CustomException e) {
            log.info(e.getMessage());

            return new ResponseEntity<>(e.getHttpStatus());
        } catch (Exception e) {
            log.info("error during IDFM stop information request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/general-message")
    @Operation(summary = "Get messages displayed on screens", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<List<InfoMessageCustom>> getGeneralMessage(@RequestParam(required = false) String lineId, @RequestParam(required = false) Integer stopId) {
        try {

            if (lineId == null && stopId == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<InfoMessageCustom> generalMessageDelivery = this.idfmGeneralMessageService.getGeneralMessages(lineId, stopId, Constants.IDFM_GENERAL_MESSAGE_URL);

            return new ResponseEntity<>(generalMessageDelivery, HttpStatus.OK);
        } catch (CustomException e) {
            log.info(e.getMessage());

            return new ResponseEntity<>(e.getHttpStatus());
        } catch (Exception e) {
            log.info("error during IDFM stop information request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
