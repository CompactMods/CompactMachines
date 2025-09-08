package dev.compactmods.machines.room.ui.upgrades;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.render.ConditionalGhostSlot;
import dev.compactmods.machines.client.render.NineSliceRenderer;
import dev.compactmods.machines.client.widget.ImageButtonBuilder;
import dev.compactmods.machines.network.room.PlayerRequestedRoomUIPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoomUpgradeScreen extends AbstractContainerScreen<RoomUpgradeMenu> {
    private final Inventory inventory;

    private static ResourceLocation CONTAINER_BACKGROUND = CompactMachines.modRL("textures/gui/psd_screen_9slice.png");

    WidgetSprites BACK_BTN_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("recipe_book/page_backward"),
        ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted")
    );

    private final NineSliceRenderer backgroundRenderer;

    public RoomUpgradeScreen(RoomUpgradeMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.inventory = playerInv;
        this.titleLabelY = 6;
        this.inventoryLabelY = 26 + 32;
        this.imageHeight = 114 + 18 + 20;

        this.backgroundRenderer = NineSliceRenderer.builder(CompactMachines.modRL("textures/gui/psd_screen_9slice.png"))
                .area(0, 0, imageWidth, imageHeight)
                .uv(32, 32)
                .sliceSize(4, 4)
                .textureSize(32, 32)
                .build();
    }

    @Override
    protected void init() {
        super.init();

        if(menu.showBackButton) {
            var backButton = ImageButtonBuilder.button(BACK_BTN_SPRITES)
                .location(leftPos - 12, topPos + 2)
                .size(8, 12)
                .message(Component.literal("Close"))
                .onPress(button -> {
                    ClientPacketDistributor.sendToServer(new PlayerRequestedRoomUIPacket(menu.roomCode));
                })
                .build();

            addRenderableWidget(backButton);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        final int white = DyeColor.WHITE.getTextColor();
        pGuiGraphics.drawString(this.font, Component.literal("Room Upgrades"), this.titleLabelX, this.titleLabelY, white, false);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, white, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        final var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);
        backgroundRenderer.render(graphics);
        pose.popMatrix();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);
        for(var i : this.menu.slots) {
            graphics.fill(i.x, i.y, i.x + 16, i.y + 16, 0x0F000000);
        }
        pose.popMatrix();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(graphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderSlotContents(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot, @Nullable String countString) {
        if (slot instanceof ConditionalGhostSlot cgs && cgs.matched(itemstack)) {
            renderGhostSlot(guiGraphics, itemstack, slot, countString);
            return;
        }

        super.renderSlotContents(guiGraphics, itemstack, slot, countString);
    }

    private void renderGhostSlot(@NotNull GuiGraphics graphics, @NotNull ItemStack itemstack, @NotNull Slot slot, @Nullable String countString) {
        graphics.renderItem(slot.getItem(), slot.x, slot.y);
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ARGB.color(150, 30, 70, 210));

        if (!itemstack.isEmpty()) {
            if (itemstack.getCount() != 1 || countString != null) {
                String s = countString == null ? String.valueOf(itemstack.getCount()) : countString;
                graphics.drawString(this.font, s, slot.x + 19 - 2 - this.font.width(s), slot.y + 6 + 3,
                        ARGB.color(120, 255, 255, 255), false);
            }
        }
    }
}
