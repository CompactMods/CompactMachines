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
public class TunnelGraphMachineTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void unregisteredMachineConnectionCheck(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        final var connections = graph.positions(MACHINE_POS).collect(Collectors.toUnmodifiableSet());

        if(!connections.isEmpty()) {
            test.fail("There should be no connections registered.");
        }

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void tunnelToMachineRelationship(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        graph.register(BlockPos.ZERO, FakeTunnelDefinition.ID, MACHINE_POS, Direction.UP);

        // Check connected machine
        graph.machine(BlockPos.ZERO).ifPresentOrElse(mp -> {
            if(!mp.equals(MACHINE_POS))
                test.fail("Connected machine position did not match.");
        }, () -> {
            test.fail("Expected the tunnel to be connected to a machine. It was not.");
        });

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void machineToTunnelRelationship(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        graph.register(BlockPos.ZERO, FakeTunnelDefinition.ID, MACHINE_POS, Direction.UP);

        final var connectedToMachine = graph.positions(MACHINE_POS)
                .collect(Collectors.toUnmodifiableSet());

        if(connectedToMachine.size() != 1) {
            test.fail("Expected machine to have 1 connection.");
        }

        if(!connectedToMachine.contains(BlockPos.ZERO)) {
            test.fail("Expected machine to be connected to the tunnel.");
        }

        test.succeed();
    }
}
