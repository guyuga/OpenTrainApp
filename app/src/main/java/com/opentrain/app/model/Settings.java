package com.opentrain.app.model;

/**
 * Created by noam on 30/05/15.
 */
public class Settings {

    public static final long SCAN_INTERVAL_TEST = 2000;
    public static final long SCAN_INTERVAL_TRAIN = 15000;
    public static final String STATION_SSID_SIMULATOR = "WiredSSID";
    public static final String STATION_SSID_TRAIN = "S-ISRAEL-RAILWAYS";
    public static final String url_get_map_from_server = "http://gtfs.otrain.org/api/data/bssids/";
    public static final String url_add_map_to_server = "http://gtfs.otrain.org/data/submit";
    public static final String url_get_stops_from_server = "http://gtfs.otrain.org/api/gtfs/stops/?format=json";
    public static final String url_get_trips_from_server = "http://gtfs.otrain.org/api/gtfs/trips/date/today/?format=json";


    public static long SCAN_INTERVAL = SCAN_INTERVAL_TRAIN;
    public static String stationSSID = STATION_SSID_TRAIN;


    public static void setTestSettings() {
        SCAN_INTERVAL = SCAN_INTERVAL_TEST;
    }

    public static void setDefaultettings() {
        SCAN_INTERVAL = SCAN_INTERVAL_TRAIN;
        stationSSID = STATION_SSID_TRAIN;
    }

}
