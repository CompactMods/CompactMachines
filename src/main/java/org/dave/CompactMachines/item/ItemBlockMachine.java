package org.dave.CompactMachines.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemBlockMachine extends ItemBlock {

	public ItemBlockMachine(Block block) {
		super(block);
		
		this.setHasSubtypes(true);
	}
	
    @Override
    public int getMetadata(int meta)
    {
        return meta;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean flag)
    {
        int metaData = itemStack.getItemDamage();

        if (metaData == 0)
        {
            list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.zero"));
        }
        else if (metaData == 1)
        {
        	list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.one"));
        }
        else if (metaData == 2)
        {
        	list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.two"));
        }
        else if (metaData == 3)
        {
        	list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.three"));
        }
        else if (metaData == 4)
        {
        	list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.four"));
        }
        else if (metaData == 5)
        {
        	list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.five"));
        }         
    }	

}
