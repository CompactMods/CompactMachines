package dev.compactmods.machines.test.tunnel;

import com.mojang.authlib.GameProfile;
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
import dev.compactmods.machines.util.CompactStructureGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import org.apache.commons.lang3.RandomUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@GameTestHolder(CompactMachines.MOD_ID)
public class TunnelTests {

    private static AABB ROOM_BOUNDS;
    private static UUID TEST_PLAYER;
    private static HashSet<Integer> TESTING_MACHINES;
    private static HashSet<BlockPos> PLACED_TUNNELS;
    private static ChunkPos TESTING_ROOM;

    @BeforeBatch(batch = TestBatches.TUNNELS)
    public static void beforeTunnelTests(final ServerLevel level) {
        final var server = level.getServer();

        try {
            TESTING_MACHINES = new HashSet<>();
            PLACED_TUNNELS = new HashSet<>();
            TEST_PLAYER = UUID.randomUUID();
            TESTING_ROOM = Rooms.createNew(server, RoomSize.NORMAL, TEST_PLAYER);

            ROOM_BOUNDS = CompactRoomData.get(server)
                    .getBounds(TESTING_ROOM);
        } catch (MissingDimensionException | NonexistentRoomException e) {
            e.printStackTrace();
        }
    }

    @AfterBatch(batch = TestBatches.TUNNELS)
    public static void afterTunnelTests(final ServerLevel level) {
        final var server = level.getServer();

        try {
            for (var mach : TESTING_MACHINES) Machines.destroy(server, mach);
            Rooms.destroy(server, TESTING_ROOM);
        } catch (MissingDimensionException | NonexistentRoomException e) {
            e.printStackTrace();
        }
    }

    @GameTestGenerator
    public static Collection<TestFunction> usingTunnelItemOnWall() {
        HashSet<TestFunction> tests = new HashSet<>();

        // TestFunction(String batch, String testName, String structure, int testTime, long setupTime, boolean isRequired, Consumer<GameTestHelper> tester)
        for (var dir : Direction.values()) {
            var template = new ResourceLocation(CompactMachines.MOD_ID, "empty_1x1");
            Consumer<GameTestHelper> test = (t) -> sidedTunnelTest(dir, t);

            var testf = new TestFunction(TestBatches.TUNNELS, "tunnel_creation_" + dir.getSerializedName(), template.toString(), 100, 0, true, test);
            tests.add(testf);
        }

        return tests;
    }

    private static void sidedTunnelTest(Direction dir, GameTestHelper t) {
        var lev = t.getLevel();
        var serv = t.getLevel().getServer();
        var comLev = serv.getLevel(Registration.COMPACT_DIMENSION);

        final var profile = new GameProfile(TEST_PLAYER, "CMRoomPlayer");

        BlockPos machineBlock = BlockPos.ZERO.above();

        AtomicReference<BlockPos> randomWallRelative = new AtomicReference<>();

        AtomicInteger currentRoom = new AtomicInteger();

        t.startSequence()
                .thenExecute(() -> {
                    t.setBlock(machineBlock, Registration.MACHINE_BLOCK_NORMAL.get());

                    try {
                        int newMachine = Machines.createNew(serv, lev, t.absolutePos(machineBlock));
                        Machines.link(serv, newMachine, TESTING_ROOM);
                        TESTING_MACHINES.add(newMachine);

                        currentRoom.set(newMachine);
                    } catch (MissingDimensionException e) {
                        e.printStackTrace();
                        t.fail(e.getMessage());
                    }
                })

                .thenExecuteAfter(5, () -> {
                    // 5 ticks later, room should be generated - find a random wall position
                    var playerInRoom = FakePlayerFactory.get(comLev, profile);
                    playerInRoom.setLevel(comLev);
                    playerInRoom.setPos(ROOM_BOUNDS.getCenter());

                    playerInRoom.getCapability(Capabilities.ROOM_HISTORY).ifPresent(rooms -> {
                        var d = new DimensionalPosition(Level.OVERWORLD, t.absolutePos(machineBlock));
                        rooms.addHistory(new PlayerRoomHistoryItem(d, currentRoom.get()));
                    });

                    ItemStack tunnelStack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
                    TunnelItem.setTunnelType(tunnelStack, Tunnels.ITEM_TUNNEL_DEF.get());

                    playerInRoom.setItemInHand(InteractionHand.MAIN_HAND, tunnelStack);

                    final var wallBounds = CompactStructureGenerator.getWallBounds(RoomSize.NORMAL, new BlockPos(ROOM_BOUNDS.getCenter()), dir);
                    final var wallPositions = BlockPos.betweenClosedStream(wallBounds).map(BlockPos::immutable).toList();
                    final var randomWall = wallPositions.get(RandomUtils.nextInt(0, wallPositions.size()));

                    randomWallRelative.set(randomWall);
                    PLACED_TUNNELS.add(randomWall);
                    TestUtil.useHeldItemOnBlockAt(comLev, playerInRoom, InteractionHand.MAIN_HAND, randomWall, dir);
                })

                .thenExecuteAfter(5, () -> {
                    int machine = currentRoom.get();
                    var wallLoc = randomWallRelative.get();

                    final var wallState = comLev.getBlockState(wallLoc);
                    if (!(wallState.getBlock() instanceof TunnelWallBlock))
                        t.fail("Bad wall block data", wallLoc);

                    if (wallState.getValue(TunnelWallBlock.TUNNEL_SIDE) != dir)
                        t.fail("Did not match clicked side", wallLoc);

                    try {
                        final var roomTunnels = RoomTunnelData.get(serv, TESTING_ROOM);
                        final var graph = roomTunnels.getGraph();

                        var placed = graph.getMachineTunnels(machine, Tunnels.ITEM_TUNNEL_DEF.get())
                                .collect(Collectors.toSet());

                        if (placed.size() != 1)
                            t.fail("Should have had only one tunnel placed; got " + placed);

                        if (!PLACED_TUNNELS.containsAll(placed))
                            t.fail("Tunnel placement locations did not match what was expected.");
                    } catch (MissingDimensionException e) {
                        t.fail(e.getMessage());
                    }
                }).thenSucceed();
    }
}
