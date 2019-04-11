
package io.github.ejif.geometry;

import java.util.Set;

import lombok.Data;

/**
 * A representation of a Voronoi diagram. Each border contains a directional line segment/ray/line,
 * and stores the point on its left (rigorously, the point p such that the cross product of the
 * sub-line vector and any vector from the sub-line to p is positive) and right (the other side).
 *
 * For example, the following border has leftPointIndex=1 and rightPointIndex=2:
 *
 * <pre>
 *    ^
 *  1 | 2
 *    |
 * </pre>
 */
@Data
public final class VoronoiDiagram {

    private final Set<Border> borders;

    @Data
    public static final class Border {

        private final int leftPointIndex;
        private final int rightPointIndex;
        private final SubLine subLine;
    }
}
