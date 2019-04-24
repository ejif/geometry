package io.github.ejif.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public final class TrapezoidalMapTest {

    private static final double SHEAR = 1e-6;

    /**
     * <pre>
     *   1
     * _____
     *   2
     * </pre>
     */
    @Test
    public void testSingleLine() {
        TrapezoidalMap map = new TrapezoidalMap(SHEAR);
        map.addEdge(DirectedEdge.line(new Point(0, 0), new Point(1, 0)), 1, 2);
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
        TrapezoidalMap map = new TrapezoidalMap(SHEAR);
        map.addEdge(DirectedEdge.ray(new Point(0, 0), -1, 0), 2, 1);
        map.addEdge(DirectedEdge.ray(new Point(0, 0), 1, 1), 1, 2);
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
        TrapezoidalMap map = new TrapezoidalMap(SHEAR);
        map.addEdge(DirectedEdge.ray(new Point(0, 0), 1, 0), 1, 2);
        map.addEdge(DirectedEdge.ray(new Point(0, 0), -1, 1), 2, 1);
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
        TrapezoidalMap map = new TrapezoidalMap(SHEAR);
        map.addEdge(DirectedEdge.line(new Point(0, 0), new Point(1, 0)), 2, 3);
        map.addEdge(DirectedEdge.line(new Point(0, 100), new Point(1, 100)), 1, 2);
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
        TrapezoidalMap map = new TrapezoidalMap(SHEAR);
        map.addEdge(DirectedEdge.segment(new Point(0, 0), new Point(50, 100)), 1, 3);
        map.addEdge(DirectedEdge.segment(new Point(50, 100), new Point(100, 0)), 2, 3);
        map.addEdge(DirectedEdge.segment(new Point(100, 0), new Point(0, 0)), 4, 3);
        assertThat(map.findRegion(new Point(25, 75))).isEqualTo(1);
        assertThat(map.findRegion(new Point(75, 75))).isEqualTo(2);
        assertThat(map.findRegion(new Point(25, 25))).isEqualTo(3);
        assertThat(map.findRegion(new Point(50, 25))).isEqualTo(3);
        assertThat(map.findRegion(new Point(75, 25))).isEqualTo(3);
        assertThat(map.findRegion(new Point(50, -1))).isEqualTo(4);
    }
}
