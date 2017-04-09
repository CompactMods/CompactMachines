package org.dave.cm2.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.item.psd.PSDCapabilityProvider;
import org.dave.cm2.item.psd.PSDFluidStorage;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;
import org.dave.cm2.reference.GuiIds;
import org.dave.cm2.reference.Resources;
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

        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            player.openGui(CompactMachines2.instance, GuiIds.PSD_WELCOME.ordinal(), world, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return new ActionResult(EnumActionResult.FAIL, itemStack);
        }

        EntityPlayerMP serverPlayer = (EntityPlayerMP)player;

        if(player.isSneaking()) {
            int coords = StructureTools.getCoordsForPos(player.getPosition());
            Vec3d pos = player.getPositionVector();
            WorldSavedDataMachines.INSTANCE.addSpawnPoint(coords, pos.xCoord, pos.yCoord, pos.zCoord);

            // TODO: Add localization
            player.addChatComponentMessage(new TextComponentString(TextFormatting.GREEN + "Entry point set!"));

            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }

        TeleportationTools.teleportPlayerOutOfMachine(serverPlayer);
        return new ActionResult(EnumActionResult.SUCCESS, itemStack);
    }



}
