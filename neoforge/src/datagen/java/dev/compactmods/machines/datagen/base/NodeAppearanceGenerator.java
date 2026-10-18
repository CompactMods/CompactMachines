package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeNode;
import dev.robotgryphon.screenlib.nodes.api.appearance.NodeAppearance;
import dev.robotgryphon.screenlib.nodes.api.appearance.NodeAppearanceProvider;
import dev.robotgryphon.screenlib.nodes.api.render.PropertyValueRenderers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/** How the room upgrade canvas's nodes look (copied from screenlib's test mod). */
public class NodeAppearanceGenerator extends NodeAppearanceProvider {
    public NodeAppearanceGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CompactMachinesCore.MOD_ID);
    }

    @Override
    protected void gather() {
        this.add(TreeCutterUpgradeNode.KEY, NodeAppearance.builder()
                .property(TreeCutterUpgradeNode.TOOLS.name(), p -> p.withRenderer(PropertyValueRenderers.ITEM_HANDLER_BLOCK))
                .property(TreeCutterUpgradeNode.DROPS.name(), p -> p.withRenderer(PropertyValueRenderers.ITEM_HANDLER_BLOCK))
                .build());
    }
}
