package dev.compactmods.machines.room.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class SpawnSubcommand {

    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var spawnRoot = Commands.literal("spawn");

        final var resetSpawn = Commands.literal("reset")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("room", StringArgumentType.string())
                        .executes(SpawnSubcommand::resetRoomSpawn));

        spawnRoot.then(resetSpawn);

        return spawnRoot;
    }

    private static int resetRoomSpawn(CommandContext<CommandSourceStack> ctx) {
        final var src = ctx.getSource();
        final var roomCode = StringArgumentType.getString(ctx, "room");

        final var roomInstance = RoomCapabilities.REGISTRY.getCapability(src.getServer())
                .get(roomCode)
                .orElseThrow();

        final var spawnManager = roomInstance.getCapability(RoomCapabilities.SPAWN_MANAGER);

        final var defaultSpawn = roomInstance.boundaries().defaultSpawn();

        spawnManager.setDefaultSpawn(defaultSpawn, Vec2.ZERO);

        src.sendSuccess(() -> Component.translatable(CommandTranslations.IDs.SPAWN_CHANGED_SUCCESSFULLY, roomCode), true);
        return 0;
    }
}
