package dev.compactmods.machines.advancement.trigger;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

public class BasicPlayerAdvTrigger extends BaseAdvancementTrigger<BasicPlayerAdvTrigger.Instance> {

    private final ResourceLocation advancementId;

    public BasicPlayerAdvTrigger(ResourceLocation advancementId) {
        this.advancementId = advancementId;
    }

    @Override
    public ResourceLocation getId() {
        return advancementId;
    }

    @Override
    public Instance createInstance(JsonObject json, DeserializationContext conditions) {
        return new Instance(this.advancementId, EntityPredicate.Composite.fromJson(json, "player", conditions));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ResourceLocation advId, EntityPredicate.Composite player) {
            super(advId, player);
        }

        public static Instance create(ResourceLocation advancement) {
            return new Instance(advancement, EntityPredicate.Composite.ANY);
        }
    }
}
