package org.dave.compactmachines3.block;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.compat.ITopInfoProvider;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;
import org.dave.compactmachines3.network.MessageParticleBlockMarker;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.render.TESRFieldProjector;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.utility.Logz;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFieldProjector extends BlockBase implements ITileEntityProvider, ITopInfoProvider {
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
            player.sendMessage(new TextComponentTranslation("hint.compactmachines3.missing_opposite_projector"));
            for(int testMagn = 1; testMagn <= 7; testMagn++) {
                BlockPos opposite = teProjector.getPos().offset(teProjector.getDirection(), testMagn*4);
                PackageHandler.instance.sendTo(new MessageParticleBlockMarker(opposite.getX() + 0.5d, opposite.getY() + 0.5d, opposite.getZ() + 0.5d), (EntityPlayerMP) player);
            }

            return true;
        }

        List<BlockPos> missingProjectors = teProjector.getMissingProjectors(magnitude);
        if(missingProjectors.size() > 0) {
            for(BlockPos missingPos : missingProjectors) {
                int x = missingPos.getX() - pos.getX();
                int y = missingPos.getY() - pos.getY();
                int z = missingPos.getZ() - pos.getZ();
                player.sendMessage(new TextComponentTranslation("hint.compactmachines3.missing_projector_at", x, y, z));
                PackageHandler.instance.sendTo(new MessageParticleBlockMarker(missingPos.getX() + 0.5d, missingPos.getY() + 0.5d, missingPos.getZ() + 0.5d), (EntityPlayerMP) player);
            }
            return true;
        }

        TileEntityFieldProjector master = teProjector.getMasterProjector();
        BlockPos invalidBlock = master.getInvalidBlockInField(magnitude);
        if(invalidBlock != null) {
            IBlockState blockState = world.getBlockState(invalidBlock);
            Block block = blockState.getBlock();
            String blockName = block.getUnlocalizedName();

            Item item = Item.getItemFromBlock(block);
            if(item != null) {
                ItemStack stack = new ItemStack(item, 1, block.getMetaFromState(blockState));
                if(stack != null) {
                    blockName = stack.getDisplayName();
                }
            }

            player.sendMessage(new TextComponentTranslation("hint.compactmachines3.invalid_block_in_field", invalidBlock.getX(), invalidBlock.getY(), invalidBlock.getZ(), blockName));
            return true;
        }


        if(!master.getActiveCraftingResult().isEmpty()) {
            player.sendMessage(new TextComponentTranslation("hint.compactmachines3.currently_crafting", master.getActiveCraftingResult().getDisplayName(), master.getCraftingProgress()));
            return true;
        }

        MultiblockRecipe result = MultiblockRecipes.tryCrafting(world, pos, null);
        if(result == null) {
            player.sendMessage(new TextComponentTranslation("hint.compactmachines3.no_recipe_found"));
        } else {
            player.sendMessage(new TextComponentTranslation("hint.compactmachines3.found_recipe_for", result.getTargetStack().getDisplayName()));
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

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(te instanceof TileEntityFieldProjector) {
            TileEntityFieldProjector tfp = (TileEntityFieldProjector) te;
            TileEntityFieldProjector master = tfp.getMasterProjector();

            if(master == null) {
                return;
            }

            ItemStack crafting = master.getActiveCraftingResult();
            if(!crafting.isEmpty()) {
                probeInfo.horizontal().text("{*top.compactmachines3.currently_crafting*}").item(crafting).itemLabel(crafting);
                probeInfo.horizontal().progress(master.getCraftingProgress(), 100);
                return;
            }

            MultiblockRecipe result = MultiblockRecipes.tryCrafting(world, data.getPos(), null);
            if(result != null) {
                probeInfo.horizontal().text("{*top.compactmachines3.found_recipe_for*}").item(result.getTargetStack()).itemLabel(result.getTargetStack());
            }
        }
    }
}
