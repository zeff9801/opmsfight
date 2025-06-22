package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedCapeLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

@OnlyIn(Dist.CLIENT)
public class PPlayerRenderer extends PHumanoidRenderer<AbstractClientPlayerEntity, AbstractClientPlayerPatch<AbstractClientPlayerEntity>, PlayerModel<AbstractClientPlayerEntity>, PlayerRenderer, HumanoidMesh> {
	public PPlayerRenderer() {
		super(() -> Meshes.BIPED, EntityType.PLAYER);

		this.addPatchedLayer(ArrowLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(Deadmau5HeadLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(CapeLayer.class, new PatchedCapeLayer());
		this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer<>());
	}

	@Override
	public void prepareModel(HumanoidMesh model, AbstractClientPlayerEntity entity, AbstractClientPlayerPatch<AbstractClientPlayerEntity> entitypatch, PlayerRenderer vanillaRenderer) {
		super.prepareModel(model, entity, entitypatch, vanillaRenderer);

		if (entitypatch.getOriginal().isSpectator()) {
			model.getAllParts().forEach(part -> part.setHidden(true));
			return;
		}

		model.getAllParts().forEach(part -> part.setHidden(false));
		model.getPart("hat").setHidden(!entity.isModelPartShown(PlayerModelPart.HAT));
		model.getPart("jacket").setHidden(!entity.isModelPartShown(PlayerModelPart.JACKET));
		model.getPart("left_pants_leg").setHidden(!entity.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG));
		model.getPart("right_pants_leg").setHidden(!entity.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG));
		model.getPart("left_sleeve").setHidden(!entity.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
		model.getPart("right_sleeve").setHidden(!entity.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
	}

	public HumanoidMesh getMesh(AbstractClientPlayerPatch<AbstractClientPlayerEntity> entitypatch) {
		return entitypatch.getOriginal().getModelName().equals("slim") ? Meshes.ALEX : Meshes.BIPED;
	}

	public ResourceLocation getTextureLocation(AbstractClientPlayerEntity p_110775_1_) {
		return p_110775_1_.getSkinTextureLocation();
	}
}