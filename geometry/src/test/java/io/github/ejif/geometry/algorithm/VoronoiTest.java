
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.SubLine;
import io.github.ejif.geometry.VoronoiDiagram.Border;

public final class VoronoiTest {

    public static final int NUM_POINTS = 10000;
    public static final double MAX_COORDINATE = 1000;

    @Test
    public void testGeneratedVoronoiDiagram_hasBorderOnPerpendicularBisector() {
        Set<Border> borders = Voronoi.createVoronoiDiagram(ImmutableList.of(new Point(0, 0), new Point(200, 100))).getBorders();
        assertThat(borders).hasSize(1);
        assertThat(Iterables.getOnlyElement(borders).getSubLine())
            .isEqualTo(new SubLine(new Point(100, 50), -100, 200, null, null));
    }

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
