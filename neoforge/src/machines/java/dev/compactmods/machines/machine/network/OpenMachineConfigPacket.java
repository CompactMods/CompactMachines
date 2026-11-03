package dev.compactmods.machines.machine.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.config.MachineConfigCanvas;
import dev.compactmods.machines.machine.ui.MachineConfigMenu;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Client → server: open the configuration screen of the machine at {@code machine} (from the machine
 * screen's toolbar). Ignored unless the player can reach the machine and it has a configuration canvas,
 * i.e. it's bound to a room.
 */
public record OpenMachineConfigPacket(GlobalPos machine) implements CustomPacketPayload {

    private static final int MAX_DISTANCE = 5;

    public static final Type<OpenMachineConfigPacket> TYPE = new Type<>(CompactMachinesCore.identifier("open_machine_config"));

    public static final StreamCodec<FriendlyByteBuf, OpenMachineConfigPacket> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, OpenMachineConfigPacket::machine,
            OpenMachineConfigPacket::new);

    public static final IPayloadHandler<OpenMachineConfigPacket> HANDLER = (pkt, ctx) -> ctx.enqueueWork(() -> {
        if (!(ctx.player() instanceof ServerPlayer player))
            return;

        final var pos = pkt.machine();
        if (!player.level().dimension().equals(pos.dimension()) || !player.isWithinBlockInteractionRange(pos.pos(), MAX_DISTANCE))
            return;

        final var canvas = player.level().getCapability(MachineConfigCanvas.CAPABILITY, pos.pos());
        if (canvas == null)
            return;

        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatableWithFallback(CompactMachinesCore.dotPrefix("machine_config"), "Machine Configuration");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                return new MachineConfigMenu(containerId, p, pos, canvas);
            }

            @Override
            public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
                MachineConfigMenu.writeClientSideData(menu, buffer);
            }
        });
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
