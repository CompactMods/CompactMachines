package dev.compactmods.machines.network.machine;


import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.machine.ClientMachinePacketHandler;
import dev.compactmods.machines.util.codec.VanillaCodecs;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record OpenMachinePreviewScreenPacket(GlobalPos machinePos, String roomCode, StructureTemplate internalBlocks) implements CustomPacketPayload {

    public static final Type<OpenMachinePreviewScreenPacket> TYPE = new Type<>(CompactMachines.modRL("open_machine_preview_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenMachinePreviewScreenPacket> STREAM_CODEC = Util.make(() -> StreamCodec.composite(
            GlobalPos.STREAM_CODEC, OpenMachinePreviewScreenPacket::machinePos,
            ByteBufCodecs.STRING_UTF8, OpenMachinePreviewScreenPacket::roomCode,
            VanillaCodecs.STRUCTURE_TEMPLATE_STREAM_CODEC, OpenMachinePreviewScreenPacket::internalBlocks,
            OpenMachinePreviewScreenPacket::new
    ));

    public static final IPayloadHandler<OpenMachinePreviewScreenPacket> HANDLER = (pkt, ctx)
            -> ClientMachinePacketHandler.openRoomPreviewScreen(pkt);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

