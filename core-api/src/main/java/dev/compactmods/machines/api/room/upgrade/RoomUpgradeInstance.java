package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.attachment.IForwardingAttachmentHolder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.UUID;
import java.util.function.Supplier;

public record RoomUpgradeInstance(RoomInstance roomInstance, UUID upgradeID, ItemStack upgradeItem) implements IForwardingAttachmentHolder {

    @Override
    public Supplier<IAttachmentHolder> attachmentHolder() {
        return () -> roomInstance.getCapability(RoomCapabilities.UPGRADE_DATA_ATTACHMENTS, upgradeID);
    }
}
