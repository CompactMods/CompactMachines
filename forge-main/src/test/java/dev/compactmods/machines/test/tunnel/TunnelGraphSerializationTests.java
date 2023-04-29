package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.graph.GraphTraversalHelper;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.NbtTestUtils;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.nbt.TunnelGraphNbtKeys;
import dev.compactmods.machines.tunnel.graph.nbt.TunnelGraphNbtSerializer;
import dev.compactmods.machines.tunnel.graph.node.TunnelNode;
import dev.compactmods.machines.tunnel.graph.node.TunnelTypeNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class TunnelGraphSerializationTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canSerializeEmptyTunnelGraph(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var nbt = TunnelGraphNbtSerializer.serialize(graph);

        if(!nbt.contains(TunnelGraphNbtKeys.NODE_GROUP, Tag.TAG_COMPOUND)) {
            test.fail("Expected an graph node compound in the dataset.");
            return;
        }

        if(!nbt.contains(TunnelGraphNbtKeys.EDGE_GROUP, Tag.TAG_COMPOUND)) {
            test.fail("Expected an graph edge compound in the dataset.");
            return;
        }

        final var nodesNbt = nbt.getCompound(TunnelGraphNbtKeys.NODE_GROUP);
        if(nodesNbt.isEmpty()) {
            test.fail("Expected graph node group to be populated.");
            return;
        }

        final var edgesNbt = nbt.getCompound(TunnelGraphNbtKeys.EDGE_GROUP);
        if(edgesNbt.isEmpty()) {
            test.fail("Expected graph edge group to be populated.");
            return;
        }

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canSerializeSingleTunnel(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();

        final var MACHINE_POS = GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO);
        graph.register(BlockPos.ZERO, Tunnels.UNKNOWN_KEY, MACHINE_POS, Direction.UP);

        final var nbt = TunnelGraphNbtSerializer.serialize(graph);

        final var nodesNbt = nbt.getCompound(TunnelGraphNbtKeys.NODE_GROUP);
        final var edgesNbt = nbt.getCompound(TunnelGraphNbtKeys.EDGE_GROUP);

        if(nodesNbt.size() != 4) {
            test.fail("Did not get expected number of nodes for a single tunnel registration.");
            return;
        }

        if(nodesNbt.getInt(TunnelGraphNbtKeys.NODE_COUNT) != 3) {
            test.fail("Expected 3 nodes accounted for; got a different result.");
        }

        NbtTestUtils.checkListSize(nodesNbt, TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_LIST, 1);
        NbtTestUtils.checkListSize(nodesNbt, TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_TYPE_LIST, 1);
        NbtTestUtils.checkListSize(nodesNbt, TunnelGraphNbtKeys.NODE_GROUP_MACHINE_LIST, 1);

        if(edgesNbt.size() != 3) {
            test.fail("Did not get expected number of edges for a single tunnel registration.");
            return;
        }

        if(edgesNbt.getInt(TunnelGraphNbtKeys.EDGE_COUNT) != 2) {
            test.fail("Expected 2 edges accounted for; got a different result.");
        }

        NbtTestUtils.checkListSize(edgesNbt, TunnelGraphNbtKeys.EDGE_GROUP_MACHINE_LIST, 1);
        NbtTestUtils.checkListSize(edgesNbt, TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_LIST, 1);

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void deserializeEarlyExitsEmptyTag(final GameTestHelper test) {
        final var tag = new CompoundTag();

        test.succeedIf(() -> {
            final var graph = TunnelGraphNbtSerializer.fromNbt(tag);
            if(graph.size() != 0)
                test.fail("Expected an empty graph.");
        });
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canDeserializeTunnels(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();

        final var MACHINE_POS = GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO);
        graph.register(BlockPos.ZERO.above(), Tunnels.UNKNOWN_KEY, MACHINE_POS, Direction.UP);
        graph.register(BlockPos.ZERO, Tunnels.UNKNOWN_KEY, MACHINE_POS, Direction.DOWN);

        final var nbt = TunnelGraphNbtSerializer.serialize(graph);

        final var newGraph = TunnelGraphNbtSerializer.fromNbt(nbt);

        long machineCount = newGraph.machines().count();
        if(machineCount != 1) {
            test.fail("Expected one machine node; got %s.".formatted(machineCount));
        }

        long tunnelCount = GraphTraversalHelper.nodes(graph.graph(), TunnelNode.class).count();
        if(tunnelCount != 2) {
            test.fail("Expected 2 tunnel nodes; got %s.".formatted(tunnelCount));
        }

        long tunnelTypeCount = GraphTraversalHelper.nodes(graph.graph(), TunnelTypeNode.class).count();
        if(tunnelTypeCount != 1) {
            test.fail("Expected 1 tunnel type node; got %s.".formatted(tunnelTypeCount));
        }

        test.succeed();
    }
}
