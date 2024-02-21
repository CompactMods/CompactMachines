package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.command.CMCommands;
import dev.compactmods.machines.api.Messages;
import dev.compactmods.machines.api.machine.MachineCreator;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.command.argument.Suggestors;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

public class CMGiveMachineSubcommand {

    private static final Logger LOGGER = LoggingUtil.modLog();

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("give")
                .requires(cs -> cs.hasPermission(ServerConfig.giveMachineLevel()));


        // /cm give new [template]
        subRoot.then(Commands.literal("new")
                .then(Commands.argument("template", ResourceLocationArgument.id())
                        .suggests(Suggestors.ROOM_TEMPLATES)
                        .executes(CMGiveMachineSubcommand::giveNewMachineExecutor)));

        // /cm give existing [room-code]
        subRoot.then(Commands.literal("existing")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMGiveMachineSubcommand::giveExistingRoomExecutor)));

        // /cm give [player]
        var giveSpecificPlayer = Commands.argument("player", EntityArgument.player());

        // /cm give [player] new [template]
        giveSpecificPlayer.then(Commands.literal("new")
                        .then(Commands.argument("template", ResourceLocationArgument.id())
                            .suggests(Suggestors.ROOM_TEMPLATES)
                            .executes(CMGiveMachineSubcommand::giveNewMachineSpecificPlayer)));

        // /cm give [player] existing [room-code]
        giveSpecificPlayer.then(Commands.literal("existing")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMGiveMachineSubcommand::giveExistingRoomSpecificPlayer)));

        subRoot.then(giveSpecificPlayer);




        return subRoot;
    }


    private static int giveNewMachineExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var templateId = ResourceLocationArgument.getId(ctx, "template");

        createAndGiveNewMachine(src, templateId, player);

        return 0;
    }
    
    private static int giveNewMachineSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var templateId = ResourceLocationArgument.getId(ctx, "template");

        createAndGiveNewMachine(src, templateId, player);

        return 0;
    }

    private static int giveExistingRoomExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var roomCode = StringArgumentType.getString(ctx, "room");

        createAndGiveExistingRoom(roomCode, player, src);

        return 0;
    }

    private static int giveExistingRoomSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var roomCode = StringArgumentType.getString(ctx, "room");

        createAndGiveExistingRoom(roomCode, player, src);

        return 0;
    }

    private static void createAndGiveNewMachine(CommandSourceStack src, ResourceLocation templateId, ServerPlayer player) {

        final var template = src.getServer().registryAccess()
                .registryOrThrow(RoomTemplate.REGISTRY_KEY)
                .get(templateId);

        if(template != null) {
            final var item = UnboundCompactMachineItem.forTemplate(templateId, template);
            if (!player.addItem(item)) {
                src.sendFailure(TranslationUtil.command(CMCommands.CANNOT_GIVE_MACHINE));
            } else {
                src.sendSuccess(() -> TranslationUtil.command(CMCommands.MACHINE_GIVEN, player.getDisplayName()), true);
            }
        } else {
            src.sendFailure(TranslationUtil.command(CMCommands.CANNOT_GIVE_MACHINE));
        }
    }

    private static void createAndGiveExistingRoom(String roomCode, ServerPlayer player, CommandSourceStack src) {
        RoomApi.registrar().get(roomCode).ifPresentOrElse(room -> {
            ItemStack newItem = MachineCreator.boundToRoom(room.code(), room.defaultMachineColor());
            if (!player.addItem(newItem)) {
                src.sendFailure(TranslationUtil.command(CMCommands.CANNOT_GIVE_MACHINE));
            } else {
                src.sendSuccess(() -> TranslationUtil.command(CMCommands.MACHINE_GIVEN, player.getDisplayName()), true);
            }
        }, () -> {
            LOGGER.error("Error giving player a new machine block: room not found.");
            src.sendFailure(TranslationUtil.message(Messages.UNKNOWN_ROOM_CHUNK, roomCode));
        });
    }
}

