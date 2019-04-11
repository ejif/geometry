
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.ImmutableList;

import io.github.ejif.geometry.Point;
import lombok.Data;

/**
 * Tests several hard-coded point configurations, and verifies that the returned Voronoi diagram is
 * similar to the expected hard-coded diagram.
 */
@Data
@RunWith(Parameterized.class)
public final class VoronoiTest1 {

    private final TestCase testCase;

    @Parameterized.Parameters
    public static List<?> parameters() {
        return ImmutableList.builder()

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .build()))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .build()))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, null))
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
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, new Point(50, 50)))
                    .add(new FlatBorder(0, 2, new Point(50, 50), null))
                    .add(new FlatBorder(1, 2, null, new Point(50, 50)))
                    .build()))

            /**
             * <pre>
             *
             *  0
             * ---
             *  1
             * ---
             *  2
             *
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 200))
                    .add(new Point(0, 100))
                    .add(new Point(0, 0))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, null))
                    .add(new FlatBorder(1, 2, null, null))
                    .build()))

            /**
             * <pre>
             *
             *  0 /
             *   /
             *  / 1 /
             *     /
             *    /  2
             *
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 200))
                    .add(new Point(100, 100))
                    .add(new Point(200, 0))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, null))
                    .add(new FlatBorder(1, 2, null, null))
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
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, new Point(50, 50)))
                    .add(new FlatBorder(0, 3, new Point(50, 50), null))
                    .add(new FlatBorder(1, 2, null, new Point(50, 50)))
                    .add(new FlatBorder(2, 3, null, new Point(50, 50)))
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
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, new Point(50, 50)))
                    .add(new FlatBorder(0, 2, new Point(50, 50), null))
                    .add(new FlatBorder(1, 2, new Point(150, 50), new Point(50, 50)))
                    .add(new FlatBorder(1, 3, null, new Point(150, 50)))
                    .add(new FlatBorder(2, 3, new Point(150, 50), null))
                    .build()))

            /**
             * <pre>
             *    |   |   |   |
             *  2 | 4 | 3 | 0 | 1
             *    |   |   |   |
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(300, 0))
                    .add(new Point(400, 0))
                    .add(new Point(0, 0))
                    .add(new Point(200, 0))
                    .add(new Point(100, 0))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, null, null))
                    .add(new FlatBorder(0, 3, null, null))
                    .add(new FlatBorder(2, 4, null, null))
                    .add(new FlatBorder(3, 4, null, null))
                    .build()))

            /**
             * <pre>
             *    |
             *  0 . 1
             *   / \
             * _. 2 ._
             *   \ /
             *  3 . 4
             *    |
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 200))
                    .add(new Point(200, 200))
                    .add(new Point(100, 100))
                    .add(new Point(0, 0))
                    .add(new Point(200, 0))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, new Point(100, 200), null))
                    .add(new FlatBorder(0, 2, new Point(0, 100), new Point(100, 200)))
                    .add(new FlatBorder(0, 3, null, new Point(0, 100)))
                    .add(new FlatBorder(1, 2, new Point(100, 200), new Point(200, 100)))
                    .add(new FlatBorder(1, 4, new Point(200, 100), null))
                    .add(new FlatBorder(2, 3, new Point(0, 100), new Point(100, 0)))
                    .add(new FlatBorder(2, 4, new Point(100, 0), new Point(200, 100)))
                    .add(new FlatBorder(3, 4, null, new Point(100, 0)))
                    .build()))

            /**
             * <pre>
             * \ 1 | 2 /
             *  \  |  /
             * 0 \ | / 3
             *    \|/
             * ____.____
             *    /|\
             * 7 / | \ 4
             *  /  |  \
             * / 6 | 5 \
             * </pre>
             */
            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 300))
                    .add(new Point(100, 400))
                    .add(new Point(300, 400))
                    .add(new Point(400, 300))
                    .add(new Point(400, 100))
                    .add(new Point(300, 0))
                    .add(new Point(100, 0))
                    .add(new Point(0, 100))
                    .build(),
                ImmutableList.<FlatBorder> builder()
                    .add(new FlatBorder(0, 1, new Point(200, 200), null))
                    .add(new FlatBorder(0, 7, null, new Point(200, 200)))
                    .add(new FlatBorder(1, 2, new Point(200, 200), null))
                    .add(new FlatBorder(2, 3, new Point(200, 200), null))
                    .add(new FlatBorder(3, 4, new Point(200, 200), null))
                    .add(new FlatBorder(4, 5, new Point(200, 200), null))
                    .add(new FlatBorder(5, 6, new Point(200, 200), null))
                    .add(new FlatBorder(6, 7, new Point(200, 200), null))
                    .build()))

            .build();
    }

    @Test
    public void testGeneratedDiagram_hasBorderVertices() {
        List<FlatBorder> flatBorders = Voronoi.createVoronoiDiagram(testCase.points).getBorders().stream()
            .map(border -> new FlatBorder(
                border.getLeftPointIndex(),
                border.getRightPointIndex(),
                border.getSubLine().getStartPoint(),
                border.getSubLine().getEndPoint()))
            .collect(Collectors.toList());
        assertThat(flatBorders).containsExactlyInAnyOrderElementsOf(testCase.expectedBorders);
    }

    @Data
    private static class TestCase {

        final List<Point> points;
        final List<FlatBorder> expectedBorders;
    }

    @Data
    private static class FlatBorder {

        final int leftPointIndex;
        final int rightPointIndex;
        final Point startPoint;
        final Point endPoint;
    }
}
