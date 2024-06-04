package yesman.epicfight.api.client.animation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.Layer.Priority;
import yesman.epicfight.api.client.model.ClientModels;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class ClientAnimator extends Animator {
	public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
		return entitypatch.isLogicalClient() ? new ClientAnimator(entitypatch) : ServerAnimator.getAnimator(entitypatch);
	}
	
	private final Map<LivingMotion, StaticAnimation> compositeLivingAnimations;
	private final Map<LivingMotion, StaticAnimation> defaultLivingAnimations;
	private final Map<LivingMotion, StaticAnimation> defaultCompositeLivingAnimations;
	public final Layer.BaseLayer baseLayer;
	private LivingMotion currentMotion;
	private LivingMotion currentCompositeMotion;
	
	public ClientAnimator(LivingEntityPatch<?> entitypatch) {
		this.entitypatch = entitypatch;
		this.currentMotion = LivingMotions.IDLE;
		this.currentCompositeMotion = LivingMotions.IDLE;
		this.compositeLivingAnimations = Maps.newHashMap();
		this.defaultLivingAnimations = Maps.newHashMap();
		this.defaultCompositeLivingAnimations = Maps.newHashMap();
		this.baseLayer = new Layer.BaseLayer(null);
	}
	
	/** Play an animation by animation instance **/
	@Override
	public void playAnimation(StaticAnimation nextAnimation, float convertTimeModifier) {
		Layer layer = nextAnimation.getLayerType() == Layer.LayerType.BASE_LAYER ? this.baseLayer : this.baseLayer.compositeLayers.get(nextAnimation.getPriority());
		layer.paused = false;
		layer.playAnimation(nextAnimation, this.entitypatch, convertTimeModifier);
	}
	
	@Override
	public void playAnimationInstantly(StaticAnimation nextAnimation) {
		this.baseLayer.paused  = false;
		this.baseLayer.playAnimationInstant(nextAnimation, this.entitypatch);
	}
	
	@Override
	public void reserveAnimation(StaticAnimation nextAnimation) {
		this.baseLayer.paused = false;
		this.baseLayer.nextAnimation = nextAnimation;
	}

	@Override
	public void addLivingAnimation(LivingMotion livingMotion, StaticAnimation animation) {
		Layer.LayerType layerType = animation.getLayerType();
		boolean isBaseLayer = (layerType == Layer.LayerType.BASE_LAYER);

		Map<LivingMotion, StaticAnimation> storage = layerType == Layer.LayerType.BASE_LAYER ? this.livingAnimations : this.compositeLivingAnimations;
		LivingMotion compareMotion = layerType == Layer.LayerType.BASE_LAYER ? this.currentMotion : this.currentCompositeMotion;
		Layer layer = layerType == Layer.LayerType.BASE_LAYER ? this.baseLayer : this.baseLayer.compositeLayers.get(animation.getPriority());
		storage.put(livingMotion, animation);

		if (livingMotion == compareMotion) {
			EntityState state = this.getEntityState();

			if (!state.inaction()) {
				layer.playLivingAnimation(animation, this.entitypatch);
			}
		}

		if (isBaseLayer) {
			animation.getProperty(ClientAnimationProperties.MULTILAYER_ANIMATION).ifPresent(multilayerAnimation -> {
				this.compositeLivingAnimations.put(livingMotion, multilayerAnimation);

				if (livingMotion == this.currentCompositeMotion) {
					EntityState state = getEntityState();

					if (!state.inaction()) {
						layer.playLivingAnimation(multilayerAnimation, this.entitypatch);
					}
				}
			});
		}
	}
	
	protected void addBaseLivingAnimation(LivingMotion livingMotion, StaticAnimation animation) {
		this.livingAnimations.put(livingMotion, animation);
		
		if (livingMotion == this.currentMotion) {
			EntityState state = this.getEntityState();
			
			if (!state.inaction()) {
				this.playAnimation(animation, 0.0F);
			}
		}
	}
	
	protected void addCompositeLivingAnimation(LivingMotion livingMotion, StaticAnimation animation) {
		if (animation != null) {
			this.compositeLivingAnimations.put(livingMotion, animation);
			
			if (livingMotion == this.currentCompositeMotion) {
				EntityState state = this.getEntityState();
				
				if (!state.inaction()) {
					this.playAnimation(animation, 0.0F);
				}
			}
		}
	}
	
	public void setCurrentMotionsAsDefault() {
		this.defaultLivingAnimations.putAll(this.livingAnimations);
		this.defaultCompositeLivingAnimations.putAll(this.compositeLivingAnimations);
	}

	@Override
	public void resetLivingAnimations() {
		super.resetLivingAnimations();
		this.compositeLivingAnimations.clear();
		this.defaultLivingAnimations.forEach(this::addLivingAnimation);
		this.defaultCompositeLivingAnimations.forEach(this::addLivingAnimation);
	}

	public StaticAnimation getLivingMotion(LivingMotion motion) {
		return this.livingAnimations.getOrDefault(motion, this.livingAnimations.get(LivingMotions.IDLE));
	}

	public StaticAnimation getCompositeLivingMotion(LivingMotion motion) {
		return this.compositeLivingAnimations.getOrDefault(motion, Animations.DUMMY_ANIMATION);
	}
	
	public void setPoseToModel(float partialTicks) {
		Joint rootJoint = this.entitypatch.getEntityModel(ClientModels.LOGICAL_CLIENT).getArmature().getRootJoint();
		this.applyPoseToJoint(rootJoint, new OpenMatrix4f(), this.getPose(partialTicks), partialTicks);
	}
	
	public void applyPoseToJoint(Joint joint, OpenMatrix4f parentTransform, Pose pose, float partialTicks) {
		OpenMatrix4f result = pose.getOrDefaultTransform(joint.getName()).getAnimationBindedMatrix(joint, parentTransform);
		joint.setPoseTransform(result);
		
		for (Joint joints : joint.getSubJoints()) {
			this.applyPoseToJoint(joints, result, pose, partialTicks);
		}
	}
	
	@Override
	public void init() {
		this.entitypatch.initAnimator(this);
		StaticAnimation idleMotion = this.livingAnimations.get(this.currentMotion);
		this.baseLayer.playAnimationInstant(idleMotion, this.entitypatch);
	}
	
	@Override
	public void poseTick() {
		this.prevPose = this.currentPose;
		this.currentPose = this.getComposedLayerPose(1.0F);
	}
	
	@Override
	public void tick() {
		this.baseLayer.update(this.entitypatch);
		this.poseTick();
		
		if (this.baseLayer.animationPlayer.isEnd() && this.baseLayer.nextAnimation == null && this.currentMotion != LivingMotions.DEATH) {
			this.entitypatch.updateMotion(false);
			this.baseLayer.playAnimation(this.getLivingMotion(this.entitypatch.currentLivingMotion), this.entitypatch, 0.0F);
		}
		
		if (!this.compareCompositeMotion(this.entitypatch.currentCompositeMotion)) {
			if (this.compositeLivingAnimations.containsKey(this.entitypatch.currentCompositeMotion)) {
				this.playAnimation(this.getCompositeLivingMotion(this.entitypatch.currentCompositeMotion), 0.0F);
			} else {
				this.getCompositeLayer(Layer.Priority.MIDDLE).off(this.entitypatch);
			}
		}
		
		if (!this.compareMotion(this.entitypatch.currentLivingMotion)) {
			if (this.livingAnimations.containsKey(this.entitypatch.currentLivingMotion)) {
				this.baseLayer.playAnimation(this.getLivingMotion(this.entitypatch.currentLivingMotion), this.entitypatch, 0.0F);
			}
		}
		
		this.currentMotion = this.entitypatch.currentLivingMotion;
		this.currentCompositeMotion = this.entitypatch.currentCompositeMotion;
	}
	
	@Override
	public void playDeathAnimation() {
		this.playAnimation(this.livingAnimations.get(LivingMotions.DEATH), 0.0F);
		this.currentMotion = LivingMotions.DEATH;
	}
	
	public StaticAnimation getJumpAnimation() {
		return this.livingAnimations.get(LivingMotions.JUMP);
	}
	
	public Layer getCompositeLayer(Layer.Priority priority) {
		return this.baseLayer.compositeLayers.get(priority);
	}

	public Pose getComposedLayerPose(float partialTicks) {
		Pose composedPose = new Pose();
		Pose baseLayerPose = this.baseLayer.getEnabledPose(this.entitypatch, partialTicks);
		Map<Layer.Priority, Pair<DynamicAnimation, Pose>> layerPoses = Maps.newLinkedHashMap();

		composedPose.putJointData(baseLayerPose);

		for (Layer.Priority priority : this.baseLayer.baseLayerPriority.uppers()) {
			Layer compositeLayer = this.baseLayer.compositeLayers.get(priority);

			if (priority == Layer.Priority.LOWEST && this.baseLayer.animationPlayer.getAnimation().isMainFrameAnimation()) {
				continue;
			}

			if (!compositeLayer.isDisabled()) {
				Pose layerPose = compositeLayer.getEnabledPose(this.entitypatch, partialTicks);
				layerPoses.put(priority, Pair.of(compositeLayer.animationPlayer.getAnimation(), layerPose));
				composedPose.putJointData(layerPose);
			}
		}

		Joint rootJoint = this.entitypatch.getEntityModel(ClientModels.LOGICAL_CLIENT).getArmature().getRootJoint(); //TODO change
		applyBindModifier(this.entitypatch, baseLayerPose, composedPose, rootJoint, layerPoses);

		return composedPose;
	}

	public Pose getComposedLayerPoseBelow(Layer.Priority priorityLimit, float partialTicks) {
		Pose composedPose = this.baseLayer.getEnabledPose(this.entitypatch, partialTicks);
		Pose baseLayerPose = this.baseLayer.getEnabledPose(this.entitypatch, partialTicks);
		Map<Layer.Priority, Pair<DynamicAnimation, Pose>> layerPoses = Maps.newLinkedHashMap();

		for (Layer.Priority priority : priorityLimit.lowers()) {
			Layer compositeLayer = this.baseLayer.compositeLayers.get(priority);

			if (priority == Layer.Priority.LOWEST && this.baseLayer.animationPlayer.getAnimation().isMainFrameAnimation()) {
				continue;
			}

			if (!compositeLayer.isDisabled()) {
				Pose layerPose = compositeLayer.getEnabledPose(this.entitypatch, partialTicks);
				layerPoses.put(priority, Pair.of(compositeLayer.animationPlayer.getAnimation(), layerPose));
				composedPose.putJointData(layerPose);
			}
		}

		Joint rootJoint = this.entitypatch.getEntityModel(ClientModels.LOGICAL_CLIENT).getArmature().getRootJoint(); //TODO change

		if (!layerPoses.isEmpty()) {
			applyBindModifier(this.entitypatch, baseLayerPose, composedPose, rootJoint, layerPoses);
		}

		return composedPose;
	}

	public static void applyBindModifier(LivingEntityPatch<?> entitypatch, Pose basePose, Pose result, Joint joint, Map<Layer.Priority, Pair<DynamicAnimation, Pose>> poses) {
		List<Priority> list = Lists.newArrayList(poses.keySet());
		Collections.reverse(list);

		for (Layer.Priority priority : list) {
			DynamicAnimation nowPlaying = poses.get(priority).getFirst();

			if (nowPlaying.isJointEnabled(entitypatch, priority, joint.getName())) {
				JointMask.BindModifier bindModifier = nowPlaying.getBindModifier(entitypatch, priority, joint.getName());

				if (bindModifier != null) {
					bindModifier.modify(entitypatch, basePose, result, priority, joint, poses);
				}

				break;
			}
		}

		for (Joint subJoints : joint.getSubJoints()) {
			applyBindModifier(entitypatch, basePose, result, subJoints, poses);
		}
	}


	public boolean compareMotion(LivingMotion motion) {

        return this.currentMotion == motion;
	}
	
	public boolean compareCompositeMotion(LivingMotion motion) {
		return this.currentCompositeMotion == motion;
	}
	
	public void resetMotion() { 
		this.currentMotion = LivingMotions.IDLE;
		this.entitypatch.currentLivingMotion = LivingMotions.IDLE;
	}
	
	public void resetCompositeMotion() {
		this.currentCompositeMotion = LivingMotions.NONE;
		this.entitypatch.currentCompositeMotion = LivingMotions.NONE;
	}
	
	public boolean isAiming() {
		return this.currentCompositeMotion == LivingMotions.AIM;
	}
	
	public void playReboundAnimation() {
		if (this.compositeLivingAnimations.containsKey(LivingMotions.SHOT)) {
			this.playAnimation(this.compositeLivingAnimations.get(LivingMotions.SHOT), 0.0F);
			this.entitypatch.currentCompositeMotion = LivingMotions.NONE;
			this.resetCompositeMotion();
		}
	}
	
	@Override
	public AnimationPlayer getPlayerFor(DynamicAnimation playingAnimation) {
		for (Layer layer : this.baseLayer.compositeLayers.values()) {
			if (layer.animationPlayer.getAnimation().equals(playingAnimation)) {
				return layer.animationPlayer;
			}
		}
		
		return this.baseLayer.animationPlayer;
	}
	public void offAllLayers() {
		for (Layer layer : this.baseLayer.compositeLayers.values()) {
			layer.off(this.entitypatch);
		}
	}
	
	public LivingEntityPatch<?> getOwner() {
		return this.entitypatch;
	}
	
	@Override
	public EntityState getEntityState() {
		TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap = new TypeFlexibleHashMap<> (false);

		for (Layer layer : this.baseLayer.compositeLayers.values()) {
			if (!layer.disabled) {
				stateMap.putAll(layer.animationPlayer.getAnimation().getStatesMap(this.entitypatch, layer.animationPlayer.getElapsedTime()));
			}

			if (layer.priority == this.baseLayer.baseLayerPriority) {
				stateMap.putAll(this.baseLayer.animationPlayer.getAnimation().getStatesMap(this.entitypatch, this.baseLayer.animationPlayer.getElapsedTime()));
			}
		}

		return new EntityState(stateMap);
	}
}