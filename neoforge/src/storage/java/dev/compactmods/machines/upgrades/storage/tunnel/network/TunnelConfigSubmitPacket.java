package dev.compactmods.machines.upgrades.storage.tunnel.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.tunnel.NamedStorageService;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigMenu;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigSubmission;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

/**
 * Client → server: the tunnel config screen was submitted. Applies to the menu the player has open,
 * if it's still a valid {@link TunnelConfigMenu}; answered with a {@link TunnelConfigStatePacket}.
 *
 * @param name The storage's (new) name.
 * @param tunnelType The tunnel type picked; only used in create mode.
 */
public record TunnelConfigSubmitPacket(String name, Optional<ResourceKey<TunnelType>> tunnelType) implements CustomPacketPayload {

    public static final Type<TunnelConfigSubmitPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("tunnel_config_submit"));

    public static final StreamCodec<FriendlyByteBuf, TunnelConfigSubmitPacket> STREAM_CODEC = StreamCodec.composite(
            // Generous cap; the real limit is checked (with a proper error) by NamedStorageService
            ByteBufCodecs.stringUtf8(NamedStorageService.MAX_NAME_LENGTH * 2), TunnelConfigSubmitPacket::name,
            ByteBufCodecs.optional(ResourceKey.streamCodec(TunnelType.REGISTRY_KEY)), TunnelConfigSubmitPacket::tunnelType,
            TunnelConfigSubmitPacket::new);

    public static final IPayloadHandler<TunnelConfigSubmitPacket> HANDLER = (pkt, ctx) -> ctx.enqueueWork(() -> {
        if (!(ctx.player() instanceof ServerPlayer player))
            return;

        if (!(player.containerMenu instanceof TunnelConfigMenu menu) || !menu.stillValid(player))
            return;

        TunnelConfigSubmission.submit(player, menu, pkt.name().trim(), pkt.tunnelType());
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
