package dev.compactmods.machines.test.migrators;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.data.migration.EarlyLevelDataFileReader;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.FileHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class EarlyFileLoaderTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.MIGRATION)
    public static void canGetDimensions(final GameTestHelper test) {
        final var resRoot = FileHelper.resourcesDir();
        if(resRoot == null) {
            test.fail("No resource path defined; cannot complete test.");
            return;
        }

        final var resLoc = resRoot.resolve("migrate").resolve("pre520");
        final var save = new LevelStorageSource.LevelDirectory(resLoc);
        final var loader = new EarlyLevelDataFileReader(save);

        final var dims = loader.dimensions();
        if(dims.isEmpty()) {
            test.fail("Did not read any dimension info from level files; this should be impossible.");
            return;
        }

        test.succeed();
    }
}
