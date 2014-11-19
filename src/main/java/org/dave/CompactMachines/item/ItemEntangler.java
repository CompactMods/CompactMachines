package org.dave.CompactMachines.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import org.dave.CompactMachines.reference.Names;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEntangler extends ItemCM {
	private static IIcon	itemIconEntangled;

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
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean flag) {
		super.addInformation(itemStack, entityPlayer, list, flag);

		if (itemStack.hasTagCompound()) {
			NBTTagCompound nbt = itemStack.getTagCompound();
			if (nbt.hasKey("size")) {
				int size = nbt.getInteger("size");
				switch (size) {
					case 0:
						list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.zero"));
						break;
					case 1:
						list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.one"));
						break;
					case 2:
						list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.two"));
						break;
					case 3:
						list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.three"));
						break;
					case 4:
						list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.four"));
						break;
					case 5:
						list.add(StatCollector.translateToLocal("tooltip.cm:machine.size.five"));
						break;
					default:
						break;
				}
			}

			int coords = nbt.getInteger("coords");
			if (coords > -1) {
				list.add(StatCollector.translateToLocal("tooltip.cm:machine.coords") + ": " + coords);
			}
		}

	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt.hasKey("coords")) {
				return itemIconEntangled;
			}
		}
		return super.getIconIndex(stack);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt.hasKey("coords")) {
				return itemIconEntangled;
			}
		}
		return super.getIcon(stack, pass);
	}
}
