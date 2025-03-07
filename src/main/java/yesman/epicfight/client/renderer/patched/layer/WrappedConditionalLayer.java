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
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class WrappedConditionalLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LayerRenderer<E, M>> extends PatchedLayer<E, T, M, R> {
	private final PatchedLayer<E, T, M, R> layer;
	private final Function<LivingEntityPatch<?>, Boolean> renderCondition;
	
	public WrappedConditionalLayer(PatchedLayer<E, T, M, R> layer, Function<LivingEntityPatch<?>, Boolean> renderCondition) {
		this.layer = layer;
		this.renderCondition = renderCondition;
	}
	
	@Override
	protected void renderLayer(T entitypatch, E entityliving, @Nullable R vanillaLayer, MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		if (this.renderCondition.apply(entitypatch)) {
			this.layer.renderLayer(entitypatch, entityliving, vanillaLayer, poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
		}
	}
}