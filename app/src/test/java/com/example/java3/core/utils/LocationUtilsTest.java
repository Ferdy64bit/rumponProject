package com.example.java3.core.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationUtilsTest {
    private static final double TANJUNG_ANOM_LAT = -6.0296;
    private static final double TANJUNG_ANOM_LON = 106.6504;
    private static final double TELUK_JAKARTA_LAT = -5.9600;
    private static final double TELUK_JAKARTA_LON = 106.8200;

    @Test
    public void calculateDistance_sameCoordinate_returnsZero() {
        double distance = LocationUtils.calculateDistance(
                TANJUNG_ANOM_LAT,
                TANJUNG_ANOM_LON,
                TANJUNG_ANOM_LAT,
                TANJUNG_ANOM_LON
        );

        assertEquals(0.0, distance, 0.0001);
    }

    @Test
    public void calculateDistance_reversedPoints_returnsSameDistance() {
        double forward = LocationUtils.calculateDistance(
                TANJUNG_ANOM_LAT,
                TANJUNG_ANOM_LON,
                TELUK_JAKARTA_LAT,
                TELUK_JAKARTA_LON
        );
        double backward = LocationUtils.calculateDistance(
                TELUK_JAKARTA_LAT,
                TELUK_JAKARTA_LON,
                TANJUNG_ANOM_LAT,
                TANJUNG_ANOM_LON
        );

        assertEquals(forward, backward, 0.0001);
    }

    @Test
    public void calculateDistance_nearbyCoastalPoints_returnsReasonableDistance() {
        double distance = LocationUtils.calculateDistance(
                TANJUNG_ANOM_LAT,
                TANJUNG_ANOM_LON,
                TELUK_JAKARTA_LAT,
                TELUK_JAKARTA_LON
        );

        assertTrue(distance > 0.0);
        assertTrue(distance > 15.0);
        assertTrue(distance < 30.0);
    }

    @Test
    public void calculateDistance_negativeLatitude_returnsFiniteDistance() {
        double distance = LocationUtils.calculateDistance(-6.2, 106.7, -6.4, 106.9);

        assertTrue(distance > 0.0);
        assertFalse(Double.isNaN(distance));
        assertFalse(Double.isInfinite(distance));
    }

    @Test
    public void calculateDistance_longDistance_returnsFiniteLargeDistance() {
        double jakartaLat = -6.2088;
        double jakartaLon = 106.8456;
        double tokyoLat = 35.6762;
        double tokyoLon = 139.6503;

        double distance = LocationUtils.calculateDistance(jakartaLat, jakartaLon, tokyoLat, tokyoLon);

        assertTrue(distance > 1000.0);
        assertFalse(Double.isNaN(distance));
        assertFalse(Double.isInfinite(distance));
    }
}
