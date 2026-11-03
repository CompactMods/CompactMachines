package dev.compactmods.machines.upgrades.storage.tunnel;

import dev.compactmods.machines.room.Rooms;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Optional;

/**
 * A tunnel, used on a room's inner solid wall to create a named storage there. The item is only
 * consumed once the storage is created (see {@link dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigSubmission}).
 */
public class TunnelItem extends Item {

    public TunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final var level = context.getLevel();
        final var pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Rooms.Blocks.SOLID_WALL.get()))
            return InteractionResult.PASS;

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (!(context.getPlayer() instanceof ServerPlayer player))
            return InteractionResult.PASS;

        final var face = context.getClickedFace();
        final var placeable = Tunnels.roomAt(level, pos)
                .filter(room -> Tunnels.facesInward(room, pos, face))
                .isPresent();

        if (!placeable)
            return InteractionResult.PASS;

        Tunnels.openConfig(player, pos, face, Optional.empty());
        return InteractionResult.SUCCESS_SERVER;
    }
}
