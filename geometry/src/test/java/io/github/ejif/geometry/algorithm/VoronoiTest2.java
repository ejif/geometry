
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.SubLine;
import io.github.ejif.geometry.VoronoiDiagram;
import io.github.ejif.geometry.VoronoiDiagram.Border;
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
    public void testGeneratedDiagram_hasCorrectVertices() {
        VoronoiDiagram diagram = Voronoi.createVoronoiDiagram(points);
        for (Border border : diagram.getBorders()) {
            Point pl = points.get(border.getLeftPointIndex());
            Point pr = points.get(border.getRightPointIndex());

            SubLine subLine = border.getSubLine();
            Point startPoint = subLine.getStartPoint() == null
                    ? new Point(
                        subLine.getAnyPoint().x - subLine.getDx() * MAX_COORDINATE,
                        subLine.getAnyPoint().y - subLine.getDy() * MAX_COORDINATE)
                    : subLine.getStartPoint();
            Point endPoint = subLine.getEndPoint() == null
                    ? new Point(
                        subLine.getAnyPoint().x + subLine.getDx() * MAX_COORDINATE,
                        subLine.getAnyPoint().y + subLine.getDy() * MAX_COORDINATE)
                    : subLine.getEndPoint();
            // Move the start point slightly towards p1 to break ties, then verify it is closest to
            // p1. Repeat for the other combinations.
            assertThatPoint(startPoint).movedSlightlyTowards(pl).isClosestTo(pl);
            assertThatPoint(endPoint).movedSlightlyTowards(pl).isClosestTo(pl);
            assertThatPoint(startPoint).movedSlightlyTowards(pr).isClosestTo(pr);
            assertThatPoint(endPoint).movedSlightlyTowards(pr).isClosestTo(pr);
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
