# Geometry

## Point location

This Java library provides an efficient algorithm for point location: given N points and O(N log N) preprocessing time, the algorithm can find the closest point to a given query point in O(log N) time. The library uses [Fortune's algorithm](http://www.cs.sfu.ca/~binay/813.2011/Fortune.pdf) to construct a Voronoi diagram, and a [trapezoidal map](https://www.ti.inf.ethz.ch/ew/lehre/CG12/lecture/Chapter%209.pdf) to answer point location queries in O(log N) expected time. (The construction of the trapezoidal map involves inserting edges in a randomized order, so the runtime is expected O(log N) for all inputs.)

Usage:

        import io.github.ejif.geometry.Point;
        import io.github.ejif.geometry.algorithm.PointLocation;

        List<Point> points = /* points */
        Point query = /* query point */
        PointLocation pointLocation = new PointLocation(points);
        Point closestPoint = pointLocation.findClosestPoint(query);

This library is [extensively tested](geometry/src/test/java/io/github/ejif/geometry/algorithm) for both randomized point configurations and discrete point configurations with collinear and concyclic points. There are no arbitrary floating point scale requirements (e.g. checks for whether two points are within `EPSILON = 1e-6`), so this library supports points at any scale.

## Maven

The geometry library is published at the following Maven coordinates. See the [Github releases page](https://github.com/ejif/geometry/releases) for the latest version.

        <dependency>
            <groupId>io.github.ejif.geometry</groupId>
            <artifactId>geometry</artifactId>
            <version>{version}</version>
        </dependency>

