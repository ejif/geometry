package io.github.ejif.geometry;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.ejif.geometry.algorithm.Points;

public final class TestUtils {

    public static Random rng() {
        return new Random(2915);
    }

    public static List<Point> randomPoints(int numPoints, Random random) {
        return Stream.generate(() -> randomPoint(random)).limit(numPoints).collect(Collectors.toList());
    }

    public static Point randomPoint(Random random) {
        return new Point(random.nextDouble() * 1000, random.nextDouble() * 1000);
    }

    public static List<Point> randomLatticePoints(int numPoints, Random random) {
        return Stream.generate(() -> randomLatticePoint(random)).limit(numPoints).distinct().collect(Collectors.toList());
    }

    public static Point randomLatticePoint(Random random) {
        return new Point(random.nextInt(10) * 100, random.nextInt(10) * 100);
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
