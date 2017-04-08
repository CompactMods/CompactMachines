package org.dave.cm2.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.item.ItemPersonalShrinkingDevice;
import org.dave.cm2.item.psd.PSDFluidStorage;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.tile.TileEntityTunnel;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.world.ChunkLoadingMachines;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.DimensionTools;
import org.dave.cm2.world.tools.TeleportationTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockMachine extends BlockBase implements IMetaBlockName, ITileEntityProvider {

    public static final PropertyEnum<EnumMachineSize> SIZE = PropertyEnum.create("size", EnumMachineSize.class);

    public BlockMachine(Material material) {
        super(material);

        this.setHardness(8.0F);
        this.setResistance(20.0F);

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);

        setDefaultState(blockState.getBaseState().withProperty(SIZE, EnumMachineSize.TINY));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        super.neighborChanged(state, world, pos, blockIn);

        if(world.isRemote) {
            return;
        }

        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
            return;
        }

        // Make sure we don't stack overflow when we get in a notifyBlockChange loop.
        // Just ensure only a single notification happens per tick.
        TileEntityMachine te = (TileEntityMachine) world.getTileEntity(pos);
        if(te.lastNeighborUpdateTick == world.getTotalWorldTime()) {
            return;
        }

        te.lastNeighborUpdateTick = world.getTotalWorldTime();
        for(EnumFacing side : EnumFacing.values()) {
            BlockPos neighborPos = te.getTunnelForSide(side);
            if(neighborPos == null) {
                continue;
            }

            WorldServer machineWorld = DimensionTools.getServerMachineWorld();
            if(!(machineWorld.getTileEntity(neighborPos) instanceof TileEntityTunnel)) {
                continue;
            }

            machineWorld.notifyNeighborsOfStateChange(neighborPos, Blockss.tunnel);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for(EnumMachineSize size : EnumMachineSize.values()) {
            list.add(new ItemStack(this, 1, size.getMeta()));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SIZE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SIZE).getMeta();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(SIZE, EnumMachineSize.getFromMeta(meta));
    }

    @Override
    public String getSpecialName(ItemStack stack) {
        return this.getStateFromMeta(stack.getItemDamage()).getValue(SIZE).getName();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMachine();
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if(world.isRemote) {
            super.breakBlock(world, pos, state);
            return;
        }

        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
            return;
        }

        TileEntityMachine te = (TileEntityMachine) world.getTileEntity(pos);
        WorldSavedDataMachines.INSTANCE.removeMachinePosition(te.coords);

        // TODO: Think about adding harvesting CMs functionality back in
        BlockMachine.spawnItemWithNBT(world, pos, state.getValue(BlockMachine.SIZE), te);

        ChunkLoadingMachines.unforceChunk(te.coords);
        world.removeTileEntity(pos);

        super.breakBlock(world, pos, state);
    }

    public static void spawnItemWithNBT(World world, BlockPos pos, EnumMachineSize size, TileEntityMachine te) {
        if(world.isRemote) {
           return;
        }

        ItemStack stack = new ItemStack(Blockss.machine, 1, size.getMeta());
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("coords", te.coords);
        if(te.hasOwner()) {
            compound.setUniqueId("owner", te.getOwner());
        }
        stack.setTagCompound(compound);

        if(te.getCustomName().length() > 0) {
            stack.setStackDisplayName(te.getCustomName());
        }

        EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY() + 0.5, pos.getZ(), stack);
        entityItem.lifespan = 1200;
        entityItem.setPickupDelay(10);

        float motionMultiplier = 0.02F;
        entityItem.motionX = (float) world.rand.nextGaussian() * motionMultiplier;
        entityItem.motionY = (float) world.rand.nextGaussian() * motionMultiplier + 0.1F;
        entityItem.motionZ = (float) world.rand.nextGaussian() * motionMultiplier;

        world.spawnEntityInWorld(entityItem);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
            // Not a tile entity machine. Should not happen.
            return;
        }

        TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(pos);
        if(tileEntityMachine.coords != -1) {
            // The machine already has data for some reason
            return;
        }

        if(stack.hasTagCompound()) {
            if(stack.getTagCompound().hasKey("coords")) {
                int coords = stack.getTagCompound().getInteger("coords");
                if (coords != -1) {
                    tileEntityMachine.coords = coords;
                }
            }

            if(stack.hasDisplayName()) {
                tileEntityMachine.setCustomName(stack.getDisplayName());
            }

            if(stack.getTagCompound().hasKey("owner")) {
                tileEntityMachine.setOwner(stack.getTagCompound().getUniqueId("owner"));
            }
        }

        if(!tileEntityMachine.hasOwner() && placer instanceof EntityPlayer) {
            tileEntityMachine.setOwner((EntityPlayer)placer);
        }

        tileEntityMachine.markDirty();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(player.isSneaking()) {
            return false;
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        if(!(world.getTileEntity(pos)instanceof TileEntityMachine)) {
            return false;
        }

        TileEntityMachine machine = (TileEntityMachine)world.getTileEntity(pos);

        ItemStack playerStack = player.getHeldItemMainhand();
        if(playerStack != null) {
            Item playerItem = playerStack.getItem();

            // TODO: Convert the ability to teleport into a machine into an itemstack capability
            if(playerItem instanceof ItemPersonalShrinkingDevice) {
                PSDFluidStorage tank = (PSDFluidStorage) playerStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                boolean freeToGo = ConfigurationHandler.MachineSettings.fluidCostForEntering == 0 || player.capabilities.isCreativeMode;
                if(!freeToGo && tank != null && tank.getFluidAmount() >= 250) {
                    tank.drainInternal(250, true);
                    freeToGo = true;
                }

                if(freeToGo) {
                    TeleportationTools.teleportPlayerToMachine((EntityPlayerMP) player, machine);

                    WorldSavedDataMachines.INSTANCE.addMachinePosition(machine.coords, pos, world.provider.getDimension());
                } else {
                    TextComponentTranslation tc = new TextComponentTranslation("item.cm2.psd.not_enough_fluid");
                    tc.getStyle().setColor(TextFormatting.DARK_RED);
                    player.addChatComponentMessage(tc);
                }
            } else if(playerStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
                if(!FluidUtil.interactWithFluidHandler(playerStack, machine.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side), player)) {
                    return false;
                }

                machine.markDirty();
            }
        }

        return true;
    }
}
