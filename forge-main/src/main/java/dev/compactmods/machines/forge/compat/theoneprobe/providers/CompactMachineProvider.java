package dev.compactmods.machines.forge.compat.theoneprobe.providers;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.forge.compat.theoneprobe.elements.PlayerFaceElement;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.forge.tunnel.TunnelItem;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.util.PlayerUtil;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.Collectors;

public class CompactMachineProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(Constants.MOD_ID, "machine");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, BlockState blockState, IProbeHitData hitData) {
        if (!blockState.is(CMTags.MACHINE_BLOCK))
            return;

        final var server = level.getServer();
        if (server == null)
            return;

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        if (compactDim == null)
            return;

        if (level.getBlockEntity(hitData.getPos()) instanceof BoundCompactMachineBlockEntity machine) {
            machine.connectedRoom().ifPresentOrElse(roomCode -> {
                final var boundTo = TranslationUtil.tooltip(Tooltips.Machines.BOUND_TO, roomCode);
                info.text(boundTo);

                final var roomGraph = CompactRoomProvider.instance(compactDim);
                roomGraph.getRoomOwner(roomCode)
                        .flatMap(owner -> PlayerUtil.getProfileByUUID(server, owner))
                        .ifPresent(p -> {
                            MutableComponent ownerText = TranslationUtil
                                    .tooltip(Tooltips.Machines.OWNER, p.getName())
                                    .withStyle(ChatFormatting.GRAY);

                            info.horizontal(new LayoutStyle()
                                            .alignment(ElementAlignment.ALIGN_CENTER)
                                            .padding(0).spacing(0))
                                    .element(new PlayerFaceElement(p))
                                    .text(ownerText);
                        });

                addTunnelInfo(probeMode, info, hitData, compactDim, machine, roomCode);
            }, () -> {
                MutableComponent newMachine = TranslationUtil
                        .message(new ResourceLocation(Constants.MOD_ID, "new_machine"))
                        .withStyle(ChatFormatting.GREEN);

                info.text(newMachine);
            });
        }
    }

    private static void addTunnelInfo(ProbeMode probeMode, IProbeInfo info, IProbeHitData hitData, ServerLevel compactDim, BoundCompactMachineBlockEntity machine, String roomCode) {
        if (compactDim == null)
            return;

        final var graph = TunnelConnectionGraph.forRoom(compactDim, roomCode);

        final var applied = graph.types(machine.getLevelPosition(), hitData.getSideHit())
                .collect(Collectors.toSet());

        switch (probeMode) {
            case NORMAL -> {
                final var group = info.horizontal(new LayoutStyle()
                        .alignment(ElementAlignment.ALIGN_TOPLEFT)
                        .padding(0)
                        .spacing(0));
                applied.forEach(tn -> {
                    ItemStack item = TunnelItem.createStack(tn);
                    group.item(item, new ItemStyle().bounds(8, 8));
                });
            }
            case EXTENDED -> {
                final var tgg = info.vertical(new LayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                applied.forEach(tn -> {
                    final var tg = tgg.horizontal(new LayoutStyle()
                            .alignment(ElementAlignment.ALIGN_CENTER)
                            .hPadding(2).vPadding(2)
                            .spacing(0));

                    ItemStack item = TunnelItem.createStack(tn);
                    tg.item(item, new ItemStyle().bounds(8, 8));
                    tg.itemLabel(item);
                });
            }
        }
    }
}
