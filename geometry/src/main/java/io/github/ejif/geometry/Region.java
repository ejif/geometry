
package io.github.ejif.geometry;

import java.util.List;

import lombok.Data;

@Data
public final class Region {

    /**
     * The edges of this region, in counterclockwise order. The end point of each edge is equal to
     * the start point of the next edge, and the end point of the last edge is equal to the start
     * point of the first edge (points at infinity are considered equal).
     */
    private final List<DirectedEdge> edges;
}
