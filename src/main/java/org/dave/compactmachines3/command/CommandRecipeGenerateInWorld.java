package org.dave.compactmachines3.command;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import org.dave.compactmachines3.block.BlockFieldProjector;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRecipeGenerateInWorld extends CommandBaseExt {
    @Override
    public String getName() {
        return "generate";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)sender.getCommandSenderEntity();

        if(args.length != 1) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.generate.exception.missing_machine_recipe"));
            return;
        }

        MultiblockRecipe recipe = MultiblockRecipes.getRecipeByName(args[0]);
        if(recipe == null) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.generate.exception.unknown_recipe"));
            return;
        }

        Vec3d vec3d = player.getPositionEyes(1.0F);
        Vec3d vec3d1 = player.getLook(1.0F);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * 32.0d, vec3d1.y * 32.0d, vec3d1.z * 32.0d);

        RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d2);
        if(result.typeOfHit != RayTraceResult.Type.BLOCK) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.generate.exception.not_looking_at_block"));
            return;
        }

        BlockPos basePos = result.getBlockPos().offset(result.sideHit, 1);

        EnumFacing dir = BlockFieldProjector.getFacingFromEntity(basePos, player);
        EnumFacing zDir = dir;
        EnumFacing yDir = EnumFacing.UP;
        EnumFacing xDir = dir.rotateY();
        for(int x = 0; x < recipe.getWidth(); x++) {
            for(int y = 0; y < recipe.getHeight(); y++) {
                for(int z = 0; z < recipe.getDepth(); z++) {
                    BlockPos livePos = basePos.offset(xDir, x);
                    livePos = livePos.offset(yDir, y);
                    livePos = livePos.offset(zDir, z);

                    IBlockState state = recipe.getStateAtBlockPos(new BlockPos(x, y, z));
                    player.world.setBlockState(livePos, state, 2);
                }
            }
        }


    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> allRecipes = MultiblockRecipes.getRecipes().stream()
                .map(multiblockRecipe -> multiblockRecipe.getName())
                .filter(s -> args.length == 0 || s.startsWith(args[0]))
                .collect(Collectors.toList());

        return allRecipes;
    }
}
