package dev.compactmods.machines.room.preview.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.ClientHooks;
import org.slf4j.Logger;

import java.io.File;

/**
 * Saves the room preview's rendered view (without the panel or its overlays) as a PNG in the
 * game's {@code screenshots} folder, like vanilla's screenshot key — including its
 * {@code ScreenshotEvent}, so screenshot mods still see it.
 *
 * <p>Unlike vanilla, the image keeps its alpha: the void around the room is transparent, so builds
 * come out cut out, ready to drop onto other backgrounds.
 */
final class PreviewScreenshot {

    private static final Logger LOGGER = LogUtils.getLogger();

    private PreviewScreenshot() {}

    /**
     * Queues a read-back of {@code texture} (RGBA8, {@code width × height}). Commands run in order,
     * so the copy sees this frame's render even though the texture is reused next frame.
     */
    static void capture(GpuTexture texture, int width, int height, String roomCode) {
        final int pixelSize = texture.getFormat().blockSize();
        final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "Room preview screenshot buffer",
                GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
                (long) width * height * pixelSize);

        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, () -> {
            final NativeImage image = new NativeImage(width, height, false);
            try (var read = buffer.map(true, false)) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        // Texture rows are bottom-up; images are top-down
                        image.setPixelABGR(x, height - y - 1, read.data().getInt((x + y * width) * pixelSize));
                    }
                }
            }
            buffer.close();
            save(image, roomCode);
        }, 0);
    }

    private static void save(NativeImage image, String roomCode) {
        final var mc = Minecraft.getInstance();
        final File dir = new File(mc.gameDirectory, Screenshot.SCREENSHOT_DIR);
        dir.mkdir();

        final var event = ClientHooks.onScreenshot(image, uniqueFile(dir, roomCode));
        if (event.isCanceled()) {
            image.close();
            report(event.getCancelMessage());
            return;
        }

        final File file = event.getScreenshotFile();
        Util.ioPool().execute(() -> {
            try (image) {
                image.writeToFile(file);
                if (event.getResultMessage() != null) {
                    report(event.getResultMessage());
                } else {
                    final Component link = Component.literal(file.getName())
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(file.getAbsoluteFile())));
                    report(Component.translatable("screenshot.success", link));
                }
            } catch (Exception e) {
                LOGGER.warn("Couldn't save room preview screenshot", e);
                report(Component.translatable("screenshot.failure", e.getMessage()));
            }
        });
    }

    /** {@code <date-time>_<room code>.png}, numbered if taken more than once in the same second. */
    private static File uniqueFile(File dir, String roomCode) {
        final String base = Util.getFilenameFormattedDateTime() + "_" + roomCode;
        File file = new File(dir, base + ".png");
        for (int n = 2; file.exists(); n++)
            file = new File(dir, base + "_" + n + ".png");
        return file;
    }

    private static void report(Component message) {
        final var mc = Minecraft.getInstance();
        mc.execute(() -> mc.showDebugChat(message));
    }
}
