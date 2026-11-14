package dev.compactmods.machines.api.room.function;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PlayerAndRoomCodeFunction<T> {

    T apply(Player player, String roomCode);
}
