package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class PatchedItemInHandLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, AM extends HumanoidMesh> extends PatchedLayer<E, T, M, LayerRenderer<E, M>, AM> {

	public PatchedItemInHandLayer() {
		super(null);
	}

	@Override
	public void renderLayer(T entitypatch, E entityliving, LayerRenderer<E, M> originalRenderer, MatrixStack matrixStackIn, IRenderTypeBuffer buffer, int packedLightIn, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		if (!(entitypatch.getArmature() instanceof HumanoidArmature humanoidArmature)) {
			return;
		}

		ItemStack mainHandStack = entitypatch.getOriginal().getMainHandItem();
		RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
		
		if (mainHandStack.getItem() != Items.AIR) {
			if (entitypatch.getOriginal().getVehicle() != null) {
				if (!entitypatch.getHoldingItemCapability(Hand.MAIN_HAND).availableOnHorse()) {
					renderEngine.getItemRenderer(mainHandStack).renderUnusableItemMount(mainHandStack, entitypatch, poses, buffer, matrixStackIn, packedLightIn);
					return;
				}
			}
			
			renderEngine.getItemRenderer(mainHandStack).renderItemInHand(mainHandStack, entitypatch, Hand.MAIN_HAND, humanoidArmature, poses, buffer, matrixStackIn, packedLightIn);
		}
		
		
		ItemStack offHandStack = entitypatch.getOriginal().getOffhandItem();
		
		if (entitypatch.isOffhandItemValid()) {
			renderEngine.getItemRenderer(offHandStack).renderItemInHand(offHandStack, entitypatch, Hand.OFF_HAND, humanoidArmature, poses, buffer, matrixStackIn, packedLightIn);
		}
	}
}