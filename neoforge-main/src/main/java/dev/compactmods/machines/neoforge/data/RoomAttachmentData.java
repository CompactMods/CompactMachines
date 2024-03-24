package dev.compactmods.machines.neoforge.data;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.jline.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RoomAttachmentData extends AttachmentHolder {

    private final String roomCode;
    private final File file;

    private RoomAttachmentData(String roomCode, File file) {
        this.roomCode = roomCode;
        this.file = file;
    }

    private static File getDataFile(File folder, String filename) {
        return new File(folder, filename + ".dat");
    }

    public static RoomAttachmentData createForRoom(MinecraftServer server, String roomCode) {
        final var dir = CompactDimension.getDataDirectory(server)
                .resolve("data")
                .resolve("room_data");

        final var file = getDataFile(dir.toFile(), roomCode);
        final var data = new RoomAttachmentData(roomCode, file);
        data.load();
        return data;
    }

    private void load() {
        try {
            var is = new FileInputStream(file);
            final var tag = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
            if(tag.contains("attachments")) {
                this.deserializeAttachments(tag.getCompound("attachments"));
            }
        } catch (IOException e) {
            LoggingUtil.modLog().error(e);
        }
    }

    public void save() {
        CompoundTag fullTag = new CompoundTag();
        fullTag.putString("version", "1.0");

        if(this.hasAttachments()) {
            var ad = this.serializeAttachments();
            if(ad != null)
                fullTag.put("attachments", ad);
        }

        try {
            NbtIo.writeCompressed(fullTag, file.toPath());
        } catch (IOException e) {
            LoggingUtil.modLog().error("Failed to write room data: " + roomCode, e);
        }
    }
}
