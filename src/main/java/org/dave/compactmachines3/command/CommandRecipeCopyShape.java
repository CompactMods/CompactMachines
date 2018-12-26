package org.dave.compactmachines3.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.network.MessageClipboard;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.utility.InheritanceUtil;
import org.dave.compactmachines3.utility.SerializationHelper;
import org.dave.compactmachines3.world.data.provider.AbstractExtraTileDataProvider;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProviderRegistry;

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
        Vec3d vec3d2 = vec3d.add(vec3d1.x * 32.0d, vec3d1.y * 32.0d, vec3d1.z * 32.0d);

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

        JsonObject resultObj = new JsonObject();
        JsonObject inputTypes = new JsonObject();
        JsonObject variants = new JsonObject();
        resultObj.add("input-types", inputTypes);

        // Create the reference map, i.e. which character maps to which block/tile
        char nextRef = 'a';
        HashMap<String, Character> nextVariant = new HashMap<>();

        HashMap<String, String> refMap = new HashMap<>();
        HashMap<String, HashMap<String, String>> variantMap = new HashMap<>();

        for(BlockPos pos : insideBlocks) {
            IBlockState state = sender.getEntityWorld().getBlockState(pos);
            String blockName = state.getBlock().getRegistryName().toString();
            int meta = state.getBlock().getMetaFromState(state);
            TileEntity te = sender.getEntityWorld().getTileEntity(pos);

            String nbtDataString = null;
            if(state.getBlock().hasTileEntity(state) && te != null) {
                NBTTagCompound nbtData = te.writeToNBT(new NBTTagCompound());
                nbtData.removeTag("x");
                nbtData.removeTag("y");
                nbtData.removeTag("z");

                for(AbstractExtraTileDataProvider provider : ExtraTileDataProviderRegistry.getDataProviders(te)) {
                    NBTTagCompound extraData = provider.writeExtraData(te);
                    String tagName = String.format("cm3_extra:%s", provider.getName());
                    nbtData.setTag(tagName, extraData);
                }

                nbtDataString = nbtData.toString();
            }

            String fullName = blockName + ":" + meta;

            String refName;
            if(refMap.containsKey(fullName)) {
                refName = refMap.get(fullName);
            } else {
                refName = "" + nextRef++;
                refMap.put(fullName, refName);

                JsonObject typeDescription = new JsonObject();
                typeDescription.addProperty("id", blockName);
                typeDescription.addProperty("meta", meta);

                inputTypes.add(refName, typeDescription);
            }

            if(!nextVariant.containsKey(refName)) {
                nextVariant.put(refName, 'A');
            }

            String variantName = null;
            if(nbtDataString != null) {
                if(!variantMap.containsKey(refName)) {
                    variantMap.put(refName, new HashMap<>());
                }

                HashMap<String, String> thisRefsVariantMap = variantMap.get(refName);

                if(thisRefsVariantMap.containsKey(nbtDataString)) {
                    variantName = thisRefsVariantMap.get(nbtDataString);
                } else {
                    char variant = nextVariant.get(refName);
                    variantName = "" + variant++;
                    nextVariant.put(refName, variant);

                    thisRefsVariantMap.put(nbtDataString, variantName);

                    JsonObject variantData = new JsonObject();
                    variantData.addProperty("nbt", nbtDataString);

                    variants.add(refName + ":" + variantName, variantData);
                }
            }

            BlockPos relative = pos.add(-minX,-minY,-minZ);
            if(variantName != null) {
                array[relative.getX()][relative.getY()][relative.getZ()] = refName + ":" + variantName;
            } else {
                array[relative.getX()][relative.getY()][relative.getZ()] = refName;
            }

        }
        if(variants.size() > 0) {
            resultObj.add("input-nbt", variants);
        }

        // Create the shape map
        JsonArray yArr = new JsonArray();
        for(int y = height-1; y >= 0; y--) {
            JsonArray zArr = new JsonArray();
            for (int z = 0; z < depth; z++) {
                JsonArray xArr = new JsonArray();
                for (int x = 0; x < width; x++) {
                    if(array[x][y][z] == null) {
                        xArr.add("_");
                    } else {
                        xArr.add(array[x][y][z]);
                    }
                }
                zArr.add(xArr);
            }
            yArr.add(zArr);
        }
        resultObj.add("shape", yArr);

        MessageClipboard message = new MessageClipboard();
        String rawJson = SerializationHelper.GSON.toJson(resultObj);
        message.setClipboardContent(rawJson.substring(1, rawJson.length()-1));
        PackageHandler.instance.sendTo(message, (EntityPlayerMP) sender.getCommandSenderEntity());

        player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-shape.success"));
    }
}
