
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.ejif.geometry.DirectedEdge;
import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.TestUtils;
import lombok.Data;

/**
 * Tests several randomly generated point configurations, and verifies that the regions are correct
 * (all points inside the region are closest to the point for that corresponding region).
 */
@Data
@RunWith(Parameterized.class)
public final class VoronoiRegionsTest {

    public static final double MAX_NUM_STEPS = 1000;
    public static final double SLIGHT_RATIO = 1e-3;

    private final List<Point> points;

    @Parameterized.Parameters
    public static List<?> parameters() {
        Random random = TestUtils.rng();
        List<List<Point>> parameters = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            parameters.add(TestUtils.randomPoints(5 + i, random));
        for (int i = 0; i < 10; i++)
            parameters.add(TestUtils.randomLatticePoints(5 + i, random));
        return parameters;
    }

    @Test
    public void testGeneratedDiagram_hasCorrectRegions() {
        Voronoi.createVoronoiDiagram(points).toRegions().forEach((pointIndex, region) -> {
            Point p = points.get(pointIndex);
            // Verify that each vertex in this region, when moved slightly closer to the region's
            // point to break ties, is closest to the region's point than to any other point.
            for (DirectedEdge edge : region.getEdges()) {
                Point cornerPoint = edge.toFiniteSegment(MAX_NUM_STEPS).getStartPoint();
                assertThat(TestUtils.findClosestPoint(points, moveSlightlyTowards(cornerPoint, p))).isEqualTo(p);
            }
        });
    }

    private static Point moveSlightlyTowards(Point point, Point target) {
        return new Point(
            target.x * SLIGHT_RATIO + point.x * (1 - SLIGHT_RATIO),
            target.y * SLIGHT_RATIO + point.y * (1 - SLIGHT_RATIO));
    }
}
