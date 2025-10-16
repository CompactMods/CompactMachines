package dev.compactmods.machines.client.room;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.config.ClientConfig;
import dev.compactmods.machines.client.widget.ImageButtonBuilder;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.network.room.PlayerRequestedTeleportPacket;
import dev.compactmods.machines.network.room.PlayerRequestedUpgradeUIPacket;
import dev.compactmods.machines.network.room.PlayerStartedRoomTrackingPacket;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class MachineRoomScreen extends Screen {

    private final GlobalPos machinePos;
    private final String roomCode;

//    private SpatialRenderer renderer;
    private AABB renderSize;

    private ImageButton psdButton;
    private ScreenRectangle screenArea;

    private boolean isLoadingRoomPreview;
    private boolean roomPreviewEnabled = true;

    public MachineRoomScreen(Component title, GlobalPos machinePos, String roomCode) {
        super(title);
        this.machinePos = machinePos;
        this.roomCode = roomCode;

        if(ClientConfig.ENABLE_ROOM_PREVIEWS.get()) {
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

        final var psdBtnSprites = new WidgetSprites(
                CompactMachines.modRL("personal_shrinking_device"),
                CompactMachines.modRL("personal_shrinking_device_disabled"),
                CompactMachines.modRL("personal_shrinking_device_highlighted"),
                CompactMachines.modRL("personal_shrinking_device_disabled"));

        this.screenArea = new ScreenRectangle((width / 2) - 130, (height / 2) - 120,
                260, 260);

        this.psdButton = ImageButtonBuilder.button(psdBtnSprites)
                .size(12, 12)
                .location(screenArea.right() - 12, screenArea.bottom() + 2)
                .onPress(btn -> {
                    ClientPacketDistributor.sendToServer(new PlayerRequestedTeleportPacket(machinePos, roomCode));
                }).build();

        addRenderableWidget(psdButton);

        // EXPERIMENTAL: Room Upgrades
        roomUpgradesButton();
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

        if(roomPreviewEnabled) {
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

        if (hasPsdInInv)
            return true;

//        if (ModList.get().isLoaded("curios")) {
//            return CuriosCompat.hasPsdCurio(player);
//        }

        return false;
    }

    private void roomUpgradesButton() {
        if (this.minecraft == null || this.minecraft.getConnection() == null) return;
        if (CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(minecraft.getConnection().enabledFeatures())) {
            final var upgradeBtnSprites = new WidgetSprites(
                    CompactMachines.modRL("upgrade_btn"),
                    CompactMachines.modRL("upgrade_btn")
            );

            var upgradeScreenBtn = ImageButtonBuilder.button(upgradeBtnSprites)
                    .size(12, 12)
                    .location(screenArea.right() - 24, screenArea.bottom() + 2)
                    .onPress(btn -> {
                        ClientPacketDistributor.sendToServer(new PlayerRequestedUpgradeUIPacket(roomCode, false));
                    }).build();

            addRenderableWidget(upgradeScreenBtn);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(screenArea.left() - 1, screenArea.top() - 1,
                screenArea.right() + 1, screenArea.bottom() + 1,
                ARGB.color(180, CommonColors.WHITE));

        guiGraphics.fill(screenArea.left(), screenArea.top(),
                screenArea.right(), screenArea.bottom(),
                ARGB.color(250, 8, 90, 120));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        {
            var rt = Component.literal(roomCode);
            graphics.drawCenteredString(font, rt, this.width / 2,
                    screenArea.top() - font.lineHeight - 2, 0xFFDEDEDE);
        }

        // Render loading
        if(roomPreviewEnabled && isLoadingRoomPreview) {
            final var loadingMsg = Component
                    .translatableWithFallback("compactmachines.preview.loading", "Loading room preview...");

            graphics.drawCenteredString(font, loadingMsg,
                    this.width / 2,
                    (height / 2) - (font.lineHeight / 2), 0xFFDEDEDE);
        }

        if(!roomPreviewEnabled) {
            final var loadingMsg = Component.translatableWithFallback("compactmachines.preview.disabled", "Room Preview Disabled");
            graphics.drawCenteredString(font, loadingMsg,
                    this.width / 2,
                    (height / 2) - (font.lineHeight / 2), 0xFFDEDEDE);
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
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
