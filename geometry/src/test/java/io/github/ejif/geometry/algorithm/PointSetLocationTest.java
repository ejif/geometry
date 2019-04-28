
package io.github.ejif.geometry.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.ejif.geometry.Point;
import io.github.ejif.geometry.TestUtils;
import lombok.Data;

@Data
@RunWith(Parameterized.class)
public final class PointSetLocationTest {

    private final TestCase testCase;

    @Parameterized.Parameters
    public static List<?> parameters() {
        Random random = TestUtils.rng();
        List<TestCase> parameters = new ArrayList<>();

        for (int i = 0; i < 10; i++)
            parameters.add(new TestCase(TestUtils.randomPoints(1 + i, random), TestUtils.randomPoints(1 + i, random)));
        parameters.add(new TestCase(TestUtils.randomPoints(100, random), TestUtils.randomPoints(100, random)));

        for (int i = 0; i < 10; i++)
            parameters.add(new TestCase(TestUtils.randomLatticePoints(4 + i, random), TestUtils.randomPoints(4 + i, random)));
        parameters.add(new TestCase(TestUtils.randomLatticePoints(100, random), TestUtils.randomPoints(100, random)));

        return parameters;
    }

    @Test
    public void testFindClosestPoint_isCorrect() {
        PointSet points = new PointSet(testCase.anchorPoints, TestUtils.rng());
        for (Point p : testCase.queryPoints)
            assertThat(points.findClosestPoint(p)).isEqualTo(TestUtils.findClosestPoint(testCase.anchorPoints, p));
    }

    @Data
    private static final class TestCase {

        final List<Point> anchorPoints;
        final List<Point> queryPoints;
    }
}
