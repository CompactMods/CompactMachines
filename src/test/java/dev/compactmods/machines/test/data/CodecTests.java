package dev.compactmods.machines.test.data;

import com.mojang.serialization.DataResult;
import dev.compactmods.machines.data.codec.CodecExtensions;
import dev.compactmods.machines.reference.EnumMachineSize;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// @GameTestHolder / @ModGameTests here?
public class CodecTests {

    @Test
    void canSerializeVector3d() {
        Vec3 expected = new Vec3(1.25d, 2.50d, 3.75d);

        DataResult<Tag> nbtResult = CodecExtensions.VECTOR3D.encodeStart(NbtOps.INSTANCE, expected);
        nbtResult.resultOrPartial(Assertions::fail)
                .ifPresent(nbt -> {
                    ListTag list = (ListTag) nbt;

                    Assertions.assertEquals(expected.x, list.getDouble(0));
                    Assertions.assertEquals(expected.y, list.getDouble(1));
                    Assertions.assertEquals(expected.z, list.getDouble(2));
                });
    }

    @Test
    void canSerializeMachineSize() {
        DataResult<Tag> result = EnumMachineSize.CODEC.encodeStart(NbtOps.INSTANCE, EnumMachineSize.LARGE);

        result.resultOrPartial(Assertions::fail)
                .ifPresent(nbt -> {
                    Assertions.assertEquals(StringTag.TYPE, nbt.getType());

                    StringTag string = (StringTag) nbt;
                    Assertions.assertNotNull(string);
                    Assertions.assertEquals(EnumMachineSize.LARGE.getSerializedName(), string.getAsString());
                });
    }
}