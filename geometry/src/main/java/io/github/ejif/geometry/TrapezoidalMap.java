
package io.github.ejif.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import io.github.ejif.geometry.algorithm.Points;
import io.github.ejif.geometry.algorithm.Voronoi;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public final class TrapezoidalMap {

    private static final Logger log = LoggerFactory.getLogger(Voronoi.class);
    private static AtomicInteger nodeId = new AtomicInteger();

    private DagNode root;
    private double shear;

    /**
     * Creates a trapezoidal map with a random shear (to ensure that no two distinct points share
     * the same x coordinate, which makes the algorithm much simpler).
     */
    public TrapezoidalMap() {
        this(Math.random());
    }

    @VisibleForTesting
    TrapezoidalMap(double shear) {
        this.root = Trapezoid.builder()
            .region(-1)
            .left(new Point(Double.NEGATIVE_INFINITY, 0))
            .right(new Point(Double.POSITIVE_INFINITY, 0))
            .build();
        this.shear = shear;
    }

    /**
     * Adds a directed edge to the trapezoidal map. The edge cannot intersect any of the other edges
     * in the trapezoidal map, other than sharing an end-point with another edge.
     *
     * @param edge
     *            the edge to add
     * @param leftRegion
     *            an ID of the region on the left of the edge
     * @param rightRegion
     *            an ID of the region on the right of the edge
     */
    public void addEdge(DirectedEdge edge, int leftRegion, int rightRegion) {
        double newDx = edge.getDx() + shear * edge.getDy();
        assert newDx != 0;
        if (newDx < 0)
            edge = edge.flip();
        DirectedEdge shearedEdge = DirectedEdge.builder()
            .anyPoint(shear(edge.getAnyPoint()))
            .dx(Math.abs(newDx))
            .dy(edge.getDy())
            .startPoint(edge.getStartPoint() == null ? new Point(Double.NEGATIVE_INFINITY, 0) : shear(edge.getStartPoint()))
            .endPoint(edge.getEndPoint() == null ? new Point(Double.POSITIVE_INFINITY, 0) : shear(edge.getEndPoint()))
            .build();
        if (newDx < 0)
            addCanonicalLine(shearedEdge, rightRegion, leftRegion);
        else
            addCanonicalLine(shearedEdge, leftRegion, rightRegion);
    }

    /**
     * Finds the ID of the region that the given point is in.
     *
     * @param point
     *            the point to find the region for
     * @return the ID of the region
     */
    public int findRegion(Point point) {
        Point shearedPoint = shear(point);
        return root.visit(new FindTrapezoidDagNodeVisitor(DirectedEdge.builder()
            .anyPoint(shearedPoint)
            .dx(0)
            .dy(0)
            .startPoint(shearedPoint)
            .endPoint(shearedPoint)
            .build())).region;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    private Point shear(Point point) {
        return new Point(point.x + shear * point.y, point.y);
    }

    private void addCanonicalLine(DirectedEdge edge, int topRegion, int bottomRegion) {
        assert edge.getDx() > 0;
        assert edge.getStartPoint() != null;
        assert edge.getEndPoint() != null;

        Trapezoid startTrapezoid = root.visit(new FindTrapezoidDagNodeVisitor(edge));
        if (startTrapezoid.left.x != edge.getStartPoint().x)
            startTrapezoid = (Trapezoid) splitVertically(startTrapezoid, edge.getStartPoint()).right;

        Trapezoid endTrapezoid = root.visit(new FindTrapezoidDagNodeVisitor(edge.flip()));
        if (endTrapezoid.right.x != edge.getEndPoint().x)
            endTrapezoid = (Trapezoid) splitVertically(endTrapezoid, edge.getEndPoint()).left;

        // Check if the line goes through only one trapezoid.
        if (startTrapezoid.left.x == endTrapezoid.left.x)
            startTrapezoid = endTrapezoid;

        List<Trapezoid> originalTrapezoids = new ArrayList<>();
        List<Trapezoid> topTrapezoids = new ArrayList<>();
        List<Trapezoid> bottomTrapezoids = new ArrayList<>();
        List<Boolean> movedToRightTops = new ArrayList<>();
        Trapezoid currentTrapezoid = startTrapezoid;
        while (true) {
            // Split the current trapezoid into a top and bottom.
            Trapezoid topTrapezoid = currentTrapezoid.toBuilder()
                .region(topRegion)
                .build();
            Trapezoid bottomTrapezoid = currentTrapezoid.toBuilder()
                .region(bottomRegion)
                .build();
            replaceNode(currentTrapezoid, YNodeDagNode.of(edge, topTrapezoid, bottomTrapezoid));
            originalTrapezoids.add(currentTrapezoid);
            topTrapezoids.add(topTrapezoid);
            bottomTrapezoids.add(bottomTrapezoid);

            // Update the trapezoid pointers, and then find the next trapezoid on the right.
            Point rightIntersection = getPointAt(edge, currentTrapezoid.right.x);
            if (rightIntersection.y > currentTrapezoid.right.y && currentTrapezoid.rightTop != null) {
                currentTrapezoid = currentTrapezoid.rightTop;
                movedToRightTops.add(true);
            } else if (rightIntersection.y < currentTrapezoid.right.y && currentTrapezoid.rightBottom != null) {
                currentTrapezoid = currentTrapezoid.rightBottom;
                movedToRightTops.add(false);
            } else {
                // We assume the new line cannot intersect the end-point of another line other than
                // at its own end-point, so this was the last trapezoid to split.
                assert currentTrapezoid == endTrapezoid;
                break;
            }
        }

        // Merge trapezoids.
        for (int i = 0; i < movedToRightTops.size(); i++)
            if (movedToRightTops.get(i)) {
                Trapezoid mergedTrapezoid = topTrapezoids.get(i).toBuilder()
                    .right(topTrapezoids.get(i + 1).right)
                    .build();
                replaceNode(topTrapezoids.get(i), mergedTrapezoid);
                replaceNode(topTrapezoids.get(i + 1), mergedTrapezoid);
                topTrapezoids.set(i, mergedTrapezoid);
                topTrapezoids.set(i + 1, mergedTrapezoid);
            } else {
                Trapezoid mergedTrapezoid = bottomTrapezoids.get(i).toBuilder()
                    .right(bottomTrapezoids.get(i + 1).right)
                    .build();
                replaceNode(bottomTrapezoids.get(i), mergedTrapezoid);
                replaceNode(bottomTrapezoids.get(i + 1), mergedTrapezoid);
                bottomTrapezoids.set(i, mergedTrapezoid);
                bottomTrapezoids.set(i + 1, mergedTrapezoid);
            }

        // Update all pointers.
        if (startTrapezoid.leftTop != null) {
            startTrapezoid.leftTop.rightTop = topTrapezoids.get(0);
            startTrapezoid.leftTop.rightBottom = bottomTrapezoids.get(0);
        }
        if (startTrapezoid.leftBottom != null) {
            startTrapezoid.leftBottom.rightTop = topTrapezoids.get(0);
            startTrapezoid.leftBottom.rightBottom = bottomTrapezoids.get(0);
        }
        for (int i = 0; i < movedToRightTops.size(); i++) {
            if (topTrapezoids.get(i) != topTrapezoids.get(i + 1)) {
                replaceLeftTrapezoid(topTrapezoids.get(i + 1), originalTrapezoids.get(i), topTrapezoids.get(i));
                replaceRightTrapezoid(topTrapezoids.get(i), originalTrapezoids.get(i + 1), topTrapezoids.get(i + 1));
            }
            if (bottomTrapezoids.get(i) != bottomTrapezoids.get(i + 1)) {
                replaceLeftTrapezoid(bottomTrapezoids.get(i + 1), originalTrapezoids.get(i), bottomTrapezoids.get(i));
                replaceRightTrapezoid(bottomTrapezoids.get(i), originalTrapezoids.get(i + 1), bottomTrapezoids.get(i + 1));
            }
        }
        if (endTrapezoid.rightTop != null) {
            endTrapezoid.rightTop.leftTop = topTrapezoids.get(topTrapezoids.size() - 1);
            endTrapezoid.rightTop.leftBottom = bottomTrapezoids.get(bottomTrapezoids.size() - 1);
        }
        if (endTrapezoid.rightBottom != null) {
            endTrapezoid.rightBottom.leftTop = topTrapezoids.get(topTrapezoids.size() - 1);
            endTrapezoid.rightBottom.leftBottom = bottomTrapezoids.get(bottomTrapezoids.size() - 1);
        }

        log.debug("Trapezoidal map:\n{}\n", this);
    }

    private XNodeDagNode splitVertically(Trapezoid trapezoid, Point splitPoint) {
        Trapezoid leftTrapezoid = trapezoid.toBuilder()
            .right(splitPoint)
            .build();
        Trapezoid rightTrapezoid = trapezoid.toBuilder()
            .left(splitPoint)
            .build();
        XNodeDagNode node = XNodeDagNode.of(splitPoint.x, leftTrapezoid, rightTrapezoid);
        replaceNode(trapezoid, node);

        leftTrapezoid.rightTop = leftTrapezoid.rightBottom = rightTrapezoid;
        rightTrapezoid.leftTop = rightTrapezoid.leftBottom = leftTrapezoid;
        if (leftTrapezoid.leftTop != null)
            replaceRightTrapezoid(leftTrapezoid.leftTop, trapezoid, leftTrapezoid);
        if (leftTrapezoid.leftBottom != null)
            replaceRightTrapezoid(leftTrapezoid.leftBottom, trapezoid, leftTrapezoid);
        if (rightTrapezoid.rightTop != null)
            replaceLeftTrapezoid(rightTrapezoid.rightTop, trapezoid, rightTrapezoid);
        if (rightTrapezoid.rightBottom != null)
            replaceLeftTrapezoid(rightTrapezoid.rightBottom, trapezoid, rightTrapezoid);

        return node;
    }

    private void replaceLeftTrapezoid(Trapezoid trapezoid, Trapezoid oldLeft, Trapezoid newLeft) {
        if (trapezoid.leftTop == oldLeft)
            trapezoid.leftTop = newLeft;
        if (trapezoid.leftBottom == oldLeft)
            trapezoid.leftBottom = newLeft;
    }

    private void replaceRightTrapezoid(Trapezoid trapezoid, Trapezoid oldRight, Trapezoid newRight) {
        if (trapezoid.rightTop == oldRight)
            trapezoid.rightTop = newRight;
        if (trapezoid.rightBottom == oldRight)
            trapezoid.rightBottom = newRight;
    }

    private void replaceNode(DagNode oldNode, DagNode newNode) {
        if (oldNode == root)
            root = newNode;
        newNode.getParents().addAll(oldNode.getParents());
        for (DagNode parent : oldNode.getParents()) {
            parent.visit(new DagNodeVisitor<Void>() {

                @Override
                public Void visitXNode(XNodeDagNode node) {
                    if (node.left == oldNode)
                        node.left = newNode;
                    else if (node.right == oldNode)
                        node.right = newNode;
                    return null;
                }

                @Override
                public Void visitYNode(YNodeDagNode node) {
                    if (node.top == oldNode)
                        node.top = newNode;
                    else if (node.bottom == oldNode)
                        node.bottom = newNode;
                    return null;
                }

                @Override
                public Void visitTrapezoid(Trapezoid trapezoid) {
                    throw new IllegalStateException("Parent should not be a trapezoid.");
                }
            });
        }
    }

    private static Point getPointAt(DirectedEdge edge, double x) {
        assert edge.getDx() != 0;
        Point p = edge.getAnyPoint();
        return new Point(x, p.y - (p.x - x) / edge.getDx() * edge.getDy());
    }

    private interface DagNode {

        int getId();

        List<DagNode> getParents();

        <T> T visit(DagNodeVisitor<T> visitor);

        String toString(int indent);
    }

    @Data
    private static final class XNodeDagNode implements DagNode {

        final int id = nodeId.incrementAndGet();
        final double x;
        DagNode left;
        DagNode right;
        final List<DagNode> parents = new ArrayList<>();

        static XNodeDagNode of(double x, DagNode left, DagNode right) {
            XNodeDagNode node = new XNodeDagNode(x);
            node.left = left;
            node.right = right;
            left.getParents().clear();
            left.getParents().add(node);
            right.getParents().clear();
            right.getParents().add(node);
            return node;
        }

        @Override
        public <T> T visit(DagNodeVisitor<T> visitor) {
            return visitor.visitXNode(this);
        }

        @Override
        public String toString() {
            return toString(0);
        }

        @Override
        public String toString(int indent) {
            return Strings.repeat(" ", indent) + String.format(
                "XNode id=%d x=%.5f left=%d right=%d parents=%s\n%s\n%s",
                id,
                x,
                left.getId(),
                right.getId(),
                parents.stream().map(DagNode::getId).collect(Collectors.toList()),
                left.toString(indent + 2),
                right.toString(indent + 2));
        }
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class YNodeDagNode implements DagNode {

        final int id = nodeId.incrementAndGet();
        final DirectedEdge edge;
        DagNode top;
        DagNode bottom;
        final List<DagNode> parents = new ArrayList<>();

        static YNodeDagNode of(DirectedEdge edge, DagNode top, DagNode bottom) {
            YNodeDagNode node = new YNodeDagNode(edge);
            node.top = top;
            node.bottom = bottom;
            top.getParents().clear();
            top.getParents().add(node);
            bottom.getParents().clear();
            bottom.getParents().add(node);
            return node;
        }

        @Override
        public <T> T visit(DagNodeVisitor<T> visitor) {
            return visitor.visitYNode(this);
        }

        @Override
        public String toString() {
            return toString(0);
        }

        @Override
        public String toString(int indent) {
            return Strings.repeat(" ", indent) + String.format(
                "YNode id=%d line=[%s, %s, %s, %s] top=%d bottom=%d parents=%s \n%s\n%s",
                id,
                edge.getStartPoint(),
                edge.getAnyPoint(),
                edge.getAnyLaterPoint(),
                edge.getEndPoint(),
                top.getId(),
                bottom.getId(),
                parents.stream().map(DagNode::getId).collect(Collectors.toList()),
                top.toString(indent + 2),
                bottom.toString(indent + 2));
        }
    }

    @Builder(toBuilder = true)
    @Data
    private static final class Trapezoid implements DagNode {

        final int id = nodeId.incrementAndGet();
        final int region;
        final Point left;
        final Point right;
        Trapezoid leftTop;
        Trapezoid leftBottom;
        Trapezoid rightTop;
        Trapezoid rightBottom;
        final List<DagNode> parents = new ArrayList<>();

        @Override
        public <T> T visit(DagNodeVisitor<T> visitor) {
            return visitor.visitTrapezoid(this);
        }

        @Override
        public String toString() {
            return toString(0);
        }

        @Override
        public String toString(int indent) {
            return Strings.repeat(" ", indent) + String.format(
                "Trapezoid id=%d region=%d left=%s right=%s leftTop=%s leftBottom=%s rightTop=%s rightBottom=%s parents=%s",
                id,
                region,
                left,
                right,
                leftTop == null ? "_" : leftTop.id,
                leftBottom == null ? "_" : leftBottom.id,
                rightTop == null ? "_" : rightTop.id,
                rightBottom == null ? "_" : rightBottom.id,
                parents.stream().map(DagNode::getId).collect(Collectors.toList()));
        }
    }

    private interface DagNodeVisitor<T> {

        T visitXNode(XNodeDagNode node);

        T visitYNode(YNodeDagNode node);

        T visitTrapezoid(Trapezoid trapezoid);
    }

    @Data
    private static final class FindTrapezoidDagNodeVisitor implements DagNodeVisitor<Trapezoid> {

        final DirectedEdge edge;

        @Override
        public Trapezoid visitXNode(XNodeDagNode node) {
            if (edge.getStartPoint().x < node.x || edge.getStartPoint().x == node.x && edge.getEndPoint().x < node.x) {
                return node.left.visit(this);
            } else {
                return node.right.visit(this);
            }
        }

        @Override
        public Trapezoid visitYNode(YNodeDagNode node) {
            double newLineToOldLine = Points.crossProduct(
                edge.getAnyPoint(),
                edge.getAnyLaterPoint(),
                node.edge.getAnyPoint(),
                node.edge.getAnyLaterPoint());
            if (Double.isInfinite(edge.getStartPoint().x)) {
                if (newLineToOldLine > 0 || newLineToOldLine == 0 && getPointAt(edge, 0).y > getPointAt(node.edge, 0).y)
                    return node.top.visit(this);
                else
                    return node.bottom.visit(this);
            } else {
                double oldLineToPoint = Points.crossProduct(
                    node.edge.getAnyPoint(),
                    node.edge.getAnyLaterPoint(),
                    node.edge.getAnyPoint(),
                    edge.getStartPoint());
                if (oldLineToPoint > 0 || oldLineToPoint == 0 && newLineToOldLine < 0)
                    return node.top.visit(this);
                else
                    return node.bottom.visit(this);
            }
        }

        @Override
        public Trapezoid visitTrapezoid(Trapezoid trapezoid) {
            return trapezoid;
        }
    }
}
