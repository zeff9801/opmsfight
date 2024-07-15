package yesman.epicfight.api.animation.types;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.client.animation.JointMaskEntry;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.JointMask.BindModifier;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class DynamicAnimation {
	protected Map<String, TransformSheet> jointTransforms;
	protected final boolean isRepeat;
	protected final float convertTime;
	protected float totalTime = 0.0F;
	
	public DynamicAnimation() {
		this(EpicFightOptions.GENERAL_ANIMATION_CONVERT_TIME, false);
	}
	
	public DynamicAnimation(float convertTime, boolean isRepeat) {
		this.jointTransforms = new HashMap<String, TransformSheet>();
		this.isRepeat = isRepeat;
		this.convertTime = convertTime;
	}
	
	public void addSheet(String jointName, TransformSheet sheet) {
		this.jointTransforms.put(jointName, sheet);
	}
	
	public final Pose getPoseByTimeRaw(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose pose = new Pose();
		for (String jointName : this.jointTransforms.keySet()) {
			if (!entitypatch.isLogicalClient() || this.isJointEnabled(entitypatch, jointName)) {
				pose.putJointData(jointName, this.jointTransforms.get(jointName).getInterpolatedTransform(time));
			}
		}
		return pose;
	}
	public boolean isClientAnimation() {
		return false;
	}
	public boolean hasTransformFor(String joint) {
		return this.getTransfroms().containsKey(joint);
	}

	public boolean isStaticAnimation() {
		return false;
	}
	public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		return 1.0F;
	}
	public ResourceLocation getRegistryName() {
		return new ResourceLocation(EpicFightMod.MODID, "");
	}

	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose pose = new Pose();

		for (String jointName : this.jointTransforms.keySet()) {
			pose.putJointData(jointName, this.jointTransforms.get(jointName).getInterpolatedTransform(time));
		}

		this.modifyPose(this, pose, entitypatch, time, partialTicks);

		return pose;
	}

	@OnlyIn(Dist.CLIENT)
	public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
		return Optional.empty();
	}
	
	/** Modify the pose which also modified in link animation. **/

	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
	}
	
	public void setLinkAnimation(Pose pose1, float convertTimeModifier, LivingEntityPatch<?> entitypatch, LinkAnimation dest) {
		if (!entitypatch.isLogicalClient()) {
			pose1 = Animations.DUMMY_ANIMATION.getPoseByTime(entitypatch, 0.0F, 1.0F);
		}
		
		float totalTime = convertTimeModifier >= 0.0F ? convertTimeModifier + this.convertTime : this.convertTime;
		boolean isNeg = convertTimeModifier < 0.0F;
		float nextStart = isNeg ? -convertTimeModifier : 0.0F;
		
		if (isNeg) {
			dest.nextStartTime = nextStart;
		}
		
		dest.getTransfroms().clear();
		dest.setTotalTime(totalTime);
		dest.setToAnimation(this);
		
		Map<String, JointTransform> data1 = pose1.getJointTransformData();
		Map<String, JointTransform> data2 = this.getPoseByTime(entitypatch, nextStart, 1.0F).getJointTransformData();
		
		for (String jointName : data1.keySet()) {
			if (data1.containsKey(jointName) && data2.containsKey(jointName)) {
				Keyframe[] keyframes = new Keyframe[2];
				keyframes[0] = new Keyframe(0.0F, data1.get(jointName));
				keyframes[1] = new Keyframe(totalTime, data2.get(jointName));
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName,sheet);
			}
		}
	}

	public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
		animationPlayer.setPlayAnimation(this);
		animationPlayer.tick(entitypatch);
		animationPlayer.begin(this, entitypatch);
	}
	
	public void begin(LivingEntityPatch<?> entitypatch) {}
	public void tick(LivingEntityPatch<?> entitypatch) {}
	public void end(LivingEntityPatch<?> entitypatch, boolean isEnd) {}
	public void end(LivingEntityPatch<?> entitypatch, DynamicAnimation nextAnimation, boolean isEnd) {}

	public void linkTick(LivingEntityPatch<?> entitypatch, LinkAnimation linkAnimation) {};
	public void linkTick(LivingEntityPatch<?> entitypatch, DynamicAnimation linkAnimation) {};


	public boolean isJointEnabled(LivingEntityPatch<?> entitypatch, String joint) {
		return this.jointTransforms.containsKey(joint);
	}
	@OnlyIn(Dist.CLIENT)
	public boolean isJointEnabled(LivingEntityPatch<?> entitypatch, Layer.Priority layer, String joint) {
		return this.jointTransforms.containsKey(joint);
	}
	
	public BindModifier getBindModifier(LivingEntityPatch<?> entitypatch, String joint) {
		return null;
	}
	@OnlyIn(Dist.CLIENT)
	public BindModifier getBindModifier(LivingEntityPatch<?> entitypatch, Layer.Priority layer, String joint) {
		return null;
	}

	public EntityState getState(LivingEntityPatch<?> entitypatch, float time) {
		return EntityState.DEFAULT_STATE;
	}

	public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, float time) {
		return new TypeFlexibleHashMap<> (false);
	}

	public <T> T getState(EntityState.StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
		return stateFactor.defaultValue();
	}

	public abstract AnimationClip getAnimationClip();

	public Map<String, TransformSheet> getTransfroms() {
		return this.getAnimationClip().getJointTransforms();
	}
	public float getPlaySpeed(LivingEntityPatch<?> entitypatch) {
		return 1.0F;
	}

	public TransformSheet getCoord() {
		return this.jointTransforms.get("Root");
	}
	
	public DynamicAnimation getRealAnimation() {
		return this;
	}
	
	public void setTotalTime(float totalTime) {
		this.totalTime = totalTime;
	}
	
	public float getTotalTime() {
		return this.totalTime - 0.001F;
	}
	
	public float getConvertTime() {
		return this.convertTime;
	}
	
	public boolean isRepeat() {
		return this.isRepeat;
	}
	
	public boolean canBePlayedReverse() {
		return false;
	}
	
	public int getId() {
		return -1;
	}
	
	public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
		return Optional.empty();
	}
	
	public boolean isMainFrameAnimation() {
		return false;
	}
	
	public boolean isReboundAnimation() {
		return false;
	}

	public boolean isMetaAnimation() {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void renderDebugging(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
		
	}
	public boolean isLinkAnimation() {
		return false;
	}
}