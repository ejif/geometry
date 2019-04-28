package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Random;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.TestUtils;

public final class PointSetTest {

    @Test
    public void testFindClosestPoint_failsForEmptyPointSet() {
        PointSet points = new PointSet(ImmutableList.of());
        assertThatThrownBy(() -> points.findClosestPoint(new Point(0, 0))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test(timeout = 1000) // milliseconds
    public void testFindClosestPoint_isPerformant() {
        Random random = TestUtils.rng();
        // Use separate RNG for algorithm and generated points.
        PointSet points = new PointSet(TestUtils.randomPoints(5000, random), TestUtils.rng());
        for (Point p : TestUtils.randomPoints(100000, random))
            points.findClosestPoint(p);
    }
}
