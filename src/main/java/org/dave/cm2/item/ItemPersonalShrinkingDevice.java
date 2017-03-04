package org.dave.cm2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;
import org.dave.cm2.world.tools.TeleportationTools;

public class ItemPersonalShrinkingDevice extends ItemBase {
    public ItemPersonalShrinkingDevice() {
        super();

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
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

        // TODO: Add set spawnpoint option when sneaking

        TeleportationTools.teleportPlayerOutOfMachine(serverPlayer);

        return new ActionResult(EnumActionResult.SUCCESS, itemStack);
    }
}
