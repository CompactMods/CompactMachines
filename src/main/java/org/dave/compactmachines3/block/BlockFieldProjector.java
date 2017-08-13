package org.dave.compactmachines3.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;
import org.dave.compactmachines3.render.TESRFieldProjector;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.Logz;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFieldProjector extends BlockBase implements ITileEntityProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockFieldProjector(Material material) {
        super(material);

        this.setHardness(8.0F);
        this.setResistance(20.0F);

        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setDefaultState(blockState.getBaseState());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityFieldProjector();
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation("compactmachines3:fieldprojectorcombined", "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFieldProjector.class, new TESRFieldProjector());
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        if(!(world.getTileEntity(pos) instanceof TileEntityFieldProjector)) {
            return false;
        }

        TileEntityFieldProjector teProjector = (TileEntityFieldProjector)world.getTileEntity(pos);

        int magnitude = teProjector.getCraftingAreaMagnitude();
        if(magnitude <= 0) {
            player.sendMessage(new TextComponentString("Missing opposite field projector! It is required to determine the field size!"));
            return true;
        }

        List<BlockPos> missingProjectors = teProjector.getMissingProjectors(magnitude);
        if(missingProjectors.size() > 0) {
            for(BlockPos missingPos : missingProjectors) {
                int x = missingPos.getX() - pos.getX();
                int y = missingPos.getY() - pos.getY();
                int z = missingPos.getZ() - pos.getZ();
                player.sendMessage(new TextComponentString("Missing field projector at " + x + ", " + y + ", " + z));
            }
            return true;
        }

        /*
        int distance = teProjector.getMaximumFieldDistance();
        if(distance == 0) {
            player.sendMessage(new TextComponentString("Something is blocking the field projector!"));
            return true;
        }
        */

        TileEntityFieldProjector master = teProjector.getMasterProjector();
        if(!master.getActiveCraftingResult().isEmpty()) {
            player.sendMessage(new TextComponentString("Currently crafting: " + master.getActiveCraftingResult().getDisplayName() + " (progress: " + master.getCraftingProgress() + ")"));
            return true;
        }

        MultiblockRecipe result = MultiblockRecipes.tryCrafting(world, pos, null);
        if(result == null) {
            player.sendMessage(new TextComponentString("No valid recipe found"));
        } else {
            player.sendMessage(new TextComponentString("Recipe for: " + result.getTargetStack().getDisplayName()));
        }

        return true;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, getFacingFromEntity(pos, placer)), 2);

        if(!(world.getTileEntity(pos) instanceof TileEntityFieldProjector)) {
            return;
        }

        TileEntityFieldProjector teProjector = (TileEntityFieldProjector) world.getTileEntity(pos);
        if(stack.hasTagCompound()) {
            if(stack.getTagCompound().hasKey("owner")) {
                teProjector.setOwner(stack.getTagCompound().getUniqueId("owner"));
            }
        }

        if(!teProjector.hasOwner() && placer instanceof EntityPlayer) {
            teProjector.setOwner((EntityPlayer)placer);
        }

        teProjector.markDirty();
    }

    public static EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
        return EnumFacing.getFacingFromVector(
                (float) (entity.posX - clickedBlock.getX()),
                0.0f,
                (float) (entity.posZ - clickedBlock.getZ())).getOpposite();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
}
