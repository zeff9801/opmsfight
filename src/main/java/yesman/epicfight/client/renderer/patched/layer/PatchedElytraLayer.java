package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class PatchedElytraLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, AM extends HumanoidMesh> extends PatchedLayer<E, T, M, ElytraLayer<E, M>, AM> {

	public PatchedElytraLayer() {
		super(null);
	}

	@Override
	public void renderLayer(T entitypatch, E livingentity, ElytraLayer<E, M> originalRenderer, MatrixStack matrixStackIn, IRenderTypeBuffer buffer, int packedLightIn, OpenMatrix4f[] poses,  float bob, float yRot, float xRot, float partialTicks) {
		if (originalRenderer.shouldRender(livingentity.getItemBySlot(EquipmentSlotType.CHEST), livingentity)) {
			//TODO Mixin elytraModel because its private
//			originalRenderer.getParentModel().copyPropertiesTo(originalRenderer.elytraModel);
//
//
//			OpenMatrix4f modelMatrix = new OpenMatrix4f();
//			modelMatrix.scale(new Vec3f(-0.9F, -0.9F, 0.9F)).translate(new Vec3f(0.0F, -0.5F, -0.1F)).mulFront(poses[8]);
//			OpenMatrix4f transpose = OpenMatrix4f.transpose(modelMatrix, null);
//			matrixStackIn.pushPose();
//			MathUtils.translateStack(matrixStackIn, modelMatrix);
//			MathUtils.rotateStack(matrixStackIn, transpose);
//
//
//
//			originalRenderer.render(matrixStackIn, buffer, packedLightIn, livingentity, livingentity.animationPosition, livingentity.animationSpeed, partialTicks, bob, yRot, xRot);
//			matrixStackIn.popPose();
		}
	}
}