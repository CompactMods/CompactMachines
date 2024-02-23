package dev.compactmods.machines.test;


import com.google.common.math.DoubleMath;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.util.AABBHelper;
import dev.compactmods.machines.util.RandomSourceUtil;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Collection;
import java.util.stream.Stream;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class AABBHelperTests {
    private static final String BATCH = "aabb-helper";

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void canFloorToY0(final GameTestHelper test) {
        // Source minY = 5
        AABB before = AABB.ofSize(new Vec3(0, 7.5, 0), 5, 5, 5);

        // Align to Y-0
        final var after = AABBHelper.alignFloor(before, 0);

        test.assertTrue(before.minY == 5, "Before was modified in-place rather than immutably moved.");
        test.assertTrue(after.minY == 0, "After y level should be zero. (was: %s)".formatted(after.minY));
        test.assertTrue(after.getYsize() == 5, "AABB size was modified; should have remained the same.");
        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void canFloorToAnotherAABB(final GameTestHelper test) {
        // Source minY = 5
        AABB before = AABB.ofSize(Vec3.ZERO.relative(Direction.UP, 7.5), 5, 5, 5);

        // Target minY = 1 (bounds are Y 1-11)
        AABB bounds = AABB.ofSize(Vec3.ZERO.relative(Direction.UP, 6), 10, 10, 10);

        // Align to Y-0
        final var after = AABBHelper.alignFloor(before, bounds);

        test.assertTrue(before.minY == 5, "Before was modified in-place rather than immutably moved.");
        test.assertTrue(after.minY == 1, "After y level should be 1. (was: %s)".formatted(after.minY));

        test.assertTrue(after.getYsize() == 5, "AABB size was modified; should have remained the same.");
        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void normalizeToZero(final GameTestHelper test) {
        AABB before = AABB.ofSize(Vec3.ZERO.relative(Direction.UP, 7.5), 5, 5, 5);

        // Align to Y-0
        final var after = AABBHelper.normalize(before);

        test.assertTrue(before.minY == 5, "Before was modified in-place rather than immutably moved.");

        test.assertTrue(after.minX == 0, "After x level was not zero (was: %s)".formatted(after.minX));
        test.assertTrue(after.minY == 0, "After y level was not zero (was: %s)".formatted(after.minY));
        test.assertTrue(after.minZ == 0, "After z level was not zero (was: %s)".formatted(after.minZ));

        test.assertTrue(after.getYsize() == 5, "AABB size was modified; should have remained the same.");
        test.succeed();
    }

    @GameTestGenerator
    public static Collection<TestFunction> normalizeBoundaryTests() {
        final var random = RandomSource.create();
        return Stream.concat(
                        RandomSourceUtil.randomVec3Stream(random).limit(10),

                        // Ensure at least one negative and one positive bound are part of the test
                        Stream.of(
                                Vec3.ZERO.subtract(-3, -2, 5),
                                Vec3.ZERO.add(2, 5, 1)
                        )
                ).map(randomOffset -> new TestFunction(
                        BATCH,
                        "normalize_boundaries_%s".formatted(randomOffset.hashCode()),
                        Constants.MOD_ID + ":empty_1x1",
                        Rotation.NONE,
                        5,
                        0,
                        true,
                        testHelper -> normalizeIntoBoundaries(testHelper, randomOffset)
                ))
                .toList();
    }

    private static void assertVec3Equals(final GameTestHelper testHelper, Vec3 actual, Vec3 expected) {
        if(!DoubleMath.fuzzyEquals(actual.x, expected.x, 0.001))
            testHelper.fail("X did not match expected value (was: %s; expected: %s)".formatted(actual.x, expected.x));

        if(!DoubleMath.fuzzyEquals(actual.y, expected.y, 0.001))
            testHelper.fail("Y did not match expected value (was: %s; expected: %s)".formatted(actual.y, expected.y));

        if(!DoubleMath.fuzzyEquals(actual.z, expected.z, 0.001))
            testHelper.fail("Z did not match expected value (was: %s; expected: %s)".formatted(actual.z, expected.z));
    }

    public static void normalizeIntoBoundaries(final GameTestHelper test, Vec3 randomOffset) {
        AABB before = AABB.ofSize(Vec3.ZERO.relative(Direction.UP, 7.5), 5, 5, 5);
        AABB bounds = AABB.ofSize(randomOffset, 5, 5, 5);

        final var after = AABBHelper.normalizeWithin(before, bounds);

        test.assertTrue(before.minY == 5, "Before was modified in-place rather than immutably moved.");

        assertVec3Equals(test, AABBHelper.minCorner(after), AABBHelper.minCorner(bounds));

        test.assertTrue(after.getYsize() == 5, "AABB size was modified; should have remained the same.");
        test.succeed();
    }
}
