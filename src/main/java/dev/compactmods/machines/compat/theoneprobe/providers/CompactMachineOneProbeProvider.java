package dev.compactmods.machines.compat.theoneprobe.providers;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.compat.CompactMachineTooltipData;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.TunnelItem;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.stream.Collectors;

public class CompactMachineOneProbeProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(CompactMachines.MOD_ID, "machine");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, BlockState blockState, IProbeHitData hitData) {
        if (!(level.getBlockEntity(hitData.getPos()) instanceof CompactMachineBlockEntity machine))
            return;

        final var server = level.getServer();
        if (server == null)
            return;

        final var compactDim = server.getLevel(Registration.COMPACT_DIMENSION);
        var data = CompactMachineTooltipData.forMachine(compactDim, machine);

        final var tunnels = data.connectedTunnels()
                .stream()
                .filter(tmi -> tmi.side().equals(hitData.getSideHit()))
                .map(tmi -> Tunnels.getDefinition(tmi.type()))
                .collect(Collectors.toSet());

        addConnectedRoom(info, data);
        addOwnerInfo(info, level, data);
        addTunnelInfo(probeMode, info, tunnels);
        addUnbreakableMessage(info, machine);
    }

    private static void addConnectedRoom(IProbeInfo info, CompactMachineTooltipData data) {
        final var connectedComponent = data.connectedRoom()
                .map(room -> TranslationUtil.tooltip(Tooltips.Machines.BOUND_TO, room)
                        .withStyle(ChatFormatting.DARK_GRAY))
                .orElse(TranslationUtil.message(new ResourceLocation(CompactMachines.MOD_ID, "new_machine"))
                        .withStyle(ChatFormatting.GREEN));

        info.mcText(connectedComponent);
    }

    private static void addOwnerInfo(IProbeInfo info, Level level, CompactMachineTooltipData data) {
        data.owner().ifPresent(ownerID -> {
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
    }

    private static void addUnbreakableMessage(IProbeInfo info, CompactMachineBlockEntity machine) {
        int unbreakableState = (machine.hasPlayersInside() ? 1 : 0) + (machine.hasTunnels() ? 2 : 0);

        final var group = info.horizontal();
        switch (unbreakableState) {
            case 0:
                break;
            case 1:
                group.mcText(TranslationUtil.tooltip(Tooltips.UNBREAKABLE_PLAYERS)
                        .withStyle(s -> s.withUnderlined(false)
                                .withColor(ChatFormatting.DARK_RED)
                                .withItalic(true)));

                group.item(new ItemStack(Items.PLAYER_HEAD), info.defaultItemStyle()
                        .bounds(8, 8));
                break;

            case 2:
                group.mcText(TranslationUtil.tooltip(Tooltips.UNBREAKABLE_TUNNELS)
                        .withStyle(s -> s.withUnderlined(false)
                                .withColor(ChatFormatting.DARK_RED)
                                .withItalic(true)));

                group.item(TunnelItem.createStack(Tunnels.ITEM_TUNNEL_DEF.get()), info.defaultItemStyle()
                        .bounds(8, 8));
                break;

            case 3:
                group.mcText(TranslationUtil.tooltip(Tooltips.UNBREAKABLE_PLAYERS_AND_TUNNELS)
                        .withStyle(s -> s.withUnderlined(false)
                                .withColor(ChatFormatting.DARK_RED)
                                .withItalic(true)));

                group.item(new ItemStack(Items.PLAYER_HEAD), info.defaultItemStyle()
                        .bounds(8, 8));

                group.item(TunnelItem.createStack(Tunnels.ITEM_TUNNEL_DEF.get()), info.defaultItemStyle()
                        .bounds(8, 8));
                break;
        }
    }

    private static void addTunnelInfo(ProbeMode probeMode, IProbeInfo info, Set<TunnelDefinition> tunnels) {
        switch (probeMode) {
            case NORMAL:
                final var group = info.horizontal(new LayoutStyle()
                        .alignment(ElementAlignment.ALIGN_TOPLEFT)
                        .padding(0)
                        .spacing(0));

                tunnels.forEach(tn -> {
                    final var item = TunnelItem.createStack(tn);
                    group.item(item, new ItemStyle().bounds(8, 8));
                });
                break;

            case EXTENDED:
                final var tgg = info.vertical(new LayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                tunnels.forEach(tn -> {
                    final var tg = tgg.horizontal(new LayoutStyle()
                            .alignment(ElementAlignment.ALIGN_CENTER)
                            .hPadding(2).vPadding(2)
                            .spacing(0));

                    final var item = TunnelItem.createStack(tn);
                    tg.item(item, new ItemStyle().bounds(8, 8));
                    tg.itemLabel(item);
                });
                break;
        }
    }
}
