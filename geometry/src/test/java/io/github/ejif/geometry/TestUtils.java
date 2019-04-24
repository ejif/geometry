package io.github.ejif.geometry;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.ejif.geometry.algorithm.Points;

public final class TestUtils {

    public static final Random RNG = new Random(2915);

    public static List<Point> randomPoints(int numPoints) {
        return Stream.generate(TestUtils::randomPoint).limit(numPoints).collect(Collectors.toList());
    }

    public static Point randomPoint() {
        return new Point(RNG.nextDouble() * 1000, RNG.nextDouble() * 1000);
    }

    public static Point findClosestPoint(List<Point> points, Point queryPoint) {
        Point closest = points.get(0);
        for (Point p : points)
            if (Points.distance(queryPoint, p) < Points.distance(queryPoint, closest))
                closest = p;
        return closest;
    }

    private TestUtils() {}
}
