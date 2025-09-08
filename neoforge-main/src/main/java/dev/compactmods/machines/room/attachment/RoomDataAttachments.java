package dev.compactmods.machines.room.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.data.CMRoomDataLocations;
import dev.compactmods.machines.data.AttachmentBasedDataFile;
import dev.compactmods.machines.data.AttachmentDataFileFactoryInput;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class RoomDataAttachments extends AttachmentBasedDataFile<RoomDataAttachments, RoomDataAttachments.AdditionalData> implements AutoCloseable {

    private final String roomCode;

    @Override
    public void close() throws Exception {
        // save
    }

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

    @Override
    public Codec<RoomDataAttachments> codec() {
        return codec;
    }

    @Override
    protected RoomDataAttachments.AdditionalData dataSupplier(RoomDataAttachments instance) {
        return new RoomDataAttachments.AdditionalData(instance.roomCode);
    }
}
