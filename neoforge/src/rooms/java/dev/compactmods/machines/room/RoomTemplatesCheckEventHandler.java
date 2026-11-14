package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class RoomTemplatesCheckEventHandler {

    public static void onAdminJoinedLevel(final PlayerEvent.PlayerLoggedInEvent event) {
        final var player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        final var serv = serverPlayer.level().getServer();
        if (serv == null)
            return;

        final var isOp = serv.getPlayerList()
                .isOp(serverPlayer.nameAndId());

        if(!isOp) return;

        final var numTemplates = RoomTemplateHelper.getTemplates(serv.registryAccess())
                .count();

        if(numTemplates == 0)
        {
            final var displayName = Component.translatableWithFallback("compactmachines.display_name", "Compact Machines");

            final var action = Component.translatableWithFallback("compactmachines.warning.no_templates.action", "[click here]")
                    .withStyle(s -> s.withColor(CommonColors.SOFT_YELLOW)
                            .withClickEvent(new ClickEvent.RunCommand("/compactmachines enable_basic_templates"))
                            .withUnderlined(true));

            final var warning = Component.translatableWithFallback("compactmachines.warning.no_templates_registered", "No Room Templates are registered! " +
                    "If you believe this is a mistake, %s to enable the built-in templates and hide this message.", action)
                    .withColor(CommonColors.SOFT_RED);

            final var message = Component.literal("[").append(displayName).append("] ")
                    .append(warning)
                    .append(CommonComponents.NEW_LINE);

            serverPlayer.sendSystemMessage(message);
        }
    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(RoomTemplatesCheckEventHandler::onAdminJoinedLevel);
    }
}
