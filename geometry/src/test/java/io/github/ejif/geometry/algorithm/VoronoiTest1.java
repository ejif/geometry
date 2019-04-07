
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.VoronoiDiagram;
import io.github.ejif.geometry.VoronoiDiagram.Border;
import io.github.ejif.geometry.VoronoiDiagram.PointPair;
import lombok.Data;

/**
 * Tests several hard-coded point configurations, and verifies that the returned Voronoi diagram is
 * similar to the expected hard-coded diagram.
 */
@Data
@RunWith(Parameterized.class)
public final class VoronoiTest1 {

    public static final double MAX_TOLERANCE = 4 * Voronoi.MAX_WIGGLE_DISTANCE;
    public static final double MAX_BOUNDS = 1 / MAX_TOLERANCE;

    private final TestCase testCase;

    @Parameterized.Parameters
    public static List<?> parameters() {
        return ImmutableList.builder()

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .build()))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .build()))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .add(new ExpectedBorder(PointPair.of(0, 1), null, null))
                    .build()))

            /**
             * <pre>
             * \
             *  \   2
             *   \
             *    .___
             *    |
             *  0 | 1
             *    |
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .add(new Point(100, 100))
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .add(new ExpectedBorder(PointPair.of(0, 1), null, new Point(50, 50)))
                    .add(new ExpectedBorder(PointPair.of(0, 2), new Point(50, 50), null))
                    .add(new ExpectedBorder(PointPair.of(1, 2), null, new Point(50, 50)))
                    .build()))

            /**
             * <pre>
             *    |   |
             *  0 | 1 | 2
             *    |   |
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .add(new Point(200, 0))
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .add(new ExpectedBorder(PointPair.of(0, 1), null, null))
                    .add(new ExpectedBorder(PointPair.of(1, 2), null, null))
                    .build()))

            /**
             * <pre>
             *    |
             *  3 | 2
             *    |
             * ___.___
             *    |
             *  0 | 1
             *    |
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .add(new Point(100, 100))
                    .add(new Point(0, 100))
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .add(new ExpectedBorder(PointPair.of(0, 1), null, new Point(50, 50)))
                    .add(new ExpectedBorder(PointPair.of(0, 3), new Point(50, 50), null))
                    .add(new ExpectedBorder(PointPair.of(1, 2), null, new Point(50, 50)))
                    .add(new ExpectedBorder(PointPair.of(2, 3), null, new Point(50, 50)))
                    .build()))

            /**
             * <pre>
             * \      |
             *  \   2 | 3
             *   \    |
             *    .___.
             *    |    \
             *  0 | 1   \
             *    |      \
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .add(new Point(100, 100))
                    .add(new Point(200, 100))
                    .build(),
                ImmutableList.<ExpectedBorder> builder()
                    .add(new ExpectedBorder(PointPair.of(0, 1), null, new Point(50, 50)))
                    .add(new ExpectedBorder(PointPair.of(0, 2), new Point(50, 50), null))
                    .add(new ExpectedBorder(PointPair.of(1, 2), new Point(50, 50), new Point(150, 50)))
                    .add(new ExpectedBorder(PointPair.of(1, 3), null, new Point(150, 50)))
                    .add(new ExpectedBorder(PointPair.of(2, 3), new Point(50, 50), null))
                    .build()))

            .build();
    }

    @Test
    public void testGeneratedDiagram_hasBordersWithConsistentParameterizationAndPoint() {
        VoronoiDiagram diagram = Voronoi.createVoronoiDiagram(testCase.points);
        for (Border border : diagram.getBorders()) {
            PointPair pointPair = border.getPointPair();
            Point p1 = testCase.points.get(pointPair.getPointIndex1());
            Point p2 = testCase.points.get(pointPair.getPointIndex2());
            assertThatPoint(border.getStartPoint()).isCloseTo(Border.computePoint(p1, p2, border.getStartT()));
            assertThatPoint(border.getEndPoint()).isCloseTo(Border.computePoint(p1, p2, border.getEndT()));
        }
    }

    @Test
    public void testGeneratedDiagram_hasExpectedBorderVertices() {
        VoronoiDiagram diagram = Voronoi.createVoronoiDiagram(testCase.points);
        Map<PointPair, ExpectedBorder> expectedBorders = Maps.uniqueIndex(testCase.borders, ExpectedBorder::getPointPair);
        int numExpectedBordersFound = 0;
        for (Border border : diagram.getBorders()) {
            if (border.getEndT() - border.getStartT() < MAX_TOLERANCE) {
                // Because of wiggled points, the actual diagram may have very small edges that don't exist in the
                // expected diagram.
                continue;
            }
            if (border.getStartT() > MAX_BOUNDS || border.getEndT() < -MAX_BOUNDS) {
                // Ignore point close to infinity caused by intersection of two lines that should be parallel.
                continue;
            }
            assertThat(expectedBorders).containsKey(border.getPointPair());
            ExpectedBorder expectedBorder = expectedBorders.get(border.getPointPair());
            assertThatPoint(border.getStartPoint()).isCloseTo(expectedBorder.getStartPoint());
            assertThatPoint(border.getEndPoint()).isCloseTo(expectedBorder.getEndPoint());
            numExpectedBordersFound++;
        }
        assertThat(numExpectedBordersFound).isEqualTo(expectedBorders.size());
    }

    private PointAssert assertThatPoint(Point actual) {
        return new PointAssert(actual);
    }

    @Data
    private static final class TestCase {

        final List<Point> points;
        final List<ExpectedBorder> borders;
    }

    @Data
    private static final class ExpectedBorder {

        final PointPair pointPair;
        final Point startPoint;
        final Point endPoint;
    }

    @Data
    private static final class PointAssert {

        final Point actual;

        void isCloseTo(Point expected) {
            if (expected == null) {
                assertThat(actual == null || Math.hypot(actual.x, actual.y) > MAX_BOUNDS).isTrue();
            } else {
                assertThat(Points.distance(actual, expected) < MAX_TOLERANCE);
            }
        }
    }
}
