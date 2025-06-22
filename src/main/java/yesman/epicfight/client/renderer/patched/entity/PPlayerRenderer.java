package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedCapeLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

@OnlyIn(Dist.CLIENT)
public class PPlayerRenderer extends PHumanoidRenderer<AbstractClientPlayerEntity, AbstractClientPlayerPatch<AbstractClientPlayerEntity>, PlayerModel<AbstractClientPlayerEntity>, PlayerRenderer, HumanoidMesh> {
	public PPlayerRenderer() {
		super(Meshes.BIPED);

		this.addPatchedLayer(ArrowLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(CapeLayer.class, new PatchedCapeLayer());
		this.addPatchedLayer(HeldItemLayer.class, new PatchedItemInHandLayer<>());
	}

	@Override
	protected void prepareModel(HumanoidMesh mesh, AbstractClientPlayerEntity entity, AbstractClientPlayerPatch<AbstractClientPlayerEntity> entitypatch, PlayerRenderer renderer) {
		super.prepareModel(mesh, entity, entitypatch, renderer);

		renderer.setModelProperties(entity);
		PlayerModel<AbstractClientPlayerEntity> model = renderer.getModel();

		mesh.head.setHidden(!model.head.visible);
		mesh.hat.setHidden(!model.hat.visible);
		mesh.jacket.setHidden(!model.jacket.visible);
		mesh.torso.setHidden(!model.body.visible);
		mesh.leftArm.setHidden(!model.leftArm.visible);
		mesh.leftLeg.setHidden(!model.leftLeg.visible);
		mesh.leftPants.setHidden(!model.leftPants.visible);
		mesh.leftSleeve.setHidden(!model.leftSleeve.visible);
		mesh.rightArm.setHidden(!model.rightArm.visible);
		mesh.rightLeg.setHidden(!model.rightLeg.visible);
		mesh.rightPants.setHidden(!model.rightPants.visible);
		mesh.rightSleeve.setHidden(!model.rightSleeve.visible);
	}

	@Override
	public HumanoidMesh getMesh(AbstractClientPlayerPatch<AbstractClientPlayerEntity> entitypatch) {
		return entitypatch.getOriginal().getModelName().equals("slim") ? Meshes.ALEX : Meshes.BIPED;
	}
}