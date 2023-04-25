package dev.compactmods.machines.test.migration;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.forge.data.migration.Pre520RoomDataMigrator;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.FileHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.util.UUID;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class RoomInfoPre520MigratorTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.MIGRATION)
    public static void canReadPre520RoomFiles(final GameTestHelper test) {

        try {
            final var oldRoomData = FileHelper.getNbtFromSavedDataFile("migrate/pre520/room_data.dat");
            final var oldData = Pre520RoomDataMigrator.loadOldRoomData(oldRoomData);

            if(oldData.oldRoomData().isEmpty()) {
                test.fail("No data loaded.");
                return;
            }

            if(oldData.oldRoomData().size() != 3) {
                test.fail("Expected 3 entries from old data; got %s".formatted(oldData.oldRoomData().size()));
                return;
            }

            if(!oldData.roomChunkLookup().containsKey(new ChunkPos(0, -64))) {
                test.fail("Expected chunk [0,-64] in chunk lookup, but it was not found.");
                return;
            }

            test.succeed();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MIGRATION)
    public static void canReadSingleRoom(final GameTestHelper test) {

        try {
            final var nbtData = FileHelper.getNbtFromSavedDataFile("migrate/pre520/single_room.dat");
            final var oldData = Pre520RoomDataMigrator.RoomDataPre520.of(nbtData);

            if(!oldData.center().equals(new BlockPos(8, 45, -1016)))
                test.fail("Room center did not match expected value.");

            if(!oldData.size().equals(RoomSize.GIANT))
                test.fail("Expected room size to be 'giant'.");

            if(!oldData.spawn().equals(new Vec3(8, 40, -1016)))
                test.fail("Room spawn did not match expected value.");

            if(!oldData.owner().equals(UUID.fromString("6878c888-1219-4639-9c1a-de524a628dcb")))
                test.fail("Owner did not match expected value.");
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }
}
