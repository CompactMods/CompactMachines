package dev.compactmods.machines.test.data;

import com.mojang.serialization.DataResult;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class CodecTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.CODEC_TESTS)
    public static void canSerializeVector3d(final GameTestHelper test) {
        Vec3 expected = new Vec3(1.25d, 2.50d, 3.75d);

        DataResult<Tag> nbtResult = CodecExtensions.VECTOR3D.encodeStart(NbtOps.INSTANCE, expected);
        nbtResult.resultOrPartial(test::fail)
                .ifPresent(nbt -> {
                    ListTag list = (ListTag) nbt;

                    if(expected.x != list.getDouble(0))
                        test.fail("Position x did not match; expected " + expected + " but got " + list);

                    if(expected.y != list.getDouble(1))
                        test.fail("Position y did not match; expected " + expected + " but got " + list);

                    if(expected.z != list.getDouble(2))
                        test.fail("Position z did not match; expected " + expected + " but got " + list);
                });

        test.succeed();
    }
}