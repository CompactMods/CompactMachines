package org.dave.compactmachines3.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.core.Registrations;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.util.CompactMachineUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

//import org.dave.compactmachines3.tile.TileEntityMachine;
//import org.dave.compactmachines3.tile.TileEntityRedstoneTunnel;
//import org.dave.compactmachines3.tile.TileEntityTunnel;

// TODO TOP Integration
public class BlockCompactMachine extends Block implements IProbeInfoProvider {

    public static final EnumProperty<EnumMachineSize> SIZE = EnumProperty.create("size", EnumMachineSize.class);

    public BlockCompactMachine() {
        super(Block.Properties
                .create(Material.IRON)
                .hardnessAndResistance(8.0F, 20.0F));

        setDefaultState(getStateContainer().getBaseState()
                .with(SIZE, EnumMachineSize.TINY));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(SIZE);
    }

    // TODO Rendering
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        Item itemBlockMachine = Item.getItemFromBlock(Blockss.machine);
//        for(EnumMachineSize size : EnumMachineSize.values()) {
//            ModelLoader.setCustomModelResourceLocation(itemBlockMachine, size.getMeta(), new ModelResourceLocation(itemBlockMachine.getRegistryName(), "size=" + size.getName()));
//        }
//    }


    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return false;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
//        TODO Tile Entity
//        if(!(blockAccess.getTileEntity(pos) instanceof TileEntityMachine)) {
//            return 0;
//        }
//
//        TileEntityMachine machine = (TileEntityMachine) blockAccess.getTileEntity(pos);
//        if(machine.isInsideItself()) {
//            return 0;
//        }
//
//        return machine.getRedstonePowerOutput(side.getOpposite());
        return 0;
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);

        if (world.isRemote()) {
            return;
        }

//        TODO Tile Entity
//        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
//            return;
//        }

        // Determine whether it's an immediate neighbor ...
        Direction facing = null;
        for (Direction dir : Direction.values()) {
            if (pos.offset(dir).equals(neighbor)) {
                facing = dir;
                break;
            }
        }

        // And do nothing if it isnt, e.g. diagonal
        if (facing == null) {
            return;
        }

//        TODO Tile Entity and Server Stuff
//        // Make sure we don't stack overflow when we get in a notifyBlockChange loop.
//        // Just ensure only a single notification happens per tick.
//        TileEntityMachine te = (TileEntityMachine) world.getTileEntity(pos);
//        if(te.isInsideItself() || te.alreadyNotifiedOnTick) {
//            return;
//        }
//
//        ServerWorld machineWorld = DimensionTools.getServerMachineWorld();
//        BlockPos neighborPos = te.getConnectedBlockPosition(facing);
//        if(neighborPos != null && machineWorld.getTileEntity(neighborPos) instanceof TileEntityTunnel) {
//            machineWorld.notifyNeighborsOfStateChange(neighborPos, Blockss.tunnel, false);
//            te.alreadyNotifiedOnTick = true;
//        }
//
//        RedstoneTunnelData tunnelData = te.getRedstoneTunnelForSide(facing);
//        if(tunnelData != null && !tunnelData.isOutput) {
//            BlockPos redstoneNeighborPos = tunnelData.pos;
//            if(redstoneNeighborPos != null && machineWorld.getTileEntity(redstoneNeighborPos) instanceof TileEntityRedstoneTunnel) {
//                machineWorld.notifyNeighborsOfStateChange(redstoneNeighborPos, Blockss.redstoneTunnel, false);
//            }
//        }
    }

//    TODO: Other creative tab versions
//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
//        for(EnumMachineSize size : EnumMachineSize.values()) {
//            items.add(new ItemStack(this, 1, size.getMeta()));
//        }
//
//    }


    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = new ItemStack(Registrations.MACHINE_BLOCK_ITEM.get(), 1);

        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putString("size", state.get(SIZE).getName());

        stack.setTag(nbt);

        return stack;
    }

//    @Override
//    public String getSpecialName(ItemStack stack) {
//        return this.getStateFromMeta(stack.getItemDamage()).getValue(SIZE).getName();
//    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return null;
        // return new TileEntityMachine();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    public void onPlayerDestroy(IWorld world, BlockPos pos, BlockState state) {
        if (world.isRemote()) {
            super.onPlayerDestroy(world, pos, state);
            return;
        }

//        TODO: Tile Entity
//        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
//            return;
//        }
//
//        TileEntityMachine te = (TileEntityMachine) world.getTileEntity(pos);
//        WorldSavedDataMachines.INSTANCE.removeMachinePosition(te.coords);
//
//        BlockMachine.spawnItemWithNBT(world, pos, state.get(BlockMachine.SIZE), te);
//
//        ChunkLoadingMachines.unforceChunk(te.coords);
        // world.removeTileEntity(pos);

        super.onPlayerDestroy(world, pos, state);
    }

//    public static void spawnItemWithNBT(IWorld world, BlockPos pos, EnumMachineSize size, TileEntityMachine te) {
//        if(world.isRemote()) {
//           return;
//        }
//
//        ItemStack stack = new ItemStack(Blockss.machine, 1, size.getMeta());
//
//        CompoundNBT compound = new CompoundNBT();
//        compound.putInt("coords", te.coords);
//        if(te.hasOwner()) {
//            compound.putUniqueId("owner", te.getOwner());
//        }
//        if(te.hasNewSchema()) {
//            compound.putString("schema", te.getSchemaName());
//        }
//
//        stack.setTag(compound);
//
//        if(te.getCustomName().length() > 0) {
//            stack.setDisplayName( te.getCustomName());
//        }
//
////        TODO Spawn machine item entity in world
////        ItemEntity entityItem = new ItemEntity(world, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, stack);
////        entityItem.lifespan = 1200;
////        entityItem.setPickupDelay(10);
////
////        float motionMultiplier = 0.02F;
////        entityItem.motionX = 0.0f;
////        entityItem.motionY = (float) world.rand.nextGaussian() * motionMultiplier + 0.1F;
////        entityItem.motionZ = 0.0f;
////
////        world.spawnEntity(entityItem);
//    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        // Load size information
        CompoundNBT nbt = stack.getOrCreateTag();
        EnumMachineSize size = CompactMachineUtil.getMachineSizeFromNBT(nbt);

        BlockState newState = state.with(SIZE, size);
        worldIn.setBlockState(pos, newState);
    }


//    @Override
//    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
//        super.onBlockPlacedBy(world, pos, state, placer, stack);
//
//        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
//            // Not a tile entity machine. Should not happen.
//            return;
//        }
//
//        TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(pos);
//        if(tileEntityMachine.coords != -1) {
//            // The machine already has data for some reason
//            return;
//        }
//
//        // TODO: Allow storing of schemas in machines
//        if(stack.hasTag()) {
//            if(stack.getTag().contains("coords")) {
//                int coords = stack.getTag().getInt("coords");
//                if (coords != -1) {
//                    tileEntityMachine.coords = coords;
//                    if(!world.isRemote) {
//                        WorldSavedDataMachines.INSTANCE.addMachinePosition(tileEntityMachine.coords, pos, world.provider.getDimension(), tileEntityMachine.getSize());
//                        StructureTools.setBiomeForCoords(coords, world.getBiome(pos));
//                    }
//                }
//            }
//
//            if(stack.getTag().contains("schema")) {
//                tileEntityMachine.setSchema(stack.getTag().getString("schema"));
//            }
//
//            if(stack.hasDisplayName()) {
//                tileEntityMachine.setCustomName(stack.getDisplayName());
//            }
//
//            if(stack.getTag().contains("ownerLeast") && stack.getTag().contains("ownerMost")) {
//                tileEntityMachine.setOwner(stack.getTag().getUniqueId("owner"));
//            }
//        }
//
//        if(!tileEntityMachine.hasOwner() && placer instanceof PlayerEntity) {
//            tileEntityMachine.setOwner((PlayerEntity) placer);
//        }
//
//        tileEntityMachine.markDirty();
//    }


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

        if (player.isSneaking())
            return ActionResultType.PASS;

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

//    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
//        if(player.isSneaking()) {
//            return false;
//        }
//
//        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
//            return true;
//        }
//
//        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
//            return false;
//        }
//
//        TileEntityMachine machine = (TileEntityMachine)world.getTileEntity(pos);
//        ItemStack playerStack = player.getHeldItemMainhand();
//        if(ShrinkingDeviceUtils.isShrinkingDevice(playerStack)) {
//            TeleportationTools.tryToEnterMachine(player, machine);
//            return true;
//        }
//
//        player.openGui(CompactMachines3.instance, GuiIds.MACHINE_VIEW.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
//        PackageHandler.instance.sendTo(new MessageMachineContent(machine.coords), (EntityPlayerMP)player);
//        PackageHandler.instance.sendTo(new MessageMachineChunk(machine.coords), (EntityPlayerMP)player);
//
//        return true;
//    }


    @Override
    public String getID() {
        return CompactMachines3.MODID + ":" + "machine";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        String size = blockState.get(SIZE).getName();
        probeInfo.text(new TranslationTextComponent("machines.sizes." + size));

//        TileEntity te = world.getTileEntity(data.getPos());
//        if(te instanceof TileEntityMachine) {
//            TileEntityMachine machine = (TileEntityMachine) te;
//            if(machine.isInsideItself()) {
//                String text = TextFormatting.DARK_RED + "{*tooltip.compactmachines3.machine.stopitsoaryn*}" + TextFormatting.RESET;
//                probeInfo.horizontal().text(new StringTextComponent(text));
//                return;
//            }
//
//            String nameOrId = "";
//            if(machine.coords < 0 && machine.getCustomName().length() == 0) {
//                nameOrId = "{*tooltip.compactmachines3.machine.coords.unused*}";
//            } else if(machine.getCustomName().length() > 0) {
//                nameOrId = machine.getCustomName();
//            } else {
//                nameOrId = "#" + machine.coords;
//            }
//
//            String coords = TextFormatting.GREEN + "{*tooltip.compactmachines3.machine.coords*} " + TextFormatting.YELLOW + nameOrId + TextFormatting.RESET;
//            probeInfo.horizontal()
//                    .text(new StringTextComponent(coords));
//
//            if(player.isCreative() && mode == ProbeMode.EXTENDED) {
//                if(machine.hasNewSchema()) {
//                    String schemaName = machine.getSchemaName();
//                    String text = TextFormatting.RED + "{*tooltip.compactmachines3.machine.schema*} " + TextFormatting.YELLOW + schemaName + TextFormatting.RESET;
//                    probeInfo.horizontal()
//                            .text(new StringTextComponent(text));
//                }
//            }
//
//            String translate = "enumfacing." + data.getSideHit().name();
//            String text = TextFormatting.YELLOW + "{*" + translate + "*}" + TextFormatting.RESET;
//            probeInfo.horizontal()
//                    .item(new ItemStack(Items.COMPASS))
//                    .text(new StringTextComponent(text));
//
//            ItemStack connectedStack = machine.getConnectedPickBlock(data.getSideHit());
//            if(connectedStack != null && !connectedStack.isEmpty()) {
//                probeInfo.horizontal().item(connectedStack).itemLabel(connectedStack);
//            }
//        }
    }
}
