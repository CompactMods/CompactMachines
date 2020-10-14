package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Arrays;

public abstract class CompactMachineUtil {

    public static void generatePlatform(IWorldWriter world, EnumMachineSize size, BlockPos center) {
        int s = size.getInternalSize() / 2;
        AxisAlignedBB toFill = new AxisAlignedBB(center, center)
                .grow(s, 0, s);

        BlockPos.getAllInBox(toFill)
                .forEach(pos -> {
                    world.setBlockState(pos, Registrations.WALL_BLOCK.get().getDefaultState(), 7);
                });
    }

    public static void generateCompactWall(IWorld world, EnumMachineSize size, BlockPos cubeCenter, Direction wallDirection) {
        int s = size.getInternalSize() / 2;

        BlockState unbreakableWall = Registrations.WALL_BLOCK.get().getDefaultState();

        BlockPos start = BlockPos.ZERO;
        AxisAlignedBB wallBounds;

        boolean horiz = wallDirection.getAxis().getPlane() == Direction.Plane.HORIZONTAL;
        if (horiz) {
            start = cubeCenter
                    .down(s)
                    .offset(wallDirection, s + 1);

            wallBounds = new AxisAlignedBB(start, start)
                    .expand(0, (s + 1) * 2, 0);
        } else {
            start = cubeCenter.offset(wallDirection, s + 1);

            wallBounds = new AxisAlignedBB(start, start)
                    .grow(s + 1, 0, s + 1);
        }

        switch (wallDirection) {
            case NORTH:
            case SOUTH:
                wallBounds = wallBounds.grow(s + 1, 0, 0);
                break;

            case WEST:
            case EAST:
                wallBounds = wallBounds.grow(0, 0, s + 1);
                break;

            case UP:
            case DOWN:
                // NO-OP: Math was done above
                break;
        }

        BlockPos.getAllInBox(wallBounds)
                .filter(world::isAirBlock)
                .map(BlockPos::toImmutable)
                .forEach(p -> world.setBlockState(p, unbreakableWall, 7));
    }

    public static void generateCompactStructure(IWorld world, EnumMachineSize size, BlockPos center) {
        // TODO
        int s = size.getInternalSize() / 2;

        BlockPos floorCenter = center.offset(Direction.DOWN, s);
        AxisAlignedBB floorBlocks = new AxisAlignedBB(floorCenter, floorCenter)
                .grow(s, 0, s);

        boolean anyAir = world.getStatesInArea(floorBlocks)
                .anyMatch(state -> state.getBlock() == Blocks.AIR);

        // if (anyAir) {
        BlockState unbreakableWall = Registrations.WALL_BLOCK.get().getDefaultState();

        // Generate the walls
        Arrays.stream(Direction.values())
                .forEach(d -> generateCompactWall(world, size, center, d));
    }

    public static void teleportInto(ServerPlayerEntity serverPlayer, BlockPos machinePos, EnumMachineSize size) {
        World serverWorld = serverPlayer.getServerWorld();

        RegistryKey<World> registrykey = serverWorld.getDimensionKey() == World.OVERWORLD ? Registrations.COMPACT_DIMENSION : World.OVERWORLD;

        MinecraftServer serv = serverWorld.getServer();
        if (serv != null) {
            ServerWorld compactWorld = serv.getWorld(registrykey);
            if (compactWorld == null)
                return;

            PlayerUtil.setLastPosition(serverPlayer);

            serv.deferTask(() -> {
                BlockPos center = new BlockPos(8, 4 + (size.getInternalSize() / 2), 8);

                CompactMachineUtil.generateCompactStructure(compactWorld, size, center);

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
        } catch (Exception ex) {
            return EnumMachineSize.TINY;
        }
    }

    public static Block getMachineBlockBySize(EnumMachineSize size) {
        switch (size) {
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
        switch (size) {
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
