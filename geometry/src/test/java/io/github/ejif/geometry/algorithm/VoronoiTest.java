
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

@Data
@RunWith(Parameterized.class)
public final class VoronoiTest {

    public static final double MAX_TOLERANCE = 4 * Voronoi.MAX_WIGGLE_DISTANCE;

    private final TestCase testCase;

    @Parameterized.Parameters
    public static List<?> parameters() {
        return ImmutableList.builder()

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .build(),
                new VoronoiDiagram(ImmutableList.<Border> builder()
                    .build())))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .build(),
                new VoronoiDiagram(ImmutableList.<Border> builder()
                    .build())))

            .add(new TestCase(
                ImmutableList.<Point> builder()
                    .add(new Point(0, 0))
                    .add(new Point(100, 0))
                    .build(),
                new VoronoiDiagram(ImmutableList.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), null, null))
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
                new VoronoiDiagram(ImmutableList.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), null, new Point(50, 50)))
                    .add(new Border(PointPair.of(0, 2), new Point(50, 50), null))
                    .add(new Border(PointPair.of(1, 2), null, new Point(50, 50)))
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
                new VoronoiDiagram(ImmutableList.<Border> builder()
                    .add(new Border(PointPair.of(0, 1), null, new Point(50, 50)))
                    .add(new Border(PointPair.of(1, 2), null, new Point(50, 50)))
                    .add(new Border(PointPair.of(2, 3), null, new Point(50, 50)))
                    .add(new Border(PointPair.of(0, 3), new Point(50, 50), null))
                    .build())))

            .build();
    }

    @Test
    public void test() {
        VoronoiDiagram diagram = Voronoi.createVoronoiDiagram(testCase.points);
        Map<PointPair, Border> expectedBorders = Maps.uniqueIndex(testCase.expectedDiagram.getBorders(), Border::getPointPair);
        int count = 0;
        for (Border border : diagram.getBorders()) {
            if (border.getStart() != null && border.getEnd() != null
                    && Points.distance(border.getStart(), border.getEnd()) < MAX_TOLERANCE) {
                // Because of wiggled points, the actual diagram may have very small edges that don't exist in the
                // expected diagram.
                continue;
            }
            Border expectedBorder = expectedBorders.get(border.getPointPair());
            assertThat(expectedBorder).isNotNull();
            if (border.getStart() == null) {
                assertThat(expectedBorder.getStart()).isNull();
            } else {
                assertThat(Points.distance(border.getStart(), expectedBorder.getStart())).isLessThan(MAX_TOLERANCE);
            }
            if (border.getEnd() == null) {
                assertThat(expectedBorder.getEnd()).isNull();
            } else {
                assertThat(Points.distance(border.getEnd(), expectedBorder.getEnd())).isLessThan(MAX_TOLERANCE);
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
