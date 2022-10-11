package dev.compactmods.machines.advancement;

import javax.annotation.Nullable;
import java.util.Map;
import com.google.common.collect.Maps;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class GenericAdvancementTriggerListenerList<T extends CriterionTriggerInstance> {
    private final Map<PlayerAdvancements, GenericAdvancementTriggerListener<T>> listeners = Maps.newHashMap();


    public void addPlayerListener(PlayerAdvancements advancements, CriterionTrigger.Listener<T> listener) {
        GenericAdvancementTriggerListener<T> listeners = this.listeners.computeIfAbsent(advancements, GenericAdvancementTriggerListener::new);
        listeners.add(listener);
    }

    public void removePlayerListener(PlayerAdvancements advancements, CriterionTrigger.Listener<T> listener) {
        GenericAdvancementTriggerListener<T> listeners = this.listeners.get(advancements);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.empty()) {
                this.listeners.remove(advancements);
            }
        }
    }

    public void removePlayerListeners(PlayerAdvancements advancements) {
        this.listeners.remove(advancements);
    }

    @Nullable
    public GenericAdvancementTriggerListener<T> getListeners(ServerPlayer player) {
        return listeners.get(player.getAdvancements());
    }
}
