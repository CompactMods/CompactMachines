package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public abstract class CompactMachineUtil {

    public static void teleportInto(ServerPlayerEntity serverPlayer) {
        World serverWorld = serverPlayer.getServerWorld();

        RegistryKey<World> registrykey = serverWorld.getDimensionKey() == World.OVERWORLD ? Registrations.COMPACT_DIMENSION : World.OVERWORLD;

        MinecraftServer serv = serverWorld.getServer();
        if (serv != null) {
            ServerWorld compactWorld = serv.getWorld(registrykey);
            serv.deferTask(() -> {
                serverPlayer.teleport(compactWorld, 8.5, 6, 8.5, serverPlayer.rotationYaw, serverPlayer.rotationPitch);
            });
        }
    }

    public static EnumMachineSize getMachineSizeFromNBT(@Nullable CompoundNBT tag) {
        try {
            if (tag == null)
                return EnumMachineSize.TINY;

            if (!tag.contains("size"))
                return EnumMachineSize.TINY;

            String sizeFromTag = tag.getString("size");
            return EnumMachineSize.getFromSize(sizeFromTag);
        }

        catch(Exception ex) {
            return EnumMachineSize.TINY;
        }
    }

    public static Block getMachineBlockBySize(EnumMachineSize size) {
        switch(size) {
            case TINY:
                return Registrations.MACHINE_BLOCK_TINY.get();

            case SMALL:
                return Registrations.MACHINE_BLOCK_SMALL.get();

            case NORMAL:
                return Registrations.MACHINE_BLOCK_NORMAL.get();

            case LARGE:
                return Registrations.MACHINE_BLOCK_LARGE.get();

            case GIANT:
                return Registrations.MACHINE_BLOCK_GIANT.get();

            case MAXIMUM:
                return Registrations.MACHINE_BLOCK_MAXIMUM.get();
        }

        return Registrations.MACHINE_BLOCK_NORMAL.get();
    }

    public static Item getMachineBlockItemBySize(EnumMachineSize size) {
        switch(size) {
            case TINY:
                return Registrations.MACHINE_BLOCK_ITEM_TINY.get();

            case SMALL:
                return Registrations.MACHINE_BLOCK_ITEM_SMALL.get();

            case NORMAL:
                return Registrations.MACHINE_BLOCK_ITEM_NORMAL.get();

            case LARGE:
                return Registrations.MACHINE_BLOCK_ITEM_LARGE.get();

            case GIANT:
                return Registrations.MACHINE_BLOCK_ITEM_GIANT.get();

            case MAXIMUM:
                return Registrations.MACHINE_BLOCK_ITEM_MAXIMUM.get();
        }

        return Registrations.MACHINE_BLOCK_ITEM_NORMAL.get();
    }
}
