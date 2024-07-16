package yesman.epicfight.api.collider;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class MultiOBBCollider extends MultiCollider<OBBCollider> {
	public MultiOBBCollider(int arrayLength, double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
		super(arrayLength, centerX, centerY, centerZ, null);

		AxisAlignedBB aabb = OBBCollider.getInitialAABB(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
		OBBCollider colliderForAll = new OBBCollider(aabb, vertexX, vertexY, vertexZ, centerX, centerY, centerZ);

		for (int i = 0; i < arrayLength; i++) {
			this.colliders.add(colliderForAll);
		}
	}

	public MultiOBBCollider(OBBCollider... colliders) {
		super(colliders);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void draw(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed) {
		int colliderCount = Math.max(Math.round((this.numberOfColliders + animation.getProperty(AttackAnimationProperty.EXTRA_COLLIDERS).orElse(0)) * attackSpeed), this.numberOfColliders);
		float partialScale = 1.0F / (colliderCount - 1);
		float interpolation = 0.0F;
		//Armature armature = entitypatch.getArmature();
		Armature armature = entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature();
		int pathIndex =  armature.searchPathIndex(joint.getName());
		EntityState state = animation.getState(entitypatch, elapsedTime);
		EntityState prevState = animation.getState(entitypatch, prevElapsedTime);
		boolean attacking = prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2);
		List<OBBCollider> colliders = Lists.newArrayList();
		float index = 0.0F;
		float interIndex = Math.min((float)(this.numberOfColliders - 1) / (colliderCount - 1), 1.0F);

		for (int i = 0; i < colliderCount; i++) {
			colliders.add(this.colliders.get((int)index).deepCopy());
			index += interIndex;
		}

		for (OBBCollider obbCollider : colliders) {
			float pt1 = prevElapsedTime + (elapsedTime - prevElapsedTime) * partialTicks;
			float pt2 = prevElapsedTime + (elapsedTime - prevElapsedTime) * interpolation;
			TransformSheet coordTransform = animation.getCoord();
			Vec3f p1 = coordTransform.getInterpolatedTranslation(pt1);
			Vec3f p2 = coordTransform.getInterpolatedTranslation(pt2);
			Vector3f gap = new Vector3f(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);

			poseStack.pushPose();
			poseStack.translate(gap.x(), gap.y(), gap.z());

			Pose pose;

			if (pathIndex == -1) {
				pose = new Pose();
				pose.putJointData("Root", JointTransform.empty());
				animation.modifyPose(animation, pose, entitypatch, elapsedTime, 1.0F);
			} else {
				pose = animation.getPoseByTime(entitypatch, pt2, 1.0F);
			}

			obbCollider.drawInternal(poseStack, buffer.getBuffer(this.getRenderType()), armature, joint, pose, pose, 1.0F, attacking ? 0xFFFF0000 : -1);
			poseStack.popPose();

			interpolation += partialScale;
		}
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderType getRenderType() {
		return this.colliders.get(0).getRenderType();
	}
}