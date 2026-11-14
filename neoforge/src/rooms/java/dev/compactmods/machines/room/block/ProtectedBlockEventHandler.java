package dev.compactmods.machines.room.block;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ProtectedBlockEventHandler {

    public static void leftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        final var player = evt.getEntity();
        final var pos = evt.getPos();
        final var lev = evt.getLevel();

        final var state = lev.getBlockState(pos);
        if(state.getBlock() instanceof ProtectedWallBlock pwb) {
            if(!pwb.canPlayerBreak(player))
                evt.setCanceled(true);
        }
    }
}
