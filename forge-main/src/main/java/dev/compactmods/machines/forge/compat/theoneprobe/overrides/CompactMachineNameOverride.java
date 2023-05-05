package dev.compactmods.machines.forge.compat.theoneprobe.overrides;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.forge.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CompactMachineNameOverride implements IBlockDisplayOverride {
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData hitData) {
        if(blockState.is(CMTags.MACHINE_BLOCK)) {
            if (level.getBlockEntity(hitData.getPos()) instanceof BoundCompactMachineBlockEntity mach) {
                final var cr = mach.connectedRoom();
                if(cr.isEmpty())
                    return false;

                cr.ifPresent(roomCode -> {
                    final var pickBlock = hitData.getPickBlock();
                    if (!pickBlock.isEmpty()) {
                        final var config = Config.getRealConfig();

                        final var modName = Tools.getModName(blockState.getBlock());
                        Component roomName;

                        try {
                            roomName = Rooms.getRoomName(level.getServer(), roomCode)
                                    .map(Component::literal)
                                    .orElse(BoundCompactMachineItem.name(pickBlock).copy());
                        } catch (NonexistentRoomException e) {
                            roomName = pickBlock.getHoverName();
                        }

                        if (Tools.show(mode, config.getShowModName())) {
                            probeInfo.horizontal()
                                    .item(pickBlock)
                                    .vertical()
                                    .mcText(roomName)
                                    .text(CompoundText.create().style(TextStyleClass.MODNAME).text(modName));
                        } else {
                            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                                    .item(pickBlock)
                                    .mcText(roomName);
                        }
                    }
                });

                return true;
            }
        }

        return false;
    }
}
