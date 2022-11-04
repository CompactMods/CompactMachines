package dev.compactmods.machines.client.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

public class EmptyLevelEntityGetter implements LevelEntityGetter<Entity> {
    @Nullable
    @Override
    public Entity get(int p_156931_) {
        return null;
    }

    @Nullable
    @Override
    public Entity get(UUID p_156939_) {
        return null;
    }

    @Override
    public Iterable<Entity> getAll() {
        return Collections.emptySet();
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156935_, Consumer<U> p_156936_) {

    }

    @Override
    public void get(AABB p_156937_, Consumer<Entity> p_156938_) {

    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156932_, AABB p_156933_, Consumer<U> p_156934_) {

    }
}
