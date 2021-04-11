package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.config.ServerConfig;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.legacy.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.legacy.SavedMachineData;
import com.robotgryphon.compactmachines.data.legacy.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class CompactMachineUtil {

    public static void teleportInto(ServerPlayerEntity serverPlayer, BlockPos machinePos, EnumMachineSize size) {
        ServerWorld serverWorld = serverPlayer.getLevel();

        MinecraftServer serv = serverWorld.getServer();
        if (serverWorld.dimension() == Registration.COMPACT_DIMENSION) {
            IFormattableTextComponent msg = new TranslationTextComponent(CompactMachines.MOD_ID + ".cannot_enter")
                    .withStyle(TextFormatting.RED);

            serverPlayer.displayClientMessage(msg, true);
            return;
        }

        ServerWorld compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.warn("Compact dimension not found; player attempted to enter machine.");
            return;
        }

        CompactMachineTile tile = (CompactMachineTile) serverWorld.getBlockEntity(machinePos);
        if (tile == null)
            return;

        serv.submitAsync(() -> {
            BlockPos spawnPoint;

            SavedMachineData machineData = SavedMachineData.getInstance(serv);
            CompactMachineServerData serverData = machineData.getData();

            if (tile.machineId == -1) {
                int nextID = serverData.getNextMachineId();

                BlockPos center = getCenterForNewMachine(nextID, size);

                CompactStructureGenerator.generateCompactStructure(compactWorld, size, center);

                tile.setMachineId(nextID);
                CompactMachineRegistrationData regData = new CompactMachineRegistrationData(nextID, center, serverPlayer.getUUID(), size);
                regData.setWorldPosition(serverWorld, machinePos);

                serverData.registerMachine(nextID, regData);
                machineData.setDirty();

                BlockPos.Mutable spawn = center.mutable();
                spawn.setY(ServerConfig.MACHINE_FLOOR_Y.get());

                spawnPoint = spawn.immutable();
            } else {
                Optional<CompactMachineRegistrationData> info = serverData.getMachineData(tile.machineId);

                // We have no machine info here?
                if (!info.isPresent()) {
                    IFormattableTextComponent text = new TranslationTextComponent("messages.compactmachines.no_machine_data")
                            .withStyle(TextFormatting.RED)
                            .withStyle(TextFormatting.BOLD);

                    serverPlayer.displayClientMessage(text, true);
                    return;
                }

                CompactMachineRegistrationData data = info.get();
                BlockPos.Mutable spawn = data.getCenter().mutable();
                spawn.setY(spawn.getY() - (size.getInternalSize() / 2));

                spawnPoint = data.getSpawnPoint().orElse(spawn);
            }

            try {
                // Mark the player as inside the machine, set external spawn, and yeet
                CompactMachinePlayerUtil.addPlayerToMachine(serverPlayer, machinePos, tile.machineId);
            } catch (Exception ex) {
                CompactMachines.LOGGER.error(ex);
            }

            serverPlayer.teleportTo(compactWorld, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, serverPlayer.yRot, serverPlayer.xRot);
        });
    }


    public static EnumMachineSize getMachineSizeFromNBT(@Nullable CompoundNBT tag) {
        try {
            if (tag == null)
                return EnumMachineSize.TINY;

            if (!tag.contains("size"))
                return EnumMachineSize.TINY;

            String sizeFromTag = tag.getString("size");
            return EnumMachineSize.getFromSize(sizeFromTag);
        } catch (Exception ex) {
            return EnumMachineSize.TINY;
        }
    }

    public static Block getMachineBlockBySize(EnumMachineSize size) {
        switch (size) {
            case TINY:
                return Registration.MACHINE_BLOCK_TINY.get();

            case SMALL:
                return Registration.MACHINE_BLOCK_SMALL.get();

            case NORMAL:
                return Registration.MACHINE_BLOCK_NORMAL.get();

            case LARGE:
                return Registration.MACHINE_BLOCK_LARGE.get();

            case GIANT:
                return Registration.MACHINE_BLOCK_GIANT.get();

            case MAXIMUM:
                return Registration.MACHINE_BLOCK_MAXIMUM.get();
        }

        return Registration.MACHINE_BLOCK_NORMAL.get();
    }

    public static Item getMachineBlockItemBySize(EnumMachineSize size) {
        switch (size) {
            case TINY:
                return Registration.MACHINE_BLOCK_ITEM_TINY.get();

            case SMALL:
                return Registration.MACHINE_BLOCK_ITEM_SMALL.get();

            case NORMAL:
                return Registration.MACHINE_BLOCK_ITEM_NORMAL.get();

            case LARGE:
                return Registration.MACHINE_BLOCK_ITEM_LARGE.get();

            case GIANT:
                return Registration.MACHINE_BLOCK_ITEM_GIANT.get();

            case MAXIMUM:
                return Registration.MACHINE_BLOCK_ITEM_MAXIMUM.get();
        }

        return Registration.MACHINE_BLOCK_ITEM_NORMAL.get();
    }

    public static BlockPos getCenterForNewMachine(int id, EnumMachineSize size) {
        Vector3i location = MathUtil.getRegionPositionByIndex(id);
        int centerY = ServerConfig.MACHINE_FLOOR_Y.get() + (size.getInternalSize() / 2);
        return new BlockPos((location.getX() * 1024) + 8, centerY, (location.getZ() * 1024) + 8);
    }

    public static void setMachineSpawn(MinecraftServer server, BlockPos position) {
        SavedMachineData machineData = SavedMachineData.getInstance(server);
        CompactMachineServerData serverData = machineData.getData();

        Optional<CompactMachineRegistrationData> compactMachineData = serverData.getMachineContainingPosition(position);
        compactMachineData.ifPresent(d -> {
            d.setSpawnPoint(position);
            serverData.updateMachineData(d);
            machineData.setDirty();
        });
    }

    public static Optional<SavedMachineData> getMachineData(ServerWorld world) {
        if (world == null)
            return Optional.empty();

        SavedMachineData md = SavedMachineData.getInstance(world.getServer());
        return Optional.of(md);
    }

    public static Optional<CompactMachineRegistrationData> getMachineInfoByInternalPosition(ServerWorld world, Vector3d pos) {
        SavedMachineData machineData = SavedMachineData.getInstance(world.getServer());
        CompactMachineServerData serverData = machineData.getData();

        return serverData.getMachineContainingPosition(pos);
    }

    public static Optional<CompactMachineRegistrationData> getMachineInfoByInternalPosition(ServerWorld world, BlockPos pos) {
        SavedMachineData machineData = SavedMachineData.getInstance(world.getServer());
        CompactMachineServerData serverData = machineData.getData();

        return serverData.getMachineContainingPosition(pos);
    }

    /**
     * Server only; updates the machine data to reflect where the "outside" of the machine is,
     * in-world.
     *
     * @param world
     * @param machineID
     * @param pos
     */
    public static void updateMachineInWorldPosition(ServerWorld world, int machineID, BlockPos pos) {
        SavedMachineData machineData = SavedMachineData.getInstance(world.getServer());
        CompactMachineServerData serverData = machineData.getData();

        Optional<CompactMachineRegistrationData> machineById = serverData.getMachineData(machineID);
        machineById.ifPresent(data -> {
            data.setWorldPosition(world, pos);
            data.removeFromPlayerInventory();

            // Write changes to disk
            serverData.updateMachineData(data);
            machineData.setDirty();
        });
    }
}
