package yesman.epicfight.client.world.capabilites.entitypatch.player;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
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
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPPlayAnimation;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

@OnlyIn(Dist.CLIENT)
public class LocalPlayerPatch extends AbstractClientPlayerPatch<ClientPlayerEntity> {
	private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
	private Minecraft minecraft;
	private LivingEntity rayTarget;
	private boolean targetLockedOn;
	private float prevStamina;
	private int prevChargingAmount;
	private float lockOnXRot;
	private float lockOnXRotO;
	private float lockOnYRot;
	private float lockOnYRotO;

	@Override
	public void onConstructed(ClientPlayerEntity entity) {
		super.onConstructed(entity);
		this.minecraft = Minecraft.getInstance();
		ClientEngine.instance.inputController.setPlayerPatch(this);
	}

	@Override
	public void onJoinWorld(ClientPlayerEntity entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		this.eventListeners.addEventListener(EventType.ACTION_EVENT_CLIENT, ACTION_EVENT_UUID, (playerEvent) -> {
			ClientEngine.instance.inputController.unlockHotkeys();
		});
	}

	public void onRespawnLocalPlayer(ClientPlayerNetworkEvent.RespawnEvent event) {
		this.onJoinWorld(event.getNewPlayer(), new EntityJoinWorldEvent(event.getNewPlayer(), event.getNewPlayer().level));
	}

	@Override
	public void updateMotion(boolean considerInaction) {
		super.updateMotion(considerInaction);

		if (!this.getClientAnimator().isAiming()) {
			if (this.currentCompositeMotion == LivingMotions.AIM) {
				this.original.getUseItemRemainingTicks();
				ClientEngine.instance.renderEngine.zoomIn();
				}
		}
	}

	@Override
	public void clientTick(LivingEvent.LivingUpdateEvent event) {
		this.prevStamina = this.getStamina();
		super.clientTick(event);

		RayTraceResult cameraHitResult = this.minecraft.hitResult;
		RenderEngine renderEngine = ClientEngine.instance.renderEngine;

		if (renderEngine.isPlayerRotationLocked()) {
			double pickRange = this.minecraft.gameMode.getPickRange();
			Vector3d vec3 = this.original.getEyePosition(1.0F);
			Vector3d vec31 = MathUtils.getVectorForRotation(renderEngine.getCorrectedXRot(), renderEngine.getCorrectedYRot());
			Vector3d vec32 = vec3.add(vec31.x * pickRange, vec31.y * pickRange, vec31.z * pickRange);
			AxisAlignedBB aabb = this.original.getBoundingBox().expandTowards(vec31.scale(pickRange)).inflate(1.0D, 1.0D, 1.0D);

			cameraHitResult = ProjectileHelper.getEntityHitResult(this.original, vec3, vec32, aabb, (hit) ->
					!hit.isSpectator() && hit.isPickable() && !hit.is(this.grapplingTarget), pickRange);
		}

		if (cameraHitResult != null && cameraHitResult.getType() == RayTraceResult.Type.ENTITY) {
			Entity hit = ((EntityRayTraceResult)cameraHitResult).getEntity();

			if (hit != this.rayTarget) {
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
				float yaw = (float)MathUtils.getYRotOfVector(toTarget);
				float pitch = (float)MathUtils.getXRotOfVector(toTarget);
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
					this.original.xRot = lockOnXRotDst;
					this.original.yRot = lockOnYRotDst;
				}
			} else {
				this.lockOnXRot = this.original.xRot;
				this.lockOnYRot = this.original.yRot;
				this.lockOnXRotO = this.lockOnXRot;
				this.lockOnYRotO = this.lockOnYRot;
			}

			if (!this.rayTarget.isAlive() || this.getOriginal().distanceToSqr(this.rayTarget) > 400.0D || (this.getAngleTo(this.rayTarget) > 100.0D && !this.targetLockedOn)) {
				this.rayTarget = null;
				EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
			}
		} else {
			this.lockOnXRot = this.original.xRot;
			this.lockOnYRot = this.original.yRot;
			this.targetLockedOn = false;
		}
	}

	/*@Override
	protected boolean isMoving() {
		return Math.abs(this.original.xxa) > 0.0004F || Math.abs(this.original.zza) > 0.0004F;
	}*/

	@Override
	protected void playReboundAnimation() {
		super.playReboundAnimation();
		ClientEngine.instance.renderEngine.zoomOut(40);
	}

	public void playAnimationClientPreemptive(StaticAnimation animation, float convertTimeModifier) {
		this.animator.playAnimation(animation, convertTimeModifier);
		EpicFightNetworkManager.sendToServer(new CPPlayAnimation(animation.getNamespaceId(), animation.getId(), convertTimeModifier, false, false));
	}

	@Override
	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier, AnimationPacketProvider packetProvider) {
		EpicFightNetworkManager.sendToServer(new CPPlayAnimation(animation.getNamespaceId(), animation.getId(), convertTimeModifier, false, true));
	}

	@Override
	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		super.updateHeldItem(mainHandCap, offHandCap);

		if (EpicFightMod.CLIENT_CONFIGS.battleAutoSwitchItems.contains(this.original.getMainHandItem().getItem())) {
			this.toBattleMode(true);
		} else if (EpicFightMod.CLIENT_CONFIGS.miningAutoSwitchItems.contains(this.original.getMainHandItem().getItem())) {
			this.toMiningMode(true);
		}
	}

	@Override
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		AttackResult result = super.tryHurt(damageSource, amount);

		if (EpicFightMod.CLIENT_CONFIGS.autoPreparation.getValue() && result.resultType == AttackResult.ResultType.SUCCESS && !this.isBattleMode()) {
			this.toBattleMode(true);
		}

		return result;
	}

	@Override
	public LivingEntity getTarget() {
		return this.rayTarget;
	}

	@Override
	public void toMiningMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.MINING) {
			ClientEngine.instance.renderEngine.downSlideSkillUI();
			if (EpicFightMod.CLIENT_CONFIGS.cameraAutoSwitch.getValue()) {
				this.minecraft.options.setCameraType(PointOfView.FIRST_PERSON);
			}

			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.MINING));
			}
		}

		super.toMiningMode(synchronize);
	}

	@Override
	public void toBattleMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.BATTLE) {
			ClientEngine.instance.renderEngine.upSlideSkillUI();

			if (EpicFightMod.CLIENT_CONFIGS.cameraAutoSwitch.getValue()) {
				this.minecraft.options.setCameraType(PointOfView.THIRD_PERSON_BACK);
			}

			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.BATTLE));
			}
		}

		super.toBattleMode(synchronize);
	}

	@Override
	public boolean isFirstPerson() {
		return this.minecraft.options.getCameraType() == PointOfView.FIRST_PERSON;
	}

	@Override
	public boolean shouldBlockMoving() {
		return ClientEngine.instance.inputController.isKeyDown(this.minecraft.options.keyDown);
	}

	/*@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		if (!this.isLogicalClient()) {
			return false;
		}

		return actionAnimation.shouldPlayerMove(this);
	}

	@Override
	public boolean consumeStamina(float amount) {
		float currentStamina = this.getStamina();
		return currentStamina >= amount;
	}*/

	public float getPrevStamina() {
		return this.prevStamina;
	}

	public int getPrevChargingAmount() {
		return this.prevChargingAmount;
	}

	public float getLerpedLockOnX(double partial) {
		return MathHelper.rotLerp((float)partial, this.lockOnXRotO, this.lockOnXRot);
	}

	public float getLerpedLockOnY(double partial) {
		return MathHelper.rotLerp((float)partial, this.lockOnYRotO, this.lockOnYRot);
	}

	/*@Override
	public float getCameraXRot() {
		RenderEngine renderEngine = ClientEngine.instance.renderEngine;

		return MathHelper.wrapDegrees(renderEngine.isPlayerRotationLocked() ? renderEngine.getCorrectedXRot() : super.getCameraXRot());
	}

	@Override
	public float getCameraYRot() {
		RenderEngine renderEngine = ClientEngine.instance.renderEngine;

		return MathHelper.wrapDegrees(renderEngine.isPlayerRotationLocked() ? renderEngine.getCorrectedYRot() : super.getCameraYRot());
	}*/

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
	public void onDeath() {
		this.original.xRot = this.lockOnXRot;
		this.original.yRot = this.lockOnYRot;
	}

	/*@Override
	public void changeModelYRot(float amount) {
		super.changeModelYRot(amount);
		EpicFightNetworkManager.sendToServer(new CPRotateEntityModelYRot(amount));
	}

	@Override
	public void correctRotation() {
		if (this.targetLockedOn) {
			if (this.rayTarget != null && !this.rayTarget.isDeadOrDying()) {
				Vector3d playerPosition = this.original.position();
				Vector3d targetPosition = this.rayTarget.position();
				Vector3d toTarget = targetPosition.subtract(playerPosition);
				float yaw = (float)MathUtils.getYRotOfVector(toTarget);
				float pitch = (float)MathUtils.getXRotOfVector(toTarget);
				this.original.yRot = yaw;
				this.original.xRot = pitch;
			} else {
				this.original.yRot = this.lockOnYRot;
				this.original.xRot = this.lockOnXRot;
			}
		}
	}*/

	@Override
	public void openSkillBook(ItemStack itemstack, Hand hand) {
		if (itemstack.hasTag() && itemstack.getTag().contains("skill")) {
			Minecraft.getInstance().setScreen(new SkillBookScreen(this.original, itemstack, hand));
		}
	}
}