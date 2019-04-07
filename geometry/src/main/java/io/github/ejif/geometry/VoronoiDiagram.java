
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
     * A border between two points in the Voronoi diagram. The parameterization of the perpendicular
     * bisector of the two points (x1, y1) and (x2, y2) is given by
     *
     * <pre>
     * x = (x1 + x2) / 2 - (y2 - y1) t
     * y = (y1 + y2) / 2 + (x2 - x1) t
     * </pre>
     *
     * The startT and endT values represent the start and end points of this bisecting line, where
     * startT < endT, startT may be -Infinity, and endT may be Infinity.
     */
    @Data
    public static final class Border {

        private final PointPair pointPair;
        private final double startT;
        private final double endT;
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
