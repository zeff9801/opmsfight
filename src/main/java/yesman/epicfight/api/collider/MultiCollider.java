package yesman.epicfight.api.collider;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.entity.PartEntity;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Collections;
import java.util.List;

public abstract class MultiCollider<T extends Collider> extends Collider {
	protected final List<T> colliders = Lists.newArrayList();
	protected final int numberOfColliders;

	public MultiCollider(int arrayLength, double centerX, double centerY, double centerZ, AxisAlignedBB outerAABB) {
		super(new Vector3d(centerX, centerY, centerZ), outerAABB);
		this.numberOfColliders = arrayLength;
	}

	@SafeVarargs
	public MultiCollider(T... colliders) {
		super(null, null);

		Collections.addAll(this.colliders, colliders);
		this.numberOfColliders = colliders.length;
	}

	public MultiCollider<T> deepCopy() {
		return null;
	}

	@Override
	public List<Entity> updateAndSelectCollideEntity(LivingEntityPatch<?> entitypatch, AttackAnimation attackAnimation, float prevElapsedTime, float elapsedTime, Joint joint, float attackSpeed) {
		int numberOf = Math.max(Math.round((this.numberOfColliders + attackAnimation.getProperty(AttackAnimationProperty.EXTRA_COLLIDERS).orElse(0)) * attackSpeed), this.numberOfColliders);
		float partialScale = 1.0F / (numberOf - 1);
		float interpolation = 0.0F;
		List<Collider> colliders = Lists.newArrayList();
		LivingEntity original = entitypatch.getOriginal();
		float index = 0.0F;
		float interIndex = Math.min((float)(this.numberOfColliders - 1) / (numberOf - 1), 1.0F);

		for (int i = 0; i < numberOf; i++) {
			colliders.add(this.colliders.get((int)index).deepCopy());
			index += interIndex;
		}

		AxisAlignedBB outerBox = null;

		for (Collider collider : colliders) {
			OpenMatrix4f transformMatrix;
			Armature armature = entitypatch.getArmature();
			int pathIndex = armature.searchPathIndex(joint.getName());

			if (pathIndex == -1) {
				Pose rootPose = new Pose();
				rootPose.putJointData("Root", JointTransform.empty());
				attackAnimation.modifyPose(attackAnimation, rootPose, entitypatch, elapsedTime, 1.0F);
				transformMatrix = rootPose.getOrDefaultTransform("Root").getAnimationBindedMatrix(entitypatch.getArmature().rootJoint, new OpenMatrix4f()).removeTranslation();
			} else {
				float interpolateTime = prevElapsedTime + (elapsedTime - prevElapsedTime) * interpolation;
				transformMatrix = armature.getBindedTransformByJointIndex(attackAnimation.getPoseByTime(entitypatch, interpolateTime, 1.0F), pathIndex);
			}

			double x = entitypatch.getXOld() + (original.getX() - entitypatch.getXOld()) * interpolation;
			double y = entitypatch.getYOld() + (original.getY() - entitypatch.getYOld()) * interpolation;
			double z = entitypatch.getZOld() + (original.getZ() - entitypatch.getZOld()) * interpolation;
			OpenMatrix4f mvMatrix = OpenMatrix4f.createTranslation(-(float)x, (float)y, -(float)z);
			transformMatrix.mulFront(mvMatrix.mulBack(entitypatch.getModelMatrix(interpolation)));
			collider.transform(transformMatrix);
			interpolation += partialScale;

			if (outerBox == null) {
				outerBox = collider.getHitboxAABB();
			} else {
				outerBox.minmax(collider.getHitboxAABB());
			}
		}

		List<Entity> entities = entitypatch.getOriginal().level.getEntities(entitypatch.getOriginal(), outerBox, (entity) -> {
			if (entity.isSpectator()) {
				return false;
			}

			if (entity instanceof PartEntity) {
				if (((PartEntity<?>)entity).getParent().is(entitypatch.getOriginal())) {
					return false;
				}
			}

			for (Collider collider : colliders) {
				if (collider.isCollide(entity)) {
					return true;
				}
			}

			return false;
		});

		return entities;
	}

	@Override
	public void drawInternal(MatrixStack poseStack, IVertexBuilder vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color) {
		int idx = 0;
		int size = this.colliders.size() - 1;

		for (T collider : this.colliders) {
			float interpolation = (float)idx / (float)size;
			Pose interpolatedPose = Pose.interpolatePose(pose1, pose2, interpolation);
			collider.drawInternal(poseStack, vertexConsumer, armature, joint, interpolatedPose, interpolatedPose, interpolation, color);
			idx++;
		}
	}

	@Override
	public List<Entity> getCollideEntities(Entity entity) {
		List<Entity> list = Lists.newArrayList();

		for (T collider : this.colliders) {
			list.addAll(collider.getCollideEntities(entity));
		}

		return list;
	}

	@Override
	public boolean isCollide(Entity opponent) {
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + " collider count: " + this.numberOfColliders + " " + this.colliders.get(0);
	}
}