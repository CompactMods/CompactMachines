package dev.compactmods.machines.compat.theoneprobe.providers;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.tunnel.TunnelItem;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.Collectors;

public class CompactMachineProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(CompactMachines.MOD_ID, "machine");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, BlockState blockState, IProbeHitData hitData) {
        if (!(blockState.getBlock() instanceof CompactMachineBlock mach))
            return;

        final var server = level.getServer();
        final var te = level.getBlockEntity(hitData.getPos());

        if (te instanceof CompactMachineBlockEntity machine) {
            if (machine.mapped()) {
                MutableComponent id = TranslationUtil
                        .tooltip(Tooltips.Machines.ID, machine.machineId, hitData.getSideHit())
                        .withStyle(ChatFormatting.GREEN);

                info.text(id);
            } else {
                MutableComponent newMachine = TranslationUtil
                        .message(new ResourceLocation(CompactMachines.MOD_ID, "new_machine"))
                        .withStyle(ChatFormatting.GREEN);

                info.text(newMachine);
            }

            machine.getOwnerUUID().ifPresent(ownerID -> {
                // Owner Name
                Player owner = level.getPlayerByUUID(ownerID);
                if (owner != null) {
                    GameProfile ownerProfile = owner.getGameProfile();
                    MutableComponent ownerText = TranslationUtil
                            .tooltip(Tooltips.Machines.OWNER, ownerProfile.getName())
                            .withStyle(ChatFormatting.GRAY);

                    info.text(ownerText);
                }
            });

            machine.getInternalChunkPos().ifPresent(room -> {
                try {
                    final var roomData = CompactRoomData.get(server);
                    final var tunnels = RoomTunnelData.get(server, room);
                    final var graph = tunnels.getGraph();

                    final var applied = graph.getTypesForSide(machine.machineId, hitData.getSideHit())
                            .collect(Collectors.toSet());

                    switch (probeMode) {
                        case NORMAL:
                            final var group = info.horizontal(new LayoutStyle()
                                    .alignment(ElementAlignment.ALIGN_TOPLEFT)
                                    .padding(0)
                                    .spacing(0));

                            applied.forEach(tn -> {
                                ItemStack item = TunnelItem.createStack(tn);
                                group.item(item, new ItemStyle().bounds(8, 8));
                            });
                            break;

                        case EXTENDED:
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
                            break;
                    }

                    final var rd = roomData.forRoom(room);
                    rd.ifPresent(r -> {
//                        final var el = new RoomPreviewElement(new RoomPreview(room, r.getSize()));
//                        el.loadBlocks(server, r);
//                        info.element(el);
                    });
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.fatal(e);
                }
            });
        }
    }
}
