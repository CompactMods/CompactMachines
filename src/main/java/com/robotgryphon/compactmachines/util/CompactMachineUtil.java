package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.SavedMachineData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
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
        ServerWorld serverWorld = serverPlayer.getServerWorld();

        MinecraftServer serv = serverWorld.getServer();
        if (serverWorld.getDimensionKey() == Registration.COMPACT_DIMENSION) {
            IFormattableTextComponent msg = new TranslationTextComponent(CompactMachines.MOD_ID + ".cannot_enter")
                    .mergeStyle(TextFormatting.RED);

            serverPlayer.sendStatusMessage(msg, true);
            return;
        }

        ServerWorld compactWorld = serv.getWorld(Registration.COMPACT_DIMENSION);
        if (compactWorld == null)
            return;

        CompactMachineTile tile = (CompactMachineTile) serverWorld.getTileEntity(machinePos);
        if (tile == null)
            return;

        serv.deferTask(() -> {
            BlockPos spawnPoint;

            CompactMachineServerData serverData = CompactMachineServerData.getInstance(serv);

            if (tile.machineId == -1) {
                int nextID = serverData.getNextMachineId();

                BlockPos center = getCenterOfMachineById(nextID);

                // Bump the center up a bit so the floor is Y = 60
                center = center.offset(Direction.UP, size.getInternalSize() / 2);

                CompactStructureGenerator.generateCompactStructure(compactWorld, size, center);

                tile.setMachineId(nextID);
                CompactMachineRegistrationData regData = new CompactMachineRegistrationData(nextID, center, serverPlayer.getUniqueID(), size);
                regData.setWorldPosition(serverWorld, machinePos);

                serverData.registerMachine(nextID, regData);
                serverData.markDirty();

                BlockPos.Mutable spawn = center.toMutable();
                spawn.setY(62);

                spawnPoint = spawn.toImmutable();
            } else {
                Optional<CompactMachineRegistrationData> info = serverData.getMachineData(tile.machineId);

                // We have no machine info here?
                if (!info.isPresent()) {
                    IFormattableTextComponent text = new TranslationTextComponent("messages.compactmachines.no_machine_data")
                            .mergeStyle(TextFormatting.RED)
                            .mergeStyle(TextFormatting.BOLD);

                    serverPlayer.sendStatusMessage(text, true);
                    return;
                }

                CompactMachineRegistrationData data = info.get();
                BlockPos.Mutable center = data.getCenter().toMutable();
                center.setY(62);

                spawnPoint = data.getSpawnPoint().orElse(center);
            }

            try {
                // Mark the player as inside the machine, set external spawn, and yeet
                CompactMachinePlayerUtil.addPlayerToMachine(serverPlayer, machinePos, tile.machineId);
            } catch (Exception ex) {
                CompactMachines.LOGGER.error(ex);
            }

            serverPlayer.teleport(compactWorld, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, serverPlayer.rotationYaw, serverPlayer.rotationPitch);
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

    public static BlockPos getCenterOfMachineById(int id) {
        Vector3i location = MathUtil.getRegionPositionByIndex(id);
        return new BlockPos((location.getX() * 1024) + 8, 60, (location.getZ() * 1024) + 8);
    }

    public static void setMachineSpawn(MinecraftServer server, BlockPos position) {
        CompactMachineServerData serverData = CompactMachineServerData.getInstance(server);
        Optional<CompactMachineRegistrationData> compactMachineData = serverData.getMachineContainingPosition(position);
        compactMachineData.ifPresent(d -> {
            d.setSpawnPoint(position);
            serverData.updateMachineData(d);
        });
    }

    public static Optional<SavedMachineData> getMachineData(ServerWorld world) {
        if (world == null)
            return Optional.empty();

        SavedMachineData md = SavedMachineData.getMachineData(world.getServer());
        return Optional.of(md);
    }

    public static Optional<CompactMachineRegistrationData> getMachineInfoByInternalPosition(ServerWorld world, Vector3d pos) {
        CompactMachineServerData data = CompactMachineServerData.getInstance(world.getServer());
        return data.getMachineContainingPosition(pos);
    }

    public static Optional<CompactMachineRegistrationData> getMachineInfoByInternalPosition(ServerWorld world, BlockPos pos) {
        CompactMachineServerData data = CompactMachineServerData.getInstance(world.getServer());
        return data.getMachineContainingPosition(pos);
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
        CompactMachineServerData serverData = CompactMachineServerData.getInstance(world.getServer());

        Optional<CompactMachineRegistrationData> machineById = serverData.getMachineData(machineID);
        machineById.ifPresent(data -> {
            data.setWorldPosition(world, pos);
            data.removeFromPlayerInventory();

            // Write changes to disk
            serverData.updateMachineData(data);
        });
    }
}
