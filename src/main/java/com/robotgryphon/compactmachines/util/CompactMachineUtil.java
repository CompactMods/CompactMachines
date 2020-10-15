package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.data.CompactMachineData;
import com.robotgryphon.compactmachines.data.MachineData;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

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
                    .expand(0, (s * 2) + 1, 0);
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

        MinecraftServer serv = serverWorld.getServer();
        if (serv != null) {
            if (serverWorld.getDimensionKey() == Registrations.COMPACT_DIMENSION) {
                IFormattableTextComponent msg = new TranslationTextComponent(CompactMachines.MODID + ".cannot_enter")
                        .mergeStyle(TextFormatting.RED);

                serverPlayer.sendStatusMessage(msg, true);
                return;
            }

            ServerWorld compactWorld = serv.getWorld(Registrations.COMPACT_DIMENSION);
            if (compactWorld == null)
                return;

            CompactMachineTile tile = (CompactMachineTile) serverWorld.getTileEntity(machinePos);
            if (tile == null)
                return;

            PlayerUtil.setLastPosition(serverPlayer);

            serv.deferTask(() -> {
                BlockPos spawnPoint;
                if (tile.machineId == -1) {
                    int nextID = MachineData.getNextMachineId(compactWorld);

                    BlockPos center = getCenterOfMachineById(nextID);

                    // Bump the center up a bit so the floor is Y = 60
                    center = center.offset(Direction.UP, size.getInternalSize() / 2);

                    CompactMachineUtil.generateCompactStructure(compactWorld, size, center);

                    tile.setMachineId(nextID);
                    MachineData.getMachineData(compactWorld)
                            .addToMachineData(nextID, new CompactMachineData(nextID, center, serverPlayer.getUniqueID(), size));

                    BlockPos.Mutable spawn = center.toMutable();
                    spawn.setY(62);

                    spawnPoint = spawn.toImmutable();
                } else {
                    MachineData md = MachineData.getMachineData(compactWorld);
                    Optional<CompactMachineData> info = md.getMachineById(tile.machineId);

                    // We have no machine info here?
                    if (!info.isPresent()) {
                        IFormattableTextComponent text = new TranslationTextComponent("messages.compactmachines.no_machine_data")
                                .mergeStyle(TextFormatting.RED)
                                .mergeStyle(TextFormatting.BOLD);

                        serverPlayer.sendStatusMessage(text, true);
                        return;
                    }

                    CompactMachineData data = info.get();
                    BlockPos.Mutable center = data.getCenter().toMutable();
                    center.setY(62);

                    spawnPoint = data.getSpawnPoint().orElse(center);
                }

                serverPlayer.teleport(compactWorld, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, serverPlayer.rotationYaw, serverPlayer.rotationPitch);
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

    public static BlockPos getCenterOfMachineById(int id) {
        Vector3i location = MathUtil.getRegionPositionByIndex(id);
        return new BlockPos((location.getX() * 1024) + 8, 60, (location.getZ() * 1024) + 8);
    }

    public static void setMachineSpawn(ServerWorld world, BlockPos position) {
        MachineData machineData = MachineData.getMachineData(world);

        Optional<CompactMachineData> compactMachineData = machineData.getMachineContainingPosition(position);
        compactMachineData.ifPresent(d -> {
            d.setSpawnPoint(position);
            machineData.updateMachineData(d);
        });
    }
}
