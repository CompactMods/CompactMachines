package dev.compactmods.machines.shrinking.history;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.dimension.CompactDimensionTransitions;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.location.GlobalPosWithRotation;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.api.history.RoomEntryMethod;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record UsedTeleportCommand(GlobalPosWithRotation previousLocation) implements RoomEntryMethod {

    public static final MapCodec<UsedTeleportCommand> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GlobalPosWithRotation.CODEC.fieldOf("previousLocation").forGetter(UsedTeleportCommand::previousLocation)
    ).apply(i, UsedTeleportCommand::new));

    @Override
    public RoomExitResult exit(MinecraftServer server, ServerPlayer player, RoomInstance leaving) {
        final var dim = server.getLevel(previousLocation.dimension());
        final var transition = CompactDimensionTransitions.to(dim, previousLocation.position());
        player.teleport(transition);
        return RoomExitResult.SUCCESS_WENT_TO_LAST_ENTRYPOINT;
    }

    @Override
    public Holder<Type<?>> type() {
        return Shrinking.EntryMethods.TELEPORT_COMMAND;
    }
}
