package dev.compactmods.machines.compat.theoneprobe.element;

import dev.compactmods.machines.room.data.RoomPreview;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class RoomPreviewElementFactory implements IElementFactory {
    @Override
    public IElement createElement(FriendlyByteBuf buf) {
        final var data = buf.readWithCodec(RoomPreview.CODEC);
        final var el = new RoomPreviewElement(data);
        el.loadBlocks(buf.readNbt());
        return el;
    }

    @Override
    public ResourceLocation getId() {
        return RoomPreviewElement.ID;
    }
}

