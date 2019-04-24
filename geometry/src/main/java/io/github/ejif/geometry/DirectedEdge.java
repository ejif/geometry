
package io.github.ejif.geometry;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a directional line segment, ray, or line that corresponds to a subset of the line
 * through anyPoint and anyPoint + (dx, dy). The subset of the line starts and ends at the given
 * points, where a null point represents that the line doesn't end in that direction.
 *
 * For example, the positive x-axis could correspond to anyPoint=(0, 0), dx=1, dy=0, startPoint=(0,
 * 0), endPoint=null.
 */
@Builder(toBuilder = true)
@Data
public final class DirectedEdge {

    private final Point anyPoint;
    private final double dx;
    private final double dy;

    @Nullable
    private final Point startPoint;
    @Nullable
    private final Point endPoint;

    /**
     * Creates a directed line segment from the start point to the end point.
     *
     * @param startPoint
     *            the point to start at
     * @param endPoint
     *            the point to end at
     * @return the directed line segment
     */
    public static DirectedEdge segment(Point startPoint, Point endPoint) {
        return DirectedEdge.builder()
            .anyPoint(startPoint)
            .dx(endPoint.x - startPoint.x)
            .dy(endPoint.y - startPoint.y)
            .startPoint(startPoint)
            .endPoint(endPoint)
            .build();
    }

    /**
     * Creates a ray starting at the given point with the given heading.
     *
     * @param startPoint
     *            the point to start at
     * @param dx
     *            the x component of a vector along this ray
     * @param dy
     *            the y component of the same vector along this ray
     * @return the ray
     */
    public static DirectedEdge ray(Point startPoint, double dx, double dy) {
        return DirectedEdge.builder()
            .anyPoint(startPoint)
            .dx(dx)
            .dy(dy)
            .startPoint(startPoint)
            .build();
    }

    /**
     * Creates a line through the two given points, directed from the first point to the second
     * point.
     *
     * @param anyPoint
     *            any point on the line
     * @param anyOtherPoint
     *            any other point on the line, in the direction of the line from the first given
     *            point
     * @return the directed line
     */
    public static DirectedEdge line(Point anyPoint, Point anyOtherPoint) {
        return DirectedEdge.builder()
            .anyPoint(anyPoint)
            .dx(anyOtherPoint.x - anyPoint.x)
            .dy(anyOtherPoint.y - anyPoint.y)
            .build();
    }

    /**
     * Returns a point on the line after the given point, in the direction of the line.
     *
     * @return the other point
     */
    public Point getAnyLaterPoint() {
        return new Point(anyPoint.x + dx, anyPoint.y + dy);
    }

    /**
     * Returns the same line segment/ray/line, but in the opposite direction.
     *
     * @return the flipped edge
     */
    public DirectedEdge flip() {
        return toBuilder()
            .dx(-dx)
            .dy(-dy)
            .startPoint(endPoint)
            .endPoint(startPoint)
            .build();
    }

    /**
     * Returns a subset of this line that is guaranteed to be finite (a line segment).
     *
     * @param maxNumSteps
     *            the maximum number of dx, dy steps to move away from anyPoint
     * @return the subset line segment
     */
    public DirectedEdge toFiniteSegment(double maxNumSteps) {
        Point newStartPoint = startPoint == null
                ? new Point(anyPoint.x - maxNumSteps * dx, anyPoint.y - maxNumSteps * dy)
                : startPoint;
        Point newEndPoint = endPoint == null
                ? new Point(anyPoint.x + maxNumSteps * dx, anyPoint.y + maxNumSteps * dy)
                : endPoint;
        return toBuilder()
            .startPoint(newStartPoint)
            .endPoint(newEndPoint)
            .build();
    }
}
