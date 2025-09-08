package dev.compactmods.machines.test.gametest.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CompactGameTestHelper extends GameTestHelper {

    public CompactGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    public AABB localBounds() {
        var bounds = getBounds();
        return bounds.move(BlockPos.ZERO.subtract(absolutePos(BlockPos.ZERO)));
    }

    public void loadStructureIntoTestArea(ResourceLocation structure, BlockPos relLocation) {
        final var structures = this.getLevel().getStructureManager();
        final var template = structures.get(structure);
        if(template.isEmpty())
            return;

        var placeAt = absolutePos(relLocation);
        template.get().placeInWorld(
                getLevel(),
                placeAt,
                placeAt,
                new StructurePlaceSettings(),
                getLevel().getRandom(),
                Block.UPDATE_ALL);
    }

    public void useHeldItemOnBlockAt(ServerLevel level, Player player, InteractionHand hand, BlockPos position, Direction side) {
        final var hitResult = new BlockHitResult(Vec3.atCenterOf(position), side, position, false);

        var item = player.getItemInHand(hand);

        // public UseOnContext(Level p_43713_, @Nullable Player p_43714_, InteractionHand p_43715_, ItemStack p_43716_, BlockHitResult p_43717_);
        var ctx = new UseOnContext(level, player, hand, item, hitResult);

        item.useOn(ctx);
    }

}
