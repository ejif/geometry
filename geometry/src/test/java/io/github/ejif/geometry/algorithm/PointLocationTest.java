package io.github.ejif.geometry.algorithm;

import java.util.Random;

import org.junit.Test;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.TestUtils;

public final class PointLocationTest {

    @Test(timeout = 1000) // milliseconds
    public void testPointLocation_isPerformant() {
        Random random = TestUtils.rng();
        // Use separate RNG for algorithm and generated points.
        PointLocation pointLocation = new PointLocation(TestUtils.randomPoints(5000, random), TestUtils.rng());
        for (Point p : TestUtils.randomPoints(100000, random))
            pointLocation.findClosestPoint(p);
    }
}
