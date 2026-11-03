package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.upgrades.storage.nodes.StorageNodes;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeNode;
import dev.robotgryphon.screenlib.nodes.api.appearance.NodeAppearance;
import dev.robotgryphon.screenlib.nodes.api.appearance.NodeAppearanceProvider;
import dev.compactmods.machines.datagen.debug.DebugNodesDatapack;
import dev.compactmods.machines.upgrades.client.StorageRenderers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/** How the room upgrade canvas's nodes look. Storages draw with {@link StorageRenderers#STORAGE}, which also handles the debug storages. */
public class NodeAppearanceGenerator extends NodeAppearanceProvider {
    public NodeAppearanceGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CompactMachinesCore.MOD_ID);
    }

    @Override
    protected void gather() {
        this.add(TreeCutterUpgradeNode.KEY, NodeAppearance.builder()
                .property(TreeCutterUpgradeNode.TOOLS.name(), p -> p.withRenderer(StorageRenderers.STORAGE))
                .property(TreeCutterUpgradeNode.DROPS.name(), p -> p.withRenderer(StorageRenderers.STORAGE))
                .build());

        this.add(StorageNodes.NAMED_ITEM_STORAGE, NodeAppearance.builder()
                .property(StorageNodes.HANDLER, p -> p.withRenderer(StorageRenderers.STORAGE))
                .build());

        // Debug nodes (dev-only datapack); harmless without it
        this.add(DebugNodesDatapack.INFINITE_ITEMS, NodeAppearance.builder()
                .property("storage", p -> p.withRenderer(StorageRenderers.STORAGE))
                .build());
        this.add(DebugNodesDatapack.VOID_ITEMS, NodeAppearance.builder()
                .property("storage", p -> p.withRenderer(StorageRenderers.STORAGE))
                .build());
    }
}
