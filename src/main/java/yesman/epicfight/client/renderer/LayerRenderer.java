package yesman.epicfight.client.renderer;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public interface LayerRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>> {
	public void addPatchedLayer(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends net.minecraft.client.renderer.entity.layers.LayerRenderer<E, M>> patchedLayer);
	
	public void addCustomLayer(PatchedLayer<E, T, M, ? extends net.minecraft.client.renderer.entity.layers.LayerRenderer<E, M>> patchedLayer);
}