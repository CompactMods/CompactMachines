package org.dave.compactmachines3.command;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.network.MessageClipboard;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;

import java.util.HashMap;
import java.util.List;

public class CommandRecipeCopyShape extends CommandBaseExt {
    @Override
    public String getName() {
        return "copy-shape";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return creative || isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)sender.getCommandSenderEntity();

        Vec3d vec3d = player.getPositionEyes(1.0F);
        Vec3d vec3d1 = player.getLook(1.0F);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * 32.0d, vec3d1.y * 32.0d, vec3d1.z * 32.0d);

        RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d2);
        if(result.typeOfHit != RayTraceResult.Type.BLOCK) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-shape.exception.not_looking_at_projector"));
            return;
        }

        IBlockState blockState = sender.getEntityWorld().getBlockState(result.getBlockPos());
        if(blockState.getBlock() != Blockss.fieldProjector) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-shape.exception.not_looking_at_projector"));
            return;
        }

        if(!(sender.getEntityWorld().getTileEntity(result.getBlockPos()) instanceof TileEntityFieldProjector)) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-shape.exception.not_looking_at_projector"));
            return;
        }

        TileEntityFieldProjector teProjector = (TileEntityFieldProjector) sender.getEntityWorld().getTileEntity(result.getBlockPos());
        List<BlockPos> insideBlocks = teProjector.getInsideBlocks();

        // Normalize the crafting area to x=0, y=0, z=0. For that we first
        // calculate the "lowest" corner so we can use that to calculate
        // the relative position of the block
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for(BlockPos pos : insideBlocks) {
            if(pos.getX() < minX)minX = pos.getX();
            if(pos.getY() < minY)minY = pos.getY();
            if(pos.getZ() < minZ)minZ = pos.getZ();
            if(pos.getX() > maxX)maxX = pos.getX();
            if(pos.getY() > maxY)maxY = pos.getY();
            if(pos.getZ() > maxZ)maxZ = pos.getZ();
        }

        int width = maxX-minX+1;
        int height = maxY-minY+1;
        int depth = maxZ-minZ+1;
        String[][][] array = new String[width][height][depth];

        String typesOutput = "\"input-types\": {\n";

        char nextRef = 'a';
        HashMap<String, String> refMap = new HashMap<>();
        for(BlockPos pos : insideBlocks) {
            IBlockState state = sender.getEntityWorld().getBlockState(pos);
            String blockName = state.getBlock().getRegistryName().toString();
            int meta = state.getBlock().getMetaFromState(state);
            String fullName = blockName + ":" + meta;

            String refName;
            if(refMap.containsKey(fullName)) {
                refName = refMap.get(fullName);
            } else {
                refName = "" + nextRef++;
                refMap.put(fullName, refName);

                typesOutput += String.format("  \"%s\": { \"id\": \"%s\", \"meta\": %d },\n", refName, blockName, meta);
            }

            BlockPos relative = pos.add(-minX,-minY,-minZ);
            array[relative.getX()][relative.getY()][relative.getZ()] = refName;
        }
        typesOutput = typesOutput.substring(0,typesOutput.length()-2) + "\n";
        typesOutput += "},\n";

        String shapeOutput = "\"shape\": [\n";
        for(int y = height-1; y >= 0; y--) {
            shapeOutput += "  [\n";
            for(int z = 0; z < depth; z++) {
                shapeOutput += "    [";
                for(int x = 0; x < width; x++) {
                    if(array[x][y][z] == null) {
                        shapeOutput += "\"_\"";
                    } else {
                        shapeOutput += "\"" + array[x][y][z] + "\"";
                    }
                    if(x != width-1)shapeOutput += ",";
                }
                shapeOutput += "]";
                if(z != depth-1)shapeOutput += ",";
                shapeOutput += "\n";
            }
            shapeOutput += "  ]";
            if(y > 0)shapeOutput += ",";
            shapeOutput += "\n";
        }
        shapeOutput += "]\n";

        MessageClipboard message = new MessageClipboard();
        message.setClipboardContent(typesOutput + shapeOutput);
        PackageHandler.instance.sendTo(message, (EntityPlayerMP) sender.getCommandSenderEntity());

        player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-shape.success"));
    }
}
