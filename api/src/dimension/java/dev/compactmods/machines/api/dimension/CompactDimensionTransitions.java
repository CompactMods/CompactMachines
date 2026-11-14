package dev.compactmods.machines.api.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class CompactDimensionTransitions {

   public static TeleportTransition to(ServerLevel targetLevel, Vec3 pos) {
	  return new TeleportTransition(targetLevel, pos, Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING);
   }

   public static TeleportTransition to(ServerLevel targetLevel, Vec3 pos, Vec2 rotation) {
	  return new TeleportTransition(targetLevel, pos, Vec3.ZERO, rotation.y, rotation.x, TeleportTransition.DO_NOTHING);
   }
}
