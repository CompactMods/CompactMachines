package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactMachines.MOD_ID)
public class TunnelGraphTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canCreateGraph(final GameTestHelper test) {

        var graph = new TunnelConnectionGraph();

        int size = graph.size();
        if (size > 0)
            test.fail("Graph should begin empty.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canRegisterTunnel(final GameTestHelper test) {

        var graph = new TunnelConnectionGraph();

        try {
            var registered = graph.registerTunnel(BlockPos.ZERO, Tunnels.UNKNOWN.get(), 1, Direction.NORTH);
            if(!registered)
                test.fail("Expected tunnel to be registered without error");

            var typed = graph.getTunnels(Tunnels.UNKNOWN.get());
            if(typed.size() != 1)
                test.fail("Tunnel not found when searching by type.");

            if(!typed.contains(BlockPos.ZERO))
                test.fail("Tunnel position not found when searching by type.");

            var side = graph.getTunnelSide(BlockPos.ZERO);
            if(side.isEmpty())
                test.fail("Tunnel to machine edge not found.");

            side.ifPresent(dir -> {
                if(dir != Direction.NORTH)
                    test.fail(String.format("Tunnel connection side is not correct; expected %s but got %s.", Direction.NORTH, dir));
            });
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }
}
