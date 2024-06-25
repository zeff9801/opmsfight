package yesman.epicfight.api.collider;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Collider {
	protected final Vector3d modelCenter;
	protected final AxisAlignedBB outerAABB;
	protected Vector3d worldCenter;

	public Collider(Vector3d center, @Nullable AxisAlignedBB outerAABB) {
		this.modelCenter = center;
		this.outerAABB = outerAABB;
		this.worldCenter = new Vector3d(0, 0, 0);
	}

	protected void transform(OpenMatrix4f mat) {
		this.worldCenter = OpenMatrix4f.transform(mat, this.modelCenter);
	}

	public List<Entity> updateAndSelectCollideEntity(LivingEntityPatch<?> entitypatch, AttackAnimation attackAnimation, float prevElapsedTime, float elapsedTime, Joint joint, float attackSpeed) {
		OpenMatrix4f transformMatrix;
		//Armature armature = entitypatch.getArmature();
		Armature armature = entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature();
		int pathIndex = armature.searchPathIndex(joint.getName());

		if (pathIndex == -1) {
			Pose rootPose = new Pose();
			rootPose.putJointData("Root", JointTransform.empty());
			attackAnimation.modifyPose(attackAnimation, rootPose, entitypatch, elapsedTime, 1.0F);
			transformMatrix = rootPose.getOrDefaultTransform("Root").getAnimationBindedMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
		} else {
			transformMatrix = armature.getBindedTransformByJointIndex(attackAnimation.getPoseByTime(entitypatch, elapsedTime, 1.0F), pathIndex);
		}

		OpenMatrix4f toWorldCoord = OpenMatrix4f.createTranslation(-(float)entitypatch.getOriginal().getX(), (float)entitypatch.getOriginal().getY(), -(float)entitypatch.getOriginal().getZ());
		transformMatrix.mulFront(toWorldCoord.mulBack(entitypatch.getModelMatrix(1.0F)));
		this.transform(transformMatrix);

		return this.getCollideEntities(entitypatch.getOriginal());
	}
	public List<Entity> getCollideEntities(Entity entity) {
		List<Entity> list = entity.level.getEntities(entity, this.getHitboxAABB());
		list.removeIf((e) -> !this.isCollide(e));

		return list;
	}

	public abstract Collider deepCopy();

	/** Display on debug mode **/
	@OnlyIn(Dist.CLIENT)
	public abstract void drawInternal(MatrixStack poseStack, IVertexBuilder vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int colliderColor);

	/** Display on debug mode **/
	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed) {
		//Armature armature = entitypatch.getArmature();
		Armature armature = entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature();
		int pathIndex =  armature.searchPathIndex(joint.getName());
		EntityState state = animation.getState(entitypatch, elapsedTime);
		EntityState prevState = animation.getState(entitypatch, prevElapsedTime);
		boolean attacking = prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2);
		Pose prevPose;
		Pose currentPose;

		if (pathIndex == -1) {
			prevPose = new Pose();
			currentPose = new Pose();
			prevPose.putJointData("Root", JointTransform.empty());
			currentPose.putJointData("Root", JointTransform.empty());
			animation.modifyPose(animation, prevPose, entitypatch, prevElapsedTime, 0.0F);
			animation.modifyPose(animation, currentPose, entitypatch, elapsedTime, 1.0F);
		} else {
			prevPose = animation.getPoseByTime(entitypatch, partialTicks, 0.0F);
			currentPose = animation.getPoseByTime(entitypatch, elapsedTime, 1.0F);
		}

		this.drawInternal(poseStack, buffer.getBuffer(this.getRenderType()), armature, joint, prevPose, currentPose, partialTicks, attacking ? 0xFFFF0000 : -1);
	}

	@OnlyIn(Dist.CLIENT)
	public abstract RenderType getRenderType();

	protected abstract boolean isCollide(Entity opponent);

	protected AxisAlignedBB getHitboxAABB() {
		return this.outerAABB.move(-this.worldCenter.x, this.worldCenter.y, -this.worldCenter.z);
	}

	@Override
	public String toString() {
		return "[ColliderInfo] type: " + this.getClass() + " center: " + this.modelCenter;
	}
}