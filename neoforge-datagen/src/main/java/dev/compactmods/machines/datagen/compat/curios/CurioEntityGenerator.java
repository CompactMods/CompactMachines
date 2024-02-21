package dev.compactmods.machines.datagen.compat.curios;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CurioEntityGenerator extends JsonCodecProvider<CurioEntityGenerator.CurioEntityDefinition> {

    public record CurioEntityDefinition(List<String> entities, List<String> slots) {
        public static Codec<CurioEntityDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.listOf().fieldOf("entities").forGetter(CurioEntityDefinition::entities),
                Codec.STRING.listOf().fieldOf("slots").forGetter(CurioEntityDefinition::slots)
        ).apply(i, CurioEntityDefinition::new));
    }

    public CurioEntityGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, PackOutput.Target.DATA_PACK, "curios/entities", PackType.SERVER_DATA, CurioEntityDefinition.CODEC, lookupProvider, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void gather() {
        var psdDef = new CurioEntityDefinition(List.of("player"), List.of("psd"));
        unconditional(new ResourceLocation(Constants.MOD_ID, "psd"), psdDef);
    }
}
