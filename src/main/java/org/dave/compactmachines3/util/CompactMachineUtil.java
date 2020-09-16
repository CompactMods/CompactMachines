package org.dave.compactmachines3.util;

import net.minecraft.nbt.CompoundNBT;
import org.dave.compactmachines3.reference.EnumMachineSize;

import javax.annotation.Nullable;

public abstract class CompactMachineUtil {

    public static EnumMachineSize getMachineSizeFromNBT(@Nullable CompoundNBT tag) {
        try {
            if (tag == null)
                return EnumMachineSize.TINY;

            if (!tag.contains("size"))
                return EnumMachineSize.TINY;

            String sizeFromTag = tag.getString("size");
            return EnumMachineSize.getFromSize(sizeFromTag);
        }

        catch(Exception ex) {
            return EnumMachineSize.TINY;
        }
    }
}
