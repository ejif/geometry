
package io.github.ejif.geometry.algorithm;

import io.github.ejif.geometry.Point;

public final class Points {

    /**
     * Returns the circumcenter of the given three points. The points must not be collinear.
     *
     * @param p1
     *            the first point
     * @param p2
     *            the second point
     * @param p3
     *            the third point
     * @return the circumcenter
     */
    public static Point circumcenter(Point p1, Point p2, Point p3) {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;
        double x3 = p3.x;
        double y3 = p3.y;
        double A1 = x2 - x1;
        double B1 = y2 - y1;
        double C1 = ((x2 - x1) * (x2 + x1) + (y2 - y1) * (y2 + y1)) / 2;
        double A2 = x3 - x1;
        double B2 = y3 - y1;
        double C2 = ((x3 - x1) * (x3 + x1) + (y3 - y1) * (y3 + y1)) / 2;
        double D = A1 * B2 - A2 * B1;
        if (D == 0)
            throw new IllegalArgumentException("Collinear points do not have a circumcenter.");
        return new Point((C1 * B2 - C2 * B1) / D, (C2 * A1 - C1 * A2) / D);
    }

    /**
     * Returns the cross product of the vector from start1 to end1, with the vector from start2 to
     * end2.
     *
     * @param start1
     *            the beginning of the first vector
     * @param end1
     *            the end of the first vector
     * @param start2
     *            the beginning of the second vector
     * @param end2
     *            the end of the second vector
     * @return the cross product
     */
    public static double crossProduct(Point start1, Point end1, Point start2, Point end2) {
        return (end1.x - start1.x) * (end2.y - start2.y) - (end1.y - start1.y) * (end2.x - start2.x);
    }

    /**
     * Returns the distance between the two points.
     *
     * @param p1
     *            the first point
     * @param p2
     *            the second point
     * @return the distance (a nonnegative number) between the two points
     */
    public static double distance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }

    private Points() {
    }
}
