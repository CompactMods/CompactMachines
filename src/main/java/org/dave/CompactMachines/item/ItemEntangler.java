package org.dave.CompactMachines.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import org.dave.CompactMachines.reference.Names;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEntangler extends ItemCM {
	private static IIcon itemIconEntangled;

	public ItemEntangler() {
		super();
		this.setUnlocalizedName(Names.Items.QUANTUMENTANGLER);
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
    	//super.registerIcons(iconRegister);

    	itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1));
        itemIconEntangled = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_entangled");
    }

    @Override
    public IIcon getIconIndex(ItemStack stack) {
		if(stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if(nbt.hasKey("coords")) {
				return itemIconEntangled;
			}
		}
		return super.getIconIndex(stack);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		if(stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if(nbt.hasKey("coords")) {
				return itemIconEntangled;
			}
		}
		return super.getIcon(stack, pass);
	}
}
