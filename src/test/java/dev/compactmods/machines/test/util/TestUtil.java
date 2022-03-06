package dev.compactmods.machines.test.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TestUtil {

    public static void loadStructureIntoTestArea(GameTestHelper test, ResourceLocation structure, BlockPos relLocation) {
        final var structures = test.getLevel().getStructureManager();
        final var template = structures.get(structure);
        if(template.isEmpty())
            return;

        var placeAt = test.absolutePos(relLocation);
        template.get().placeInWorld(
                test.getLevel(),
                placeAt,
                placeAt,
                new StructurePlaceSettings(),
                test.getLevel().getRandom(),
                Block.UPDATE_ALL);
    }

    public static void useHeldItemOnBlockAt(ServerLevel level, Player player, InteractionHand hand, BlockPos position, Direction side) {
        final var hitResult = new BlockHitResult(Vec3.atCenterOf(position), side, position, true);

        var item = player.getItemInHand(hand);

        // public UseOnContext(Level p_43713_, @Nullable Player p_43714_, InteractionHand p_43715_, ItemStack p_43716_, BlockHitResult p_43717_);
        var ctx = new UseOnContext(level, player, hand, item, hitResult);

        item.useOn(ctx);
    }

    public static void useItemOnBlockAt(GameTestHelper test, Player player, BlockPos position) {
        BlockState blockstate = test.getBlockState(position);
        final var worldPosition = test.absolutePos(position);

        blockstate.use(test.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(worldPosition), Direction.NORTH, worldPosition, true));
    }

}
