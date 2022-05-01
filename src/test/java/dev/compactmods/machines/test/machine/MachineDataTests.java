package dev.compactmods.machines.test.machine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.core.LevelBlockPosition;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.FileHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactMachines.MOD_ID)
public class MachineDataTests {
    final static Path EXTERNAL = Paths.get("scenario", "single-machine-player-inside", "machines_external.nbt");

    final static Codec<List<CompactMachineData.MachineData>> c = CompactMachineData.MachineData.CODEC.listOf()
            .fieldOf("locations")
            .stable()
            .codec();

    @GameTest(template = "empty_5x5", timeoutTicks = 240, batch = TestBatches.MACHINE_DATA)
    public static void canLoadSingleMachineData(final GameTestHelper game) throws IOException {
        // The external point is overworld @ 8x4x8 (it was made in a default void superflat)
        LevelBlockPosition OUTSIDE = new LevelBlockPosition(
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
