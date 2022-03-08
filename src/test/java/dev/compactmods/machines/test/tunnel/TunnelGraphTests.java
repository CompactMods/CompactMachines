package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.stream.Collectors;

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
            if (!registered)
                test.fail("Expected tunnel to be registered without error");

            var typed = graph.getTunnelsByType(Tunnels.UNKNOWN.get());
            if (typed.size() != 1)
                test.fail("Tunnel not found when searching by type.");

            if (!typed.contains(BlockPos.ZERO))
                test.fail("Tunnel position not found when searching by type.");

            var side = graph.getTunnelSide(BlockPos.ZERO);
            if (side.isEmpty())
                test.fail("Tunnel to machine edge not found.");

            side.ifPresent(dir -> {
                if (dir != Direction.NORTH)
                    test.fail(String.format("Tunnel connection side is not correct; expected %s but got %s.", Direction.NORTH, dir));
            });
        } catch (Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canFetchSupportingTunnelsOnSide(final GameTestHelper test) {
        var graph = new TunnelConnectionGraph();

        graph.registerTunnel(BlockPos.ZERO, Tunnels.ITEM_TUNNEL_DEF.get(), 1, Direction.NORTH);
        graph.registerTunnel(BlockPos.ZERO.above(), Tunnels.ITEM_TUNNEL_DEF.get(), 1, Direction.SOUTH);
        graph.registerTunnel(BlockPos.ZERO.below(), Tunnels.UNKNOWN.get(), 1, Direction.NORTH);

        final var positions = graph.getTunnelsSupporting(1, Direction.NORTH, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .collect(Collectors.toSet());

        if(positions.isEmpty())
            test.fail("Items are supported on the item tunnel type.");

        if(positions.size() != 1)
            test.fail("Should only have one position found.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canFetchSidedTypes(final GameTestHelper test) {
        var graph = new TunnelConnectionGraph();

        // Register three tunnels - two types on the north side (different positions), one on south
        graph.registerTunnel(BlockPos.ZERO, Tunnels.ITEM_TUNNEL_DEF.get(), 1, Direction.NORTH);
        graph.registerTunnel(BlockPos.ZERO.above(), Tunnels.ITEM_TUNNEL_DEF.get(), 1, Direction.SOUTH);
        graph.registerTunnel(BlockPos.ZERO.below(), Tunnels.UNKNOWN.get(), 1, Direction.NORTH);

        final var positions = graph.getTypesForSide(1, Direction.NORTH)
                .collect(Collectors.toSet());

        if(positions.size() != 2)
            test.fail("Should have two tunnel types for the machine side.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canSerializeData(final GameTestHelper test) {

        var graph = new TunnelConnectionGraph();

        var registered = graph.registerTunnel(BlockPos.ZERO, Tunnels.UNKNOWN.get(), 1, Direction.NORTH);
        if (!registered)
            test.fail("Expected tunnel to be registered without error");

        var savedData = graph.serializeNBT();

        if (!savedData.contains("nodes"))
            test.fail("Did not serialize node information.");

        var d = new TunnelConnectionGraph();
        d.deserializeNBT(savedData);

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canDeserializeTunnelData(final GameTestHelper test) {

        var graph = new TunnelConnectionGraph();

        var data = new CompoundTag();

        ListTag nodeList = new ListTag();
        nodeList.add(makeTunnelTag(test, BlockPos.ZERO));
        nodeList.add(makeTunnelTag(test, BlockPos.ZERO.north()));
        nodeList.add(makeTunnelTag(test, BlockPos.ZERO.south()));

        data.put("nodes", nodeList);
        data.put("edges", new ListTag());

        graph.deserializeNBT(data);

        if(graph.size() != 3)
            test.fail("Did not get expected number of nodes.");

        if (!graph.hasTunnel(BlockPos.ZERO))
            test.fail("Did not serialize node information.");

        test.succeed();
    }

    @Nonnull
    private static CompoundTag makeTunnelTag(GameTestHelper test, BlockPos location) {
        CompoundTag tunnTag = new CompoundTag();
        TunnelNode tunn = new TunnelNode(location);
        var tunnNbt = TunnelNode.CODEC.encodeStart(NbtOps.INSTANCE, tunn)
                .getOrThrow(false, test::fail);

        tunnTag.putUUID("id", UUID.randomUUID());
        tunnTag.put("data", tunnNbt);
        return tunnTag;
    }
}
