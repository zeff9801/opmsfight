package yesman.epicfight.api.client.forgeevent;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RegisterResourceLayersEvent<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LivingRenderer<E, M>, AM extends AnimatedMesh> extends Event {
	private final Map<ResourceLocation, LayerUtil.LayerProvider<E, T, M, R, AM>> layersbyid;
	
	public RegisterResourceLayersEvent(Map<ResourceLocation, LayerUtil.LayerProvider<E, T, M, R, AM>> layersbyid) {
		this.layersbyid = layersbyid;
	}
	
	public void register(ResourceLocation rl, LayerUtil.LayerProvider<E, T, M, R, AM> layerAdder) {
		this.layersbyid.put(rl, layerAdder);
	}
}