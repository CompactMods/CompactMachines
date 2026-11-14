package dev.compactmods.machines.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * Snapshot taken from a {@link dev.compactmods.machines.machine.block.CompactMachineBlockEntity}
 * each frame by {@link CompactMachineRenderer}. Holds only what the submit pass needs to draw
 * the optional shader overlay — keeps the BER deferral pipeline cheap.
 */
public class MachineRenderState extends BlockEntityRenderState {

    /** Smoothed game-time in ticks ({@code gameTime + partialTick}). Drives shader animation. */
    public float gameTime;

    /**
     * Snapshot of the core item currently held by the block entity. Re-used
     * across frames (cleared in extract when the slot is empty). The submit
     * pass floats and spins it at the centre of the block, similar in spirit
     * to the enchantment table's book.
     */
    public final ItemStackRenderState coreItem = new ItemStackRenderState();

    /** {@code true} when {@link #coreItem} has at least one layer to draw. */
    public boolean hasCoreItem;
}
