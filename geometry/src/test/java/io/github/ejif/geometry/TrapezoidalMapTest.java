package io.github.ejif.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public final class TrapezoidalMapTest {

    private final TrapezoidalMap map = new TrapezoidalMap(1e-6);

    /**
     * <pre>
     *   1
     * _____
     *   2
     * </pre>
     */
    @Test
    public void testSingleLine() {
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

    /**
     * <pre>
     *   \    2
     *    \ .__.
     *  1  \  3
     * .__. \
     *  4    \
     * </pre>
     */
    @Test
    public void testLongCut() {
        map.addEdge(DirectedEdge.segment(new Point(0, 0), new Point(100, 0)), 1, 4);
        map.addEdge(DirectedEdge.segment(new Point(200, 100), new Point(300, 100)), 2, 3);
        map.addEdge(DirectedEdge.line(new Point(100, 100), new Point(200, 0)), 3, 1);
        assertThat(map.findRegion(new Point(50, 50))).isEqualTo(1);
        assertThat(map.findRegion(new Point(250, 150))).isEqualTo(2);
        assertThat(map.findRegion(new Point(250, 0))).isEqualTo(3);
        assertThat(map.findRegion(new Point(50, -50))).isEqualTo(4);
    }

    /**
     * <pre>
     * 1 . 2
     *  / \
     * . 3 .
     *  \ /
     * 4 . 5
     * </pre>
     */
    @Test
    public void testDiamond() {
        map.addEdge(DirectedEdge.segment(new Point(50, 100), new Point(100, 50)), 2, 3);
        map.addEdge(DirectedEdge.segment(new Point(50, 0), new Point(0, 50)), 4, 3);
        map.addEdge(DirectedEdge.segment(new Point(50, 100), new Point(0, 50)), 3, 1);
        map.addEdge(DirectedEdge.segment(new Point(50, 0), new Point(100, 50)), 3, 5);
        assertThat(map.findRegion(new Point(1, 99))).isEqualTo(1);
        assertThat(map.findRegion(new Point(99, 99))).isEqualTo(2);
        assertThat(map.findRegion(new Point(49, 51))).isEqualTo(3);
        assertThat(map.findRegion(new Point(51, 49))).isEqualTo(3);
        assertThat(map.findRegion(new Point(1, 1))).isEqualTo(4);
        assertThat(map.findRegion(new Point(99, 1))).isEqualTo(5);
    }

    /**
     * <pre>
     * \  1
     *  \.__
     * 2
     * _____
     * 3
     *   .__
     *  / 4
     * </pre>
     */
    @Test
    public void testPlaceLinesAboveAndBelowEquator() {
        map.addEdge(DirectedEdge.line(new Point(0, 0), new Point(1, 0)), 2, 3);
        map.addEdge(DirectedEdge.ray(new Point(0, 100), 1, 0), 1, 2);
        map.addEdge(DirectedEdge.ray(new Point(0, 100), -1, 1), 2, 1);
        map.addEdge(DirectedEdge.ray(new Point(0, -100), -1, -1), 4, 3);
        map.addEdge(DirectedEdge.ray(new Point(0, -100), 1, 0), 3, 4);
        assertThat(map.findRegion(new Point(-50, 200))).isEqualTo(1);
        assertThat(map.findRegion(new Point(50, 150))).isEqualTo(1);
        assertThat(map.findRegion(new Point(-50, 100))).isEqualTo(2);
        assertThat(map.findRegion(new Point(50, 50))).isEqualTo(2);
        assertThat(map.findRegion(new Point(-50, -100))).isEqualTo(3);
        assertThat(map.findRegion(new Point(50, -50))).isEqualTo(3);
        assertThat(map.findRegion(new Point(-50, -200))).isEqualTo(4);
        assertThat(map.findRegion(new Point(50, -150))).isEqualTo(4);
    }
}
