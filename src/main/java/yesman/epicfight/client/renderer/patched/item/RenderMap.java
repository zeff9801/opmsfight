package yesman.epicfight.client.renderer.patched.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class RenderMap extends RenderItemBase {
	private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, Hand hand, HumanoidArmature armature, OpenMatrix4f[] poses, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight) {
		OpenMatrix4f modelMatrix = this.getCorrectionMatrix(stack, entitypatch, hand);
		boolean isInMainhand = (hand == Hand.MAIN_HAND);
		Joint holdingHand = isInMainhand ? armature.toolR : armature.toolL;
		OpenMatrix4f jointTransform = poses[holdingHand.getId()];
		modelMatrix.mulFront(jointTransform);
		
		poseStack.pushPose();
		this.mulPoseStack(poseStack, modelMatrix);
		
		if (hand == Hand.MAIN_HAND && entitypatch.getOriginal().getOffhandItem().isEmpty()) {
			poseStack.scale(2.0F, 2.0F, 2.0F);
		}
		
		Minecraft mc = Minecraft.getInstance();
		mc.gameRenderer.itemInHandRenderer.renderMap(poseStack, buffer, packedLight, stack);
		IVertexBuilder vertexconsumer = buffer.getBuffer(MAP_BACKGROUND);
	    Matrix4f matrix4f = poseStack.last().pose();
		
		vertexconsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(packedLight).endVertex();
		vertexconsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(packedLight).endVertex();
		vertexconsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(packedLight).endVertex();
		vertexconsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(packedLight).endVertex();

		poseStack.popPose();
    }
}