package dev.compactmods.machines.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.core.CompactMachinesNet;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.client.MachineRoomScreen;
import dev.compactmods.machines.room.network.PlayerRequestedTeleportPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class PSDIconButton extends ExtendedButton {
    private final MachineRoomScreen parent;

    public PSDIconButton(MachineRoomScreen parent, int xPos, int yPos) {
        super(xPos, yPos, 20, 22, Component.empty(), PSDIconButton::onClicked);
        this.active = false;
        this.parent = parent;
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.render(pose, mouseX, mouseY, partialTicks);

        this.parent.getMinecraft().getItemRenderer().renderAndDecorateItem(
                new ItemStack(Registration.PERSONAL_SHRINKING_DEVICE.get()),
                x + 2, y + 2, 40);
    }

    private static void onClicked(Button button) {
        if (button instanceof PSDIconButton psd && button.active) {
            var menu = psd.parent.getMenu();
            var mach = psd.parent.getMachine();
            var room = menu.getRoom();
            CompactMachinesNet.CHANNEL.sendToServer(new PlayerRequestedTeleportPacket(mach, room));
        }
    }

    public void setEnabled(boolean has) {
        this.active = has;
    }
}
