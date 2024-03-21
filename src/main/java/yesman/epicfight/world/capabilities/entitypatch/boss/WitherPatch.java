package yesman.epicfight.world.capabilities.entitypatch.boss;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.AttackResult.ResultType;
import yesman.epicfight.api.utils.EpicFightDamageSource;
import yesman.epicfight.api.utils.ExtendedDamageSource;
import yesman.epicfight.api.utils.ExtendedDamageSource.StunType;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.DroppedNetherStar;
import yesman.epicfight.world.entity.WitherGhostClone;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviorGoal;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

public class WitherPatch extends MobPatch<WitherEntity> {
	private static final DataParameter<Boolean> DATA_ARMOR_ACTIVED = EntityDataManager.defineId(WitherEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DATA_GHOST = EntityDataManager.defineId(WitherEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> DATA_TRANSPARENCY = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
	private static final DataParameter<Vector3d> DATA_LASER_DESTINATION_A = EntityDataManager.defineId(WitherEntity.class, EpicFightDataSerializers.VEC3);
	private static final DataParameter<Vector3d> DATA_LASER_DESTINATION_B = EntityDataManager.defineId(WitherEntity.class, EpicFightDataSerializers.VEC3);
	private static final DataParameter<Vector3d> DATA_LASER_DESTINATION_C = EntityDataManager.defineId(WitherEntity.class, EpicFightDataSerializers.VEC3);
	private static final List<DataParameter<Vector3d>> DATA_LASER_TARGET_POSITIONS = ImmutableList.of(DATA_LASER_DESTINATION_A, DATA_LASER_DESTINATION_B, DATA_LASER_DESTINATION_C);
	private static final DataParameter<Integer> DATA_LASER_TARGET_A = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
	private static final DataParameter<Integer> DATA_LASER_TARGET_B = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
	private static final DataParameter<Integer> DATA_LASER_TARGET_C = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
	private static final List<DataParameter<Integer>> DATA_LASER_TARGETS = ImmutableList.of(DATA_LASER_TARGET_A, DATA_LASER_TARGET_B, DATA_LASER_TARGET_C);
	
	private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = (p_213797_0_) -> {
		return p_213797_0_.getMobType() != CreatureAttribute.UNDEAD && p_213797_0_.attackable();
	};
	
	public static final EntityPredicate WTIHER_TARGETING_CONDITIONS = (new EntityPredicate()).range(20.0D).selector(LIVING_ENTITY_SELECTOR);
	public static final EntityPredicate WTIHER_GHOST_TARGETING_CONDITIONS = (new EntityPredicate()).range(20.0D).selector(LIVING_ENTITY_SELECTOR).ignoreInvisibilityTesting();
	
	private boolean blockedNow;
	private int deathTimerExt;
	private int blockingCount;
	private int blockingStartTick;
	private LivingEntityPatch<?> blockingEntity;
	
	@Override
	public void onConstructed(WitherEntity witherBoss) {
		super.onConstructed(witherBoss);
		this.original.getEntityData().define(DATA_ARMOR_ACTIVED, false);
		this.original.getEntityData().define(DATA_GHOST, false);
		this.original.getEntityData().define(DATA_TRANSPARENCY, 0);
		this.original.getEntityData().define(DATA_LASER_DESTINATION_A, new Vector3d(Double.NaN, Double.NaN, Double.NaN));
		this.original.getEntityData().define(DATA_LASER_DESTINATION_C, new Vector3d(Double.NaN, Double.NaN, Double.NaN));
		this.original.getEntityData().define(DATA_LASER_DESTINATION_B, new Vector3d(Double.NaN, Double.NaN, Double.NaN));
		this.original.getEntityData().define(DATA_LASER_TARGET_A, 0);
		this.original.getEntityData().define(DATA_LASER_TARGET_B, 0);
		this.original.getEntityData().define(DATA_LASER_TARGET_C, 0);
	}
	
	@Override
	public void initAI() {
		super.initAI();
		this.original.goalSelector.addGoal(1, new WitherChasingGoal());
		this.original.goalSelector.addGoal(0, new WitherGhostAttackGoal());
		this.original.goalSelector.addGoal(0, new CombatBehaviorGoal<>(this, MobCombatBehaviors.WITHER.build(this)));
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.original.getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(3.0F);
	}
	
	@Override
	public void initAnimator(ClientAnimator clientAnimator) {
		clientAnimator.addLivingAnimation(LivingMotions.IDLE, Animations.WITHER_IDLE);
		clientAnimator.addLivingAnimation(LivingMotions.DEATH, Animations.WITHER_DEATH);
		clientAnimator.setCurrentMotionsAsDefault();
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else {
			currentLivingMotion = LivingMotions.IDLE;
		}
	}
	
	@Override
	public void tick(LivingUpdateEvent event) {
		if (this.original.getHealth() <= 0.0F) {
			if (this.original.deathTime > 1 && this.deathTimerExt < 17) {
				this.deathTimerExt++;
				this.original.deathTime--;
			}
		}
		
		if (!this.getEntityState().inaction()) {
			int targetId = this.original.getAlternativeTarget(0);
			Entity target = this.original.level.getEntity(targetId);
			
			if (target != null) {
				Vector3d vec3 = target.position().subtract(this.original.position()).normalize();
				float yrot = MathUtils.rotlerp(this.original.yRot, (float)MathHelper.atan2(vec3.z, vec3.x) * (180F / (float)Math.PI) - 90.0F, 10.0F);
				this.original.yRot = (yrot);
			}
		}
		
		super.tick(event);
	}
	
	@Override
	public void clientTick(LivingUpdateEvent event) {
		super.clientTick(event);
		this.original.setDeltaMovement(0.0D, 0.0D, 0.0D);
		int transparencyCount = this.getTransparency();
		
		if (transparencyCount != 0) {
			this.setTransparency(transparencyCount + (transparencyCount > 0 ? -1 : 1));
		}
	}
	
	@Override
	public void serverTick(LivingUpdateEvent event) {
		super.serverTick(event);
		
		if (this.original.getHealth() <= this.original.getMaxHealth() * 0.5F) {
			if (!this.isArmorActivated() && !this.getEntityState().inaction() && this.original.getInvulnerableTicks() <= 0 && this.original.isAlive()) {
				this.playAnimationSynchronized(Animations.WITHER_SPELL_ARMOR, 0.0F);
			}
		} else {
			if (this.isArmorActivated()) {
				this.setArmorActivated(false);
			}
		}
		
		if (this.animator.getPlayerFor(null).getAnimation().equals(Animations.WITHER_CHARGE) && this.getEntityState().attacking() && ForgeEventFactory.getMobGriefingEvent(this.original.level, this.original)) {
			int x = MathHelper.floor(this.original.getX());
			int y = MathHelper.floor(this.original.getY());
			int z = MathHelper.floor(this.original.getZ());
			boolean flag = false;
			
			for (int j = -1; j <= 1; ++j) {
				for (int k2 = -1; k2 <= 1; ++k2) {
					for (int k = 0; k <= 3; ++k) {
						int l2 = x + j;
						int l = y + k;
						int i1 = z + k2;
						BlockPos blockpos = new BlockPos(l2, l, i1);
						BlockState blockstate = this.original.level.getBlockState(blockpos);
						
						if (blockstate.canEntityDestroy(this.original.level, blockpos, this.original) && ForgeEventFactory.onEntityDestroyBlock(this.original, blockpos, blockstate)) {
							flag = this.original.level.destroyBlock(blockpos, true, this.original) || flag;
						}
					}
				}
			}
			
			if (flag) {
				this.original.level.levelEvent((PlayerEntity) null, 1022, this.original.blockPosition(), 0);
			}
		}

		if (this.blockedNow) {
			if (this.blockingCount < 0) {
				this.playAnimationSynchronized(Animations.WITHER_NEUTRALIZED, 0.0F);
				this.original.playSound(EpicFightSounds.NEUTRALIZE_BOSSES, 5.0F, 1.0F);
				this.blockedNow = false;
				this.blockingEntity = null;
			} else {
				if (this.original.tickCount % 4 == (this.blockingStartTick - 1) % 4) {
					if (this.original.position().distanceToSqr(this.blockingEntity.getOriginal().position()) < 9.0D) {

						EpicFightDamageSource extendedSource = (EpicFightDamageSource) this.getDamageSource(StunType.SHORT, Animations.WITHER_CHARGE, Hand.MAIN_HAND);
						extendedSource.setImpact(4.0F);
						extendedSource.setInitialPosition(this.lastAttackPosition);
						AttackResult attackResult = this.tryHarm(this.blockingEntity.getOriginal(), extendedSource, blockingCount);

						if (attackResult.resultType == AttackResult.ResultType.SUCCESS) {
							this.blockingEntity.getOriginal().hurt((DamageSource)extendedSource, 4.0F);
							this.blockedNow = false;
							this.blockingEntity = null;
						}
					} else {
						this.blockedNow = false;
						this.blockingEntity = null;
					}
				}
			}
		}
	}
	
	@Override
	public void onAttackBlocked(HurtEvent.Pre hurtEvent, LivingEntityPatch<?> opponent) {
		DamageSource damageSource = hurtEvent.getDamageSource();
		
		if (damageSource instanceof ExtendedDamageSource) {
			ExtendedDamageSource extendedDamageSource = ((ExtendedDamageSource)damageSource);
			
			if (extendedDamageSource.getAnimationId() == Animations.WITHER_CHARGE.getId()) {
				if (!this.blockedNow) {
					this.blockedNow = true;
					this.blockingStartTick = this.original.tickCount;
					this.blockingEntity = opponent;
					this.playAnimationSynchronized(Animations.WITHER_BLOCKED, 0.0F);
				}
				
				this.blockingCount--;
				Vector3d lookAngle = opponent.getOriginal().getLookAngle();
				lookAngle = lookAngle.subtract(0.0D, lookAngle.y, 0.0D);
				lookAngle.scale(0.1D);
				
				Vector3d dest = opponent.getOriginal().position().add(lookAngle);
				
				this.original.setPos(dest.x, dest.y, dest.z);
			}
		}
	}
	
	@Override
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		DynamicAnimation animation = this.getAnimator().getPlayerFor(null).getAnimation();
		
		if (animation.equals(Animations.WITHER_CHARGE) || animation.equals(Animations.WITHER_BLOCKED)) {
			Entity entity = damageSource.getDirectEntity();
			
			if (entity instanceof AbstractArrowEntity) {
				return new AttackResult(ResultType.MISSED, 0.0F);
			}
		}
		
		return super.tryHurt(damageSource, amount);
	}

	@Override
	public void onDeath() {
		super.onDeath();
		
		if (!this.isLogicalClient() && this.original.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			Vector3d startMovement = this.original.getLookAngle().scale(0.4D).add(0.0D, 0.63D, 0.0D);
			ItemEntity itemEntity = new DroppedNetherStar(this.original.level, this.original.position().add(0.0D, this.original.getBbHeight() * 0.5D, 0.0D), startMovement);
			this.original.level.addFreshEntity(itemEntity);
		}
	}
	
	@Override
	public boolean onDrop(LivingDropsEvent event) {
		event.getDrops().removeIf((itemEntity) -> itemEntity.getItem().getItem().equals(Items.NETHER_STAR));
		return false;
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float prevYRot;
		float yRot;
		
		if (this.original.getVehicle() instanceof LivingEntity) {
			LivingEntity ridingEntity = (LivingEntity)this.original.getVehicle();
			prevYRot = ridingEntity.yBodyRotO;
			yRot = ridingEntity.yBodyRot;
		} else {
			prevYRot = this.isLogicalClient() ? this.original.yBodyRotO : this.original.yRotO;
			yRot = this.isLogicalClient() ? this.original.yBodyRot : this.original.yRot;
		}
		
		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, prevYRot, yRot, partialTicks, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public <M extends Model> M getEntityModel(Models<M> modelDB) {
		return modelDB.wither;
	}
	
	@Override
	public StaticAnimation getHitAnimation(StunType stunType) {
		return null;
	}
	
	public void startCharging() {
		this.setLastAttackPosition();
		this.blockingCount = 3;
	}
	
	public void setArmorActivated(boolean set) {
		this.original.getEntityData().set(DATA_ARMOR_ACTIVED, set);
	}
	
	public boolean isArmorActivated() {
		return this.original.getEntityData().get(DATA_ARMOR_ACTIVED);
	}
	
	public void setGhost(boolean set) {
		this.original.getEntityData().set(DATA_GHOST, set);
		this.original.setNoGravity(set);
		this.setTransparency(set ? 40 : -40);
		this.original.setInvisible(set);
	}
	
	public boolean isGhost() {
		return this.original.getEntityData().get(DATA_GHOST);
	}
	
	public void setTransparency(int set) {
		this.original.getEntityData().set(DATA_TRANSPARENCY, set);
	}
	
	public int getTransparency() {
		return this.original.getEntityData().get(DATA_TRANSPARENCY);
	}
	
	public void setLaserTargetPosition(int head, Vector3d pos) {
		this.original.getEntityData().set(DATA_LASER_TARGET_POSITIONS.get(head), pos);
	}
	
	public Vector3d getLaserTargetPosition(int head) {
		return this.original.getEntityData().get(DATA_LASER_TARGET_POSITIONS.get(head));
	}
	
	public void setLaserTarget(int head, Entity target) {
		this.original.getEntityData().set(DATA_LASER_TARGETS.get(head), target != null ? target.getId() : -1);
	}
	
	public Entity getLaserTargetEntity(int head) {
		int laserTarget = this.original.getEntityData().get(DATA_LASER_TARGETS.get(head));
		return laserTarget > 0 ? this.original.level.getEntity(laserTarget) : null;
	}
	
	public Entity getAlternativeTargetEntity(int head) {
		int id = this.original.getAlternativeTarget(head);
		
		return id > 0 ? this.original.level.getEntity(id) : null;
	}
	
	public double getHeadX(int index) {
		if (index <= 0) {
			return this.original.getX();
		} else {
			float f = (this.original.yRot + (float) (180 * (index - 1))) * ((float) Math.PI / 180F);
			float f1 = MathHelper.cos(f);
			return this.original.getX() + (double) f1 * 1.3D;
		}
	}
	
	public double getHeadY(int index) {
		return index <= 0 ? this.original.getY() + 3.0D : this.original.getY() + 2.2D;
	}
	
	public double getHeadZ(int index) {
		if (index <= 0) {
			return this.original.getZ();
		} else {
			float f = (this.original.yRot + (float) (180 * (index - 1))) * ((float) Math.PI / 180F);
			float f1 = MathHelper.sin(f);
			return this.original.getZ() + (double) f1 * 1.3D;
		}
	}
	
	public class WitherGhostAttackGoal extends Goal {
		private int ghostSummonCount;
		private int maxGhostSpawn;
		private int summonInverval;
		private int cooldown;
		
		public WitherGhostAttackGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}
		
		@Override
		public boolean canUse() {
			return --this.cooldown < 0 && WitherPatch.this.isArmorActivated() && !WitherPatch.this.getEntityState().inaction() && WitherPatch.this.original.getTarget() != null;
		}
		
		@Override
		public boolean canContinueToUse() {
			return this.ghostSummonCount <= this.maxGhostSpawn;
		}
		
		@Override
		public void start() {
			WitherPatch.this.playAnimationSynchronized(Animations.WITHER_GHOST_STANDBY, 0.0F);
			WitherPatch.this.updateEntityState();
			WitherPatch.this.setGhost(true);
			List<LivingEntity> nearbyEnemies = this.getNearbyTargets();
			this.ghostSummonCount = 0;
			this.summonInverval = 25;
			this.maxGhostSpawn = MathHelper.clamp(nearbyEnemies.size() / 2, 2, 4);
		}
		
		@Override
		public void tick() {
			if (--this.summonInverval <= 0) {
				if (this.ghostSummonCount < this.maxGhostSpawn) {
					List<LivingEntity> nearbyEnemies = this.getNearbyTargets();
					
					if (!nearbyEnemies.isEmpty()) {
						LivingEntity randomTarget = nearbyEnemies.get(WitherPatch.this.original.getRandom().nextInt(nearbyEnemies.size()));
						Vector3d summonPosition = randomTarget.position().add(new Vector3d(0.0D, 0.0D, 6.0D).yRot(WitherPatch.this.original.getRandom().nextFloat() * 360.0F));
						WitherGhostClone ghostclone = new WitherGhostClone((ServerWorld)WitherPatch.this.original.level, summonPosition, randomTarget);
						WitherPatch.this.original.level.addFreshEntity(ghostclone);
					} else {
						this.ghostSummonCount = this.maxGhostSpawn + 1;
					}
				}
				
				this.ghostSummonCount++;
				this.summonInverval = (this.ghostSummonCount < this.maxGhostSpawn) ? 25 : 35;
				
				if (this.ghostSummonCount == this.maxGhostSpawn) {
					LivingEntity target = WitherPatch.this.original.getTarget();
					
					if (target != null) {
						Vector3d summonPosition = target.position().add(new Vector3d(0.0D, 0.0D, 6.0D).yRot(WitherPatch.this.original.getRandom().nextFloat() * 360.0F)).add(0.0D, 5.0D, 0.0D);
						WitherPatch.this.original.setPos(summonPosition.x, summonPosition.y, summonPosition.z);
						WitherPatch.this.original.lookAt(EntityAnchorArgument.Type.FEET, WitherPatch.this.original.getTarget().position());
					}
				}
			}
		}
		
		@Override
		public void stop() {
			this.cooldown = 300;
			
			if (WitherPatch.this.original.getTarget() != null) {
				WitherPatch.this.playSound(SoundEvents.WITHER_AMBIENT, -0.1F, 0.1F);
				WitherPatch.this.playAnimationSynchronized(Animations.WITHER_CHARGE, 0.0F);
			} else {
				WitherPatch.this.playAnimationSynchronized(Animations.OFF_ANIMATION_HIGHEST, 0.0F);
			}
			
			WitherPatch.this.setGhost(false);
		}
		
		public List<LivingEntity> getNearbyTargets() {
			return WitherPatch.this.original.level.getNearbyEntities(LivingEntity.class, WTIHER_GHOST_TARGETING_CONDITIONS, WitherPatch.this.original, WitherPatch.this.original.getBoundingBox().inflate(20.0D, 5.0D, 20.0D));
		}
	}
	
	public class WitherChasingGoal extends Goal {
		public WitherChasingGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}
		
		@Override
		public boolean canUse() {
			return WitherPatch.this.original.getAlternativeTarget(0) > 0;
		}
		
		@Override
		public void tick() {
			WitherEntity witherBoss = WitherPatch.this.getOriginal();
			Vector3d vec3 = witherBoss.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D);
			Entity entity = witherBoss.level.getEntity(WitherPatch.this.original.getAlternativeTarget(0));
			
			if (!WitherPatch.this.getEntityState().hurt() && !WitherPatch.this.blockedNow) {
				if (entity != null) {
					Vector3d vec31 = new Vector3d(entity.getX() - witherBoss.getX(), 0.0D, entity.getZ() - witherBoss.getZ());
					double d0 = vec3.y;
					
					if (witherBoss.getY() < entity.getY() || !witherBoss.isPowered() && witherBoss.getY() < entity.getY() + 5.0D && !WitherPatch.this.getAnimator().getPlayerFor(null).getAnimation().getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(false)) {
						d0 = Math.max(0.0D, d0);
						d0 = d0 + (0.3D - d0 * (double) 0.6F);
					}
					
					vec3 = new Vector3d(vec3.x, d0, vec3.z);
					double followingRange = witherBoss.isPowered() ? 9.0D : 49.0D;
					
					if (MathUtils.horizontalDistanceSqr(vec31) > followingRange && !WitherPatch.this.getEntityState().inaction()) {
						Vector3d vec32 = vec31.normalize();
						vec3 = vec3.add(vec32.x * 0.3D - vec3.x * 0.6D, 0.0D, vec32.z * 0.3D - vec3.z * 0.6D);
					}
				}
				
				witherBoss.setDeltaMovement(vec3);
			}
		}
	}
}