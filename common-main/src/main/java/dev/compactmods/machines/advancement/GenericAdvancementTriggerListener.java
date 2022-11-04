package dev.compactmods.machines.advancement;

import java.util.Set;
import com.google.common.collect.Sets;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;

public class GenericAdvancementTriggerListener<T extends CriterionTriggerInstance> {

    private final PlayerAdvancements advancements;
    private final Set<CriterionTrigger.Listener<T>> listeners = Sets.newHashSet();

    public GenericAdvancementTriggerListener(PlayerAdvancements advancements) {
        this.advancements = advancements;
    }

    public void add(CriterionTrigger.Listener<T> listener) {
        listeners.add(listener);
    }

    public void remove(CriterionTrigger.Listener<T> listener) {
        listeners.remove(listener);
    }

    public boolean empty() {
        return listeners.isEmpty();
    }

    public void trigger() {
        listeners.forEach(a -> a.run(advancements));
    }
}
