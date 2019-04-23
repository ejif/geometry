package io.github.ejif.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public final class TrapezoidalMapTest {

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

    /**
     * <pre>
     * \
     *  \  1
     *   \.___
     * 2
     * </pre>
     */
    @Test
    public void testTwoRaysReverseOrder() {
        TrapezoidalMap map = new TrapezoidalMap(1e-6);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(0)
            .startPoint(new Point(0, 0))
            .endPoint(new Point(Double.POSITIVE_INFINITY, 0))
            .build(), 1, 2);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(-1)
            .startPoint(new Point(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
            .endPoint(new Point(0, 0))
            .build(), 1, 2);
        assertThat(map.findRegion(new Point(-1, 2))).isEqualTo(1);
        assertThat(map.findRegion(new Point(1, 1))).isEqualTo(1);
        assertThat(map.findRegion(new Point(-2, 1))).isEqualTo(2);
        assertThat(map.findRegion(new Point(-1, -1))).isEqualTo(2);
        assertThat(map.findRegion(new Point(1, -1))).isEqualTo(2);
    }

    /**
     * <pre>
     *  1
     * ___
     *  2
     * ___
     *  3
     * </pre>
     */
    @Test
    public void testNonintersectingLines() {
        TrapezoidalMap map = new TrapezoidalMap(1e-6);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(0)
            .startPoint(new Point(Double.NEGATIVE_INFINITY, 0))
            .endPoint(new Point(Double.POSITIVE_INFINITY, 0))
            .build(), 2, 3);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 100))
            .dx(1)
            .dy(0)
            .startPoint(new Point(Double.NEGATIVE_INFINITY, 0))
            .endPoint(new Point(Double.POSITIVE_INFINITY, 0))
            .build(), 1, 2);
        assertThat(map.findRegion(new Point(0, -1))).isEqualTo(3);
        assertThat(map.findRegion(new Point(0, 50))).isEqualTo(2);
        assertThat(map.findRegion(new Point(0, 101))).isEqualTo(1);
    }

    /**
     * <pre>
     *  1 . 2
     *   / \
     *  / 3 \
     * ._____.
     *    4
     * </pre>
     */
    @Test
    public void testTriangle() {
        TrapezoidalMap map = new TrapezoidalMap(1e-6);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(2)
            .startPoint(new Point(0, 0))
            .endPoint(new Point(50, 100))
            .build(), 1, 3);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(100, 0))
            .dx(1)
            .dy(-2)
            .startPoint(new Point(50, 100))
            .endPoint(new Point(100, 0))
            .build(), 2, 3);
        map.addLine(SubLine.builder()
            .anyPoint(new Point(0, 0))
            .dx(1)
            .dy(0)
            .startPoint(new Point(0, 0))
            .endPoint(new Point(100, 0))
            .build(), 3, 4);
        assertThat(map.findRegion(new Point(25, 75))).isEqualTo(1);
        assertThat(map.findRegion(new Point(75, 75))).isEqualTo(2);
        assertThat(map.findRegion(new Point(25, 25))).isEqualTo(3);
        assertThat(map.findRegion(new Point(50, 25))).isEqualTo(3);
        assertThat(map.findRegion(new Point(75, 25))).isEqualTo(3);
        assertThat(map.findRegion(new Point(50, -1))).isEqualTo(4);
    }
}
