
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import io.github.ejif.geometry.DirectedEdge;
import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.TestUtils;
import io.github.ejif.geometry.VoronoiDiagram.Border;

public final class VoronoiTest {

    @Test
    public void testGeneratedVoronoiDiagram_hasBorderOnPerpendicularBisector() {
        Set<Border> borders = Voronoi.createVoronoiDiagram(ImmutableList.of(new Point(0, 0), new Point(200, 100))).getBorders();
        assertThat(borders).hasSize(1);
        assertThat(Iterables.getOnlyElement(borders).getEdge())
            .isEqualTo(DirectedEdge.builder()
                .anyPoint(new Point(100, 50))
                .dx(-100)
                .dy(200)
                .build());
    }

    @Test(timeout = 1000) // milliseconds
    public void testCreateVoronoiDiagram_isPerformant() {
        // Ensure that DEBUG logging, which is not performant, is disabled.
        assertThat(LoggerFactory.getLogger(Voronoi.class).isDebugEnabled()).isFalse();
        Voronoi.createVoronoiDiagram(TestUtils.randomPoints(10000));
    }
}
