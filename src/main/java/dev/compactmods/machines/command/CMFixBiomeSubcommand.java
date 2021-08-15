package dev.compactmods.machines.command;

import java.util.Arrays;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

public class CMFixBiomeSubcommand {
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("fixbiome")
                .executes(CMFixBiomeSubcommand::fixBiomeDefault)
                .then(
                        Commands.argument("biome", ResourceLocationArgument.id())
                                .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                                .executes(CMFixBiomeSubcommand::specificBiome)
                );
    }


    private static int specificBiome(CommandContext<CommandSource> ctx) {
        if(!ctx.getSource().getLevel().dimension().equals(Registration.COMPACT_DIMENSION)) {
            ctx.getSource().sendFailure(TranslationUtil.message(Messages.FIXBIOME_IN_BAD_DIMENSION));
            return -1;
        }

        final ResourceLocation biomeId = ResourceLocationArgument.getId(ctx, "biome");
        updateBiomeData(ctx, biomeId);
        return 0;
    }


    private static int fixBiomeDefault(CommandContext<CommandSource> ctx) {
        if(!ctx.getSource().getLevel().dimension().equals(Registration.COMPACT_DIMENSION)) {
            ctx.getSource().sendFailure(TranslationUtil.message(Messages.FIXBIOME_IN_BAD_DIMENSION));
            return -1;
        }

        final ResourceLocation biomeId = Biomes.PLAINS.location();
        updateBiomeData(ctx, biomeId);
        return 0;
    }

    private static void updateBiomeData(CommandContext<CommandSource> ctx, ResourceLocation biomeId) {
        final MinecraftServer server = ctx.getSource().getServer();
        final ServerWorld level = server.getLevel(Registration.COMPACT_DIMENSION);
        if (level == null) {
            CompactMachines.LOGGER.error("Error: Compact dimension not registered.");
            return;
        }

        final Vector3d position = ctx.getSource().getPosition();
        final BlockPos excAt = new BlockPos(position.x, position.y, position.z);
        final Chunk chunkAt = level.getChunkAt(excAt);

        final BiomeContainer biomes = chunkAt.getBiomes();
        if (biomes != null) {
            final Biome newBiome = server.registryAccess()
                    .registryOrThrow(Registry.BIOME_REGISTRY)
                    .get(RegistryKey.create(Registry.BIOME_REGISTRY, biomeId));

            Arrays.fill(biomes.biomes, newBiome);

            chunkAt.setUnsaved(true);

            SChunkDataPacket pkt = new SChunkDataPacket(chunkAt, 65535);

            PacketDistributor.TRACKING_CHUNK
                    .with(() -> level.getChunkAt(excAt))
                    .send(pkt);
        }
    }
}
