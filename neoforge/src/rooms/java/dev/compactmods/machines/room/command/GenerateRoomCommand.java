package dev.compactmods.machines.room.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.generation.RoomGenerationException;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.room.command.argument.Suggestors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.Util;

public class GenerateRoomCommand {

    private static final String CREATED_I18N_KEY;

    static {
        CREATED_I18N_KEY = Util.makeDescriptionId("generation", CompactMachinesCore.identifier("created_new_room_via_command"));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        // /cm rooms
        final LiteralArgumentBuilder<CommandSourceStack> subRoot = LiteralArgumentBuilder.literal("generate");

        // TODO: /cm rooms generate [template]

        // generate [template]
        subRoot.then(Commands.argument("template", IdentifierArgument.id())
                .suggests(Suggestors.ROOM_TEMPLATES)
                .executes(GenerateRoomCommand::exec));

        return subRoot;
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var player = src.getPlayerOrException();
        final var templateId = IdentifierArgument.getId(ctx, "template");

        final var template = RoomTemplateHelper.getTemplate(src.registryAccess(), templateId);

        try {
            final var server = ctx.getSource().getServer();
            final var generator = server.getCapability(RoomCapabilities.GENERATOR);

            if (generator == null) {
                src.sendFailure(Component.translatableWithFallback(Util.makeDescriptionId("error", CompactMachinesCore.identifier("capability_not_found")), "Room Generator not registered. Report this as a bug!"));
                return -1;
            }

            final var generationDetails = generator.createNew()
                    .owner(player.getUUID())
                    .template(template)
                    .build();

            final var instance = generator.generate(generationDetails);
            instance.ifPresent(result -> {
                Component preamble = Component.translatableWithFallback(CREATED_I18N_KEY, "Generated new room from template. New room ID: ");
                Component success = Component.literal(result.newCode())
                        .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand("/compactmachines room_core bind_to " + result.newCode()))
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to give a new core")))
                                .withUnderlined(true));

                final var message = Component.empty()
                        .append(preamble)
                        .append(success);

                src.sendSuccess(() -> message, true);
            });

        } catch (RoomGenerationException e) {
            CompactMachinesCore.modLog().error(e);
            src.sendFailure(Component.literal("Failed: " + e.getMessage()));
        }

        return 0;
    }

}
