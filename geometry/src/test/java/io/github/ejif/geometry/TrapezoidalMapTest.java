package io.github.ejif.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TrapezoidalMapTest {

    /**
     * <pre>
     *   1
     * _____
     *   2
     * </pre>
     */
    @Test
    public void testSingleLine() {
        TrapezoidalMap map = new TrapezoidalMap(1e-6);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(0)
            .startPoint(new Point(Double.NEGATIVE_INFINITY, 0))
            .endPoint(new Point(Double.POSITIVE_INFINITY, 0))
            .build(), 1, 2);
        assertThat(map.findRegion(new Point(0, 1))).isEqualTo(1);
        assertThat(map.findRegion(new Point(0, -1))).isEqualTo(2);
    }

    /**
     * <pre>
     *       /
     *  1   /
     * ___./
     *       2
     * </pre>
     */
    @Test
    public void testTwoRays() {
        TrapezoidalMap map = new TrapezoidalMap(1e-6);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(0)
            .startPoint(new Point(Double.NEGATIVE_INFINITY, 0))
            .endPoint(new Point(0, 0))
            .build(), 1, 2);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(1)
            .startPoint(new Point(0, 0))
            .endPoint(new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
            .build(), 1, 2);
        assertThat(map.findRegion(new Point(-1, 1))).isEqualTo(1);
        assertThat(map.findRegion(new Point(1, 2))).isEqualTo(1);
        assertThat(map.findRegion(new Point(-1, -1))).isEqualTo(2);
        assertThat(map.findRegion(new Point(2, 1))).isEqualTo(2);
        assertThat(map.findRegion(new Point(1, -1))).isEqualTo(2);
    }
}
