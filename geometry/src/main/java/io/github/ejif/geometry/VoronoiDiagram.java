
package io.github.ejif.geometry;

import java.util.Set;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
