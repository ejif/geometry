
package io.github.ejif.geometry;

import java.util.Set;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A representation of a Voronoi diagram. Each point index appears in k border objects, and a
 * polygon can be constructed from those borders by taking matching endpoints. A null endpoint
 * represents a point at infinity. For example, given the following region (the two lines at the top
 * extend to infinity):
 *
 * <pre>
 *  ^   ^
 *  |   |
 *  |   |
 *  | A |
 *  |   |
 *  .___.
 * </pre>
 *
 * the borders with A as a point in its point pair are [null, (0, 0)], [(0, 0), (100, 0)], and
 * [(100, 0), null].
 */
@Data
public final class VoronoiDiagram {

    private final Set<Border> borders;

    /**
     * A border between two points (x1, y1) and (x2, y2) in the Voronoi diagram. This border may be
     * a line segment, a ray, or a line, and is a subset of the perpendicular bisector of the two
     * points.
     *
     * A parameterization of the perpendicular bisector is given by:
     *
     * <pre>
     * x = (x1 + x2) / 2 - (y2 - y1) t
     * y = (y1 + y2) / 2 + (x2 - x1) t.
     * </pre>
     *
     * The border stores the smaller and larger values of t that correspond to the endpoints of this
     * border, and the value can be -Infinity or Infinity. The border also stores the point, which
     * is guaranteed to be exactly the same as the endpoints of adjacent borders (computing the
     * point from t may result in slightly different points due to precision errors).
     */
    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Border {

        private final PointPair pointPair;
        private final double startT;
        private final Point startPoint;
        private final double endT;
        private final Point endPoint;

        public static Border of(PointPair pointPair, double startT, Point startPoint, double endT, Point endPoint) {
            Preconditions.checkArgument(startT < endT, "Border must satisfy startT < endT");
            return new Border(pointPair, startT, startPoint, endT, endPoint);
        }

        /**
         * Compute a point on the perpendicular bisector of (p1, p2) at t, using the
         * parameterization above. For example, each border satisfies computePoint(p1, p2, startT) =
         * startPoint and computePoint(p1, p2, endT) = endPoint, but we recommend using the fields
         * directly to avoid precision errors.
         *
         * @param p1
         *            the first point
         * @param p2
         *            the second point
         * @param t
         *            the parameter
         * @return a point on the perpendicular bisector of the two points, at the given parameter
         */
        public static Point computePoint(Point p1, Point p2, double t) {
            if (Double.isInfinite(t))
                return null;
            double x1 = p1.x;
            double y1 = p1.y;
            double x2 = p2.x;
            double y2 = p2.y;
            return new Point((x1 + x2) / 2 - (y2 - y1) * t, (y1 + y2) / 2 + (x2 - x1) * t);
        }
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PointPair {

        private final int pointIndex1;
        private final int pointIndex2;

        public static PointPair of(int pointIndex1, int pointIndex2) {
            Preconditions.checkArgument(pointIndex1 < pointIndex2, "Point pair must satisfy pointIndex1 < pointIndex2");
            return new PointPair(pointIndex1, pointIndex2);
        }
    }
}
