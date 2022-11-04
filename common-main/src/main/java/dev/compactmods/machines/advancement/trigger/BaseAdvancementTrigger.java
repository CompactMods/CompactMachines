package dev.compactmods.machines.advancement.trigger;

import dev.compactmods.machines.advancement.GenericAdvancementTriggerListenerList;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public abstract class BaseAdvancementTrigger<T extends AbstractCriterionTriggerInstance> implements CriterionTrigger<T> {

    private final GenericAdvancementTriggerListenerList<T> listeners = new GenericAdvancementTriggerListenerList<>();

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<T> list) {
        listeners.addPlayerListener(advancements, list);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<T> list) {
        listeners.removePlayerListener(advancements, list);
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements advancements) {
        listeners.removePlayerListeners(advancements);
    }

    public void trigger(ServerPlayer player) {
        final var listeners = this.listeners.getListeners(player);
        if(listeners != null)
            listeners.trigger();
    }
}
