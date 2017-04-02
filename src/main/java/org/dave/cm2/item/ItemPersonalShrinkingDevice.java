package org.dave.cm2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.StructureTools;
import org.dave.cm2.world.tools.TeleportationTools;

public class ItemPersonalShrinkingDevice extends ItemBase {
    public ItemPersonalShrinkingDevice() {
        super();

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }

        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
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
