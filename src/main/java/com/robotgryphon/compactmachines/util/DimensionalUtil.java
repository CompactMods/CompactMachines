package com.robotgryphon.compactmachines.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import java.util.EnumSet;
import java.util.Set;

public class DimensionalUtil {
    private static void teleport(Entity entityIn, ServerWorld worldIn, double x, double y, double z, float yaw, float pitch) { //modified version of TeleportCommand.java: 123: TeleportCommand#teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y, double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch, @Nullable TeleportCommand.Facing facing) throws CommandSyntaxException
        if (entityIn.removed) return;
        Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
        set.add(SPlayerPositionLookPacket.Flags.X_ROT);
        set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
        if (entityIn instanceof ServerPlayerEntity) {
            ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
            worldIn.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getEntityId());
            entityIn.stopRiding();
            if (((ServerPlayerEntity)entityIn).isSleeping())
                ((ServerPlayerEntity)entityIn).stopSleepInBed(true, true);
            if (worldIn == entityIn.world)
                ((ServerPlayerEntity)entityIn).connection.setPlayerLocation(x, y, z, yaw, pitch, set);
            else
                ((ServerPlayerEntity)entityIn).teleport(worldIn, x, y, z, yaw, pitch);
            entityIn.setRotationYawHead(yaw);
        } else {
            float f1 = MathHelper.wrapDegrees(yaw);
            float f = MathHelper.wrapDegrees(pitch);
            f = MathHelper.clamp(f, -90.0F, 90.0F);
            if (worldIn == entityIn.world) {
                entityIn.setLocationAndAngles(x, y, z, f1, f);
                entityIn.setRotationYawHead(f1);
            } else {
                entityIn.detach();
                Entity entity = entityIn;
                entityIn = entityIn.getType().create(worldIn);
                if (entityIn == null)
                    return;
                entityIn.copyDataFromOld(entity);
                entityIn.setLocationAndAngles(x, y, z, f1, f);
                entityIn.setRotationYawHead(f1);
                worldIn.addFromAnotherDimension(entityIn);
                entity.removed = true;
            }
        }
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, BlockPos destPos) {
        if (entity == null || entity.world.isRemote()) return;
        ServerWorld world;
        if (destType != null)
            world = entity.getServer().getWorld(destType);
        else
            world = (ServerWorld)entity.getEntityWorld();
        teleport(entity, world, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5, entity.rotationYaw, entity.rotationPitch);
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, Vector3d vec) {
        teleportEntity(entity, destType, vec.x, vec.y, vec.z);
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, double x, double y, double z) {
        if (entity == null || entity.world.isRemote()) return;
        ServerWorld world;
        if (destType != null)
            world = entity.getServer().getWorld(destType);
        else
            world = (ServerWorld)entity.getEntityWorld();
        teleport(entity, world, x, y, z, entity.rotationYaw, entity.rotationPitch);
    }
}
