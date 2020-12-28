package org.dave.compactmachines3.tile;


import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.misc.SoundHandler;

public class TileEntityCraftingHologram extends TileEntity implements ITickable {
    private MultiblockRecipe recipe = null;
    private int progress = 0;

    public TileEntityCraftingHologram() {
    }

    public TileEntityCraftingHologram(MultiblockRecipe recipe) {
        this.recipe = recipe;
    }

    public void setRecipe(MultiblockRecipe recipe) {
        this.recipe = recipe;
    }

    public MultiblockRecipe getRecipe() {
        return recipe;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos()).grow(6.0d);
    }

    @Override
    public void update() {
        if(world.isRemote) {
            return;
        }

        if(recipe == null) {
            return;
        }

        if (progress % 20 == 0 && progress < recipe.getTicks()-60) {
            world.playSound(null, getPos(), SoundHandler.crafting, SoundCategory.BLOCKS, 0.4f, 1.0f);
        }

        if (progress > recipe.getTicks()) {
            // Crafting has finished, spawn the created item
            EntityItem entityItem = new EntityItem(world, getPos().getX()+0.5f, getPos().getY(), getPos().getZ()+0.5f, recipe.getTargetStack());
            entityItem.lifespan = 2400;
            entityItem.setPickupDelay(10);

            entityItem.motionX = 0.0f;
            entityItem.motionY = 0.15f;
            entityItem.motionZ = 0.0f;

            world.spawnEntity(entityItem);
            world.setBlockToAir(getPos());
        }

        progress++;
        this.markDirty();
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        progress = compound.getInteger("progress");

        if(compound.hasKey("recipe")) {
            this.recipe = MultiblockRecipes.getRecipeByName(compound.getString("recipe"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if(recipe != null) {
            compound.setString("recipe", recipe.getName());
        }

        compound.setInteger("progress", progress);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

}
