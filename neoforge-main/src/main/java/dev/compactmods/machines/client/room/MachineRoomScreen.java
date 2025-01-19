package dev.compactmods.machines.client.room;

import dev.compactmods.gander.render.geometry.BakedLevel;
import dev.compactmods.gander.ui.widget.SpatialRenderer;
import dev.compactmods.machines.CommonConfig;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.client.widget.ImageButtonBuilder;
import dev.compactmods.machines.compat.curios.CuriosCompat;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.network.room.PlayerRequestedTeleportPacket;
import dev.compactmods.machines.network.room.PlayerRequestedUpgradeUIPacket;
import dev.compactmods.machines.network.room.PlayerStartedRoomTrackingPacket;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

public class MachineRoomScreen extends Screen {

    private final GlobalPos machinePos;
    private final String roomCode;

    private SpatialRenderer renderer;

    private ImageButton psdButton;
    private ScreenRectangle screenArea;

    public MachineRoomScreen(Component title, GlobalPos machinePos, String roomCode) {
        super(title);
        this.machinePos = machinePos;
        this.roomCode = roomCode;

        // Send packet to server for block data
        PacketDistributor.sendToServer(new PlayerStartedRoomTrackingPacket(roomCode));
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
                260, 240);

        this.psdButton = ImageButtonBuilder.button(psdBtnSprites)
                .size(12, 12)
                .location(screenArea.right() - 12, screenArea.bottom() + 12)
                .onPress(btn -> {
                    PacketDistributor.sendToServer(new PlayerRequestedTeleportPacket(machinePos, roomCode));
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

    private static boolean checkForShrinkingDevice() {
        final var player = Minecraft.getInstance().player;
        if(player == null)
            return false;

        if(player.isCreative()) return true;

        final var hasPsdInInv = player.getInventory()
                .contains(slotItem -> slotItem.has(Shrinking.DataComponents.SHRINKING_CONFIG) || slotItem.is(PSDTags.ITEM));

        if(hasPsdInInv)
            return true;

        if(ModList.get().isLoaded("curios")) {
            return CuriosCompat.hasPsdCurio(player);
        }

        return false;
    }

    private void roomUpgradesButton() {
        if(this.minecraft == null || this.minecraft.getConnection() == null) return;
        if(CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(minecraft.getConnection().enabledFeatures()))
        {
            final var upgradeBtnSprites = new WidgetSprites(
                    CompactMachines.modRL("upgrade_btn"),
                    CompactMachines.modRL("upgrade_btn")
            );

            var upgradeScreenBtn = ImageButtonBuilder.button(upgradeBtnSprites)
                    .size(12, 12)
                    .location(screenArea.right() - 24, screenArea.bottom() + 12)
                    .onPress(btn -> {
                        PacketDistributor.sendToServer(new PlayerRequestedUpgradeUIPacket(roomCode, false));
                    }).build();

            addRenderableWidget(upgradeScreenBtn);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // TODO: Re-enable once Gander scissors itself properly
//        guiGraphics.fill(screenArea.left(), screenArea.top(), screenArea.right(), screenArea.bottom(),
//                FastColor.ARGB32.color(180, CommonColors.WHITE));
//
//        guiGraphics.fill(screenArea.left() + 2, screenArea.top() + 2,
//                screenArea.right() - 2, screenArea.bottom() - 2,
//                FastColor.ARGB32.color(255, CommonColors.BLACK));
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);

        final var pose = graphics.pose();
        pose.pushPose();
        {
            pose.translate(this.width / 2f, 0, 0);

            // graphics.drawCenteredString(font, this.ti, 0, this.titleLabelY, 0xFFFFFFFF);

            var rt = Component.literal(roomCode);
            pose.scale(0.7f, 0.7f, 0.7f);
            graphics.drawCenteredString(font, rt, 0, font.lineHeight + 7, 0xFFDEDEDE);
        }
        pose.popPose();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    public void updateScene(BakedLevel bakedLevel) {
        if(this.renderer != null) {
            this.renderer.dispose();
            renderables.remove(renderer);
        }

        this.renderer = addRenderableOnly(new SpatialRenderer(bakedLevel));
        renderer.camera().zoom(calculateZoomForRoom(AABB.of(bakedLevel.blockBoundaries())));
        renderer.camera().lookUp(3 / 12f);
    }

    private static float calculateZoomForRoom(AABB internalSize) {
        double maxSize = Math.max(internalSize.getXsize(), internalSize.getZsize());
        return (float) (-1.0f * maxSize) - 10;
    }
}
