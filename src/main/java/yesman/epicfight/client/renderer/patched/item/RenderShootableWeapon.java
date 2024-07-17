package yesman.epicfight.client.renderer.patched.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class RenderShootableWeapon extends RenderItemBase {
	public RenderShootableWeapon(OpenMatrix4f correctionMatrix) {
		super(correctionMatrix, correctionMatrix);
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, Hand hand, HumanoidArmature armature, OpenMatrix4f[] poses, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight) {
		OpenMatrix4f modelMatrix = this.getCorrectionMatrix(stack, entitypatch, hand);
		OpenMatrix4f jointTransform = poses[armature.toolL.getId()];
		modelMatrix.mulFront(jointTransform);

		poseStack.pushPose();
		this.mulPoseStack(poseStack, modelMatrix);

		Minecraft mc = Minecraft.getInstance();
		mc.gameRenderer.itemInHandRenderer.renderItem(entitypatch.getOriginal(), stack, TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, packedLight);
		poseStack.popPose();

		GlStateManager._enableDepthTest();
	}
}