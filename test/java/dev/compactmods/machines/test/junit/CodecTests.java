package dev.compactmods.machines.test.junit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ExtendWith(EphemeralTestServerProvider.class)
public class CodecTests {

   @Test
   public void test() {
	  final var MAP = Map.of(UUID.randomUUID(), List.of("hello", "world"));

	  final var result = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING.listOf())
		  .encodeStart(JsonOps.INSTANCE, MAP)
		  .resultOrPartial(Assertions::fail);

	  Assertions.assertNotNull(result);
   }

   @Test
   public void canSerializeVector3d() {
	  Vec3 expected = new Vec3(1.25d, 2.50d, 3.75d);

	  DataResult<Tag> nbtResult = Vec3.CODEC.encodeStart(NbtOps.INSTANCE, expected);
	  nbtResult.resultOrPartial(Assertions::fail)
		  .ifPresent(nbt -> {
			 ListTag list = (ListTag) nbt;

			 Assertions.assertEquals(expected.x, list.getDouble(0), "Position x did not match.");
			 Assertions.assertEquals(expected.y, list.getDouble(1), "Position y did not match.");
			 Assertions.assertEquals(expected.z, list.getDouble(2), "Position z did not match.");
		  });
   }
}