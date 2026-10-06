package com.gdamiens.website.controller.object.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * @param mission SNCF mission code, when given
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimetableEntry(Instant time, String destination, String mission) {
}
