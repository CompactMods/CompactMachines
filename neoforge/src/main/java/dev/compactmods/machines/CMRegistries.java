package dev.compactmods.machines;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.client.shader.flag.FlagShader;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;

import java.util.stream.Stream;

public interface CMRegistries {

	DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, CompactMachinesCore.MOD_ID);

	DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);

	DeferredRegister<FlagShader> FLAG_SHADERS = DeferredRegister.create(FlagShader.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    static void setup(IEventBus modBus) {
		Stream.of(TABS,
				CMDataAttachments.ATTACHMENT_TYPES,
				DATA_COMPONENTS
		).forEach(r -> r.register(modBus));

		modBus.addListener((NewDatapackRegistryEvent newRegistries) -> {
			newRegistries.worldRegistry(RoomTemplate.REGISTRY_KEY, RoomTemplate.CODEC, RoomTemplate.CODEC);
			newRegistries.worldRegistry(FlagShader.REGISTRY_KEY, FlagShader.CODEC, FlagShader.CODEC);
			newRegistries.worldRegistry(TunnelType.REGISTRY_KEY, TunnelType.CODEC, TunnelType.CODEC);
		});
	}
}
