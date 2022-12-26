package com.gdamiens.website.utils;

import com.gdamiens.website.controller.object.StopAreaCSV;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.List;

public class GeoUtils {

    private GeoUtils() {}

    public static void convertLambert94ToLatLong(List<StopAreaCSV> stopAreas) {

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem lambert93 = crsFactory.createFromName("epsg:2154");
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("epsg:4326");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform wgsToUtm = ctFactory.createTransform(lambert93, wgs84);

        stopAreas.parallelStream().forEach(stopAreaCSV -> {
            ProjCoordinate result = new ProjCoordinate();
            wgsToUtm.transform(new ProjCoordinate(stopAreaCSV.getLongitude(), stopAreaCSV.getLatitude()), result);
            stopAreaCSV.setLongitude(result.x);
            stopAreaCSV.setLatitude(result.y);
            }
        );
    }
}
