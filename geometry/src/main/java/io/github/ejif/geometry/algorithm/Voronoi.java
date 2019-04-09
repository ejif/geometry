
package io.github.ejif.geometry.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.VoronoiDiagram;
import io.github.ejif.geometry.VoronoiDiagram.Border;
import io.github.ejif.geometry.VoronoiDiagram.PointPair;
import lombok.Builder;
import lombok.Data;

public final class Voronoi {

    public static final double MAX_WIGGLE_DISTANCE = 1e-6;

    private static final Logger log = LoggerFactory.getLogger(Voronoi.class);

    /**
     * Computes the Voronoi diagram of the given points using Fortune's algorithm. See
     * http://www.cs.sfu.ca/~binay/813.2011/Fortune.pdf.
     *
     * @param points
     *            the points to compute Fortune's algorithm for
     * @return the Voronoi diagram
     */
    public static VoronoiDiagram createVoronoiDiagram(List<Point> points) {
        return createVoronoiDiagram(points, new Random(2915));
    }

    /**
     * Computes the Voronoi diagram of the given points using Fortune's algorithm. See
     * http://www.cs.sfu.ca/~binay/813.2011/Fortune.pdf.
     *
     * @param points
     *            the points to compute Fortune's algorithm for
     * @param random
     *            the random number generator, used for wiggling points slightly to avoid degenerate
     *            configurations
     * @return the Voronoi diagram
     */
    public static VoronoiDiagram createVoronoiDiagram(List<Point> points, Random random) {
        // Wiggle each point to ensure that no two points have the same x or y coordinate, no three
        // points are collinear, and no four points with the same circumcenter.
        List<Point> wiggledPoints = points.stream()
            .map(point -> new Point(
                point.x + random.nextDouble() * MAX_WIGGLE_DISTANCE,
                point.y + random.nextDouble() * MAX_WIGGLE_DISTANCE))
            .collect(Collectors.toList());
        log.debug("Wiggled points: {}", wiggledPoints);
        log.debug("");

        // Maintain a priority queue of events as we move the sweep line from left to right.
        PriorityQueue<Event> events = new PriorityQueue<>(Comparator.comparing(Event::getX));
        for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            events.add(PointEvent.builder()
                .x(wiggledPoints.get(pointIndex).x)
                .pointIndex(pointIndex)
                .build());
        }

        /**
         * For any arc on the beachline surrounded by an arc before and after it, add a vertex event
         * at the rightmost point of the circumcircle of their foci.
         */
        Consumer<Arc> processArc = arc -> {
            Point point = wiggledPoints.get(arc.pointIndex);
            Point prevPoint = wiggledPoints.get(arc.prev.pointIndex);
            Point nextPoint = wiggledPoints.get(arc.next.pointIndex);

            // The cross product must be positive, otherwise this arc will never collapse into a point.
            if (Points.crossProduct(point, prevPoint, point, nextPoint) <= 0)
                return;
            Point circumcenter = Points.circumcenter(prevPoint, point, nextPoint);
            events.add(VertexEvent.builder()
                .x(circumcenter.x + Points.distance(point, circumcenter))
                .toRemove(arc)
                .circumcenter(circumcenter)
                .build());
        };

        /**
         * Manage a sorted set of all arcs on the beach line. An arc is a contiguous section of a
         * parabola on the beach line. Even though multiple arcs on the beach line may be part of
         * the same parabola, the arc's previous and next neighbors uniquely define the arc.
         * <p>
         * The order of arcs depends on the sweep line, but Fortune's algorithm guarantees that the
         * order will be stable as long as we add each new arc in the proper place, and remove each
         * arc once we arrive at its corresponding vertex event.
         */
        AtomicDouble sweepX = new AtomicDouble();
        Function<Arc, Interval> getInterval = arc -> {
            double xs = sweepX.get();
            Point currPoint = wiggledPoints.get(arc.pointIndex);

            // If the focus of the parabola is on the sweep line, the parabola is a degenerate
            // horizontal ray passing through the focus.
            if (currPoint.x == xs)
                return new Interval(currPoint.y, currPoint.y);

            double min = arc.prev == null
                    ? Double.NEGATIVE_INFINITY
                    : findIntersection(xs, currPoint, wiggledPoints.get(arc.prev.pointIndex));
            double max = arc.next == null
                    ? Double.POSITIVE_INFINITY
                    : findIntersection(xs, wiggledPoints.get(arc.next.pointIndex), currPoint);
            return new Interval(min, max);
        };
        TreeSet<Arc> arcs = new TreeSet<>(Comparator.comparing(getInterval));

        // A function to log the current arcs in the beach line and verify that invariants are
        // satisfied; used for debugging only.
        Runnable logArcs = () -> {
            if (!log.isDebugEnabled())
                return;
            List<String> arcStrings = new ArrayList<>();
            if (!arcs.isEmpty())
                for (Arc arc = arcs.first(); arc != null; arc = arc.next)
                    arcStrings.add(String.format("%s %s", arc, getInterval.apply(arc)));
            log.debug("Arcs at {}: {}", sweepX.get(), Joiner.on(", ").join(arcStrings));
            log.debug("Arcs: {}", arcs);

            int count = 0;
            if (!arcs.isEmpty())
                for (Arc arc = arcs.first(); arc != null; arc = arc.next) {
                    assert arcs.contains(arc);
                    if (arc.prev != null)
                        assert arc.prev.next == arc;
                    if (arc.next != null)
                        assert arc.next.prev == arc;
                    count++;
                }
            assert arcs.size() == count;
        };

        // A map from the indices of each adjacent set of 3 points in the Voronoi diagram to their circumcenter.
        Map<Set<Integer>, Point> vertices = new HashMap<>();
        while (!events.isEmpty()) {
            logArcs.run();
            log.debug("Events: {}", events);
            Event e = events.poll();
            log.debug("Processing event {}", e);
            log.debug("");
            if (e instanceof PointEvent) {
                PointEvent event = (PointEvent) e;

                // Insert degenerate arc at [point.y, point.y].
                Arc arc = Arc.builder()
                    .pointIndex(event.pointIndex)
                    .build();
                sweepX.set(event.x);
                Arc prev = arcs.lower(arc);
                if (prev != null) {
                    Arc next = prev.toBuilder()
                        .prev(arc)
                        .build();
                    prev.next = arc;
                    arc.prev = prev;
                    arc.next = next;
                    if (next.next != null)
                        next.next.prev = next;
                    arcs.add(arc);
                    arcs.add(next);
                } else {
                    // The first arc on the beach line starts at -Infinity, so if there is no
                    // previous arc, then this is the first arc on the beach line.
                    arcs.add(arc);
                }

                /**
                 * Check the two new sets of adjacent three arcs to see if either or both trigger a
                 * vertex event later on. (The previous and next arcs have the same focus, so they
                 * cannot trigger a vertex event with the current arc.)
                 */
                if (arc.prev != null && arc.prev.prev != null)
                    processArc.accept(arc.prev);
                if (arc.next != null && arc.next.next != null)
                    processArc.accept(arc.next);
            } else if (e instanceof VertexEvent) {
                VertexEvent event = (VertexEvent) e;
                Arc toRemove = event.toRemove;
                // If this arc was already removed by another vertex event, then ignore it.
                if (arcs.remove(toRemove)) {
                    if (toRemove.prev != null)
                        toRemove.prev.next = toRemove.next;
                    if (toRemove.next != null)
                        toRemove.next.prev = toRemove.prev;
                    Set<Integer> pointIndices = ImmutableSet.of(toRemove.prev.pointIndex, toRemove.pointIndex, toRemove.next.pointIndex);
                    assert !vertices.containsKey(pointIndices);
                    vertices.put(pointIndices, event.circumcenter);

                    // Process the two new sets of adjacent three arcs after this arc is removed.
                    if (toRemove.prev != null && toRemove.prev.prev != null)
                        processArc.accept(toRemove.prev);
                    if (toRemove.next != null && toRemove.next.next != null)
                        processArc.accept(toRemove.next);
                }
            }
        }
        vertices.forEach((pointIndices, circumcenter) -> {
            log.debug("Vertex at {} (circumcenter of {})", circumcenter, pointIndices);
        });

        /**
         * For each vertex/circumcenter, take the three pairs of points and store the three rays
         * emanating away from the vertex.
         */
        Multimap<PointPair, Ray> allRays = ArrayListMultimap.create();
        vertices.forEach((pointIndices, circumcenter) -> {
            int sumPointIndices = 0;
            for (int i : pointIndices)
                sumPointIndices += i;
            for (int i1 : pointIndices)
                for (int i2 : pointIndices)
                    if (i1 < i2) {
                        Point p1 = wiggledPoints.get(i1);
                        Point p2 = wiggledPoints.get(i2);
                        Point p3 = wiggledPoints.get(sumPointIndices - i1 - i2);
                        PointPair pointPair = PointPair.of(i1, i2);
                        double x1 = p1.x;
                        double y1 = p1.y;
                        double x2 = p2.x;
                        double y2 = p2.y;
                        double xc = circumcenter.x;
                        double yc = circumcenter.y;

                        // The value of t that corresponds to the point on the perpendicular
                        // bisector of p1 and p2 that is closest to the circumcenter. Equivalent
                        // to minimizing (taking the vertex of the parabola):
                        // hypot((x1 + x2) / 2 - (y2 - y1)t - xc, (y1 + y2) / 2 + (x2 - x1)t - yc).
                        double t = ((y2 - y1) * (x1 + x2 - 2 * xc) - (x2 - x1) * (y1 + y2 - 2 * yc))
                                / (2 * ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));

                        // The ray points in the opposite direction as p3 from the line (p1, p2).
                        boolean isLessThanT = Points.crossProduct(p1, p2, p1, p3) > 0;

                        allRays.put(pointPair, new Ray(circumcenter, t, isLessThanT));
                    }
        });

        /**
         * For each pair of adjacent points in the Voronoi diagram, if only one ray was stored in
         * the previous step, then store that ray in the Voronoi diagram; otherwise, if two rays
         * were stored, then store the line segment equal to the intersection of the two rays.
         */
        Set<Border> borders = new HashSet<>();
        for (PointPair pointPair : allRays.keySet()) {
            List<Ray> rays = new ArrayList<>(allRays.get(pointPair));
            if (rays.size() == 1) {
                Ray ray = rays.get(0);
                if (ray.isLessThanT) {
                    borders.add(Border.of(pointPair, Double.NEGATIVE_INFINITY, null, ray.t, ray.point));
                } else {
                    borders.add(Border.of(pointPair, ray.t, ray.point, Double.POSITIVE_INFINITY, null));
                }
            } else if (rays.size() == 2) {
                Ray ray1 = rays.get(0);
                Ray ray2 = rays.get(1);
                if (ray1.t < ray2.t) {
                    borders.add(Border.of(pointPair, ray1.t, ray1.point, ray2.t, ray2.point));
                } else {
                    borders.add(Border.of(pointPair, ray2.t, ray2.point, ray1.t, ray1.point));
                }
            } else {
                assert false;
            }
        }

        // Special case: if there are 2 points, there is a single line and no vertices.
        if (points.size() == 2)
            borders.add(Border.of(PointPair.of(0, 1), Double.NEGATIVE_INFINITY, null, Double.POSITIVE_INFINITY, null));

        return new VoronoiDiagram(borders);
    }

    /**
     * Finds the intersection point y of the two parabolas such that the first parabola is closer to
     * the directrix at y + epsilon.
     *
     * @param xs
     *            the directrix of the two parabolas
     * @param p1
     *            the focus of the first parabola
     * @param p2
     *            the focus of the second parabola
     * @return the intersection point with the given property
     */
    private static double findIntersection(double xs, Point p1, Point p2) {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;
        if (x1 == xs)
            return y1;
        if (x2 == xs)
            return y2;
        double A = 1 / (x1 - xs) - 1 / (x2 - xs);
        double B = y1 / (x1 - xs) - y2 / (x2 - xs);
        double C = (y1 * y1 + x1 * x1 - xs * xs) / (x1 - xs) - (y2 * y2 + x2 * x2 - xs * xs) / (x2 - xs);
        assert A != 0;
        double iy1 = (B - Math.sqrt(B * B - A * C)) / A;
        double iy2 = (B + Math.sqrt(B * B - A * C)) / A;
        return (iy1 - y1) / (x1 - xs) > (iy1 - y2) / (x2 - xs) ? iy1 : iy2;
    }

    private interface Event {

        double getX();
    }

    @Builder
    @Data
    private static class PointEvent implements Event {

        final double x;
        final int pointIndex;
    }

    @Builder
    @Data
    private static class VertexEvent implements Event {

        final double x;
        final Arc toRemove;
        final Point circumcenter;
    }

    @Builder(toBuilder = true)
    @Data
    private static class Arc {

        final int pointIndex;
        Arc prev;
        Arc next;

        @Override
        public String toString() {
            return String.format("Voronoi.Arc(pointIndex=%s, prev=%s, next=%s)", pointIndex, prev == null ? "_" : prev.pointIndex,
                next == null ? "_" : next.pointIndex);
        }
    }

    @Data
    private static class Interval implements Comparable<Interval> {

        final double min;
        final double max;

        @Override
        public int compareTo(Interval other) {
            return min != other.min ? Double.compare(min, other.min) : Double.compare(max, other.max);
        }

        @Override
        public String toString() {
            return String.format("[%.9f, %.9f]", min, max);
        }
    }

    @Data
    private static class Ray {

        final Point point;
        final double t;
        final boolean isLessThanT;
    }

    private Voronoi() {
    }
}
