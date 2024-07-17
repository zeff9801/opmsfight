package yesman.epicfight.client.renderer.patched.item;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class RenderShield extends RenderItemBase {
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, Hand hand, HumanoidArmature armature, OpenMatrix4f[] poses, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight) {
		OpenMatrix4f modelMatrix = this.getCorrectionMatrix(stack, entitypatch, hand);
		boolean isInMainhand = (hand == Hand.MAIN_HAND);
		Joint holdingHand = isInMainhand ? armature.toolR : armature.toolL;
		OpenMatrix4f jointTransform = poses[holdingHand.getId()];
		modelMatrix.mulFront(jointTransform);

		poseStack.pushPose();
		this.mulPoseStack(poseStack, modelMatrix);
		TransformType transformType = isInMainhand ? TransformType.THIRD_PERSON_RIGHT_HAND : TransformType.THIRD_PERSON_LEFT_HAND;
		Minecraft mc = Minecraft.getInstance();
		IBakedModel model = mc.getItemRenderer().getItemModelShaper().getItemModel(stack);
		mc.getItemRenderer().render(stack, transformType, !isInMainhand, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, model);

		poseStack.popPose();
	}
}