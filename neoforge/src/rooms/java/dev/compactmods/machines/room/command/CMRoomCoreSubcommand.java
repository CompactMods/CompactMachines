package dev.compactmods.machines.room.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.RoomTranslations;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.room.command.argument.Suggestors;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.logging.log4j.Logger;

public class CMRoomCoreSubcommand {

    private static final Logger LOGGER = CompactMachinesCore.modLog();

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("room_core")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));


        // room_core new [template]
        subRoot.then(Commands.literal("new")
                .then(Commands.argument("template", IdentifierArgument.id())
                        .suggests(Suggestors.ROOM_TEMPLATES)
                        .executes(CMRoomCoreSubcommand::giveNewMachineExecutor)));

        // room_core bind_to [room-roomCode]
        subRoot.then(Commands.literal("bind_to")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMRoomCoreSubcommand::bindExistingRoomExecutor)));

        // room_core give [player]
        var giveSpecificPlayer = Commands.argument("player", EntityArgument.player());

        // /cm give [player] new [template]
        giveSpecificPlayer.then(Commands.literal("new")
                .then(Commands.argument("template", IdentifierArgument.id())
                        .suggests(Suggestors.ROOM_TEMPLATES)
                        .executes(CMRoomCoreSubcommand::giveNewMachineSpecificPlayer)));

        // /cm give [player] bind_to [room-roomCode]
        giveSpecificPlayer.then(Commands.literal("bind_to")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMRoomCoreSubcommand::bindExistingRoomSpecificPlayer)));

        subRoot.then(giveSpecificPlayer);


        return subRoot;
    }


    private static int giveNewMachineExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var templateId = IdentifierArgument.getId(ctx, "template");

        bindTemplate(src, templateId, player);

        return 0;
    }

    private static int giveNewMachineSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var templateId = IdentifierArgument.getId(ctx, "template");

        bindTemplate(src, templateId, player);

        return 0;
    }

    private static int bindExistingRoomExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var roomCode = StringArgumentType.getString(ctx, "room");

        bindExistingRoomCode(roomCode, player, src);

        return 0;
    }

    private static int bindExistingRoomSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var roomCode = StringArgumentType.getString(ctx, "room");

        bindExistingRoomCode(roomCode, player, src);

        return 0;
    }

    private static void bindTemplate(CommandSourceStack src, Identifier templateId, ServerPlayer player) {

        final var template = RoomTemplateHelper.getTemplateHolder(src.getServer().registryAccess(), templateId);
        if (template.isBound()) {

            final var access = ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND);

            var held = access.getResource();
            var originalHeld = access.getResource();
            if (held.isEmpty())
                held = ItemResource.of(Items.PAPER);

            held = held.with(Rooms.DataComponents.ROOM_TEMPLATE_ID, templateId);
            held = held.without(Rooms.DataComponents.BOUND_ROOM_CODE);

            swapHeldItem(player, src, access, held, originalHeld);
        } else {
            src.sendFailure(CommandTranslations.CANNOT_GIVE_MACHINE.get());
        }
    }

    private static void bindExistingRoomCode(String roomCode, ServerPlayer player, CommandSourceStack src) {
        final var server = src.getServer();
        final var registry = server.getCapability(RoomCapabilities.REGISTRY);
        if(registry == null)
            return;

        registry.get(roomCode).ifPresentOrElse(room -> {
            final var access = ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND);

            var held = access.getResource();
            var originalHeld = access.getResource();
            if (held.isEmpty())
                held = ItemResource.of(Items.PAPER);

            held = held.with(Rooms.DataComponents.BOUND_ROOM_CODE, roomCode);
            held = held.without(Rooms.DataComponents.ROOM_TEMPLATE_ID);

            swapHeldItem(player, src, access, held, originalHeld);
        }, () -> {
            LOGGER.error("Error giving player a new machine block: room not found.");
            src.sendFailure(RoomTranslations.UNKNOWN_ROOM_BY_CODE.apply(roomCode));
        });
    }

    private static void swapHeldItem(ServerPlayer player, CommandSourceStack src, ItemAccess access, ItemResource held, ItemResource originalHeld) {
        try (var tx = Transaction.openRoot()) {
            int changed = 0;
            if(originalHeld.isEmpty())
                changed = access.insert(held, 1, tx);
            else
                changed = access.exchange(held, 1, tx);

            if (changed == 1) {
                tx.commit();
                src.sendSuccess(() -> CommandTranslations.MACHINE_GIVEN.apply(player), true);
            } else {
                src.sendFailure(CommandTranslations.CANNOT_GIVE_MACHINE.get());
            }
        }
    }
}

