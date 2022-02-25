package dev.compactmods.machines.test.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import dev.compactmods.machines.test.util.FileHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;

@DisplayName("External Machine Data")
public class MachineExternalDataTests {
    final static Path EXTERNAL = Paths.get("scenario", "single-machine-player-inside", "machines_external.dat");

    final static Codec<List<CompactMachineData.MachineData>> c = CompactMachineData.MachineData.CODEC.listOf()
            .fieldOf("locations")
            .stable()
            .codec();

    @GameTest(templateNamespace = CompactMachines.MOD_ID, prefixTemplateWithClassname = false, template = "empty_5x", timeoutTicks = 240)
    public static void canLoadSingleMachineData(final GameTestHelper game) throws IOException {
        // The external point is overworld @ 8x4x8 (it was made in a default void superflat)
        DimensionalPosition OUTSIDE = new DimensionalPosition(
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld")),
                new BlockPos(8, 4, 8)
        );

        CompoundTag nbt = FileHelper.getNbtFromFile(EXTERNAL.toString());
        CompoundTag data = nbt.getCompound("data");
        DataResult<List<CompactMachineData.MachineData>> result = c.parse(NbtOps.INSTANCE, data);

        if (data.isEmpty()) {
            game.fail("Expected data to be read from external data file; it was empty.");
            return;
        }

        Optional<List<CompactMachineData.MachineData>> res = result.result();
        if (res.isEmpty()) {
            game.fail("Expected machine info; got nothing.");
            return;
        }

        CompactMachines.LOGGER.debug("hi - " + game.getTick());

        res.ifPresent(list -> {
            if (list.size() != 1) {
                game.fail("Expected exactly one connection; got " + list.size());
                return;
            }

            CompactMachines.LOGGER.debug("hi - " + game.getTick());

            CompactMachineData.MachineData extern = list.get(0);
            if(!extern.location.equals(OUTSIDE)) {
                game.fail("Connected position is not correct.");
                return;
            }

            game.succeed();
        });
    }
}
