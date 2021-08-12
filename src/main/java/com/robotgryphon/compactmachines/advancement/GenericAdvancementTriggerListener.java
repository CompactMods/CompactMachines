package com.robotgryphon.compactmachines.advancement;

import java.util.Set;
import com.google.common.collect.Sets;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;

public class GenericAdvancementTriggerListener<T extends ICriterionInstance> {

    private final PlayerAdvancements advancements;
    private final Set<ICriterionTrigger.Listener<T>> listeners = Sets.newHashSet();

    public GenericAdvancementTriggerListener(PlayerAdvancements advancements) {
        this.advancements = advancements;
    }

    public void add(ICriterionTrigger.Listener<T> listener) {
        listeners.add(listener);
    }

    public void remove(ICriterionTrigger.Listener<T> listener) {
        listeners.remove(listener);
    }

    public boolean empty() {
        return listeners.isEmpty();
    }

    public void trigger() {
        listeners.forEach(a -> a.run(advancements));
    }
}
