package com.gdamiens.website.controller.object.v2;

/**
 * Another stop area reachable on foot from this one.
 *
 * @param minTransferSeconds shortest transfer time given by the GTFS
 */
public record Connection(String id, String name, Integer minTransferSeconds) {
}
