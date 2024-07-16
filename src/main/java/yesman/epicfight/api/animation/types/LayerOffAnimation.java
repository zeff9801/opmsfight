package yesman.epicfight.api.animation.types;

import net.minecraft.client.Minecraft;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.client.animation.Layer.Priority;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Optional;

public class LayerOffAnimation extends DynamicAnimation {
	private final AnimationClip animationClip = new AnimationClip();

	private DynamicAnimation lastAnimation;
	private Pose lastPose;
	private Priority layerPriority;
	
	public LayerOffAnimation(Priority layerPriority) {
		this.layerPriority = layerPriority;
	}
	
	public void setLastPose(Pose pose) {
		this.lastPose = pose;
	}
	
	@Override
	public void end(LivingEntityPatch<?> entitypatch, boolean isEnd) {
		if (entitypatch.isLogicalClient()) {
			entitypatch.getClientAnimator().baseLayer.disableLayer(this.layerPriority);
		}
	}

	@Override
	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose lowerLayerPose = entitypatch.getClientAnimator().getComposedLayerPoseBelow(this.layerPriority, Minecraft.getInstance().getFrameTime());
		return Pose.interpolatePose(this.lastPose, lowerLayerPose, time / this.totalTime);
	}

	@Override
	public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
		return this.lastAnimation.getJointMaskEntry(entitypatch, useCurrentMotion);
	}

	@Override
	public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
		return this.lastAnimation.getProperty(propertyType);
	}

	public void setLastAnimation(DynamicAnimation animation) {
		this.lastAnimation = animation;
	}

	@Override
	public boolean doesHeadRotFollowEntityHead() {
		return this.lastAnimation.doesHeadRotFollowEntityHead();
	}

	@Override
	public DynamicAnimation getRealAnimation() {
		return Animations.DUMMY_ANIMATION;
	}

	@Override
	public AnimationClip getAnimationClip() {
		return this.animationClip;
	}

	@Override
	public boolean hasTransformFor(String joint) {
		return this.lastPose.getJointTransformData().containsKey(joint);
	}

	@Override
	public boolean isLinkAnimation() {
		return true;
	}
}