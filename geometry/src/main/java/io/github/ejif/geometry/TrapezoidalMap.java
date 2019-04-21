
package io.github.ejif.geometry;

import java.util.HashSet;
import java.util.Set;

import io.github.ejif.geometry.algorithm.Points;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public final class TrapezoidalMap {

    private DagNode root;

    public TrapezoidalMap() {
        root = Trapezoid.builder()
            .region(-1)
            .left(new Point(Double.NEGATIVE_INFINITY, 0))
            .right(new Point(Double.POSITIVE_INFINITY, 0))
            .build();
    }

    private Trapezoid addLine(SubLine subLine, int topRegion, int bottomRegion) {
        assert subLine.getDx() > 0;

        Trapezoid startRegion = root.visit(new FindTrapezoidDagNodeVisitor(subLine));
        Trapezoid endRegion = root.visit(new FindTrapezoidDagNodeVisitor(subLine.flip()));

        double startX = subLine.getStartPoint() == null ? Double.NEGATIVE_INFINITY : subLine.getStartPoint().x;
        Trapezoid currentTrapezoid = startRegion;
        if (startRegion.left.x != startX) {
            // Split the leftmost trapezoid into two adjacent parts.
            Trapezoid leftTrapezoid = currentTrapezoid.toBuilder()
                .region(startRegion.region)
                .right(subLine.getStartPoint())
                .build();
            Trapezoid rightTrapezoid = Trapezoid.builder()
                .region(startRegion.region)
                .left(subLine.getStartPoint())
                .build();
            leftTrapezoid.rightTop = leftTrapezoid.rightBottom = rightTrapezoid;
            rightTrapezoid.leftTop = rightTrapezoid.leftBottom = leftTrapezoid;
            replaceNode(startRegion, XNodeDagNode.of(subLine.getStartPoint().x, leftTrapezoid, rightTrapezoid));
            currentTrapezoid = leftTrapezoid;
        }
        double endX = subLine.getEndPoint() == null ? Double.POSITIVE_INFINITY : subLine.getEndPoint().x;
        while (currentTrapezoid.right.x < endX) {
            // Split the current trapezoid into a top and bottom.
            Point leftIntersection = getPointAt(subLine, currentTrapezoid.left.x);
            Point rightIntersection = getPointAt(subLine, currentTrapezoid.right.x);
            Trapezoid topTrapezoid = currentTrapezoid.toBuilder()
                .region(topRegion)
                .build();
            Trapezoid bottomTrapezoid = currentTrapezoid.toBuilder()
                .region(bottomRegion)
                .build();
            if (leftIntersection.y > currentTrapezoid.left.y) {
                topTrapezoid.left = null;
                topTrapezoid.leftBottom = topTrapezoid.leftTop;
            } else if (leftIntersection.y < currentTrapezoid.left.y) {
                bottomTrapezoid.left = null;
                bottomTrapezoid.leftTop = bottomTrapezoid.leftBottom;
            }
            replaceNode(currentTrapezoid, YNodeDagNode.of(subLine, topTrapezoid, bottomTrapezoid));

            // Update the trapezoid pointers, and then find the next trapezoid on the right.
            if (rightIntersection.y > currentTrapezoid.right.y) {
                topTrapezoid.right = null;
                topTrapezoid.rightBottom = topTrapezoid.rightTop;
                currentTrapezoid = currentTrapezoid.rightTop;
            } else if (rightIntersection.y < currentTrapezoid.right.y) {
                bottomTrapezoid.right = null;
                bottomTrapezoid.rightTop = bottomTrapezoid.rightBottom;
                currentTrapezoid = currentTrapezoid.rightBottom;
            } else {
                // This can only happen if the line intersects the end-point of another line.
                assert false;
            }
        }

        // TODO
        return null;
    }

    private void replaceNode(DagNode node, DagNode newNode) {
        if (node == root)
            root = newNode;
        newNode.getParents().addAll(node.getParents());
        for (DagNode parent : node.getParents()) {
            parent.visit(new DagNodeVisitor<Void>() {

                @Override
                public Void visitXNode(XNodeDagNode node) {
                    if (node.left == node)
                        node.left = newNode;
                    else if (node.right == node)
                        node.right = newNode;
                    return null;
                }

                @Override
                public Void visitYNode(YNodeDagNode node) {
                    if (node.top == node)
                        node.top = newNode;
                    else if (node.bottom == node)
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

    private static Point getPointAt(SubLine subLine, double x) {
        assert subLine.getDx() != 0;
        Point p = subLine.getAnyPoint();
        return new Point(x, p.y - (p.x - x) / subLine.getDx() * subLine.getDy());
    }

    private interface DagNode {

        Set<DagNode> getParents();

        <T> T visit(DagNodeVisitor<T> visitor);
    }

    @Data
    private static final class XNodeDagNode implements DagNode {

        final double x;
        DagNode left;
        DagNode right;
        Set<DagNode> parents = new HashSet<>();

        static XNodeDagNode of(double x, DagNode left, DagNode right) {
            XNodeDagNode node = new XNodeDagNode(x);
            node.left = left;
            node.right = right;
            if (left != null) {
                left.getParents().clear();
                left.getParents().add(node);
            }
            if (right != null) {
                right.getParents().clear();
                right.getParents().add(node);
            }
            return node;
        }

        @Override
        public <T> T visit(DagNodeVisitor<T> visitor) {
            return visitor.visitXNode(this);
        }
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class YNodeDagNode implements DagNode {

        final SubLine subLine;
        DagNode top;
        DagNode bottom;
        Set<DagNode> parents = new HashSet<>();

        static YNodeDagNode of(SubLine subLine, DagNode top, DagNode bottom) {
            YNodeDagNode node = new YNodeDagNode(subLine);
            node.top = top;
            node.bottom = bottom;
            if (top != null) {
                top.getParents().clear();
                top.getParents().add(node);
            }
            if (bottom != null) {
                bottom.getParents().clear();
                bottom.getParents().add(node);
            }
            return node;
        }

        @Override
        public <T> T visit(DagNodeVisitor<T> visitor) {
            return visitor.visitYNode(this);
        }
    }

    @Builder(toBuilder = true)
    @Data
    private static final class Trapezoid implements DagNode {

        final int region;
        Point left;
        Point right;
        Trapezoid leftTop;
        Trapezoid leftBottom;
        Trapezoid rightTop;
        Trapezoid rightBottom;
        final Set<DagNode> parents = new HashSet<>();

        @Override
        public <T> T visit(DagNodeVisitor<T> visitor) {
            return visitor.visitTrapezoid(this);
        }
    }

    private interface DagNodeVisitor<T> {

        T visitXNode(XNodeDagNode node);

        T visitYNode(YNodeDagNode node);

        T visitTrapezoid(Trapezoid trapezoid);
    }

    @Data
    private static final class FindTrapezoidDagNodeVisitor implements DagNodeVisitor<Trapezoid> {

        final SubLine subLine;

        @Override
        public Trapezoid visitXNode(XNodeDagNode node) {
            if (subLine.getStartPoint() == null || subLine.getStartPoint().x < node.x
                    || subLine.getStartPoint().x == node.x && subLine.getEndPoint().x < node.x) {
                return node.left.visit(this);
            } else {
                return node.right.visit(this);
            }
        }

        @Override
        public Trapezoid visitYNode(YNodeDagNode node) {
            double newLineToOldLine = Points.crossProduct(
                subLine.getAnyPoint(),
                subLine.getAnyLaterPoint(),
                node.subLine.getAnyPoint(),
                node.subLine.getAnyLaterPoint());
            if (subLine.getStartPoint() == null) {
                if (newLineToOldLine > 0 || newLineToOldLine == 0 && getPointAt(subLine, 0).y > getPointAt(node.subLine, 0).y)
                    return node.top.visit(this);
                else
                    return node.bottom.visit(this);
            } else {
                double oldLineToPoint = Points.crossProduct(
                    node.subLine.getAnyPoint(),
                    node.subLine.getAnyLaterPoint(),
                    node.subLine.getAnyPoint(),
                    subLine.getStartPoint());
                // TODO only need to check newLineToOldLine if an endpoint can intersect another line
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
