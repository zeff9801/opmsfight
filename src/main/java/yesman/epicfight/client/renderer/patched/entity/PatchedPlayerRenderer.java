package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@OnlyIn(Dist.CLIENT)
public class PatchedPlayerRenderer extends PatchedLivingEntityRenderer<AbstractClientPlayerEntity, PlayerPatch<AbstractClientPlayerEntity>, PlayerModel<AbstractClientPlayerEntity>, PlayerRenderer, HumanoidMesh> {
	public PatchedPlayerRenderer() {
		super(EntityType.PLAYER);

		this.addPatchedLayer(ElytraLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(HeldItemLayer.class, new PatchedItemInHandLayer<>());
		this.addPatchedLayer(BipedArmorLayer.class, new WearableItemLayer<>(() -> Meshes.BIPED, true));
		this.addPatchedLayer(HeadLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(ArrowLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(SpinAttackEffectLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(CapeLayer.class, new EmptyLayer<>());
	}

	@Override
	public void render(AbstractClientPlayerEntity entity, PlayerPatch entitypatch, PlayerRenderer renderer, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight, float partialTicks) {
		if (entitypatch.getOriginal().isInvisible()) {
			return;
		}

		super.render(entity, (PlayerPatch<AbstractClientPlayerEntity>)entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);
	}

	@Override
	public yesman.epicfight.api.client.model.MeshProvider<HumanoidMesh> getMeshProvider(PlayerPatch entitypatch) {
		return ((PlayerPatch<AbstractClientPlayerEntity>)entitypatch).getOriginal().getModelName().equals("slim") ? () -> Meshes.ALEX : () -> Meshes.BIPED;
	}

	@Override
	public yesman.epicfight.api.client.model.MeshProvider<HumanoidMesh> getDefaultMesh() {
		return () -> Meshes.BIPED;
	}

	@Override
	protected void setJointTransforms(PlayerPatch entitypatch, yesman.epicfight.api.model.Armature armature, Pose pose, float partialTicks) {
		super.setJointTransforms((PlayerPatch<AbstractClientPlayerEntity>)entitypatch, armature, pose, partialTicks);
	}
} 