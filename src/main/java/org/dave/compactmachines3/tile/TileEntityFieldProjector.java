package org.dave.compactmachines3.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.dave.compactmachines3.block.BlockFieldProjector;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.*;

public class TileEntityFieldProjector extends TileEntity implements ITickable {
    public int ticks = 0;
    private Random rand = new Random();

    private int activeMagnitude = 0;

    private MultiblockRecipe activeRecipe;
    private int progress = 0;

    protected UUID owner;


    public TileEntityFieldProjector() {
        super();
    }

    public EnumFacing getDirection() {
        return this.getWorld().getBlockState(this.pos).getValue(BlockFieldProjector.FACING);
    }

    public int getCraftingProgress() {
        return progress;
    }

    public MultiblockRecipe getActiveRecipe() {
        return activeRecipe;
    }

    public ItemStack getActiveCraftingResult() {
        if(activeRecipe == null) {
            return ItemStack.EMPTY;
        }

        return activeRecipe.getTargetStack();
    }

    public List<BlockPos> getInsideBlocks() {
        if(activeMagnitude == 0) {
            return Collections.emptyList();
        }

        int fieldDimension = activeMagnitude * 2 - 1;
        int fieldRadius = (fieldDimension+1)/2;

        BlockPos frontTopLeft = this.getPos()
                .offset(this.getDirection(), activeMagnitude+1)
                .offset(EnumFacing.UP, fieldRadius-1)
                .offset(this.getDirection().rotateYCCW(), fieldRadius-1);

        List<BlockPos> insideBlocks = new ArrayList<>();
        for(int x = 0; x < fieldDimension; x++) {
            for (int y = 0; y < fieldDimension; y++) {
                for (int z = 0; z < fieldDimension; z++) {
                    BlockPos blockToCheck = frontTopLeft
                            .offset(this.getDirection(), x)
                            .offset(EnumFacing.DOWN, y)
                            .offset(this.getDirection().rotateY(), z);

                    // ((WorldServer)world).spawnParticle(EnumParticleTypes.REDSTONE, true, blockToCheck.getX() + 0.5d, blockToCheck.getY() + 0.5d, blockToCheck.getZ() + 0.5d, 10, 0.0d, 0.0d, 0.0d, 0);

                    if(getWorld().isAirBlock(blockToCheck)) {
                        continue;
                    }

                    insideBlocks.add(blockToCheck);
                }
            }
        }

        return insideBlocks;
    }

    public TileEntityFieldProjector getNextProjectorCW() {
        for(int size = 1; size < ConfigurationHandler.Settings.getMaximumMagnitude(); size++) {
            BlockPos pos = this.getPos()
                    .offset(this.getDirection(), size)
                    .offset(this.getDirection().rotateY(), size);

            if(getWorld().getTileEntity(pos) instanceof TileEntityFieldProjector) {
                return (TileEntityFieldProjector) getWorld().getTileEntity(pos);
            }
        }

        return null;
    }

    public TileEntityFieldProjector getMasterProjector() {
        TileEntityFieldProjector result = this;
        while(result != null && !result.isMaster()) {
            result = result.getNextProjectorCW();
        }

        return result;
    }

    public boolean isMaster() {
        return this.getDirection() == EnumFacing.WEST;
    }

    public List<BlockPos> getMissingProjectors(int magnitude) {
        int radius = magnitude*2;

        BlockPos center = this.getPos().offset(this.getDirection(), radius);

        BlockPos opposite = center.offset(this.getDirection(), radius);
        BlockPos cw = center.offset(this.getDirection().rotateY(), radius);
        BlockPos ccw = center.offset(this.getDirection().rotateYCCW(), radius);

        List<BlockPos> missingBlocks = new ArrayList<>();
        IBlockState oppositeState = world.getBlockState(opposite);

        if(oppositeState.getBlock() != Blockss.fieldProjector || oppositeState.getValue(BlockFieldProjector.FACING) != this.getDirection().getOpposite() || world.isBlockPowered(opposite)) {
            missingBlocks.add(opposite);
        }

        IBlockState cwState = world.getBlockState(cw);
        if(cwState.getBlock() != Blockss.fieldProjector || cwState.getValue(BlockFieldProjector.FACING) != this.getDirection().rotateY().getOpposite() || world.isBlockPowered(cw)) {
            missingBlocks.add(cw);
        }

        IBlockState ccwState = world.getBlockState(ccw);
        if(ccwState.getBlock() != Blockss.fieldProjector || ccwState.getValue(BlockFieldProjector.FACING) != this.getDirection().rotateYCCW().getOpposite() || world.isBlockPowered(ccw)) {
            missingBlocks.add(ccw);
        }

        return missingBlocks;
    }

    public int getCraftingAreaMagnitude() {
        for(int magnitude = 1; magnitude <= ConfigurationHandler.Settings.getMaximumMagnitude() +1; magnitude++) {
            BlockPos opposite = getPos().offset(getDirection(), magnitude*4);
            if(!(world.getTileEntity(opposite) instanceof TileEntityFieldProjector)) {
                continue;
            }

            return magnitude;
        }

        return 0;
    }

    public boolean canGenerateFieldAtMagnitude(int magnitude) {
        if(!isMaster()) {
            return false;
        }

        if(getInvalidBlockInField(magnitude) == null) {
            return true;
        }

        return false;
    }

    public BlockPos getInvalidBlockInField(int magnitude) {
        if(!isMaster()) {
            return null;
        }

        int fieldDimension = (magnitude+1) * 2 - 1;

        BlockPos center = this.getPos().offset(this.getDirection(), magnitude);
        BlockPos topLeft = center.offset(EnumFacing.UP, magnitude).offset(this.getDirection().rotateYCCW(), magnitude);

        List<BlockPos> positionsToCheck = StructureTools.getCubePositions(topLeft, fieldDimension, fieldDimension, fieldDimension, false);
        for(BlockPos pos : positionsToCheck) {
            if(!getWorld().isAirBlock(pos)) {
                return pos;
            }
        }

        return null;
    }

    @Override
    public void update() {
        ticks++;

        if(world.isBlockPowered(getPos())) {
            return;
        }

        int magnitude = getCraftingAreaMagnitude();
        if(magnitude <= 0) {
            return;
        }

        // There is nothing to do when we are missing one of the other field projectors
        if(getMissingProjectors(magnitude).size() > 0) {
            activeMagnitude = 0;
            return;
        }

        // One of the projectors must be a master projector
        TileEntityFieldProjector master = getMasterProjector();
        if(master == null) {
            return;
        }

        // Then check the crafting area is free, this is done by the master projector
        if(!master.canGenerateFieldAtMagnitude(magnitude)) {
            return;
        }

        activeMagnitude = magnitude;
        if(world.isRemote) {
            spawnFieldParticles();

            // The client is already done
            return;
        }

        if(isMaster() && activeRecipe != null) {
            if (progress > 100) {
                // Crafting has finished, spawn the created item
                // TODO: Make it possible to craft multiple items per recipe

                BlockPos center = this.getPos().offset(this.getDirection(), magnitude * 2);

                EntityItem entityItem = new EntityItem(world, center.getX(), center.getY(), center.getZ(), activeRecipe.getTargetStack());
                entityItem.lifespan = 2400;
                entityItem.setPickupDelay(10);

                entityItem.motionX = 0.0f;
                entityItem.motionY = 0.1f;
                entityItem.motionZ = 0.0f;

                world.spawnEntity(entityItem);

                progress = 0;
                activeRecipe = null;
                markDirty();
            }

            progress++;
        }

        // Determine whether there is a catalyst item inside this projectors field
        double growWD = (magnitude*2+1)/2;
        BlockPos center = this.getPos().offset(this.getDirection(), magnitude);
        AxisAlignedBB centerBB = new AxisAlignedBB(center).grow(0, growWD, 0);
        if(getDirection() == EnumFacing.NORTH || getDirection() == EnumFacing.SOUTH) {
            centerBB = centerBB.grow(growWD, 0, 0);
        } else {
            centerBB = centerBB.grow(0, 0, growWD);
        }

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, centerBB);
        // TODO: Master also needs to check the top of the cube
        for(EntityItem item : items) {
            int age = ObfuscationReflectionHelper.getPrivateValue(EntityItem.class, item, "age");
            if(age > ConfigurationHandler.Settings.maximumCraftingCatalystAge) {
                continue;
            }

            MultiblockRecipe multiblockRecipe = MultiblockRecipes.tryCrafting(world, getPos(), item.getItem().getItem());
            if(multiblockRecipe == null) {
                continue;
            }

            // Remove blocks and item from the world
            for(BlockPos pos : getInsideBlocks()) {
                world.setBlockToAir(pos);
                ((WorldServer)world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX(), pos.getY(), pos.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.01D, new int[0]);
            }
            item.setDead();
            // TODO: Sound effects: zzzzaaap

            // Tell the master field projector that it is now crafting
            master.activeRecipe = multiblockRecipe;
            master.progress = 0;
            master.markDirty();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(getPos());
            world.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    private void spawnFieldParticles() {
        BlockPos center = this.getPos().offset(this.getDirection(), activeMagnitude);
        BlockPos topLeft = center.offset(EnumFacing.UP, activeMagnitude).offset(this.getDirection().rotateY(), activeMagnitude);

        EnumParticleTypes particle = EnumParticleTypes.END_ROD;
        if(getMasterProjector() != null && getMasterProjector().activeRecipe != null) {
            particle = EnumParticleTypes.EXPLOSION_LARGE;
        }

        for(int x = 0; x < activeMagnitude*2; x++) {
            for(int y = 0; y < activeMagnitude*2; y++) {
                BlockPos pos = topLeft.offset(EnumFacing.DOWN, y).offset(this.getDirection().rotateYCCW(), x);
                if(getWorld().isAirBlock(pos)) {
                    // There is chance we will do nothing this tick
                    if(rand.nextFloat() >= 0.6) {
                        double xPos = pos.getX() + rand.nextDouble();
                        double yPos = pos.getY() + rand.nextDouble();
                        double zPos = pos.getZ() + rand.nextDouble();
                        getWorld().spawnParticle(particle, true, xPos, yPos, zPos, 0.0D, 0.01D, 0.0D, new int[0]);

                        // There is an even lesser chance we spawn the "creation" particles, the ones flying to the field...
                        if(rand.nextFloat() >= 0.95) {
                            double xPos2 = this.getPos().getX() + 0.5f;
                            double yPos2 = this.getPos().getY() + 0.66f;
                            double zPos2 = this.getPos().getZ() + 0.5f;

                            double speedMultiplier = 0.09d;
                            double xSpeed = (xPos-xPos2) * speedMultiplier;
                            double ySpeed = (yPos-yPos2) * speedMultiplier;
                            double zSpeed = (zPos-zPos2) * speedMultiplier;

                            getWorld().spawnParticle(EnumParticleTypes.END_ROD, true, xPos2, yPos2, zPos2, xSpeed, ySpeed, zSpeed, new int[0]);
                        }
                    }
                }

                // Master takes care of topping off the cube
                if(isMaster() && y < activeMagnitude*2-1 && x < activeMagnitude*2-1) {
                    pos = topLeft.offset(getDirection(), y+1).offset(this.getDirection().rotateYCCW(), x+1);
                    //if(rand.nextFloat() < 0.5) continue;

                    double xPos = pos.getX() + rand.nextDouble();
                    double yPos = pos.getY() + rand.nextDouble();
                    double zPos = pos.getZ() + rand.nextDouble();
                    getWorld().spawnParticle(particle, true, xPos, yPos, zPos, 0.0D, 0.01D, 0.0D, new int[0]);
                }
            }
        }
    }





    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        progress = compound.getInteger("progress");
        owner = compound.getUniqueId("owner");
        if(compound.hasKey("recipe")) {
            activeRecipe = MultiblockRecipes.getRecipeByName(compound.getString("recipe"));
        } else {
            activeRecipe = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if(isMaster()) {
            compound.setInteger("progress", progress);

            if (activeRecipe != null) {
                compound.setString("recipe", activeRecipe.getName());
            }
        }

        if(hasOwner()) {
            compound.setUniqueId("owner", this.owner);
        }

        return compound;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(getOwner()).getName();
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setOwner(EntityPlayer player) {
        if(player == null) {
            return;
        }

        setOwner(player.getUniqueID());
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
