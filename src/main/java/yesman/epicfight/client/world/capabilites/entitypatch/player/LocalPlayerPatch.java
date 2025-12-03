package yesman.epicfight.client.world.capabilites.entitypatch.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPPlayAnimation;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class LocalPlayerPatch extends AbstractClientPlayerPatch<ClientPlayerEntity> {
	private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
	private Minecraft minecraft;
	private final FirstPersonLayer firstPersonLayer = new FirstPersonLayer();
	private LivingEntity rayTarget;
	private boolean targetLockedOn;
	private float prevStamina;
	private int prevChargingAmount;
	private AnimationSubFileReader.PovSettings povSettings;

	private float lockOnXRot;
	private float lockOnXRotO;
	private float lockOnYRot;
	private float lockOnYRotO;

	@Override
	public void onConstructed(ClientPlayerEntity entity) {
		super.onConstructed(entity);
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public void onJoinWorld(ClientPlayerEntity entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);

		this.eventListeners.addEventListener(EventType.ACTION_EVENT_CLIENT, ACTION_EVENT_UUID, (playerEvent) -> {
			ClientEngine.getInstance().controllEngine.unlockHotkeys();
		});
	}

	public void onRespawnLocalPlayer(ClientPlayerNetworkEvent.RespawnEvent event) {
		this.onJoinWorld(event.getNewPlayer(),
				new EntityJoinWorldEvent(event.getNewPlayer(), event.getNewPlayer().level));
	}

	@Override
	public void updateMotion(boolean considerInaction) {
		super.updateMotion(considerInaction);

		if (!this.getClientAnimator().isAiming()) {
			if (this.currentCompositeMotion == LivingMotions.AIM) {
				this.original.getUseItemRemainingTicks();
				ClientEngine.getInstance().renderEngine.zoomIn();
			}
		}
	}

	@Override
	public void tick(LivingEvent.LivingUpdateEvent event) {
		this.prevStamina = this.getStamina();

		if (this.isChargingSkill()) {
			this.prevChargingAmount = this.getChargingSkill().getChargingAmount(this);
		} else {
			this.prevChargingAmount = 0;
		}

		super.tick(event);
	}

	@Override
	public void clientTick(LivingEvent.LivingUpdateEvent event) {
		this.prevStamina = this.getStamina();

		super.clientTick(event);
		this.updateFirstPersonLayer();
		this.applyPovViewLimit();

		RayTraceResult cameraHitResult = this.minecraft.hitResult;

		if (cameraHitResult != null && cameraHitResult.getType() == RayTraceResult.Type.ENTITY) {
			Entity hit = ((EntityRayTraceResult) cameraHitResult).getEntity();

			if (hit != this.rayTarget && hit != this.original) {
				if (hit instanceof LivingEntity livingentity) {
					if (!(hit instanceof ArmorStandEntity) && !this.targetLockedOn) {
						this.rayTarget = livingentity;
					}
				} else if (hit instanceof PartEntity<?> partEntity) {
					Entity parent = partEntity.getParent();

					if (parent instanceof LivingEntity parentLivingEntity && !this.targetLockedOn) {
						this.rayTarget = parentLivingEntity;
					}
				} else {
					this.rayTarget = null;
				}

				if (this.rayTarget != null) {
					EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(this.getTarget().getId()));
				}
			}
		}

		if (this.rayTarget != null) {
			if (this.targetLockedOn) {
				Vector3d playerPosition = this.original.getEyePosition(1.0F);
				Vector3d targetPosition = this.rayTarget.getEyePosition(1.0F);
				Vector3d toTarget = targetPosition.subtract(playerPosition);
				float yaw = (float) MathUtils.getYRotOfVector(toTarget);
				float pitch = (float) MathUtils.getXRotOfVector(toTarget);
				PointOfView cameraType = this.minecraft.options.getCameraType();
				this.lockOnXRotO = this.lockOnXRot;
				this.lockOnYRotO = this.lockOnYRot;
				float lockOnXRotDst = pitch + (cameraType.isFirstPerson() ? 0.0F : 30.0F);
				lockOnXRotDst = MathHelper.clamp(lockOnXRotDst, 0.0F, 60.0F);

				if (cameraType.isMirrored()) {
					lockOnXRotDst = -lockOnXRotDst;
				}

				float lockOnYRotDst = yaw + (cameraType.isMirrored() ? 180.0F : 0.0F);
				float xDiff = MathHelper.wrapDegrees(lockOnXRotDst - this.lockOnXRotO);
				float yDiff = MathHelper.wrapDegrees(lockOnYRotDst - this.lockOnYRotO);
				float xLerp = MathHelper.clamp(xDiff * 0.4F, -30.0F, 30.0F);
				float yLerp = MathHelper.clamp(yDiff * 0.4F, -30.0F, 30.0F);

				this.lockOnXRot = this.lockOnXRotO + xLerp;
				this.lockOnYRot = this.lockOnYRotO + yLerp;

				if (!this.getEntityState().turningLocked() || this.getEntityState().lockonRotate()) {
					this.original.xRot = (lockOnXRotDst);
					this.original.yRot = (lockOnYRotDst);
				}
			} else {
				this.lockOnXRot = this.original.xRot;
				this.lockOnYRot = this.original.yRot;
				this.lockOnXRotO = this.lockOnXRot;
				this.lockOnYRotO = this.lockOnYRot;
			}

			if (!this.rayTarget.isAlive() || this.getOriginal().distanceToSqr(this.rayTarget) > 400.0D
					|| (this.getAngleTo(this.rayTarget) > 100.0D && !this.targetLockedOn)) {
				this.rayTarget = null;
				EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
			}
		} else {
			this.lockOnXRot = this.original.xRot;
			this.lockOnYRot = this.original.yRot;
			this.targetLockedOn = false;
		}

		CapabilityItem itemCap = this.getHoldingItemCapability(Hand.MAIN_HAND);

		switch (itemCap.getZoomInType()) {
			case ALWAYS:
				ClientEngine.getInstance().renderEngine.zoomIn();
				break;
			case USE_TICK:
				if (this.original.getUseItemRemainingTicks() > 0) {
					ClientEngine.getInstance().renderEngine.zoomIn();
				} else {
					ClientEngine.getInstance().renderEngine.zoomOut(40);
				}

				break;
			case AIMING:
				if (this.getClientAnimator().isAiming()) {
					ClientEngine.getInstance().renderEngine.zoomIn();
				} else {
					ClientEngine.getInstance().renderEngine.zoomOut(40);
				}

				break;
			case CUSTOM:
				// Zoom manually handled
				break;
			default:
				ClientEngine.getInstance().renderEngine.zoomOut(0);
		}
	}

	private void updateFirstPersonLayer() {
		yesman.epicfight.api.animation.types.DynamicAnimation currentPlaying = this.firstPersonLayer.animationPlayer.getAnimation().getRealAnimation();

		boolean noPovAnimation = this.getClientAnimator().iterVisibleLayersUntilFalse(layer -> {
			if (layer.isOff()) {
				return true;
			}

            yesman.epicfight.api.animation.types.DynamicAnimation realAnimation = layer.animationPlayer.getAnimation().getRealAnimation();

            if (!(realAnimation instanceof StaticAnimation)) {
                return true;
            }

            StaticAnimation realStatic = (StaticAnimation)realAnimation;
            Optional<StaticAnimation> optPovAnimation = realStatic.getProperty(ClientAnimationProperties.POV_ANIMATION);
            Optional<AnimationSubFileReader.PovSettings> optPovSettings = realStatic.getProperty(ClientAnimationProperties.POV_SETTINGS);

			optPovAnimation.ifPresent(povAnimation -> {
				if (!povAnimation.equals(currentPlaying)) {
                    this.firstPersonLayer.playAnimation(povAnimation, realStatic, this, 0.0F);
                    this.povSettings = optPovSettings.orElse(null);
                }
            });

			return !optPovAnimation.isPresent();
		});

		if (noPovAnimation && !currentPlaying.equals(Animations.EMPTY_ANIMATION) && !currentPlaying.equals(Animations.DUMMY_ANIMATION)) {
			this.firstPersonLayer.off();
		}

		this.firstPersonLayer.update(this);

		if (this.firstPersonLayer.animationPlayer.getAnimation().equals(Animations.EMPTY_ANIMATION)) {
			this.povSettings = null;
		}
	}

	private void applyPovViewLimit() {
		if (this.povSettings == null || this.getFirstPersonLayer().isOff()) {
			return;
		}

		if (!this.minecraft.options.getCameraType().isFirstPerson()) {
			return;
		}

		AnimationSubFileReader.PovSettings.ViewLimit viewLimit = this.povSettings.viewLimit();

		if (viewLimit == null) {
			return;
		}

		this.original.xRot = MathHelper.clamp(this.original.xRot, viewLimit.xRotMin(), viewLimit.xRotMax());
		float yCamera = MathHelper.wrapDegrees(this.original.yRot);
		float yBody = MathUtils.findNearestRotation(yCamera, this.getYRot());
		float yClamped = MathHelper.clamp(yCamera, yBody + viewLimit.yRotMin(), yBody + viewLimit.yRotMax());

		if (yClamped != yCamera) {
			this.original.yRot = yClamped;
		}
	}

	public void playAnimationClientPreemptive(StaticAnimation animation, float convertTimeModifier) {
		this.animator.playAnimation(animation, convertTimeModifier);
		EpicFightNetworkManager.sendToServer(new CPPlayAnimation(animation.getId(), convertTimeModifier, false, false));
	}

	@Override
	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier,
			AnimationPacketProvider packetProvider) {
		EpicFightNetworkManager.sendToServer(new CPPlayAnimation(animation.getId(), convertTimeModifier, false, true));
	}

	@Override
	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		super.updateHeldItem(mainHandCap, offHandCap);

		if (EpicFightMod.CLIENT_CONFIGS.battleAutoSwitchItems.contains(this.original.getMainHandItem().getItem())) {
			this.toEpicFightMode(true);
		} else if (EpicFightMod.CLIENT_CONFIGS.miningAutoSwitchItems
				.contains(this.original.getMainHandItem().getItem())) {
			this.toMiningMode(true);
		}
	}

	/*
	 * public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem
	 * offHandCap) {
	 * this.cancelAnyAction(); //this.cancelItemUse();
	 * 
	 * this.getClientAnimator().iterAllLayers((layer) -> {
	 * if (layer.isOff()) {
	 * return;
	 * }
	 * 
	 * layer.animationPlayer.getRealAnimation().get().getProperty(AnimationProperty.
	 * StaticAnimationProperty.ON_ITEM_CHANGE_EVENT).ifPresent((event) -> {
	 * event.params(mainHandCap, offHandCap);
	 * event.execute(this, layer.animationPlayer.getRealAnimation(),
	 * layer.animationPlayer.getPrevElapsedTime(),
	 * layer.animationPlayer.getElapsedTime());
	 * });
	 * });
	 * }
	 */

	@Override
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		AttackResult result = super.tryHurt(damageSource, amount);

		if (EpicFightMod.CLIENT_CONFIGS.autoPreparation.getValue()
				&& result.resultType == AttackResult.ResultType.SUCCESS && !this.isBattleMode()) {
			this.toEpicFightMode(true);
		}

		return result;
	}

	@Override
	public LivingEntity getTarget() {
		return this.rayTarget;
	}

	@Override
	public void toMiningMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.VANILLA) {
			ClientEngine.getInstance().renderEngine.downSlideSkillUI();
			if (EpicFightMod.CLIENT_CONFIGS.cameraAutoSwitch.getValue()) {
				this.minecraft.options.setCameraType(PointOfView.FIRST_PERSON);
			}

			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.VANILLA));
			}
		}

		super.toMiningMode(synchronize);
	}

	@Override
	public void toEpicFightMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.EPICFIGHT) {
			ClientEngine.getInstance().renderEngine.upSlideSkillUI();

			if (EpicFightMod.CLIENT_CONFIGS.cameraAutoSwitch.getValue()) {
				this.minecraft.options.setCameraType(PointOfView.THIRD_PERSON_BACK);
			}

			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.EPICFIGHT));
			}
		}

		super.toEpicFightMode(synchronize);
	}

	@Override
	public boolean isFirstPerson() {
		return this.minecraft.options.getCameraType() == PointOfView.FIRST_PERSON;
	}

	@Override
	public boolean shouldBlockMoving() {
		return ClientEngine.getInstance().controllEngine.isKeyDown(this.minecraft.options.keyDown);
	}

	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		if (!this.isLogicalClient()) {
			return false;
		}

		return actionAnimation.shouldPlayerMove(this);
	}

	public float getPrevStamina() {
		return this.prevStamina;
	}

	public int getPrevChargingAmount() {
		return this.prevChargingAmount;
	}

	public FirstPersonLayer getFirstPersonLayer() {
		return this.firstPersonLayer;
	}

	public AnimationSubFileReader.PovSettings getPovSettings() {
		return this.povSettings;
	}

	public boolean hasCameraAnimation() {
		return this.povSettings != null && this.povSettings.cameraTransform() != null;
	}

	public float getLerpedLockOnX(double partial) {
		return MathHelper.rotLerp((float) partial, this.lockOnXRotO, this.lockOnXRot);
	}

	public float getLerpedLockOnY(double partial) {
		return MathHelper.rotLerp((float) partial, this.lockOnYRotO, this.lockOnYRot);
	}

	public boolean isTargetLockedOn() {
		return this.targetLockedOn;
	}

	public void setLockOn(boolean targetLockedOn) {
		this.targetLockedOn = targetLockedOn;
	}

	public void toggleLockOn() {
		this.targetLockedOn = !this.targetLockedOn;
	}

	@Override
	public void onDeath(LivingDeathEvent event) {
		super.onDeath(event);

		this.original.xRot = (this.lockOnXRot);
		this.original.yRot = (this.lockOnYRot);
	}

	@Override
	public void setModelYRot(float amount, boolean sendPacket) {
		super.setModelYRot(amount, sendPacket);

		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot(amount));
		}
	}

	public float getModelYRot() {
		return this.modelYRot;
	}

	public void setModelYRotInGui(float rotDeg) {
		this.useModelYRot = true;
		this.modelYRot = rotDeg;
	}

	public void disableModelYRotInGui(float originalDeg) {
		this.useModelYRot = false;
		this.modelYRot = originalDeg;
	}

	@Override
	public void disableModelYRot(boolean sendPacket) {
		super.disableModelYRot(sendPacket);

		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public class FirstPersonLayer extends Layer {
		private final TransformSheet linkCameraTransform = new TransformSheet(List.of(new Keyframe(0.0F, JointTransform.empty()), new Keyframe(Float.MAX_VALUE, JointTransform.empty())));

		public FirstPersonLayer() {
			super(null);
		}

		public void playAnimation(StaticAnimation nextFirstPersonAnimation, StaticAnimation originalAnimation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier) {
			Optional<AnimationSubFileReader.PovSettings> povSettings = originalAnimation.getProperty(ClientAnimationProperties.POV_SETTINGS);

			boolean hasPrevCameraAnimation = LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null;
			boolean hasNextCameraAnimation = povSettings.isPresent() && povSettings.get().cameraTransform() != null;

			if (hasPrevCameraAnimation || hasNextCameraAnimation) {
				if (hasPrevCameraAnimation) {
					this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
				} else {
					this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(JointTransform.empty());
				}

				if (hasNextCameraAnimation) {
					this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(povSettings.get().cameraTransform().getKeyframes()[0].transform());
				} else {
					this.linkCameraTransform.getKeyframes()[1].transform().clearTransform();
				}

				this.linkCameraTransform.getKeyframes()[1].setTime(nextFirstPersonAnimation.getTransitionTime());
			}

			super.playAnimation(nextFirstPersonAnimation, entitypatch, transitionTimeModifier);
		}

		public void off() {
			if (LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null) {
				this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
				this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(JointTransform.empty());
				this.linkCameraTransform.getKeyframes()[1].setTime(0.2F);
			}

			super.off(LocalPlayerPatch.this);
		}

		@Override
		protected Pose getCurrentPose(LivingEntityPatch<?> entitypatch) {
			return this.animationPlayer.isEmpty() ? super.getCurrentPose(entitypatch) : this.animationPlayer.getCurrentPose(entitypatch, 0.0F);
		}

		public TransformSheet getLinkCameraTransform() {
			return this.linkCameraTransform;
		}
	}

	@Override
	public void correctRotation() {
		if (this.targetLockedOn) {
			if (this.rayTarget != null && !this.rayTarget.isDeadOrDying()) {
				Vector3d playerPosition = this.original.position();
				Vector3d targetPosition = this.rayTarget.position();
				Vector3d toTarget = targetPosition.subtract(playerPosition);
				float yaw = (float) MathUtils.getYRotOfVector(toTarget);
				float pitch = (float) MathUtils.getXRotOfVector(toTarget);
				this.original.yRot = (yaw);
				this.original.xRot = (pitch);
			} else {
				this.original.yRot = (this.lockOnYRot);
				this.original.xRot = (this.lockOnXRot);
			}
		}
	}

	@Override
	public void openSkillBook(ItemStack itemstack, Hand hand) {
		if (itemstack.hasTag() && itemstack.getTag().contains("skill")) {
			Minecraft.getInstance().setScreen(new SkillBookScreen(this.original, itemstack, hand));
		}
	}

	@Override
	public int getFlyInputDirection() {
		return (int) Math.signum(this.original.input.forwardImpulse);
	}
}
