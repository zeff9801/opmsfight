package yesman.epicfight.client.world.capabilites.entitypatch.player;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IRideable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.UseAction;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.forgeevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.RangedWeaponCapability;

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
			ClientAnimator animator = this.getClientAnimator();

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
				double y = original.yCloak - original.yCloakO;

				if (Math.abs(y) < 0.04D) {
					animator.baseLayer.pause();
				} else {
					animator.baseLayer.resume();

					animator.baseLayer.animationPlayer.setReversed(y < 0);
				}
			} else if (!original.abilities.flying) {
				if (original.isUnderWater() && (original.yCloak - original.yCloakO) < -0.005)
					currentLivingMotion = LivingMotions.FLOAT;
				else if (original.yCloak - original.yCloakO < -0.4F || this.isAirborneState())
					currentLivingMotion = LivingMotions.FALL;
				else if (this.isMoving()) {
					if (original.isCrouching())
						currentLivingMotion = LivingMotions.SNEAK;
					else if (original.isSprinting())
						currentLivingMotion = LivingMotions.RUN;
					else
						currentLivingMotion = LivingMotions.WALK;

					animator.baseLayer.animationPlayer.setReversed(original.zza < 0);

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
		//	else if (useAnim == UseAction.SPYGLASS)
		//		currentCompositeMotion = LivingMotions.SPECTATE;
			else
				currentCompositeMotion = currentLivingMotion;
		} else {
			if (this.original.getMainHandItem().getItem() instanceof ShootableItem  && CrossbowItem.isCharged(this.original.getMainHandItem()))
				currentCompositeMotion = LivingMotions.AIM;
			else if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
			else if (this.original.swinging && this.original.getSleepingPos().isEmpty())
				currentCompositeMotion = LivingMotions.DIGGING;
			else
				currentCompositeMotion = currentLivingMotion;

			if (this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM && activeItemCap instanceof RangedWeaponCapability) {
				this.playReboundAnimation();
			}
		}

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
			this.updateHeldItem(this.getHoldingItemCapability(Hand.MAIN_HAND), this.getHoldingItemCapability(Hand.OFF_HAND));

			if (isMainHandChanged) {
				this.prevHeldItem = this.original.inventory.getSelected().getItem();
			}

			if (isOffHandChanged) {
				this.prevHeldItemOffHand = this.original.inventory.offhand.get(0).getItem();
			}
		}

		/** {@link LivingDeathEvent} never fired for client players **/
		if (this.original.deathTime == 1) {
			this.getClientAnimator().playDeathAnimation();
		}
	}

	protected boolean isMoving() {
		return Math.abs(this.original.xxa) > 0.01F || Math.abs(this.original.zza) > 0.01F;
	}

	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		this.cancelAnyAction();
	}

	@Override
	public void reserveAnimation(StaticAnimation animation) {
		this.animator.reserveAnimation(animation);
	}

	

	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		return false;
	}

	@Override
	public boolean overrideRender() {
		boolean originalShouldRender = this.isBattleMode() || !EpicFightMod.CLIENT_CONFIGS.filterAnimation.getValue();

		RenderEpicFightPlayerEvent renderepicfightplayerevent = new RenderEpicFightPlayerEvent(this, originalShouldRender);
		MinecraftForge.EVENT_BUS.post(renderepicfightplayerevent);

		return renderepicfightplayerevent.getShouldRender();
	}


	

	
	@Override
	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier, AnimationPacketProvider packetProvider) {
		;
	}
	
	@Override
	public <M extends Model> M getEntityModel(Models<M> modelDB) {
		return this.original.getModelName().equals("slim") ? modelDB.bipedAlex : modelDB.biped;
	}
	
	@Override
	public boolean shouldSkipRender() {
		return !this.isBattleMode() && EpicFightMod.CLIENT_CONFIGS.filterAnimation.getValue();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public OpenMatrix4f getHeadMatrix(float partialTick) {
        float yaw = 0;
        float pitch = 0;
        float prvePitch = 0;
        
		if (this.getEntityState().inaction() || this.original.getVehicle() != null || (!original.isOnGround() && this.original.onClimbable())) {
	        yaw = 0;
		} else {
			float f = MathUtils.lerpBetween(this.prevBodyYaw, this.bodyYaw, partialTick);
			float f1 = MathUtils.lerpBetween(this.original.yHeadRotO, this.original.yHeadRot, partialTick);
	        yaw = f1 - f;
		}
        
		if (!(this.original.isFallFlying() || this.original.isVisuallySwimming())) {
			prvePitch = this.original.xRotO;
			pitch = this.original.xRot;
		}
        
		return MathUtils.getModelMatrixIntegral(0, 0, 0, 0, 0, 0, prvePitch, pitch, yaw, yaw, partialTick, 1, 1, 1);
	}

	@Override
	public OpenMatrix4f getModelMatrix(float partialTick) {
		Direction direction;

		if (this.original.isAutoSpinAttack()) {
			OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0, partialTick, 0.9375F, 0.9375F, 0.9375F);
			float yRot = MathUtils.lerpBetween(this.original.yRotO, this.original.yRot, partialTick);
			float xRot = MathUtils.lerpBetween(this.original.xRotO, this.original.xRot, partialTick);

			mat.rotateDeg(-yRot, Vec3f.Y_AXIS)
					.rotateDeg(-xRot, Vec3f.X_AXIS)
					.rotateDeg((this.original.tickCount + partialTick) * -55.0F, Vec3f.Z_AXIS)
					.translate(0F, -0.39F, 0F);

			return mat;
		} else if (this.original.isFallFlying()) {
			OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0, partialTick, 0.9375F, 0.9375F, 0.9375F);
			float f1 = (float)this.original.getFallFlyingTicks() + partialTick;
			float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);

			mat.rotateDeg(-MathHelper.rotLerp(partialTick, this.original.yBodyRotO, this.original.yBodyRot), Vec3f.Y_AXIS).rotateDeg(f2 * (-this.original.xRot), Vec3f.X_AXIS);

			Vector3d vec3d = this.original.getViewVector(partialTick);
			Vector3d vec3d1 = this.original.getDeltaMovement();
			double d0 = vec3d1.horizontalDistanceSqr();
			double d1 = vec3d.horizontalDistanceSqr();

			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (vec3d1.x * vec3d.x + vec3d1.z * vec3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
				double d3 = vec3d1.x * vec3d.z - vec3d1.z * vec3d.x;
				mat.rotate((float)-((Math.signum(d3) * Math.acos(d2))), Vec3f.Z_AXIS);
			}

			return mat;
		} else if (this.original.isSleeping()) {
			BlockState blockstate = this.original.getFeetBlockState();
			float yRot = 0.0F;

			if (blockstate.isBed(this.original.level, this.original.getSleepingPos().orElse(null), this.original)) {
				if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
					switch(blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
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

			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yRot, yRot, 0, 0.9375F, 0.9375F, 0.9375F);
		} else if ((direction = this.getLadderDirection(this.original.getFeetBlockState(), this.original.level, this.original.blockPosition(), this.original)) != Direction.UP) {
			float yRot = 0.0F;

			switch(direction) {
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

			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yRot, yRot, 0.0F, 0.9375F, 0.9375F, 0.9375F);
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

			if (!this.getEntityState().inaction() && this.original.getPose() == net.minecraft.world.entity.Pose.SWIMMING) {
				float f = this.original.getSwimAmount(partialTick);
				float f3 = this.original.isInWater() ? this.original.getXRot() : 0;
				float f4 = Mth.lerp(f, 0.0F, f3);
				xRotO = f4;
				xRot = f4;
			}

			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, xRotO, xRot, yRotO, yRot, partialTick, 0.9375F, 0.9375F, 0.9375F);
		}
	}
	
//	@Override
//	public OpenMatrix4f getModelMatrix(float partialTick) {
//		Direction direction;
//
//		if (this.original.isAutoSpinAttack()) {
//			OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0, partialTick, 0.9375F, 0.9375F, 0.9375F);
//			float yRot  = MathUtils.lerpBetween(this.original.yRotO, this.original.yRot, partialTick);
//			float xRot  = MathUtils.lerpBetween(this.original.xRotO, this.original.xRot, partialTick);
//
//
//			mat.rotateDeg(-yRot, Vec3f.Y_AXIS).rotateDeg(-xRot, Vec3f.X_AXIS).rotateDeg((this.original.tickCount + partialTick) * -55.0F, Vec3f.Z_AXIS).translate(0F, -0.39F, 0F);
//
//            return mat;
//		} else if (this.original.isFallFlying()) {
//			OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0, partialTick, 0.9375F, 0.9375F, 0.9375F);
//
//            float f1 = (float)this.getOriginal().getFallFlyingTicks() + partialTick;
//            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
//
//			mat.rotateDeg(-MathHelper.rotLerp(partialTick, this.original.yBodyRotO, this.original.yBodyRot), Vec3f.Y_AXIS).rotateDeg(f2 * (-this.original.xRot), Vec3f.X_AXIS);
//
//            if (!this.getOriginal().isAutoSpinAttack()) {
//            	mat.rotateDeg((float)(f2 * (-this.original.xRot)), Vec3f.X_AXIS);
//            }
//
//            Vector3d vector3d = this.original.getViewVector(partialTick);
//            Vector3d vector3d1 = this.original.getDeltaMovement();
//            double d0 = Entity.getHorizontalDistanceSqr(vector3d1);
//            double d1 = Entity.getHorizontalDistanceSqr(vector3d);
//
//            if (d0 > 0.0D && d1 > 0.0D) {
//               double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
//               double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
//               mat.rotate((float)-(Math.signum(d3) * Math.acos(d2)), Vec3f.Z_AXIS);
//            }
//
//            return mat;
//
//		} else if (this.original.isSleeping()) {
//			BlockState blockstate = this.original.getFeetBlockState();
//			float yaw = 0.0F;
//
//			if (blockstate.isBed(this.original.level, this.original.getSleepingPos().orElse(null), this.original)) {
//				if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
//            		switch(blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
//            		case EAST:
//        				yaw = 90.0F;
//        				break;
//        			case WEST:
//        				yaw = -90.0F;
//        				break;
//        			case SOUTH:
//        				yaw = 180.0F;
//        				break;
//        			default:
//        				break;
//            		}
//            	}
//			}
//
//			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yaw, yaw, 0, 1.0F, 1.0F, 1.0F);
//		} else if ((direction = this.getLadderDirection(this.original.getFeetBlockState(), this.original.level, this.original.blockPosition(), this.original)) != Direction.UP) {
//			float yaw = 0.0F;
//
//			switch(direction) {
//			case EAST:
//				yaw = 90.0F;
//				break;
//			case WEST:
//				yaw = -90.0F;
//				break;
//			case SOUTH:
//				yaw = 180.0F;
//				break;
//			default:
//				break;
//			}
//
//			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yaw, yaw, 0.0F, 1.0F, 1.0F, 1.0F);
//		} else {
//			float yaw;
//			float prevRotYaw;
//			float rotyaw;
//			float prevPitch = 0;
//			float pitch = 0;
//
//			if (this.original.getVehicle() instanceof LivingEntity) {
//				LivingEntity ridingEntity = (LivingEntity)this.original.getVehicle();
//				prevRotYaw = ridingEntity.yBodyRotO;
//				rotyaw = ridingEntity.yBodyRot;
//			} else {
//				yaw = MathUtils.lerpBetween(this.prevYaw, this.yaw, partialTick);
//				prevRotYaw = this.prevBodyYaw + yaw;
//				rotyaw = this.bodyYaw + yaw;
//			}
//
//			if (!this.getEntityState().inaction() && this.original.getPose() == Pose.SWIMMING) {
//				float f = this.original.getSwimAmount(partialTick);
//				float f3 = this.original.isInWater() ? this.original.xRot : 0;
//		        float f4 = MathHelper.lerp(f, 0.0F, f3);
//		        prevPitch = f4;
//		        pitch = f4;
//			}
//
//			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, prevPitch, pitch, prevRotYaw, rotyaw, partialTick, 1.0F, 1.0F, 1.0F);
//		}
//	}
	
	public Direction getLadderDirection(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull LivingEntity entity) {
		boolean isSpectator = (entity instanceof PlayerEntity && ((PlayerEntity)entity).isSpectator());
        if (isSpectator || this.original.isOnGround() || !this.original.isAlive()) {
        	return Direction.UP;
        }
        
		if (ForgeConfig.SERVER.fullBoundingBoxLadders.get()) {
            if (state.isLadder(world, pos, entity)) {
            	if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            		return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            	}
            	
            	if (state.hasProperty(BlockStateProperties.UP) && state.getValue(BlockStateProperties.UP)) {
            		return Direction.UP;
            	} else if (state.hasProperty(BlockStateProperties.NORTH) && state.getValue(BlockStateProperties.NORTH)) {
            		return Direction.SOUTH;
            	} else if (state.hasProperty(BlockStateProperties.WEST) && state.getValue(BlockStateProperties.WEST)) {
            		return Direction.EAST;
            	} else if (state.hasProperty(BlockStateProperties.SOUTH) && state.getValue(BlockStateProperties.SOUTH)) {
            		return Direction.NORTH;
            	} else if (state.hasProperty(BlockStateProperties.EAST) && state.getValue(BlockStateProperties.EAST)) {
            		return Direction.WEST;
            	}
            }
		} else {
            AxisAlignedBB bb = entity.getBoundingBox();
            int mX = MathHelper.floor(bb.minX);
            int mY = MathHelper.floor(bb.minY);
            int mZ = MathHelper.floor(bb.minZ);
            
			for (int y2 = mY; y2 < bb.maxY; y2++) {
				for (int x2 = mX; x2 < bb.maxX; x2++) {
					for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                        BlockPos tmp = new BlockPos(x2, y2, z2);
                        state = world.getBlockState(tmp);
						if (state.isLadder(world, tmp, entity)) {
							if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			            		return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
			            	}
			            	if (state.hasProperty(BlockStateProperties.UP) && state.getValue(BlockStateProperties.UP)) {
			            		return Direction.UP;
			            	} else if (state.hasProperty(BlockStateProperties.NORTH) && state.getValue(BlockStateProperties.NORTH)) {
			            		return Direction.SOUTH;
			            	} else if (state.hasProperty(BlockStateProperties.WEST) && state.getValue(BlockStateProperties.WEST)) {
			            		return Direction.EAST;
			            	} else if (state.hasProperty(BlockStateProperties.SOUTH) && state.getValue(BlockStateProperties.SOUTH)) {
			            		return Direction.NORTH;
			            	} else if (state.hasProperty(BlockStateProperties.EAST) && state.getValue(BlockStateProperties.EAST)) {
			            		return Direction.WEST;
			            	}
                        }
                    }
                }
            }
        }
		return Direction.UP;
	}
}