
package io.github.ejif.geometry.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.VoronoiDiagram.Border;

public final class PointSet {

    private final List<Point> points;
    private final TrapezoidalMap trapezoidalMap;

    /**
     * Creates an object encapsulating a set of n points, indexed for efficient point location
     * queries.
     *
     * @param points
     *            the set of points
     */
    public PointSet(List<Point> points) {
        this(points, new Random());
    }

    @VisibleForTesting
    PointSet(List<Point> points, Random random) {
        this.points = ImmutableList.copyOf(points);
        this.trapezoidalMap = new TrapezoidalMap(random);

        List<Border> borders = new ArrayList<>(Voronoi.createVoronoiDiagram(points).getBorders());

        // Inserting n edges into a trapezoidal map is worst case O(n^2), but is average case O(n log n).
        Collections.shuffle(borders, random);

        for (Border border : borders)
            trapezoidalMap.addEdge(border.getEdge(), border.getLeftPointIndex(), border.getRightPointIndex());
    }

    /**
     * Get the points of this point set.
     *
     * @return the list of points
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * Finds the closest anchor point to the given point. If there are multiple closest points, an
     * arbitrary one is returned.
     *
     * @param point
     *            a point
     * @return the anchor point closest to the point
     * @throws IllegalArgumentException if the point set contains no points
     */
    public Point findClosestPoint(Point point) throws IllegalArgumentException {
        if (points.isEmpty())
            throw new IllegalArgumentException("Point set contains no points.");
        if (points.size() == 1)
            return points.get(0);

        int region = trapezoidalMap.findRegion(point);
        return points.get(region);
    }
}
