package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.attachment.IForwardingAttachmentHolder;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record RoomUpgradeInstance(RoomInstance roomInstance, UUID upgradeID, ItemStack upgradeItem) implements IForwardingAttachmentHolder {

    @Override
    public Supplier<IAttachmentHolder> attachmentHolder() {
        return () -> roomInstance.getCapability(RoomCapabilities.UPGRADE_DATA_ATTACHMENTS, upgradeID);
    }

    public List<RoomUpgradeComponent> upgradeComponents() {
        final var list = upgradeItem.get(CMDataComponents.UPGRADE_LIST_COMPONENT);
        if(list != null) return list.components();
        return Collections.emptyList();
    }
}
