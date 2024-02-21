package dev.compactmods.machines.neoforge.room.ui;

import dev.compactmods.machines.neoforge.network.PlayerRequestedTeleportPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;

public class PSDIconButton extends ExtendedButton {
    private final MachineRoomScreen parent;

    public PSDIconButton(MachineRoomScreen parent, int xPos, int yPos) {
        super(xPos, yPos, 20, 22, Component.empty(), PSDIconButton::onClicked);
        this.active = false;
        this.parent = parent;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    //    @Override
//    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
//        super.render(pose, mouseX, mouseY, partialTicks);
//
////        this.parent.getMinecraft().getItemRenderer().renderStatic(
////                new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()),
////                x + 2, y + 2, 40);
//    }

    private static void onClicked(Button button) {
        if (button instanceof PSDIconButton psd && button.active) {
            var menu = psd.parent.getMenu();
            var mach = psd.parent.getMachine();
            var room = menu.getRoom();
            PacketDistributor.SERVER.noArg().send(new PlayerRequestedTeleportPacket(mach, room));
        }
    }

    public void setEnabled(boolean has) {
        this.active = has;
    }
}
