package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.client.render.CompactMachineRenderer;
import dev.compactmods.machines.machine.client.render.MachineShaderRenderer;
import dev.compactmods.machines.machine.client.shader.flag.FlagShader;
import dev.compactmods.machines.machine.client.shader.flag.FlagShaders;
import dev.compactmods.machines.machine.client.shader.MachineShaders;
import dev.compactmods.machines.machine.Machines;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;

public interface MachinesClient {
   static void registerEvents(IEventBus modBus) {
	  modBus.addListener(MachineColors::onBlockColors);
	  modBus.addListener(MachineColors::onItemColors);
	  modBus.addListener(MachinesClient::registerRenderers);
	  modBus.addListener(MachinesClient::registerRenderPipelines);
	  MachineToolbar.BUTTONS.register(modBus);

       NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, MachineShaderRenderer::extractPrideRenderState);
       NeoForge.EVENT_BUS.addListener(MachineShaderRenderer::prepareBuffers);
       NeoForge.EVENT_BUS.addListener(MachineShaderRenderer::afterTranslucent);
   }

   static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
	  event.registerBlockEntityRenderer(Machines.BlockEntities.MACHINE.get(), CompactMachineRenderer::new);
   }

   /**
    * Hand each machine pipeline to NeoForge so {@code RenderPipelines.PIPELINES_BY_LOCATION}
    * picks up the {@code compactmachines:pipeline/*} mappings and compiles the
    * matching shader sources on shader pack load.
    *
    * <p>The tye-dye pipeline is fixed; the flag pipelines are generated at
    * event time, one per registered {@link FlagShader},
    * each with its palette baked in via shader defines.</p>
    */
   static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
	  event.registerPipeline(MachineShaders.TYE_DYE_PIPELINE);
	  FlagShaders.registerAll(event);
   }
}
