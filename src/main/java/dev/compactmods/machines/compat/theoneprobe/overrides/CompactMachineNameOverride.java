package dev.compactmods.machines.compat.theoneprobe.overrides;

import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.config.Config;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CompactMachineNameOverride implements IBlockDisplayOverride {
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData hitData) {
        if(blockState.getBlock() instanceof CompactMachineBlock) {
            if (level.getBlockEntity(hitData.getPos()) instanceof CompactMachineBlockEntity mach) {
                final var cr = mach.getConnectedRoom();
                if(cr.isEmpty())
                    return false;

                cr.ifPresent(room -> {
                    final var pickBlock = hitData.getPickBlock();
                    if (!pickBlock.isEmpty()) {
                        final var config = Config.getRealConfig();

                        final var modName = Tools.getModName(blockState.getBlock());
                        BaseComponent roomName;

                        try {
                            roomName = Rooms.getRoomName(level.getServer(), room)
                                    .map(n -> (BaseComponent) new TextComponent(n))
                                    .orElse((BaseComponent) pickBlock.getHoverName());
                        } catch (NonexistentRoomException e) {
                            roomName = (BaseComponent) pickBlock.getHoverName();
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
