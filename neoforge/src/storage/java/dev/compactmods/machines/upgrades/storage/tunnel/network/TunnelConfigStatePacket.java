package dev.compactmods.machines.upgrades.storage.tunnel.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigMenu;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

/**
 * Server → client: the outcome of a {@link TunnelConfigSubmitPacket}. Updates the open tunnel config
 * menu, so the screen switches to edit mode on success or shows the error on failure.
 *
 * @param containerId Menu the submission came from; ignored if the player has since closed it.
 * @param state       The storage as it now stands, if the submission succeeded.
 * @param error       Why the submission was refused, if it was.
 */
public record TunnelConfigStatePacket(int containerId, Optional<TunnelConfigState> state, Optional<Component> error)
        implements CustomPacketPayload {

    public static final Type<TunnelConfigStatePacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("tunnel_config_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TunnelConfigStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TunnelConfigStatePacket::containerId,
            ByteBufCodecs.optional(TunnelConfigState.STREAM_CODEC), TunnelConfigStatePacket::state,
            ByteBufCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC), TunnelConfigStatePacket::error,
            TunnelConfigStatePacket::new);

    public static final IPayloadHandler<TunnelConfigStatePacket> HANDLER = (pkt, ctx) -> ctx.enqueueWork(() -> {
        if (ctx.player().containerMenu instanceof TunnelConfigMenu menu && menu.containerId == pkt.containerId())
            menu.update(pkt.state(), pkt.error());
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
