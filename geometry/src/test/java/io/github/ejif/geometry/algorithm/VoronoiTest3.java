
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import io.github.ejif.geometry.Point;

/**
 * Verifies that the Voronoi algorithm is performant.
 */
public final class VoronoiTest3 {

    public static final int NUM_POINTS = 10000;
    public static final double MAX_COORDINATE = 1000;

    @Test(timeout = 1000) // milliseconds
    public void testCreateVoronoiDiagram_isPerformant() {
        // Ensure that DEBUG logging, which is not performant, is disabled.
        assertThat(LoggerFactory.getLogger(Voronoi.class).isDebugEnabled()).isFalse();

        Random random = new Random(2915);
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < NUM_POINTS; i++)
            points.add(new Point(random.nextDouble() * MAX_COORDINATE, random.nextDouble() * MAX_COORDINATE));
        Voronoi.createVoronoiDiagram(points);
    }
}
