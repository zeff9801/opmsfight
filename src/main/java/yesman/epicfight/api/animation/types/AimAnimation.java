package yesman.epicfight.api.animation.types;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AimAnimation extends StaticAnimation {
	public StaticAnimation lookUp;
	public StaticAnimation lookDown;
	public StaticAnimation lying;
	
	public AimAnimation(float convertTime, boolean repeatPlay, String path1, String path2, String path3, String path4, Model model) {
		super(convertTime, repeatPlay, path1, model);
		this.lookUp = new StaticAnimation(convertTime, repeatPlay, path2, model, true);
		this.lookDown = new StaticAnimation(convertTime, repeatPlay, path3, model, true);
		this.lying = new StaticAnimation(convertTime, repeatPlay, path4, model, true);
	}
	
	public AimAnimation(boolean repeatPlay, String path1, String path2, String path3, String path4, Model model) {
		this(EpicFightOptions.GENERAL_ANIMATION_CONVERT_TIME, repeatPlay, path1, path2, path3, path4, model);
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		ClientAnimator animator = entitypatch.getClientAnimator();
		Layer layer = animator.getCompositeLayer(this.getPriority());
		AnimationPlayer player = layer.animationPlayer;
		
		if (player.getElapsedTime() >= this.totalTime - 0.06F) {
			layer.pause();
		}
	}

	@Override
	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		if (!entitypatch.isFirstPerson()) {
			LivingMotion livingMotion = entitypatch.getCurrentLivingMotion();

			if (livingMotion == LivingMotions.SWIM || livingMotion == LivingMotions.FLY || livingMotion == LivingMotions.CREATIVE_FLY) {
				Pose pose = this.lying.getPoseByTime(entitypatch, time, partialTicks);
				this.modifyPose(this, pose, entitypatch, time, partialTicks);

				return pose;
			} else {
				float pitch = entitypatch.getOriginal().getViewXRot(Minecraft.getInstance().getFrameTime());
				StaticAnimation interpolateAnimation;
				interpolateAnimation = (pitch > 0) ? this.lookDown : this.lookUp;
				Pose pose1 = super.getPoseByTime(entitypatch, time, partialTicks);
				Pose pose2 = interpolateAnimation.getPoseByTime(entitypatch, time, partialTicks);
				this.modifyPose(this, pose2, entitypatch, time, partialTicks);
				Pose interpolatedPose = Pose.interpolatePose(pose1, pose2, (Math.abs(pitch) / 90.0F));

				return interpolatedPose;
			}
		}

		return super.getPoseByTime(entitypatch, time, partialTicks);
	}

	@Override
	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		if (!entitypatch.isFirstPerson()) {
			JointTransform chest = pose.getOrDefaultTransform("Chest");
			JointTransform head = pose.getOrDefaultTransform("Head");
			float f = 90.0F;
			float ratio = (f - Math.abs(entitypatch.getOriginal().xRot)) / f;
			float yawOffset = entitypatch.getOriginal().getVehicle() != null ? entitypatch.getOriginal().getYHeadRot() : entitypatch.getOriginal().yBodyRot;
			MathUtils.mulQuaternion(Vector3f.YP.rotationDegrees(MathHelper.wrapDegrees(yawOffset - entitypatch.getOriginal().getYHeadRot()) * ratio), head.rotation(), head.rotation());
			chest.frontResult(JointTransform.getRotation(Vector3f.YP.rotationDegrees(MathHelper.wrapDegrees(entitypatch.getOriginal().getYHeadRot() - yawOffset) * ratio)), OpenMatrix4f::mulAsOriginFront);
		}
	}
	
	@Override
	public <V> StaticAnimation addProperty(StaticAnimationProperty<V> propertyType, V value) {
		super.addProperty(propertyType, value);
		this.lookDown.addProperty(propertyType, value);
		this.lookUp.addProperty(propertyType, value);
		this.lying.addProperty(propertyType, value);
		return this;
	}
	
	@Override
	public void loadAnimation(IResourceManager resourceManager) {
		load(resourceManager, this);
		load(resourceManager, this.lookUp);
		load(resourceManager, this.lookDown);
		load(resourceManager, this.lying);
	}
}