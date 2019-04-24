
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.DirectedEdge;
import lombok.Data;

/**
 * Tests several randomly generated point configurations, and verifies that the regions are correct
 * (all points inside the region are closest to the point for that corresponding region).
 */
@Data
@RunWith(Parameterized.class)
public final class VoronoiRegionsTest {

    public static final int NUM_TEST_CASES = 10;
    public static final int MIN_NUM_POINTS = 5;
    public static final double MAX_COORDINATE = 1000;
    public static final double SLIGHT_RATIO = 1e-3;

    private final List<Point> points;

    @Parameterized.Parameters
    public static List<?> parameters() {
        Random random = new Random(2915);
        List<List<Point>> parameters = new ArrayList<>();
        for (int i = 0; i < NUM_TEST_CASES; i++) {
            int testCaseSize = MIN_NUM_POINTS + i;
            List<Point> points = new ArrayList<>();
            for (int j = 0; j < testCaseSize; j++)
                points.add(new Point(random.nextDouble() * MAX_COORDINATE, random.nextDouble() * MAX_COORDINATE));
            parameters.add(points);
        }
        return parameters;
    }

    @Test
    public void testGeneratedDiagram_hasCorrectRegions() {
        Voronoi.createVoronoiDiagram(points).toRegions().forEach((pointIndex, region) -> {
            Point p = points.get(pointIndex);
            // Verify that each vertex in this region, when moved slightly closer to the region's
            // point to break ties, is closest to the region's point than to any other point.
            for (DirectedEdge edge : region.getEdges())
                assertThatPoint(edge.toFiniteSegment(MAX_COORDINATE).getStartPoint()).movedSlightlyTowards(p).isClosestTo(p);
        });
    }

    private PointAssert assertThatPoint(Point point) {
        return new PointAssert(point);
    }

    @Data
    private final class PointAssert {

        final Point point;

        PointAssert movedSlightlyTowards(Point other) {
            return new PointAssert(new Point(
                other.x * SLIGHT_RATIO + point.x * (1 - SLIGHT_RATIO),
                other.y * SLIGHT_RATIO + point.y * (1 - SLIGHT_RATIO)));
        }

        void isClosestTo(Point expectedClosest) {
            Point closest = points.get(0);
            for (Point p : points)
                if (Points.distance(point, p) < Points.distance(point, closest))
                    closest = p;
            assertThat(closest).isEqualTo(expectedClosest);
        }
    }
}
