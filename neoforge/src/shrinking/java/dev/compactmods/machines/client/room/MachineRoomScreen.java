package dev.compactmods.machines.client.room;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.room.client.RoomClientConfig;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.network.room.PlayerRequestedTeleportPacket;
import dev.compactmods.machines.room.network.PlayerStartedRoomTrackingPacket;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class MachineRoomScreen extends Screen {

    private final GlobalPos machinePos;
    private final String roomCode;

    //    private SpatialRenderer renderer;
    private AABB renderSize;

    private SpriteIconButton psdButton;
    private ScreenRectangle screenArea;

    private boolean isLoadingRoomPreview;
    private boolean roomPreviewEnabled = true;

    private final WidgetSprites psdBtnSprites = new WidgetSprites(
            CompactMachinesCore.identifier("personal_shrinking_device"),
            CompactMachinesCore.identifier("personal_shrinking_device_disabled"),
            CompactMachinesCore.identifier("personal_shrinking_device_highlighted"),
            CompactMachinesCore.identifier("personal_shrinking_device_disabled"));

    public MachineRoomScreen(Component title, GlobalPos machinePos, String roomCode) {
        super(title);
        this.machinePos = machinePos;
        this.roomCode = roomCode;

        if (RoomClientConfig.ENABLE_ROOM_PREVIEWS.get()) {
            // Send packet to server for block data
            this.isLoadingRoomPreview = true;
            ClientPacketDistributor.sendToServer(new PlayerStartedRoomTrackingPacket(roomCode));
        } else {
            this.roomPreviewEnabled = false;
        }
    }

    @Override
    protected void init() {
        super.init();

        this.screenArea = new ScreenRectangle((width / 2) - 130, (height / 2) - 120,
                260, 260);

        this.psdButton = addRenderableWidget(SpriteIconButton.builder(CommonComponents.EMPTY, this::teleportIntoRoom, true)
                .size(12, 12)
                .sprite(psdBtnSprites, 12, 12)
                .build());;

        this.psdButton.setPosition(screenArea.right() - 12, screenArea.bottom() + 2);
    }

    private void teleportIntoRoom(Button ignored) {
        ClientPacketDistributor.sendToServer(new PlayerRequestedTeleportPacket(machinePos, roomCode));
    }

    @Override
    public void tick() {
        super.tick();
        psdButton.active = checkForShrinkingDevice();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {

        final var keyCode = event.key();
        final float rotateSpeed = 1 / 12f;

        if (roomPreviewEnabled) {
            if (keyCode == InputConstants.KEY_R) {
//                renderer.camera().resetLook();
//                renderer.recalculateTranslucency();
                return true;
            }

            if (keyCode == InputConstants.KEY_UP) {
//                renderer.camera().lookUp(rotateSpeed);
//                renderer.recalculateTranslucency();
                return true;
            }

            if (keyCode == InputConstants.KEY_DOWN) {
//                renderer.camera().lookDown(rotateSpeed);
//                renderer.recalculateTranslucency();
                return true;
            }

            if (keyCode == InputConstants.KEY_LEFT) {
//                renderer.camera().lookLeft(rotateSpeed);
//                renderer.recalculateTranslucency();
                return true;
            }

            if (keyCode == InputConstants.KEY_RIGHT) {
//                renderer.camera().lookRight(rotateSpeed);
//                renderer.recalculateTranslucency();
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
//        if (roomPreviewEnabled && renderer != null) {
//            var camPosition = this.renderer.camera().getPosition();
//
//            // Only allow zooming up to 2 blocks from center
//            if (scrollY > 0 && camPosition.distanceTo(Vec3.ZERO) >= 2)
//                this.renderer.zoom(scrollY);
//
//            // Zoom out up to 50 blocks away from center
//            if (scrollY < 0 && camPosition.distanceTo(Vec3.ZERO) <= 100)
//                this.renderer.zoom(scrollY);
//
//            return true;
//        }

        return false;
    }

    private static boolean checkForShrinkingDevice() {
        final var player = Minecraft.getInstance().player;
        if (player == null)
            return false;

        if (player.isCreative()) return true;

        final var hasPsdInInv = player.getInventory()
                .contains(slotItem -> slotItem.has(Shrinking.DataComponents.SHRINKING_CONFIG));

        return hasPsdInInv;

//        if (ModList.get().isLoaded("curios")) {
//            return CuriosCompat.hasPsdCurio(player);
//        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(screenArea.left() - 1, screenArea.top() - 1,
                screenArea.right() + 1, screenArea.bottom() + 1,
                ARGB.color(180, CommonColors.WHITE));

        guiGraphics.fill(screenArea.left(), screenArea.top(),
                screenArea.right(), screenArea.bottom(),
                ARGB.color(250, 8, 90, 120));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(graphics, mouseX, mouseY, partialTick);

        {
            var rt = Component.literal(roomCode);
            graphics.centeredText(font, rt, this.width / 2,
                    screenArea.top() - font.lineHeight - 2, 0xFFDEDEDE);
        }

        final var previewLoadingId = CompactMachinesCore.dotPrefix("preview.loading");
        final var previewDisabledId = CompactMachinesCore.dotPrefix("preview.disabled");

        final var previewLoading = Component
                .translatableWithFallback(previewLoadingId, "Loading room preview...");

        final var previewDisabled = Component.translatableWithFallback(previewDisabledId, "Room Preview Disabled");

        // Render loading
        var previewState = CommonComponents.EMPTY;
        if (roomPreviewEnabled && isLoadingRoomPreview)
            previewState = previewLoading;

        if (!roomPreviewEnabled)
            previewState = previewDisabled;

        if(!CommonComponents.EMPTY.equals(previewState))
            graphics.centeredText(font, previewState,
                    this.width / 2,
                    (height / 2) - (font.lineHeight / 2), 0xFFDEDEDE);

        for (Renderable renderable : this.renderables) {
            renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
    }

//    public void updateSceneRenderer(CompletableFuture<BakedLevel> future) {
//        this.isLoadingRoomPreview = true;
//        future.thenAcceptAsync(this::updateScene);
//    }
//
//    public void updateScene(BakedLevel bakedLevel) {
//        if (this.renderer != null) {
//            renderables.remove(renderer);
//        }
//
//        this.renderer = addRenderableOnly(new SpatialRenderer(bakedLevel, screenArea.left(), screenArea.top(),
//                screenArea.width(), screenArea.height()));
//
//        this.renderSize = bakedLevel.blockBoundaries();
//
//        renderer.camera().zoom(calculateZoomForRoom(this.renderSize));
//        renderer.camera().lookUp(3 / 12f);
//
//        this.isLoadingRoomPreview = false;
//    }

    private static float calculateZoomForRoom(AABB internalSize) {
        boolean tallRoom = Math.max(internalSize.getXsize(), internalSize.getZsize()) < internalSize.getYsize();
        boolean sidesEqual = internalSize.getXsize() == internalSize.getZsize();
        boolean isCube = sidesEqual && internalSize.getZsize() == internalSize.getYsize();

        // All sides equal, simple zoom algo
        if (isCube) {
            return -1.0f * (float) Math.sqrt(Math.pow(internalSize.getXsize(), 2) * 3);
        }

        if (sidesEqual) {
            final var cSquared = Math.sqrt(
                    (Math.pow(internalSize.getXsize(), 2) * 2) +
                            Math.pow(internalSize.getYsize(), 2)
            );

            return (float) (-1.0f * cSquared);
        }

        final var cSquared = Math.sqrt(
                Math.pow(internalSize.getXsize(), 2) +
                        Math.pow(internalSize.getYsize(), 2) +
                        Math.pow(internalSize.getZsize(), 2)
        );

        return (float) (-1.0f * cSquared);
    }
}
