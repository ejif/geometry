
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
public final class SubLine {

    private final Point anyPoint;
    private final double dx;
    private final double dy;

    @Nullable
    private final Point startPoint;
    @Nullable
    private final Point endPoint;

    /**
     * Returns the same line segment/ray/line, but in the opposite direction.
     *
     * @return the flipped subline
     */
    public SubLine flip() {
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
    public SubLine toFiniteSegment(double maxNumSteps) {
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
