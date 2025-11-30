package yesman.epicfight.client.world.capabilites.entitypatch.player;

import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.IRideable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.forgeevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.api.utils.VectorUtils;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.RangedWeaponCapability;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class AbstractClientPlayerPatch<T extends AbstractClientPlayerEntity> extends PlayerPatch<T> {
	private Item prevHeldItem;
	private Item prevHeldItemOffHand;

	@Override
	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		this.prevHeldItem = Items.AIR;
		this.prevHeldItemOffHand = Items.AIR;
	}

	@Override
	public void updateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (!this.state.updateLivingMotion() && considerInaction) {
			currentLivingMotion = LivingMotions.INACTION;
		} else {
			if (original.isFallFlying() || original.isAutoSpinAttack()) {
				currentLivingMotion = LivingMotions.FLY;
			} else if (original.getVehicle() != null) {
				if (original.getVehicle() instanceof IRideable)
					currentLivingMotion = LivingMotions.MOUNT;
				else
					currentLivingMotion = LivingMotions.SIT;
			} else if (original.isVisuallySwimming()) {
				currentLivingMotion = LivingMotions.SWIM;
			} else if (original.isSleeping()) {
				currentLivingMotion = LivingMotions.SLEEP;
			} else if (!original.isOnGround() && original.onClimbable()) {
				currentLivingMotion = LivingMotions.CLIMB;
			} else if (!original.abilities.flying) {
				ClientAnimator animator = this.getClientAnimator();

				if (original.isUnderWater() && (original.getY() - this.yo) < -0.005)
					currentLivingMotion = LivingMotions.FLOAT;
				else if (original.getY() - this.yo < -0.4F || this.isAirborneState())
					currentLivingMotion = LivingMotions.FALL;
				else if (this.isMoving()) {
					if (original.isCrouching())
						currentLivingMotion = LivingMotions.SNEAK;
					else if (original.isSprinting())
						currentLivingMotion = LivingMotions.RUN;
					else
						currentLivingMotion = LivingMotions.WALK;

					animator.baseLayer.animationPlayer.setReversed(this.dz < 0);

				} else {
					animator.baseLayer.animationPlayer.setReversed(false);

					if (original.isCrouching())
						currentLivingMotion = LivingMotions.KNEEL;
					else
						currentLivingMotion = LivingMotions.IDLE;
				}
			} else {
				if (this.isMoving())
					currentLivingMotion = LivingMotions.CREATIVE_FLY;
				else
					currentLivingMotion = LivingMotions.CREATIVE_IDLE;
			}
		}

		MinecraftForge.EVENT_BUS.post(new UpdatePlayerMotionEvent.BaseLayer(this, this.currentLivingMotion));
		CapabilityItem activeItemCap = this.getHoldingItemCapability(this.original.getUsedItemHand());

		this.updateCompositeMotionState(activeItemCap);

		MinecraftForge.EVENT_BUS.post(new UpdatePlayerMotionEvent.CompositeLayer(this, this.currentCompositeMotion));
	}

	@Override
	protected void clientTick(LivingUpdateEvent event) {
		super.clientTick(event);

		if (!this.getEntityState().updateLivingMotion()) {
			this.original.yBodyRot = this.original.yRot;
		}

		boolean isMainHandChanged = this.prevHeldItem != this.original.inventory.getSelected().getItem();
		boolean isOffHandChanged = this.prevHeldItemOffHand != this.original.inventory.offhand.get(0).getItem();

		if (isMainHandChanged || isOffHandChanged) {
			this.updateHeldItem(this.getHoldingItemCapability(Hand.MAIN_HAND),
					this.getHoldingItemCapability(Hand.OFF_HAND));

			if (isMainHandChanged) {
				this.prevHeldItem = this.original.inventory.getSelected().getItem();
			}

			if (isOffHandChanged) {
				this.prevHeldItemOffHand = this.original.inventory.offhand.get(0).getItem();
			}
		}

		if (this.original.deathTime == 1) {
			this.getClientAnimator().playDeathAnimation();
		}
	}

	protected boolean isMoving() {
		return Math.abs(this.dx) > 0.01F || Math.abs(this.dz) > 0.01F;
	}

	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		this.cancelAnyAction();
	}

	@Override
	public void reserveAnimation(StaticAnimation animation) {
		this.animator.reserveAnimation(animation);
	}

	@Override
	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier,
			AnimationPacketProvider packetProvider) {
	}

	@Override
	public boolean overrideRender() {
		boolean originalShouldRender = this.isBattleMode() || !EpicFightMod.CLIENT_CONFIGS.filterAnimation.getValue();

		RenderEpicFightPlayerEvent renderepicfightplayerevent = new RenderEpicFightPlayerEvent(this,
				originalShouldRender);
		MinecraftForge.EVENT_BUS.post(renderepicfightplayerevent);

		return renderepicfightplayerevent.getShouldRender();
	}

	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		return false;
	}

	@Override
	public void poseTick(DynamicAnimation animation, Pose pose, float elapsedTime, float partialTicks) {
		if (pose.hasTransform("Head") && this.armature.hasJoint("Head")) {
			if (animation.doesHeadRotFollowEntityHead()) {
				float headRelativeRot = MathHelper.rotLerp(partialTicks,
						MathHelper.wrapDegrees(this.modelYRotO - this.original.yHeadRotO),
						MathHelper.wrapDegrees(this.modelYRot - this.original.yHeadRot));
				OpenMatrix4f headTransform = this.armature.getBindedTransformFor(pose,
						this.armature.searchJointByName("Head"));
				OpenMatrix4f toOriginalRotation = headTransform.removeScale().removeTranslation().invert();
				Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
				Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);
				OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(headRelativeRot, yAxis)
						.rotateDeg(-MathHelper.rotLerp(partialTicks, this.original.xRotO, this.original.xRot), xAxis);
				pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
			}
		}
	}

	@Override
	public OpenMatrix4f getModelMatrix(float partialTick) {

		if (this.original.isAutoSpinAttack()) {
			OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0,
					partialTick, PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
			float yRot = MathUtils.lerpBetween(this.original.yRotO, this.original.yRot, partialTick);
			float xRot = MathUtils.lerpBetween(this.original.xRotO, this.original.xRot, partialTick);

			mat.rotateDeg(-yRot, Vec3f.Y_AXIS)
					.rotateDeg(-xRot, Vec3f.X_AXIS)
					.rotateDeg((this.original.tickCount + partialTick) * -55.0F, Vec3f.Z_AXIS)
					.translate(0F, -0.39F, 0F);

			return mat;
		} else if (this.original.isFallFlying()) {
			OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0,
					partialTick, PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
			float f1 = (float) this.original.getFallFlyingTicks() + partialTick;
			float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);

			mat.rotateDeg(-MathHelper.rotLerp(partialTick, this.original.yBodyRotO, this.original.yBodyRot),
					Vec3f.Y_AXIS).rotateDeg(f2 * (-this.original.xRot), Vec3f.X_AXIS);

			Vector3d vec3d = this.original.getViewVector(partialTick);
			Vector3d prevDelta = new Vector3d(this.original.xOld, this.original.yOld, this.original.zOld);
			Vector3d currDelta = this.original.getDeltaMovement();
			Vector3d vec3d1 = VectorUtils.lerp(prevDelta, currDelta, partialTick);
			double d0 = VectorUtils.horizontalDistanceSqr(vec3d1);
			double d1 = VectorUtils.horizontalDistanceSqr(vec3d);

			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (vec3d1.x * vec3d.x + vec3d1.z * vec3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
				double d3 = vec3d1.x * vec3d.z - vec3d1.z * vec3d.x;
				mat.rotate((float) -((Math.signum(d3) * Math.acos(d2))), Vec3f.Z_AXIS);
			}

			return mat;

		} else if (this.original.isSleeping()) {
			BlockState blockstate = this.original.getFeetBlockState();
			float yRot = 0.0F;

			if (blockstate.isBed(this.original.level, this.original.getSleepingPos().orElse(null), this.original)) {
				if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
					switch (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
						case EAST:
							yRot = 90.0F;
							break;
						case WEST:
							yRot = -90.0F;
							break;
						case SOUTH:
							yRot = 180.0F;
							break;
						default:
							break;
					}
				}
			}
			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yRot, yRot, 0,
					PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
		} else {
			float yRotO;
			float yRot;
			float xRotO = 0;
			float xRot = 0;

			if (this.original.getVehicle() instanceof LivingEntity ridingEntity) {
				yRotO = ridingEntity.yBodyRotO;
				yRot = ridingEntity.yBodyRot;
			} else {
				yRotO = this.modelYRotO;
				yRot = this.modelYRot;
			}

			if (!this.getEntityState().inaction()
					&& this.original.getPose() == net.minecraft.entity.Pose.SWIMMING) {
				float f = this.original.getSwimAmount(partialTick);
				float f3 = this.original.isInWater() ? this.original.xRot : 0;
				float f4 = MathHelper.lerp(f, 0.0F, f3);
				xRotO = f4;
				xRot = f4;
			}

			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, xRotO, xRot, yRotO, yRot,
					partialTick, PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
		}
	}

	private void updateCompositeMotionState(CapabilityItem activeItemCap) {
		if (this.original.isUsingItem()) {
			UseAction useAnim = this.original.getUseItem().getUseAnimation();
			UseAction capUseAnim = activeItemCap.getUseAnimation(this);

			if (useAnim == UseAction.BLOCK || capUseAnim == UseAction.BLOCK)
				if (activeItemCap.getWeaponCategory() == WeaponCategories.SHIELD)
					currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
				else
					currentCompositeMotion = LivingMotions.BLOCK;
			else if (useAnim == UseAction.BOW || useAnim == UseAction.SPEAR)
				currentCompositeMotion = LivingMotions.AIM;
			else if (useAnim == UseAction.CROSSBOW)
				currentCompositeMotion = LivingMotions.RELOAD;
			else if (useAnim == UseAction.DRINK)
				currentCompositeMotion = LivingMotions.DRINK;
			else if (useAnim == UseAction.EAT)
				currentCompositeMotion = LivingMotions.EAT;
			// else if (useAnim == UseAction.SPYGLASS) TODO extend and add spyglass action
			// with a custom goggle
			// currentCompositeMotion = LivingMotions.SPECTATE;
			else
				currentCompositeMotion = currentLivingMotion;
		} else {
			if (this.original.getMainHandItem().getItem() instanceof ShootableItem
					&& CrossbowItem.isCharged(this.original.getMainHandItem()))
				currentCompositeMotion = LivingMotions.AIM;
			if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation()
					.getRealAnimation().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
			else if (this.original.swinging && this.original.getSleepingPos().isEmpty())
				currentCompositeMotion = LivingMotions.DIGGING;
			else
				currentCompositeMotion = currentLivingMotion;

			if (this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM
					&& activeItemCap instanceof RangedWeaponCapability) {
				this.playReboundAnimation();
			}
		}
	}

}