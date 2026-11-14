package dev.compactmods.machines.shrinking.history;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.CompactDimensionTransitions;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.location.GlobalPosWithRotation;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;
import dev.compactmods.machines.shrinking.api.history.RoomEntryMethod;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import static com.mojang.text2speech.Narrator.LOGGER;

public record UsedShrinkingDeviceOnMachine(GlobalPosWithRotation position, GlobalPos machine) implements RoomEntryMethod {

    public UsedShrinkingDeviceOnMachine(ServerPlayer player, RoomInstance ignored, GlobalPos machine) {
        this(GlobalPosWithRotation.fromPlayer(player), machine);
    }

    public static final MapCodec<UsedShrinkingDeviceOnMachine> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GlobalPosWithRotation.CODEC.fieldOf("position").forGetter(UsedShrinkingDeviceOnMachine::position),
            GlobalPos.CODEC.fieldOf("machine").forGetter(UsedShrinkingDeviceOnMachine::machine)
    ).apply(i, UsedShrinkingDeviceOnMachine::new));

    @Override
    public RoomExitResult exit(MinecraftServer server, ServerPlayer player, RoomInstance leaving) {
        if(!CompactDimension.isLevelCompact(player.level().dimension()))
            return RoomExitResult.FAILED_NOT_IN_COMPACT_DIM;

        final var level = server.getLevel(position.dimension());
        if (level != null) {
            LOGGER.debug("Teleporting player {} to {} as they jump up a level...", player.getUUID(), position);
            player.teleport(CompactDimensionTransitions.to(level, position.position(), position.rotation()));

            return RoomExitResult.SUCCESS_WENT_TO_LAST_ENTRYPOINT;
        } else {
            LOGGER.error("Player tracking points to an unknown dimension. Teleporting player {} to their default spawn instead.", player.getUUID());
            ShrinkingHelper.teleportPlayerToRespawnOrOverworld(server, player);

            return RoomExitResult.SUCCESS_WENT_TO_SPAWN;
        }
    }

    @Override
    public Holder<Type<?>> type() {
        return Shrinking.EntryMethods.PERSONAL_SHRINKING_DEVICE;
    }
}
