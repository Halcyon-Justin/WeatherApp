package com.weather.app;

import com.weather.app.models.GeocodeData;
import static org.junit.Assert.assertEquals;


public class GeoCodeDataTestUtils {
    public static void assertGetGridIdEquals(String message, GeocodeData expected, GeocodeData actual, double delta) {
        assertEquals(message + " (lat)", expected.getLat(), actual.getLat(), delta);
        assertEquals(message + " (lng)", expected.getLng(), actual.getLng(), delta);
        assertEquals(message + " (gridId)", expected.getGridId(), actual.getGridId());
    }
}