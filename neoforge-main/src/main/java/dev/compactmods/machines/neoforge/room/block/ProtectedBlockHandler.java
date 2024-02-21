package dev.compactmods.machines.neoforge.room.block;

import dev.compactmods.machines.api.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ProtectedBlockHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        final var player = evt.getEntity();
        final var pos = evt.getPos();
        final var lev = evt.getLevel();

        final var state = lev.getBlockState(pos);
        if(state.getBlock() instanceof ProtectedWallBlock pwb) {
            if(!pwb.canPlayerBreak(lev, player, pos))
                evt.setCanceled(true);
        }
    }
}
