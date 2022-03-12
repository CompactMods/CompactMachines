package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.*;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.TestUtil;
import dev.compactmods.machines.tunnel.TunnelItem;
import dev.compactmods.machines.tunnel.TunnelWallBlock;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.DimensionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GameTestHolder(CompactMachines.MOD_ID)
public class TunnelTests {

    private static AABB ROOM_BOUNDS;
    private static ChunkPos TESTING_ROOM;

    private static Logger LOG = LogManager.getLogger();

    @BeforeBatch(batch = TestBatches.TUNNELS)
    public static void beforeTunnelTests(final ServerLevel level) {
        final var server = level.getServer();

        try {
            var compactLevel = server.getLevel(Registration.COMPACT_DIMENSION);
            if (compactLevel == null) {
                CompactMachines.LOGGER.warn("Compact dimension not found; recreating it.");
                DimensionUtil.createAndRegisterWorldAndDimension(server);
            }

            LOG.info("Starting tunnel tests; creating a new room...");

            TESTING_ROOM = Rooms.createNew(server, RoomSize.NORMAL, UUID.randomUUID());

            LOG.info("New room generated at chunk: {}", TESTING_ROOM);

            ROOM_BOUNDS = CompactRoomData.get(server).getBounds(TESTING_ROOM);
        } catch (MissingDimensionException | NonexistentRoomException e) {
            e.printStackTrace();
        }
    }

    @AfterBatch(batch = TestBatches.TUNNELS)
    public static void afterTunnelTests(final ServerLevel level) {
        final var server = level.getServer();

        try {
            LOG.info("Starting test room destruction.");
            Stream<Integer> connected = Rooms.getConnectedMachines(server, TESTING_ROOM);
            connected.forEach(mach -> Machines.destroy(server, mach));

            Rooms.destroy(server, TESTING_ROOM);
            LOG.info("Finished destruction of test room.");
        } catch (MissingDimensionException | NonexistentRoomException e) {
            e.printStackTrace();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNELS, timeoutTicks = 150)
    public static void tunnel_item_transforms_wall(final GameTestHelper test) {
        var serv = test.getLevel().getServer();
        var comLev = serv.getLevel(Registration.COMPACT_DIMENSION);

        final var seq = test.startSequence();
        int offset = 0;
        for (var dir : Direction.values()) {
            seq.thenExecuteAfter(offset, () -> {
                try {
                    useTunnelItem(test, comLev, dir);
                } catch (MissingDimensionException e) {
                    e.printStackTrace();
                }
            });

            offset += 5;
        }

        seq.thenIdle(60)
                .thenExecute(() -> checkTunnelWalls(test, comLev))
                .thenSucceed();
    }

    private static void checkTunnelWalls(GameTestHelper test, ServerLevel comLev) {
        RoomTunnelData roomTunnels = null;
        try {
            roomTunnels = RoomTunnelData.get(comLev.getServer(), TESTING_ROOM);
        } catch (MissingDimensionException e) {
            test.fail(e.getMessage());
        }

        final var graph = roomTunnels.getGraph();

        final long countedTunnels = comLev.getBlockStates(ROOM_BOUNDS.inflate(1))
                .filter(s -> s.getBlock() instanceof TunnelWallBlock)
                .count();

        final var positions = graph.getTunnelNodesByType(Tunnels.ITEM_TUNNEL_DEF.get())
                .map(TunnelNode::position)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        if (countedTunnels != 6)
            test.fail("Expected 6 tunnel walls inside the room boundaries. Got " + countedTunnels);

        if (positions.size() != 6)
            test.fail("Expected a tunnel to be placed on all 6 sides. Got " + positions.size());

        for (var tunnelLocation : positions) {

            final var machine = graph.connectedMachine(tunnelLocation).orElse(-1);
            if (machine == -1)
                test.fail("Tunnel not connected to a machine: " + tunnelLocation);

            var side = graph.getTunnelSide(tunnelLocation);
            if (side.isEmpty()) {
                test.fail("Did not find side for tunnel: " + tunnelLocation);
                return;
            }

            final var dir = side.get();

            final var wallState = comLev.getBlockState(tunnelLocation);
            if (!(wallState.getBlock() instanceof TunnelWallBlock))
                test.fail("Bad wall block data", tunnelLocation);

            graph.connectedMachine(tunnelLocation).ifPresentOrElse(m -> {
                if (!Objects.equals(m, machine))
                    test.fail("Tunnel " + tunnelLocation + " (" + dir + ") is not linked to the correct machine. Got: " + m + "; Expected: " + machine);
            }, () -> test.fail("Tunnel " + tunnelLocation + " (" + dir + ") is not linked to a machine."));

            var placed = graph.getMachineTunnels(machine, Tunnels.ITEM_TUNNEL_DEF.get())
                    .collect(Collectors.toSet());

            if (placed.size() != 1)
                test.fail("Should have had one tunnel placed; got " + placed);
        }
    }

    private static void useTunnelItem(GameTestHelper test, ServerLevel comLev, Direction dir) throws MissingDimensionException {
        // room should be generated - find a random wall position
        var playerInRoom = test.makeMockPlayer();
        playerInRoom.level = comLev;
        playerInRoom.setPos(ROOM_BOUNDS.getCenter());

        ItemStack tunnelStack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
        TunnelItem.setTunnelType(tunnelStack, Tunnels.ITEM_TUNNEL_DEF.get());

        playerInRoom.setItemInHand(InteractionHand.MAIN_HAND, tunnelStack);

        int machine = Machines.createNew(comLev.getServer(), test.getLevel(), new BlockPos(2, 2, 2).relative(dir, 2));

        LOG.info("Created new machine " + machine + "; using it for side: " + dir);

        BlockPos machineBlock = new BlockPos(2, 2, 2)
                .relative(dir, 2);

        playerInRoom.getCapability(Capabilities.ROOM_HISTORY).ifPresent(rooms -> {
            var d = new DimensionalPosition(Level.OVERWORLD, test.absolutePos(machineBlock));
            rooms.addHistory(new PlayerRoomHistoryItem(d, machine));
        });

        final var wallBounds = CompactStructureGenerator.getWallBounds(RoomSize.NORMAL, new BlockPos(ROOM_BOUNDS.getCenter()), dir);
        final var wallPositions = BlockPos.betweenClosedStream(wallBounds).map(BlockPos::immutable).toList();
        final var randomWall = wallPositions.get(RandomUtils.nextInt(0, wallPositions.size()));

        LOG.info("Using tunnel item on wall at {} (machine {}, chunk {})", randomWall, machine, new ChunkPos(randomWall));

        TestUtil.useHeldItemOnBlockAt(comLev, playerInRoom, InteractionHand.MAIN_HAND, randomWall, dir.getOpposite());
    }
}
