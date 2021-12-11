package dev.compactmods.machines.advancement.trigger;

import com.google.gson.JsonObject;
import dev.compactmods.machines.advancement.GenericAdvancementTriggerListener;
import dev.compactmods.machines.advancement.GenericAdvancementTriggerListenerList;
import dev.compactmods.machines.api.core.Advancements;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.advancements.CriterionTrigger.Listener;

public class HowDidYouGetHereTrigger implements CriterionTrigger<HowDidYouGetHereTrigger.Instance> {

    private final GenericAdvancementTriggerListenerList<Instance> listeners = new GenericAdvancementTriggerListenerList<>();

    @Override
    public ResourceLocation getId() {
        return Advancements.HOW_DID_YOU_GET_HERE;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<Instance> list) {
        listeners.addPlayerListener(advancements, list);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<Instance> list) {
        listeners.removePlayerListener(advancements, list);
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements advancements) {
        listeners.removePlayerListeners(advancements);
    }

    @Override
    public Instance createInstance(JsonObject json, DeserializationContext conditions) {
        return new Instance(EntityPredicate.Composite.fromJson(json, "player", conditions));
    }

    public void trigger(ServerPlayer player) {
        final GenericAdvancementTriggerListener<Instance> listeners = this.listeners.getListeners(player);
        if(listeners != null)
            listeners.trigger();
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(EntityPredicate.Composite player) {
            super(Advancements.HOW_DID_YOU_GET_HERE, player);
        }

        public static Instance create() {
            return new Instance(EntityPredicate.Composite.ANY);
        }
    }
}
