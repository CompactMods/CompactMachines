package org.dave.compactmachines3.world.data.provider;

import net.minecraft.tileentity.TileEntity;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.utility.AnnotatedInstanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExtraTileDataProviderRegistry {
    private static List<AbstractExtraTileDataProvider> extraTileData = new ArrayList<>();

    public static void registerExtraTileDataProviders() {
        for(AbstractExtraTileDataProvider etdp : AnnotatedInstanceUtil.getExtraTileDataProviders()) {
            if(etdp.getName() == null) {
                continue;
            }

            CompactMachines3.logger.info("Registered extra tile data provider '{}'", etdp.getName());
            extraTileData.add(etdp);
        }
    }

    public static boolean hasDataProvider(TileEntity tileEntity) {
        return extraTileData.stream().anyMatch(provider -> provider.worksWith(tileEntity));
    }

    public static List<AbstractExtraTileDataProvider> getDataProviders(TileEntity tileEntity) {
        return extraTileData.stream().filter(provider -> provider.worksWith(tileEntity)).collect(Collectors.toList());
    }
}
