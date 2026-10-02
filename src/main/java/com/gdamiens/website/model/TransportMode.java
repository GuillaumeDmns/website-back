package com.gdamiens.website.model;

public enum TransportMode {
    BUS,
    NOCTILIEN,
    METRO,
    TRAM,
    TER,
    TRANSILIEN,
    RER;

    /**
     * GTFS route_type: 0 tram, 1 metro, 2 train (RER, TER or Transilien according to the IDFM agency), 3 bus,
     * 6 aerial lift and 7 funicular are shown with trams
     */
    public static TransportMode fromGtfs(Short routeType, String agencyName, String routeShortName) {
        if (routeType == null) {
            return BUS;
        }

        return switch (routeType) {
            case 0, 6, 7 -> TRAM;
            case 1 -> METRO;
            case 2 -> switch (agencyName == null ? "" : agencyName) {
                case "RER" -> RER;
                case "TER" -> TER;
                default -> TRANSILIEN;
            };
            default -> routeShortName != null && routeShortName.matches("N\\d+.*") ? NOCTILIEN : BUS;
        };
    }
}
