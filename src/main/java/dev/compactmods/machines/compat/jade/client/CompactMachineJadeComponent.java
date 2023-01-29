package dev.compactmods.machines.compat.jade.client;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.compat.CompactMachineTooltipData;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.tunnel.TunnelItem;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class CompactMachineJadeComponent {
    public static final IComponentProvider BODY = (tooltip, blockAccessor, pluginConfig) -> {
        final var level = blockAccessor.getLevel();

        final var data = CompactMachineTooltipData.CODEC
                .parse(NbtOps.INSTANCE, blockAccessor.getServerData())
                .getOrThrow(false, CompactMachines.LOGGER::error);

        final var elHelper = tooltip.getElementHelper();
        final var tunnels = data.connectedTunnels()
                .stream()
                .filter(tmi -> tmi.side().equals(blockAccessor.getSide()))
                .map(tmi -> Tunnels.getDefinition(tmi.type()))
                .toList();

        addConnectedRoom(tooltip, data);
        addOwnerInfo(tooltip, level, data);
        addTunnelInfo(tooltip, elHelper, tunnels);
        addUnbreakableMessage(tooltip, data);
    };

    private static void addConnectedRoom(ITooltip tooltip, CompactMachineTooltipData data) {
        final var connectedComponent = data.connectedRoom()
                .map(room -> TranslationUtil.tooltip(Tooltips.Machines.BOUND_TO, room)
                        .withStyle(ChatFormatting.DARK_GRAY))
                .orElse(TranslationUtil.message(new ResourceLocation(CompactMachines.MOD_ID, "new_machine"))
                        .withStyle(ChatFormatting.GREEN));

        tooltip.add(connectedComponent);
    }

    private static void addOwnerInfo(ITooltip tooltip, Level level, CompactMachineTooltipData data) {
        data.owner().ifPresent(ownerID -> {
            // Owner Name
            Player owner = level.getPlayerByUUID(ownerID);
            if (owner != null) {
                GameProfile ownerProfile = owner.getGameProfile();
                MutableComponent ownerText = TranslationUtil
                        .tooltip(Tooltips.Machines.OWNER, ownerProfile.getName())
                        .withStyle(ChatFormatting.GRAY);

                tooltip.add(ownerText);
            }
        });
    }

    private static void addTunnelInfo(ITooltip tooltip, IElementHelper elHelper, List<TunnelDefinition> tunnels) {
        try {
            if (!tunnels.isEmpty()) {
                var space = elHelper.spacer(0, 3);
                tooltip.add(space);

                tooltip.add(elHelper.spacer(1, 10));
                tunnels.forEach(tmi -> {
                    final var item = TunnelItem.createStack(tmi);
                    tooltip.append(elHelper.item(item, 1f)
                            .align(IElement.Align.LEFT)
                            .size(new Vec2(8, 8))
                            .translate(new Vec2(-6, -6)));
                });
            }
        }

        catch(Exception e) {}
    }

    private static void addUnbreakableMessage(ITooltip tooltip, CompactMachineTooltipData data) {
        int unbreakableState = (data.hasPlayers() ? 1 : 0) + (data.hasTunnels() ? 2 : 0);
        final var elHelper = tooltip.getElementHelper();
        switch (unbreakableState) {
            case 0: break;
            case 1:
                tooltip.add(TranslationUtil.tooltip(Tooltips.UNBREAKABLE_PLAYERS)
                        .withStyle(s -> s.withUnderlined(false)
                                .withColor(ChatFormatting.DARK_RED)
                                .withItalic(true)));

                tooltip.append(elHelper.item(new ItemStack(Items.PLAYER_HEAD), 0.75f)
                        .translate(new Vec2(0, -3)));

                break;

            case 2:
                tooltip.add(TranslationUtil.tooltip(Tooltips.UNBREAKABLE_TUNNELS)
                        .withStyle(s -> s.withUnderlined(false)
                                .withColor(ChatFormatting.DARK_RED)
                                .withItalic(true)));

                tooltip.append(elHelper.item(TunnelItem.createStack(Tunnels.ITEM_TUNNEL_DEF.get()), 0.75f)
                        .translate(new Vec2(-2, -3)));
                break;

            case 3:
                tooltip.add(TranslationUtil.tooltip(Tooltips.UNBREAKABLE_PLAYERS_AND_TUNNELS)
                        .withStyle(s -> s.withUnderlined(false)
                                .withColor(ChatFormatting.DARK_RED)
                                .withItalic(true)));

                tooltip.append(elHelper.item(new ItemStack(Items.PLAYER_HEAD), 0.75f)
                        .translate(new Vec2(0, -3)));

                tooltip.append(elHelper.item(TunnelItem.createStack(Tunnels.ITEM_TUNNEL_DEF.get()), 0.75f)
                        .translate(new Vec2(-2, -3)));
                break;
        }
    }
}
