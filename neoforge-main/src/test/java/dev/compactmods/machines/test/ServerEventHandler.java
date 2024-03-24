package dev.compactmods.machines.test;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.test.util.DimensionForcer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEventHandler {

    final static Logger LOG = LogManager.getLogger();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStarting(final ServerStartingEvent evt) {
        final var serverStarting = evt.getServer();
        DimensionForcer.forceLoadCMDim(serverStarting);
    }

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent evt) {
        final MinecraftServer server = evt.getServer();

        // Add "test/resources" as a resource pack to the pack repository
        final var packs = server.getPackRepository();

        final String test_resources = System.getenv("CM_TEST_RESOURCES");
        if(test_resources != null) {

            final var testPack = new FolderRepositorySource(Path.of(test_resources), PackType.SERVER_DATA, PackSource.DEFAULT, new DirectoryValidator(l -> true));
            packs.addPackFinder(testPack);
            packs.reload();

            // add "file/resources" to selected pack list
            final ImmutableSet<String> toSelect = ImmutableSet.<String>builder()
                    .addAll(packs.getSelectedIds())
                    .add("file/test_pack")
                    .build();

            packs.setSelected(toSelect);

            try {
                server.reloadResources(packs.getSelectedIds()).get();

                server.getResourceManager()
                        .listResources("structures", rl -> rl.getNamespace().equals(Constants.MOD_ID))
                        .forEach((rl, res) -> LOG.debug(rl.toDebugFileName()));

            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Failed to reload test resource packs.", e);
            }
        }
    }
}
