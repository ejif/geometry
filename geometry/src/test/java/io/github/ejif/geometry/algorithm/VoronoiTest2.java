
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.VoronoiDiagram;
import io.github.ejif.geometry.VoronoiDiagram.Border;
import io.github.ejif.geometry.VoronoiDiagram.PointPair;
import lombok.Data;

/**
 * Tests several randomly generated point configurations, and verifies that the Voronoi diagram
 * satisfies expected properties (points on each side of every border in the Voronoi diagram are
 * closest to the point for that corresponding region).
 */
@Data
@RunWith(Parameterized.class)
public final class VoronoiTest2 {

    public static final int NUM_TEST_CASES = 10;
    public static final int MIN_NUM_POINTS = 5;
    public static final double MAX_COORDINATE = 1000;
    public static final double WIGGLE_RATIO = 1e-3;

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
    public void testGeneratedDiagram_hasCorrectVertices() {
        VoronoiDiagram diagram = Voronoi.createVoronoiDiagram(points);
        for (Border border : diagram.getBorders()) {
            PointPair pointPair = border.getPointPair();
            Point p1 = points.get(pointPair.getPointIndex1());
            Point p2 = points.get(pointPair.getPointIndex2());
            double x1 = p1.x;
            double y1 = p1.y;
            double x2 = p2.x;
            double y2 = p2.y;
            double startT = Math.max(border.getStartT(), -MAX_COORDINATE);
            double endT = Math.min(border.getEndT(), MAX_COORDINATE);
            Point start = new Point((x1 + x2) / 2 - (y2 - y1) * startT, (y1 + y2) / 2 + (x2 - x1) * startT);
            Point end = new Point((x1 + x2) / 2 - (y2 - y1) * endT, (y1 + y2) / 2 + (x2 - x1) * endT);
            // Move the start point slightly towards p1 to break ties, then verify it is closest to
            // p1. Repeat for the other combinations.
            assertThatPoint(start).movedSlightlyTowards(p1).isClosestTo(p1);
            assertThatPoint(end).movedSlightlyTowards(p1).isClosestTo(p1);
            assertThatPoint(start).movedSlightlyTowards(p2).isClosestTo(p2);
            assertThatPoint(end).movedSlightlyTowards(p2).isClosestTo(p2);
        }
    }

    private PointAssert assertThatPoint(Point point) {
        return new PointAssert(point);
    }

    @Data
    private final class PointAssert {

        final Point point;

        PointAssert movedSlightlyTowards(Point other) {
            return new PointAssert(new Point(
                other.x * WIGGLE_RATIO + point.x * (1 - WIGGLE_RATIO),
                other.y * WIGGLE_RATIO + point.y * (1 - WIGGLE_RATIO)));
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
