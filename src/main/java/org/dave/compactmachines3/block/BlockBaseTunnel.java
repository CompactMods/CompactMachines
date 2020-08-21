package org.dave.compactmachines3.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.compat.ITopInfoProvider;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.misc.CubeTools;
import org.dave.compactmachines3.tile.BaseTileEntityTunnel;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.StructureTools;

public abstract class BlockBaseTunnel extends BlockProtected implements ITileEntityProvider, ITopInfoProvider {
    public static final PropertyDirection MACHINE_SIDE = PropertyDirection.create("machineside");

    public BlockBaseTunnel(Material material) {
        super(material);
        this.setLightOpacity(1);
        this.setLightLevel(1.0f);
    }

    @Override
    public boolean isBlockProtected(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return CubeTools.shouldSideBeRendered(blockAccess, pos, side);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos whatblock) {
        super.neighborChanged(state, world, pos, blockIn, whatblock);

        if(world.isRemote) {
            return;
        }

        if(!(world.getTileEntity(pos) instanceof BaseTileEntityTunnel)) {
            return;
        }

        BaseTileEntityTunnel tunnel = (BaseTileEntityTunnel)world.getTileEntity(pos);
        if(tunnel.alreadyNotifiedOnTick) {
            return;
        }

        tunnel.alreadyNotifiedOnTick = true;
        notifyOverworldNeighbor(pos);
    }

    public void notifyOverworldNeighbor(BlockPos pos) {
        if (WorldSavedDataMachines.INSTANCE == null) {
            return;
        }
        DimensionBlockPos dimpos = WorldSavedDataMachines.INSTANCE.machinePositions.get(StructureTools.getCoordsForPos(pos));
        if(dimpos == null) {
            return;
        }

        WorldServer realWorld = DimensionTools.getWorldServerForDimension(dimpos.getDimension());
        if(realWorld == null || !(realWorld.getTileEntity(dimpos.getBlockPos()) instanceof TileEntityMachine)) {
            return;
        }

        realWorld.notifyNeighborsOfStateChange(dimpos.getBlockPos(), Blockss.machine, false);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(te instanceof BaseTileEntityTunnel) {
            BaseTileEntityTunnel tnt = (BaseTileEntityTunnel) te;

            String translate = "enumfacing." + blockState.getValue(BlockBaseTunnel.MACHINE_SIDE).getName();
            probeInfo.horizontal()
                    .item(new ItemStack(Items.COMPASS), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(TextFormatting.YELLOW + "{*" + translate + "*}" + TextFormatting.RESET);

            ItemStack connectedStack = tnt.getConnectedPickBlock();
            if(connectedStack != null && !connectedStack.isEmpty()) {
                probeInfo.horizontal().item(connectedStack, probeInfo.defaultItemStyle().width(14).height(14)).itemLabel(connectedStack);
            }
        }
    }
}
