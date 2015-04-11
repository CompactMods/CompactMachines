package org.dave.CompactMachines.handler;

import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author soniex2
 */
public class BlockProxyHandler {
    // HashSet means it's fast to add/remove items
    public static HashMap<Integer, HashSet<TileEntityInterface>> interfaceMap = new HashMap<Integer, HashSet<TileEntityInterface>>();
    public static HashMap<Integer, HashSet<TileEntityMachine>> machineMap = new HashMap<Integer, HashSet<TileEntityMachine>>();

    public static HashSet<TileEntityInterface> getIS(int coords) {
        HashSet<TileEntityInterface> set = interfaceMap.get(coords);
        if (set == null) {
            set = new HashSet<TileEntityInterface>();
            interfaceMap.put(coords, set);
        }
        return set;
    }

    public static HashSet<TileEntityMachine> getMS(int coords) {
        HashSet<TileEntityMachine> set = machineMap.get(coords);
        if (set == null) {
            set = new HashSet<TileEntityMachine>();
            machineMap.put(coords, set);
        }
        return set;
    }

    public static void add(int coords, TileEntityInterface tileEntityInterface) {
        getIS(coords).add(tileEntityInterface);
    }

    public static void remove(int coords, TileEntityInterface tileEntityInterface) {
        HashSet<TileEntityInterface> set = interfaceMap.get(coords);
        if (set != null) {
            set.remove(tileEntityInterface);
            if (set.isEmpty()) {
                interfaceMap.remove(coords);
            }
        }
    }

    public static void add(int coords, TileEntityMachine tileEntityMachine) {
        getMS(coords).add(tileEntityMachine);
    }

    public static void remove(int coords, TileEntityMachine tileEntityMachine) {
        HashSet<TileEntityMachine> set = machineMap.get(coords);
        if (set != null) {
            set.remove(tileEntityMachine);
            if (set.isEmpty()) {
                machineMap.remove(coords);
            }
        }
    }
}
