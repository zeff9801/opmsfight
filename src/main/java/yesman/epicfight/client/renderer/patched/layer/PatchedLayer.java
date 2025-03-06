package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class PatchedLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LayerRenderer<E, M>> {
	public void renderLayer(E entityliving, T entitypatch, LayerRenderer<E, M> vanillaLayer, MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		this.renderLayer(entitypatch, entityliving, this.castLayer(vanillaLayer), poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
	}

	protected abstract void renderLayer(T entitypatch, E entityliving, @Nullable R vanillaLayer, MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks);

	@SuppressWarnings("unchecked")
	protected R castLayer(LayerRenderer<E, M> layer) {
		return (R)layer;
	}
}