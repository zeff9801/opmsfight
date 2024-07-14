package yesman.epicfight.api.animation.types;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.client.animation.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.JointMaskEntry;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActionAnimation extends MainFrameAnimation {
	public ActionAnimation(float convertTime, String path, Armature armature) {
		this(convertTime, Float.MAX_VALUE, path, armature);
	}

	public ActionAnimation(float convertTime, String path, Armature armature, boolean noRegister) {
		this(convertTime, Float.MAX_VALUE, path, armature, noRegister);
	}

	public ActionAnimation(float convertTime, float postDelay, String path, Armature armature) {
		this(convertTime, postDelay, path, armature, false);
	}

	public ActionAnimation(float convertTime, float postDelay, String path, Armature armature, boolean noRegister) {
		super(convertTime, path, armature, noRegister);

		this.stateSpectrumBlueprint.clear()
				.newTimePair(0.0F, postDelay)
				.addState(EntityState.MOVEMENT_LOCKED, true)
				.addState(EntityState.UPDATE_LIVING_MOTION, false)
				.addState(EntityState.CAN_BASIC_ATTACK, false)
				.addState(EntityState.CAN_SKILL_EXECUTION, false)
				.newTimePair(0.01F, postDelay)
				.addState(EntityState.TURNING_LOCKED, true)
				.newTimePair(0.0F, Float.MAX_VALUE)
				.addState(EntityState.INACTION, true);

		this.addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
	}
	
	public <V> ActionAnimation addProperty(AnimationProperty.MoveCoordFunctions<V> propertyType, V value) {
		this.properties.put(propertyType, value);
		return this;
	}


	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);

		entitypatch.cancelAnyAction();

	//	if (entitypatch.shouldMoveOnCurrentSide(this)) {
			entitypatch.correctRotation();

			if (this.getProperty(AnimationProperty.MoveCoordFunctions.STOP_MOVEMENT).orElse(false)) {
				entitypatch.getOriginal().setDeltaMovement(0.0D, entitypatch.getOriginal().getDeltaMovement().y, 0.0D);
				entitypatch.getOriginal().xxa = 0.0F;
				entitypatch.getOriginal().yya = 0.0F;
				entitypatch.getOriginal().zza = 0.0F;
			}

			//AnimationProperty.MoveCoordSetter moveCoordSetter = this.getProperty(AnimationProperty.MoveCoordFunctions.COORD_SET_BEGIN).orElse(AnimationProperty.AttackAnimationProperty.RAW_COORD);
			//moveCoordSetter.set(this, entitypatch, entitypatch.getArmature().getActionAnimationCoord());


			AnimationProperty.MoveCoordSetter actionCoordSetter = this.getProperty(AnimationProperty.MoveCoordFunctions.COORD_SET_BEGIN).orElse((self, entitypatch$2, transformSheet) -> {
				transformSheet.readFrom(self.jointTransforms.get("Root"));
			});

			entitypatch.getAnimator().getPlayerFor(this).setActionAnimationCoord(this, entitypatch, actionCoordSetter);
		//}

	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		this.move(entitypatch, this);
	}
	
	@Override
	public void linkTick(LivingEntityPatch<?> entitypatch, LinkAnimation linkAnimation) {
		this.move(entitypatch, linkAnimation);
	};
	
	private void move(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		if (!this.validateMovement(entitypatch, animation)) {
			return;
		}

		EntityState state = this.getState(entitypatch, entitypatch.getAnimator().getPlayerFor(this).getElapsedTime());

		if (state.inaction()) {
			LivingEntity livingentity = entitypatch.getOriginal();
			Vec3f vec3 = this.getCoordVector(entitypatch, animation);
			BlockPos blockpos = new BlockPos(livingentity.getX(), livingentity.getBoundingBox().minY - 1.0D, livingentity.getZ());
			BlockState blockState = livingentity.level.getBlockState(blockpos);
			ModifiableAttributeInstance movementSpeed = livingentity.getAttribute(Attributes.MOVEMENT_SPEED);
			boolean soulboost = blockState.is(BlockTags.SOUL_SPEED_BLOCKS) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, livingentity) > 0;
			double speedFactor = soulboost ? 1.0D : livingentity.level.getBlockState(blockpos).getBlock().getSpeedFactor();
			double moveMultiplier = this.getProperty(AnimationProperty.MoveCoordFunctions.AFFECT_SPEED).orElse(false) ? (movementSpeed.getValue() / movementSpeed.getBaseValue()) : 1.0F;
			livingentity.move(MoverType.SELF, new Vector3d(vec3.x * moveMultiplier, vec3.y, vec3.z * moveMultiplier * speedFactor));
		}
	}

	protected boolean validateMovement(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		if (!entitypatch.shouldMoveOnCurrentSide(this)) {
			return false;
		}

		if (animation.isLinkAnimation()) {
			if (!this.getProperty(AnimationProperty.MoveCoordFunctions.MOVE_ON_LINK).orElse(true)) {
				return false;
			} else {
				return this.shouldMove(0.0F);
			}
		} else {
			return this.shouldMove(entitypatch.getAnimator().getPlayerFor(animation).getElapsedTime());
		}
	}
	private boolean shouldMove(float currentTime) {
		if (this.properties.containsKey(AnimationProperty.MoveCoordFunctions.MOVE_TIME)) {
			ActionTime[] actionTimes = this.getProperty(AnimationProperty.MoveCoordFunctions.MOVE_TIME).get();
			for (ActionTime actionTime : actionTimes) {
				if (actionTime.begin <= currentTime && currentTime <= actionTime.end) {
					return true;
				}
			}
			
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		if (this.getProperty(AnimationProperty.MoveCoordFunctions.COORD).isEmpty()) {
			JointTransform jt = pose.getOrDefaultTransform("Root");
			Vec3f jointPosition = jt.translation();
			OpenMatrix4f toRootTransformApplied = entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature().searchJointByName("Root").getLocalTrasnform().removeTranslation();
			OpenMatrix4f toOrigin = OpenMatrix4f.invert(toRootTransformApplied, null);
			Vec3f worldPosition = OpenMatrix4f.transform3v(toRootTransformApplied, jointPosition, null);
			worldPosition.x = 0.0F;
			worldPosition.y = (this.getProperty(AnimationProperty.MoveCoordFunctions.MOVE_VERTICAL).orElse(false) && worldPosition.y > 0.0F) ? 0.0F : worldPosition.y;
			worldPosition.z = 0.0F;
			OpenMatrix4f.transform3v(toOrigin, worldPosition, worldPosition);
			jointPosition.x = worldPosition.x;
			jointPosition.y = worldPosition.y;
			jointPosition.z = worldPosition.z;
		}

		super.modifyPose(animation, pose, entitypatch, time, partialTicks);
	}

	@Override
	public void setLinkAnimation(DynamicAnimation fromAnimation, Pose startPose, boolean isOnSameLayer, float convertTimeModifier, LivingEntityPatch<?> entitypatch, LinkAnimation dest) {
		dest.resetNextStartTime();

		float playTime = this.getPlaySpeed(entitypatch, dest);
		AnimationProperty.PlaybackSpeedModifier playSpeedModifier = this.getRealAnimation().getProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER).orElse(null);

		if (playSpeedModifier != null) {
			playTime = playSpeedModifier.modify(this, entitypatch, playTime, 0.0F, playTime);
		}

		playTime = Math.abs(playTime);
		playTime *= EpicFightOptions.A_TICK;

		float linkTime = convertTimeModifier > 0.0F ? convertTimeModifier + this.convertTime : this.convertTime;
		float totalTime = playTime * (int)Math.ceil(linkTime / playTime);
		float nextStartTime = Math.max(0.0F, -convertTimeModifier);
		nextStartTime += totalTime - linkTime;

		dest.setNextStartTime(nextStartTime);
		dest.getTransfroms().clear();
		dest.setTotalTime(totalTime + 0.001F);
		dest.setConnectedAnimations(fromAnimation, this);

		Pose nextStartPose = this.getPoseByTime(entitypatch, nextStartTime, 1.0F);

		if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
			this.removeRootTranslation(entitypatch, nextStartPose, nextStartTime);
		}

		Map<String, JointTransform> data1 = startPose.getJointTransformData();
		Map<String, JointTransform> data2 = nextStartPose.getJointTransformData();
		Set<String> joint1 = new HashSet<>(isOnSameLayer ? data1.keySet() : Set.of());
		Set<String> joint2 = new HashSet<> (data2.keySet());

		if (entitypatch.isLogicalClient()) {
			JointMaskEntry entry = fromAnimation.getJointMaskEntry(entitypatch, false).orElse(null);
			JointMaskEntry entry2 = this.getJointMaskEntry(entitypatch, true).orElse(null);

			if (entry != null && entitypatch.isLogicalClient()) {
				joint1.removeIf((jointName) -> entry.isMasked(fromAnimation.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER ?
						entitypatch.getClientAnimator().currentMotion() : entitypatch.getClientAnimator().currentCompositeMotion(), jointName));
			}

			if (entry2 != null && entitypatch.isLogicalClient()) {
				joint2.removeIf((jointName) -> entry2.isMasked(this.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER ?
						entitypatch.getCurrentLivingMotion() : entitypatch.currentCompositeMotion, jointName));
			}
		}

		joint1.addAll(joint2);

		if (linkTime != totalTime) {
			Pose pose = this.getPoseByTime(entitypatch, 0.0F, 0.0F);
			Map<String, JointTransform> poseData = pose.getJointTransformData();

			if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
				this.removeRootTranslation(entitypatch, pose, 0.0F);
			}

			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[3];
				keyframes[0] = new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty()));
				keyframes[1] = new Keyframe(linkTime, poseData.get(jointName));
				keyframes[2] = new Keyframe(totalTime, data2.get(jointName));

				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		} else {
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[2];
				keyframes[0] = new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty()));
				keyframes[1] = new Keyframe(totalTime, data2.get(jointName));

				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		}
	}
	public void removeRootTranslation(LivingEntityPatch<?> entitypatch, Pose pose, float poseTime) {
		JointTransform jt = pose.getOrDefaultTransform("Root");

		if (this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).isEmpty()) {
			//Vec3f withPosition = entitypatch.getArmature().getActionAnimationCoord().getInterpolatedTranslation(poseTime);
			Vec3f withPosition = entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature().getActionAnimationCoord().getInterpolatedTranslation(poseTime);
			jt.translation().set(withPosition);
		} else {
			TransformSheet coordTransform = this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).get();
			Vec3f nextCoord = coordTransform.getKeyframes()[0].transform().translation();
			jt.translation().add(0.0F, 0.0F, nextCoord.z);
		}
	}
	protected Vec3f getCoordVector(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		if (this.getProperty(AnimationProperty.MoveCoordFunctions.COORD_SET_TICK).isPresent()) {
			AnimationProperty.MoveCoordSetter moveCoordSetter = this.getProperty(AnimationProperty.MoveCoordFunctions.COORD_SET_TICK).orElse(null);

			if (animation instanceof LinkAnimation) {
				moveCoordSetter.set(animation, entitypatch, animation.jointTransforms.get("Root"));
			} else {
				entitypatch.getAnimator().getPlayerFor(this).setActionAnimationCoord(this, entitypatch, moveCoordSetter);
			}
		}

		TransformSheet rootCoord;

		if (animation instanceof LinkAnimation) {
			rootCoord = animation.jointTransforms.get("Root");
		} else {
			rootCoord = entitypatch.getAnimator().getPlayerFor(this).getActionAnimationCoord();

			if (rootCoord == null) {
				rootCoord = animation.jointTransforms.get("Root");
			}
		}

		LivingEntity livingentity = entitypatch.getOriginal();
		AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
		JointTransform jt = rootCoord.getInterpolatedTransform(player.getElapsedTime());
		JointTransform prevJt = rootCoord.getInterpolatedTransform(player.getPrevElapsedTime());
		Vec4f currentpos = new Vec4f(jt.translation().x, jt.translation().y, jt.translation().z, 1.0F);
		Vec4f prevpos = new Vec4f(prevJt.translation().x, prevJt.translation().y, prevJt.translation().z, 1.0F);
		OpenMatrix4f rotationTransform = entitypatch.getModelMatrix(1.0F).removeTranslation();
		OpenMatrix4f localTransform = entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature().searchJointByName("Root").getLocalTrasnform().removeTranslation();
		rotationTransform.mulBack(localTransform);
		currentpos.transform(rotationTransform);
		prevpos.transform(rotationTransform);
		boolean hasNoGravity = entitypatch.getOriginal().isNoGravity();
		boolean moveVertical = this.getProperty(AnimationProperty.MoveCoordFunctions.MOVE_VERTICAL).orElse(false);
		float dx = prevpos.x - currentpos.x;
		float dy = (moveVertical || hasNoGravity) ? currentpos.y - prevpos.y : 0.0F;
		float dz = prevpos.z - currentpos.z;
		dx = Math.abs(dx) > 0.0000001F ? dx : 0.0F;
		dz = Math.abs(dz) > 0.0000001F ? dz : 0.0F;

		if (moveVertical && currentpos.y > 0.0F && !hasNoGravity) {
			Vector3d motion = livingentity.getDeltaMovement();
			livingentity.setDeltaMovement(motion.x, motion.y <= 0 ? (motion.y + 0.08D) : motion.y, motion.z);
		}

		return new Vec3f(dx, dy, dz);
	}

	@OnlyIn(Dist.CLIENT)
	public boolean shouldPlayerMove(LocalPlayerPatch playerpatch) {
		return playerpatch.isLogicalClient();
	}

	public static class ActionTime {
		private float begin;
		private float end;
		
		private ActionTime(float begin, float end) {
			this.begin = begin;
			this.end = end;
		}
		
		public static ActionTime crate(float begin, float end) {
			return new ActionTime(begin, end);
		}
	}
}