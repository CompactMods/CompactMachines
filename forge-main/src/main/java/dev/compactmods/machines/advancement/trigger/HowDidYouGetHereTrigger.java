package dev.compactmods.machines.advancement.trigger;

import com.google.gson.JsonObject;
import dev.compactmods.machines.api.Advancements;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

public class HowDidYouGetHereTrigger extends BaseAdvancementTrigger<HowDidYouGetHereTrigger.Instance> {

    @Override
    public ResourceLocation getId() {
        return Advancements.HOW_DID_YOU_GET_HERE;
    }

    @Override
    public Instance createInstance(JsonObject json, DeserializationContext conditions) {
        return new Instance(EntityPredicate.Composite.fromJson(json, "player", conditions));
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
