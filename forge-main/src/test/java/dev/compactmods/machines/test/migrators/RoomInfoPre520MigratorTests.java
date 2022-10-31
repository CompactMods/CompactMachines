package dev.compactmods.machines.test.migrators;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.data.migration.Pre520RoomDataMigrator;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;

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

            if(oldData.oldRoomData().size() != 2) {
                test.fail("Expected 2 entries from old data; got %s".formatted(oldData.oldRoomData().size()));
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
}
