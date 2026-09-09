package com.example.dispatch.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceCalculatorTest {

    private final DistanceCalculator calculator = new DistanceCalculator();

    @Test
    void distanceBetweenIdenticalPointsIsZero() {
        double distance = calculator.haversineDistanceKm(28.6139, 77.2090, 28.6139, 77.2090);
        assertEquals(0.0, distance, 0.0001);
    }

    @Test
    void distanceBetweenDelhiAndChennaiIsApproximatelyCorrect() {
        // Connaught Place, Delhi -> Anna Salai, Chennai. Known great-circle distance ~ 1760 km.
        double distance = calculator.haversineDistanceKm(28.6139, 77.2090, 13.0827, 80.2707);
        assertTrue(distance > 1700 && distance < 1820,
                "Expected distance to be roughly 1700-1820 km but was " + distance);
    }

    @Test
    void distanceIsSymmetric() {
        double d1 = calculator.haversineDistanceKm(28.7041, 77.1025, 28.5355, 77.3910);
        double d2 = calculator.haversineDistanceKm(28.5355, 77.3910, 28.7041, 77.1025);
        assertEquals(d1, d2, 0.0001);
    }

    @Test
    void shortLocalDistanceIsSmall() {
        // Karol Bagh -> Connaught Place, Delhi, roughly 4-6 km apart.
        double distance = calculator.haversineDistanceKm(28.7041, 77.1025, 28.6139, 77.2090);
        assertTrue(distance > 5 && distance < 20,
                "Expected a modest intra-city distance but was " + distance);
    }
}
