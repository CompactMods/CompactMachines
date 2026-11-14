package dev.compactmods.machines.room;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

public class RoomItemHandler {
    public static void handleTooltips(ItemTooltipEvent event) {
        final var stack = event.getItemStack();
        if (stack.isEmpty())
            return;

        final var ctx = event.getContext();
        final var tooltips = event.getToolTip();

        var additions = new ArrayList<Component>();

        final var roomCode = stack.get(Rooms.DataComponents.BOUND_ROOM_CODE);
        final var roomTemplate = stack.get(Rooms.DataComponents.ROOM_TEMPLATE_ID);
//        if(roomCode != null || roomTemplate != null) {
//            additions.add(CommonComponents.space());
//        }

        if (roomCode != null) {
            additions.add(Component.literal("Bound To: " + roomCode)
                    .withoutShadow()
                    .withColor(CommonColors.DARK_GRAY));
        }

        if (roomTemplate != null) {
            additions.add(Component.literal("Room Template: " + roomTemplate)
                    .withoutShadow()
                    .withColor(CommonColors.DARK_GRAY));
        }

        tooltips.addAll(1, additions);
    }
}
