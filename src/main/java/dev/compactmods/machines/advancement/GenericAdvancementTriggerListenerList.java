package dev.compactmods.machines.advancement;

import javax.annotation.Nullable;
import java.util.Map;
import com.google.common.collect.Maps;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;

public class GenericAdvancementTriggerListenerList<T extends ICriterionInstance> {
    private final Map<PlayerAdvancements, GenericAdvancementTriggerListener<T>> listeners = Maps.newHashMap();


    public void addPlayerListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<T> listener) {
        GenericAdvancementTriggerListener<T> listeners = this.listeners.computeIfAbsent(advancements, GenericAdvancementTriggerListener::new);
        listeners.add(listener);
    }

    public void removePlayerListener(PlayerAdvancements advancements, ICriterionTrigger.Listener<T> listener) {
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
    public GenericAdvancementTriggerListener<T> getListeners(ServerPlayerEntity player) {
        return listeners.get(player.getAdvancements());
    }
}
