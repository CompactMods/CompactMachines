package dev.compactmods.machines.room.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.data.CMRoomDataLocations;
import dev.compactmods.machines.data.AttachmentBasedDataFile;
import dev.compactmods.machines.data.AttachmentDataFileFactoryInput;
import dev.compactmods.machines.data.SafeAttachmentsCodec;
import dev.compactmods.machines.data.CMDataFile;
import dev.compactmods.machines.data.CodecHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentHolder;

import java.nio.file.Path;
import java.util.function.Function;

public class RoomDataAttachments extends AttachmentBasedDataFile<RoomDataAttachments, RoomDataAttachments.AdditionalData> {

    private final String roomCode;

    public record AdditionalData(String roomCode) {
        public static final MapCodec<AdditionalData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("room_code").forGetter(x -> x.roomCode)
        ).apply(i, AdditionalData::new));
    }

    public RoomDataAttachments(MinecraftServer server, String roomCode) {
        super(server, AdditionalData.CODEC, RoomDataAttachments::new);
        this.roomCode = roomCode;
    }

    public RoomDataAttachments(AttachmentDataFileFactoryInput<AdditionalData> fromCodec) {
        super(fromCodec.server(), AdditionalData.CODEC, RoomDataAttachments::new);
        this.roomCode = fromCodec.additionalData().roomCode();
    }

    @Override
    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.ROOM_DATA_ATTACHMENTS.apply(server);
    }

    public String roomCode() {
        return roomCode;
    }

    @Override
    public Codec<RoomDataAttachments> codec() {
        return codec;
    }

    @Override
    protected RoomDataAttachments.AdditionalData dataSupplier(RoomDataAttachments instance) {
        return new RoomDataAttachments.AdditionalData(instance.roomCode());
    }
}
