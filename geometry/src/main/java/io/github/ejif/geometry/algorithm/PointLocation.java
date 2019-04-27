
package io.github.ejif.geometry.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.VoronoiDiagram.Border;

public final class PointLocation {

    private final List<Point> points;
    private final TrapezoidalMap trapezoidalMap;

    /**
     * Generates a PointLocation object among the given anchor points to answer the following query
     * efficiently in log n time: which of the anchor points is closest to a particular point P?
     *
     * @param points
     *            the set of points
     */
    public PointLocation(List<Point> points) {
        this(points, new Random());
    }

    @VisibleForTesting
    PointLocation(List<Point> points, Random random) {
        if (points.isEmpty())
            throw new IllegalArgumentException("Cannot generate PointLocation object with no points.");

        this.points = ImmutableList.copyOf(points);
        this.trapezoidalMap = new TrapezoidalMap(random);

        List<Border> borders = new ArrayList<>(Voronoi.createVoronoiDiagram(points).getBorders());

        // Inserting n edges into a trapezoidal map is worst case O(n^2), but is average case O(n log n).
        Collections.shuffle(borders, random);

        for (Border border : borders)
            trapezoidalMap.addEdge(border.getEdge(), border.getLeftPointIndex(), border.getRightPointIndex());
    }

    /**
     * Finds the closest anchor point to the given point. If there are multiple closest points, an
     * arbitrary one is returned.
     *
     * @param point
     *            a point
     * @return the anchor point closest to the point
     */
    public Point findClosestPoint(Point point) {
        if (points.size() == 1)
            return points.get(0);

        int region = trapezoidalMap.findRegion(point);
        return points.get(region);
    }
}
