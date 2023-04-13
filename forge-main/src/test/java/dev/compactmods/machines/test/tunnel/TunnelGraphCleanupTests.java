package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class TunnelGraphCleanupTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canCleanupOrphanedMachines(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        graph.register(BlockPos.ZERO, FakeTunnelDefinition.ID, MACHINE_POS, Direction.UP);

        graph.unregister(BlockPos.ZERO);

        final var machines = graph.machines().collect(Collectors.toSet());
        if(!machines.isEmpty())
            test.fail("Expected no machines to remain registered; got %s".formatted(machines.size()));
    }
}
