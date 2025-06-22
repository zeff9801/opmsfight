package yesman.epicfight.client.renderer.patched.entity;


import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.client.model.MeshProvider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.PatchedHeadLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class PHumanoidRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends BipedModel<E>, R extends LivingRenderer<E, M>, AM extends HumanoidMesh> extends PatchedLivingEntityRenderer<E, T, M, R, AM> {
	private final MeshProvider<AM> mesh;

	public PHumanoidRenderer(MeshProvider<AM> mesh, EntityType<?> entityType) {
		super(entityType);

		this.mesh = mesh;
		this.addPatchedLayer(BipedArmorLayer.class, new WearableItemLayer<>(this.mesh, false));
		this.addPatchedLayer(HeldItemLayer.class, new PatchedItemInHandLayer<>());
		this.addPatchedLayer(HeadLayer.class, new PatchedHeadLayer<>());
	}

	@Override
	protected void setJointTransforms(T entitypatch, Armature armature, Pose pose, float partialTicks) {
		if (entitypatch.getOriginal().isBaby()) {
			pose.getOrDefaultTransform("Head").frontResult(JointTransform.getScale(new Vec3f(1.25F, 1.25F, 1.25F)), OpenMatrix4f::mul);
		}
	}

	@Override
	protected float getDefaultLayerHeightCorrection() {
		return 0.75F;
	}

	@Override
	public MeshProvider<AM> getDefaultMesh() {
		return this.mesh;
	}
}