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

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class TunnelGraphTunnelTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void unboundTunnelPositionReturnsNoResult(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();

        final var connectedTo = graph.machine(BlockPos.ZERO);
        if(connectedTo.isPresent()) {
            test.fail("There should not be a connection.");
        }

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void returnsNothingForUnregisteredLocations(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();

        graph.info(BlockPos.ZERO).ifPresent(info -> {
            test.fail("Got registration info for an unregistered position: %s".formatted(info.type()));
        });

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canRegisterSingleTunnel(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        graph.register(BlockPos.ZERO, FakeTunnelDefinition.ID, MACHINE_POS, Direction.UP);

        if(!graph.has(BlockPos.ZERO))
            test.fail("Graph is reporting tunnel is not registered.");

        graph.info(BlockPos.ZERO).ifPresentOrElse(info -> {
            if(!info.location().equals(BlockPos.ZERO))
                test.fail("Tunnel location is not correct.");

            if(!info.machine().equals(MACHINE_POS))
                test.fail("Tunnel machine is not correct.");

            if(!info.side().equals(Direction.UP))
                test.fail("Tunnel side is not correct.");

            if(!info.type().equals(FakeTunnelDefinition.ID.location()))
                test.fail("Tunnel type is not correct.");
        }, () -> {
            test.fail("Tunnel was not registered correctly.");
        });

        test.succeed();
    }
}
