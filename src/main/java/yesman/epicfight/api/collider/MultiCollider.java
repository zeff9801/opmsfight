package yesman.epicfight.api.collider;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class MultiCollider<T extends Collider> extends Collider {
	protected final int numberOfColliders;
	protected final List<T> colliders;
	public AxisAlignedBB entityCallAABB;

	public MultiCollider(int arrayLength, double centerX, double centerY, double centerZ, AxisAlignedBB entityCallAABB) {
		super(new Vector3d(centerX, centerY, centerZ), entityCallAABB);
		this.entityCallAABB = entityCallAABB;
		this.numberOfColliders = arrayLength;
		this.colliders = Lists.newArrayList();
	}

	public MultiCollider(T... colliders) {
		this(colliders.length, colliders[0].modelCenter.x, colliders[0].modelCenter.y, colliders[0].modelCenter.z, colliders[0].outerAABB);
		for (int i = 0; i < colliders.length; i++) {
			this.colliders.add(colliders[i]);
		}
	}

	@Override
	public void transform(OpenMatrix4f modelMatrix) {
		for (T collider : this.colliders) {
			collider.transform(modelMatrix);
		}
		super.transform(modelMatrix);
	}

	@Override
	public List<Entity> getCollideEntities(Entity entity) {
		List<Entity> collidedEntities = Lists.newArrayList();
		List<Entity> list = entity.level.getEntities(entity, this.getHitboxAABB(), (e) -> {
			if (e == entity || e.isSpectator()) {
				return false;
			}

			if (e instanceof PartEntity<?> part && part.getParent().is(entity)) {
				return false;
			}

			return true;
		});

		for (Entity opponent : list) {
			for (T collider : this.colliders) {
				if (collider.isCollide(opponent)) {
					collidedEntities.add(opponent);
					break;
				}
			}
		}

		return collidedEntities;
	}

	@Override
	public boolean isCollide(Entity opponent) {
		throw new UnsupportedOperationException("Don't call this directly");
	}

	@Override
	protected AxisAlignedBB getHitboxAABB() {
		AxisAlignedBB combined = null;

		for (Collider collider : this.colliders) {
			AxisAlignedBB box = collider.getHitboxAABB();

			if (combined == null) {
				combined = box;
			} else {
				double minX = Math.min(combined.minX, box.minX);
				double minY = Math.min(combined.minY, box.minY);
				double minZ = Math.min(combined.minZ, box.minZ);
				double maxX = Math.max(combined.maxX, box.maxX);
				double maxY = Math.max(combined.maxY, box.maxY);
				double maxZ = Math.max(combined.maxZ, box.maxZ);
				combined = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
			}
		}

		if (combined != null) {
			return combined;
		}

		if (this.outerAABB != null) {
			return this.outerAABB.move(-this.worldCenter.x, this.worldCenter.y, -this.worldCenter.z);
		}

		return new AxisAlignedBB(this.worldCenter.x, this.worldCenter.y, this.worldCenter.z, this.worldCenter.x, this.worldCenter.y, this.worldCenter.z);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void drawInternal(MatrixStack poseStack, IVertexBuilder vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int colliderColor) {
		throw new UnsupportedOperationException("Don't call this directly");
	}

	@OnlyIn(Dist.CLIENT)
	public abstract void draw(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed);

	@Override
	public Collider deepCopy() {
		throw new UnsupportedOperationException("Don't call this directly");
	}

	@Override
	public CompoundNBT serialize(CompoundNBT resultTag) {
		resultTag = super.serialize(resultTag);

		if (resultTag == null) {
			resultTag = new CompoundNBT();
		}

		resultTag.putInt("number", this.numberOfColliders);

		ListNBT center = new ListNBT();
		center.add(DoubleNBT.valueOf(this.modelCenter.x));
		center.add(DoubleNBT.valueOf(this.modelCenter.y));
		center.add(DoubleNBT.valueOf(this.modelCenter.z));
		resultTag.put("center", center);

		return resultTag;
	}

	@OnlyIn(Dist.CLIENT)
	protected Pose getPoseForCollider(AttackAnimation animation, Joint joint, Armature armature, LivingEntityPatch<?> entitypatch, float time, float weight) {
		int pathIndex = armature.searchPathIndex(joint.getName());
		Pose pose;

		if (pathIndex == -1) {
			pose = new Pose();
			pose.putJointData("Root", JointTransform.empty());
			animation.modifyPose(animation, pose, entitypatch, time, weight);
		} else {
			pose = animation.getPoseByTime(entitypatch, time, weight);
		}

		return pose;
	}

	@OnlyIn(Dist.CLIENT)
	protected net.minecraft.util.math.vector.Vector3f getGap(TransformSheet coordTransform, float t1, float t2, Vec3f holder1, Vec3f holder2) {
		Vec3f p1 = coordTransform.getInterpolatedTranslation(t1, holder1);
		Vec3f p2 = coordTransform.getInterpolatedTranslation(t2, holder2);
		return new net.minecraft.util.math.vector.Vector3f(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
	}
}
