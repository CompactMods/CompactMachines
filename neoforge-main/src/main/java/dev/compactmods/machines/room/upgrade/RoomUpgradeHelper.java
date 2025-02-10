package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.util.function.Predicate;

public class RoomUpgradeHelper {
    public static void cleanDeadUpgrades(RoomInstance roomInstance) {

        final var upgradeInv = roomInstance.getData(CMDataAttachments.UPGRADE_ITEMS);
        final var upgradeReg = roomInstance.getCapability(RoomCapabilities.UPGRADES);

        if (upgradeInv.items().allMatch(ItemStack::isEmpty)) {
            upgradeReg.clearCache();
            return;
        }

        // Check upgrades
        final var upgradeItems = upgradeInv.items()
                .filter(Predicate.not(ItemStack::isEmpty))
                .toList();
    }

    public static void verifyChunkloaderUpgrades(ServerLevel serverLevel, TicketHelper ticketHelper) {
        final var manager = CompactMachines.upgradeManager();
        if(manager == null) return;

        // Clean all tickets - upgrade system will re-register
        final var tickets = ticketHelper.getEntityTickets();
        tickets.keySet().forEach(ticketHelper::removeAllTickets);
    }
}
