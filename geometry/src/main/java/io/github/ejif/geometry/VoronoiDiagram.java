
package io.github.ejif.geometry;

import java.util.List;

import com.google.common.base.Preconditions;

import io.github.ejif.geometry.algorithm.Points;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public final class VoronoiDiagram {

    private final List<Border> borders;

    /**
     * A border between two points in the Voronoi diagram. The order of start and end is such that
     * {@link Points#crossProduct} applied on (point1, point2, start, end) is strictly positive. One
     * or both of the start and end points may be null, which represents that the corresponding
     * point is/points are at infinity.
     */
    @Data
    public static final class Border {

        private final PointPair pointPair;
        private final Point start;
        private final Point end;
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
