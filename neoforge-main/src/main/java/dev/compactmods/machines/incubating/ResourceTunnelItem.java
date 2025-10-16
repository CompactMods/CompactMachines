package dev.compactmods.machines.incubating;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.transfer.resource.Resource;

public class ResourceTunnelItem<TResource extends Resource> extends Item {

    public ResourceTunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final var level = context.getLevel();
        if(level.isClientSide() || !CompactDimension.isLevelCompact(level))
            return InteractionResult.PASS;

        final var pos = context.getClickedPos();
        final var targetDir = context.getClickedFace().getOpposite();

        // Get machines bound to room
        final var room = CompactMachines.chunkManager()
                .findRoomByChunk(new ChunkPos(pos))
                .flatMap(CompactMachines::room);

        room.ifPresentOrElse(instance -> {

        }, () -> {

        });

        return InteractionResult.PASS;
    }
}
