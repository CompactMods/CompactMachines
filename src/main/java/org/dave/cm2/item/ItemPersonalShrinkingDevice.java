package org.dave.cm2.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.block.BlockMiniaturizationFluid;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.item.psd.PSDCapabilityProvider;
import org.dave.cm2.item.psd.PSDFluidStorage;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;
import org.dave.cm2.reference.GuiIds;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.StructureTools;
import org.dave.cm2.world.tools.TeleportationTools;

import java.util.List;

public class ItemPersonalShrinkingDevice extends ItemBase {
    public ItemPersonalShrinkingDevice() {
        super();

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
        this.setMaxStackSize(1);
        this.setMaxDamage(Fluid.BUCKET_VOLUME * 4);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        PSDFluidStorage tank = (PSDFluidStorage) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        return 1D - ((double)tank.getFluidAmount() / (double)tank.getCapacity());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new PSDCapabilityProvider(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);

        PSDFluidStorage tank = (PSDFluidStorage) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        tooltip.add(I18n.format("tooltip." + CompactMachines2.MODID + ".psd.charge", tank.getFluidAmount() * 100 / tank.getCapacity()));

        if(GuiScreen.isShiftKeyDown()) {
            tooltip.add(I18n.format("tooltip." + CompactMachines2.MODID + ".psd.hint"));
        }
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        PSDFluidStorage tank = (PSDFluidStorage) stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if(tank.getFluidAmount() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        if(hand == EnumHand.OFF_HAND) {
            return new ActionResult(EnumActionResult.FAIL, itemStack);
        }

        PSDFluidStorage tank = (PSDFluidStorage) itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (tank.getFluidAmount() <= 3500) {
            RayTraceResult raytraceresult = this.rayTrace(world, player, true);
            if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos tracepos = raytraceresult.getBlockPos();
                IBlockState state = world.getBlockState(tracepos);
                boolean allowed = world.isBlockModifiable(player, tracepos) && player.canPlayerEdit(tracepos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemStack);

                if (allowed && state.getBlock() == Blockss.miniaturizationFluidBlock && state.getValue(BlockMiniaturizationFluid.LEVEL).intValue() == 0) {
                    world.setBlockState(tracepos, Blocks.AIR.getDefaultState(), 11);
                    player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);

                    tank.fill(new FluidStack(Fluidss.miniaturizationFluid, 1000), true);
                    return new ActionResult(EnumActionResult.SUCCESS, itemStack);
                }
            }
        }

        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            player.openGui(CompactMachines2.instance, GuiIds.PSD_WELCOME.ordinal(), world, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }

        if(world.isRemote && world.provider.getDimension() == ConfigurationHandler.Settings.dimensionId && player instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP)player;

            if(player.isSneaking()) {
                int coords = StructureTools.getCoordsForPos(player.getPosition());
                Vec3d pos = player.getPositionVector();
                WorldSavedDataMachines.INSTANCE.addSpawnPoint(coords, pos.xCoord, pos.yCoord, pos.zCoord);

                TextComponentTranslation tc = new TextComponentTranslation("item.cm2.psd.spawnpoint_set");
                tc.getStyle().setColor(TextFormatting.GREEN);
                player.addChatComponentMessage(tc);

                return new ActionResult(EnumActionResult.SUCCESS, itemStack);
            }

            TeleportationTools.teleportPlayerOutOfMachine(serverPlayer);
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }

        return new ActionResult(EnumActionResult.FAIL, itemStack);
    }



}
