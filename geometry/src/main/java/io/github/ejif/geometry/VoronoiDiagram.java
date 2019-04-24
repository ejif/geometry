
package io.github.ejif.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import lombok.Data;

/**
 * A representation of a Voronoi diagram. Each border contains a directional line segment/ray/line,
 * and stores the point on its left (rigorously, the point p such that the cross product of the
 * sub-line vector and any vector from the sub-line to p is positive) and right (the other side).
 *
 * For example, the following border has leftPointIndex=1 and rightPointIndex=2:
 *
 * <pre>
 *    ^
 *  1 | 2
 *    |
 * </pre>
 */
@Data
public final class VoronoiDiagram {

    private final Set<Border> borders;

    @Data
    public static final class Border {

        private final int leftPointIndex;
        private final int rightPointIndex;
        private final DirectedEdge edge;
    }

    /**
     * Returns a map from each point p (referenced by its index) to the region of points that are
     * closest to p than to any other point.
     *
     * @return the regions
     */
    public Map<Integer, Region> toRegions() {
        Multimap<Integer, DirectedEdge> allEdges = ArrayListMultimap.create();
        for (Border border : borders) {
            allEdges.put(border.leftPointIndex, border.edge);
            allEdges.put(border.rightPointIndex, border.edge.flip());
        }
        ImmutableMap.Builder<Integer, Region> regions = ImmutableMap.builder();
        for (int pointIndex : allEdges.keySet()) {
            List<DirectedEdge> edges = new ArrayList<>(allEdges.get(pointIndex));
            Map<Point, DirectedEdge> edgesByStartPoint = new HashMap<>();
            for (DirectedEdge edge : edges.subList(1, edges.size()))
                edgesByStartPoint.put(edge.getStartPoint(), edge);
            ImmutableList.Builder<DirectedEdge> orderedEdges = ImmutableList.builder();
            for (DirectedEdge edge = edges.get(0); edge != null; edge = edgesByStartPoint.remove(edge.getEndPoint()))
                orderedEdges.add(edge);
            regions.put(pointIndex, new Region(orderedEdges.build()));
        }
        return regions.build();
    }
}
