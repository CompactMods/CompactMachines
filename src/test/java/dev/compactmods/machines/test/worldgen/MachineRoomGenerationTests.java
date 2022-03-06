package dev.compactmods.machines.test.worldgen;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.TestUtil;
import dev.compactmods.machines.util.CompactStructureGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactMachines.MOD_ID)
public class MachineRoomGenerationTests {

    @GameTestGenerator
    public static Collection<TestFunction> generatesMachinesCorrectly() {
        HashSet<TestFunction> tests = new HashSet<>();

        // TestFunction(String batch, String testName, String structure, int testTime, long setupTime, boolean isRequired, Consumer<GameTestHelper> tester)
        for (var size : RoomSize.values()) {
            var template = new ResourceLocation(CompactMachines.MOD_ID, size.getSerializedName());
            Consumer<GameTestHelper> test = (t) -> {
                var lev = t.getLevel();
                TestUtil.loadStructureIntoTestArea(t, template, new BlockPos(0, 17, 0));

                var centerSpawn = t.absolutePos(new BlockPos(7, 8, 7));
                CompactStructureGenerator.generateCompactStructure(lev, size, centerSpawn);

                BlockPos.betweenClosedStream(BlockPos.ZERO.above(), new BlockPos(15, 16, 15))
                        .forEach(pos -> {
                            var compare = pos.above(16);
                            t.assertSameBlockState(pos, compare);
                        });

                t.succeed();
            };

            var testf = new TestFunction(TestBatches.ROOM_GENERATION, "room_" + size.getSerializedName(),
                    "compactmachines:empty_15x31", 50, 0, true, test);
            tests.add(testf);
        }

        return tests;
    }

    @GameTest(template = "empty_5x5", batch = "dimension")
    public static void usingPsdCreatesRoom(final GameTestHelper test) {
        var player = test.makeMockPlayer();
        var serv = test.getLevel().getServer();

        var machLoc = new BlockPos(3, 0, 3);

        test.setBlock(machLoc, Registration.MACHINE_BLOCK_NORMAL.get());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Registration.PERSONAL_SHRINKING_DEVICE.get()));

        test.startSequence()
                .thenExecute(() -> TestUtil.useItemOnBlockAt(test, player, machLoc))
                .thenExecuteAfter(2, () -> {
                    if (test.getBlockEntity(machLoc) instanceof CompactMachineBlockEntity mach) {

                        if (!mach.mapped())
                            test.fail("Machine was not mapped to a room.");

                        if (mach.machineId == -1)
                            test.fail("Machine ID not set after PSD usage.");

                        var roomid = mach.getInternalChunkPos();
                        if (roomid.isEmpty()) {
                            test.fail("Room was not registered.");
                            return;
                        }

                        ChunkPos room = roomid.get();
                        try {
                            boolean destroyed = Rooms.destroy(serv, room);
                            if (!destroyed)
                                test.fail("Room was not destroyed after test.");

                            final var rooms = CompactRoomData.get(serv);
                            if (rooms.isRegistered(room))
                                test.fail("Room was not unregistered after test.");

                        } catch (MissingDimensionException | NonexistentRoomException e) {
                            test.fail(e.toString());
                        }

                        test.succeed();
                    } else {
                        test.fail("Expected machine block to have a block entity.");
                    }
                });
    }


}
