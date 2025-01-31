package dev.compactmods.machines.room.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.data.CMRoomDataLocations;
import dev.compactmods.machines.data.AttachmentBasedDataFile;
import dev.compactmods.machines.data.AttachmentDataFileFactoryInput;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstanceKey;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class RoomUpgradeDataAttachments extends AttachmentBasedDataFile<RoomUpgradeDataAttachments, RoomUpgradeDataAttachments.RoomUpgradeMetadata> {

    private final RoomUpgradeInstanceKey upgradeId;

    public RoomUpgradeDataAttachments(MinecraftServer server, RoomUpgradeInstanceKey upgradeId) {
        super(server, RoomUpgradeMetadata.CODEC, RoomUpgradeDataAttachments::new);
        this.upgradeId = upgradeId;
    }

    public RoomUpgradeDataAttachments(AttachmentDataFileFactoryInput<RoomUpgradeMetadata> input) {
        super(input.server(), RoomUpgradeMetadata.CODEC, RoomUpgradeDataAttachments::new);
        this.upgradeId = input.additionalData().upgradeId();
    }

    @Override
    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.ROOM_DATA_ATTACHMENTS.apply(server)
                .resolve("upgrade_data")
                .resolve(upgradeId.roomCode());
    }

    @Override
    public Codec<RoomUpgradeDataAttachments> codec() {
        return codec;
    }

    @Override
    protected RoomUpgradeMetadata dataSupplier(RoomUpgradeDataAttachments instance) {
        return new RoomUpgradeMetadata(instance.upgradeId);
    }

    public record RoomUpgradeMetadata(RoomUpgradeInstanceKey upgradeId) {
        public static MapCodec<RoomUpgradeMetadata> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RoomUpgradeInstanceKey.CODEC.fieldOf("instance_id").forGetter(RoomUpgradeMetadata::upgradeId)
        ).apply(i, RoomUpgradeMetadata::new));
    }
}
