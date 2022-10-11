package dev.compactmods.machines.room.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record InitialRoomBlockDataPacket(StructureTemplate blocks) {

    public static InitialRoomBlockDataPacket fromNetwork(FriendlyByteBuf buf) {
        final var nbt = buf.readNbt();
        final var struct = new StructureTemplate();
        struct.load(nbt);

        return new InitialRoomBlockDataPacket(struct);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        final var tag = blocks.save(new CompoundTag());
        buf.writeNbt(tag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientRoomNetworkHandler.handleBlockData(this));
        return true;
    }
}
