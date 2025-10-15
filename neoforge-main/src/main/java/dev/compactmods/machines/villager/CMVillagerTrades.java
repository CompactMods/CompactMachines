package dev.compactmods.machines.villager;

import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class CMVillagerTrades {

    public static void addSpatialTinkererTrades(final VillagerTradesEvent trades) {
        if (!trades.getType().location().equals(Villagers.TINKERER_ID))
            return;

        final var tradeList = trades.getTrades();
        final var tradeReg = trades.getRegistries().lookupOrThrow(Villagers.TRADES.getRegistryKey());

        tradeReg.listElements().forEach(trade -> {
            final var t = trade.value();
            tradeList.get(1).add(t);
        });
    }
}
