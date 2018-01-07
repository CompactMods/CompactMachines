package org.dave.compactmachines3.world.data.provider.cofh;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import org.dave.compactmachines3.world.data.provider.AbstractExtraTileDataProvider;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProvider;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ExtraTileDataProvider(mod = "cofhcore")
public class CofhGrid extends AbstractExtraTileDataProvider {
    private Class enumClass;
    private Class applicableClass;

    private Method getVisualConnectionType;

    private Field clientConnections;
    private Method enumClassOrdinal;

    public CofhGrid() {
        try {
            applicableClass = Class.forName("cofh.thermaldynamics.duct.tiles.TileGrid");

            getVisualConnectionType = applicableClass.getDeclaredMethod("getVisualConnectionType", int.class);
            getVisualConnectionType.setAccessible(true);

            clientConnections = applicableClass.getDeclaredField("clientConnections");
            clientConnections.setAccessible(true);

            enumClass = Class.forName("cofh.thermaldynamics.block.BlockDuct$ConnectionType");
            enumClassOrdinal = Enum.class.getDeclaredMethod("ordinal");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class getApplicableClass() {
        return applicableClass;
    }

    private int getVisualConnectionType(TileEntity tileEntity, int side) {
        try {
            Object obj = getVisualConnectionType.invoke(tileEntity, side);
            return (int) enumClassOrdinal.invoke(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void setClientConnections(TileEntity tileEntity, Object[] arr) {
        try {
            clientConnections.set(tileEntity, arr);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NBTTagCompound writeExtraData(TileEntity tileEntity) {
        NBTTagCompound result = new NBTTagCompound();

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 6; i++) {
            list.appendTag(new NBTTagInt(getVisualConnectionType(tileEntity, i)));
        }
        result.setTag("Connections", list);
        return result;
    }

    @Override
    public void readExtraData(TileEntity tileEntity, NBTTagCompound data) {
        Object[] newArray = (Object[]) Array.newInstance(enumClass, 6);

        NBTTagList list = data.getTagList("Connections", Constants.NBT.TAG_INT);
        for(int i = 0; i < 6; i++) {
            int value = ((NBTTagInt)list.get(i)).getInt();
            newArray[i] = enumClass.getEnumConstants()[value];
        }
        setClientConnections(tileEntity, newArray);
    }
}
