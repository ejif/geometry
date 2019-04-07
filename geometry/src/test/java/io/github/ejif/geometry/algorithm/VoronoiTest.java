
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.VoronoiDiagram;
import io.github.ejif.geometry.VoronoiDiagram.Border;
import io.github.ejif.geometry.VoronoiDiagram.PointPair;
import lombok.Data;

@Data
@RunWith(Parameterized.class)
public final class VoronoiTest {

    public static final double MAX_TOLERANCE = 4 * Voronoi.MAX_WIGGLE_DISTANCE;
    public static final double MAX_BOUNDS = 1 / MAX_TOLERANCE;

    private final TestCase testCase;

    @Parameterized.Parameters
    public static List<?> parameters() {
        return ImmutableList.builder()

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .build(),
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .build())))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .build(),
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .build())))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .build(),
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
                    .build())))

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
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), Double.NEGATIVE_INFINITY, 0.5))
                    .add(new Border(PointPair.of(0, 2), 0, Double.POSITIVE_INFINITY))
                    .add(new Border(PointPair.of(1, 2), Double.NEGATIVE_INFINITY, 0.5))
                    .build())))


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
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
                    .add(new Border(PointPair.of(1, 2), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
                    .build())))

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
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), Double.NEGATIVE_INFINITY, 0.5))
                    .add(new Border(PointPair.of(0, 3), -0.5, Double.POSITIVE_INFINITY))
                    .add(new Border(PointPair.of(1, 2), Double.NEGATIVE_INFINITY, 0.5))
                    .add(new Border(PointPair.of(2, 3), Double.NEGATIVE_INFINITY, 0.5))
                    .build())))

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
                new VoronoiDiagram(ImmutableSet.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), Double.NEGATIVE_INFINITY, 0.5))
                    .add(new Border(PointPair.of(0, 2), 0, Double.POSITIVE_INFINITY))
                    .add(new Border(PointPair.of(1, 2), -0.5, 0.5))
                    .add(new Border(PointPair.of(1, 3), Double.NEGATIVE_INFINITY, 0))
                    .add(new Border(PointPair.of(2, 3), -0.5, Double.POSITIVE_INFINITY))
                    .build())))

            .build();
    }

    @Test
    public void testGeneratedDiagramSimilarToExpected() {
        VoronoiDiagram diagram = Voronoi.createVoronoiDiagram(testCase.points);
        Map<PointPair, Border> expectedBorders = Maps.uniqueIndex(testCase.expectedDiagram.getBorders(), Border::getPointPair);
        int count = 0;
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
            Border expectedBorder = expectedBorders.get(border.getPointPair());
            assertThat(expectedBorder).isNotNull();
            if (expectedBorder.getStartT() == Double.NEGATIVE_INFINITY) {
                assertThat(border.getStartT()).isLessThan(-MAX_BOUNDS);
            } else {
                assertThat(border.getStartT()).isCloseTo(expectedBorder.getStartT(), within(MAX_TOLERANCE));
            }
            if (expectedBorder.getEndT() == Double.POSITIVE_INFINITY) {
                assertThat(border.getEndT()).isGreaterThan(MAX_BOUNDS);
            } else {
                assertThat(border.getEndT()).isCloseTo(expectedBorder.getEndT(), within(MAX_TOLERANCE));
            }
            count++;
        }
        assertThat(count).isEqualTo(expectedBorders.size());
    }

    @Data
    private static final class TestCase {

        final List<Point> points;
        final VoronoiDiagram expectedDiagram;
    }
}
