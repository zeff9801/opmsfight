package yesman.epicfight.api.animation.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.RegistryObject;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.*;
import yesman.epicfight.api.utils.TypeFlexibleHashMap.TypeKey;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.entity.eventlistener.AttackEndEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import javax.annotation.Nullable;
import java.util.*;

public class AttackAnimation extends ActionAnimation {

	public final Phase[] phases;

	/** Entities that collided **/
	public static final TypeKey<List<LivingEntity>> HIT_ENTITIES = Lists::newArrayList;

	/** Entities that actually hurt **/
	public static final TypeKey<List<LivingEntity>> HURT_ENTITIES = Lists::newArrayList;

	public static final TypeKey<Integer> MAX_STRIKES_COUNT = () -> 0;

	public AttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		this(convertTime, path, armature, new Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, colliderJoint, collider));
	}

	public AttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, Hand hand, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		this(convertTime, path, armature, new Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, hand, colliderJoint, collider));
	}

	public AttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, Hand hand, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature, boolean noRegister) {
		this(convertTime, path, armature, noRegister, new Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, hand, colliderJoint, collider));
	}

	public AttackAnimation(float convertTime, String path, Armature armature, Phase... phases) {
		this(convertTime, path, armature, false, phases);
	}


	public AttackAnimation(float convertTime, String path, Armature armature, boolean noRegister, Phase... phases) {
		super(convertTime, path, armature, noRegister);

		this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_LOC_TARGET);
		this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_LOC_TARGET);
		this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
		this.phases = phases;
		this.stateSpectrumBlueprint.clear();

		for (Phase phase : phases) {
			if (!phase.noStateBind) {
				this.bindPhaseState(phase);
			}
		}
	}

	protected void bindPhaseState(Phase phase) {
		float preDelay = phase.preDelay;

		this.stateSpectrumBlueprint
				.newTimePair(phase.start, preDelay)
				.addState(EntityState.PHASE_LEVEL, 1)
				.newTimePair(phase.start, phase.contact)
				.addState(EntityState.CAN_SKILL_EXECUTION, false)
				.newTimePair(phase.start, phase.recovery)
				.addState(EntityState.MOVEMENT_LOCKED, true)
				.addState(EntityState.UPDATE_LIVING_MOTION, false)
				.addState(EntityState.CAN_BASIC_ATTACK, false)
				.newTimePair(phase.start, phase.end)
				.addState(EntityState.INACTION, true)
				.newTimePair(phase.antic, phase.end)
				.addState(EntityState.TURNING_LOCKED, true)
				.newTimePair(preDelay, phase.contact)
				.addState(EntityState.ATTACKING, true)
				.addState(EntityState.PHASE_LEVEL, 2)
				.newTimePair(phase.contact, phase.end)
				.addState(EntityState.PHASE_LEVEL, 3)
		;
	}

	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);

		entitypatch.setLastAttackSuccess(false);
	}

	@Override
	public void linkTick(LivingEntityPatch<?> entitypatch, DynamicAnimation linkAnimation) {
		super.linkTick(entitypatch, linkAnimation);

		if (!entitypatch.isLogicalClient() && entitypatch instanceof MobPatch<?> mobpatch) {
			AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
			float elapsedTime = player.getElapsedTime();
			EntityState state = this.getState(entitypatch, linkAnimation, elapsedTime);

			if (state.getLevel() == 1 && !state.turningLocked()) {
				mobpatch.getOriginal().getNavigation().stop();
				entitypatch.getOriginal().attackAnim = 2;
				LivingEntity target = entitypatch.getTarget();

				if (target != null) {
					entitypatch.rotateTo(target, entitypatch.getYRotLimit(), false);
				}
			}
		}

		if (!entitypatch.isLogicalClient()) {
			this.attackTick(entitypatch, linkAnimation);
		}
	}

	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);

		if (!entitypatch.isLogicalClient()) {
			this.attackTick(entitypatch, this);
		}
	}

	@Override
	public void end(LivingEntityPatch<?> entitypatch, DynamicAnimation nextAnimation, boolean isEnd) {
		super.end(entitypatch, nextAnimation, isEnd);

		if (entitypatch instanceof ServerPlayerPatch playerpatch && isEnd) {
			playerpatch.getEventListener().triggerEvents(EventType.ATTACK_ANIMATION_END_EVENT, new AttackEndEvent(playerpatch, this));
		}

		if (entitypatch instanceof HumanoidMobPatch<?> mobpatch && entitypatch.isLogicalClient()) {
			MobEntity entity = mobpatch.getOriginal();

			if (entity.getTarget() != null && !entity.getTarget().isAlive()) {
				entity.setTarget(null);
			}
		}
	}

	protected void attackTick(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
		float prevElapsedTime = player.getPrevElapsedTime();
		float elapsedTime = player.getElapsedTime();
		EntityState prevState = this.getState(entitypatch, animation, prevElapsedTime);
		EntityState state = this.getState(entitypatch, animation, elapsedTime);
		Phase phase = this.getPhaseByTime(animation.isLinkAnimation() ? 0.0F : elapsedTime);

		if (state.getLevel() == 1 && !state.turningLocked()) {
			if (entitypatch instanceof MobPatch<?> mobpatch) {
				mobpatch.getOriginal().getNavigation().stop();
				entitypatch.getOriginal().attackAnim = 2;
				LivingEntity target = entitypatch.getTarget();

				if (target != null) {
					entitypatch.rotateTo(target, entitypatch.getYRotLimit(), false);
				}
			}
		}

		if (prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2)) {
			if (!prevState.attacking() || (phase != this.getPhaseByTime(prevElapsedTime) && (state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2)))) {
				entitypatch.playSound(this.getSwingSound(entitypatch, phase), 0.0F, 0.0F);
				entitypatch.removeHurtEntities();
			}

			this.hurtCollidingEntities(entitypatch, prevElapsedTime, elapsedTime, prevState, state, phase);
		}
	}

	public void hurtCollidingEntities(LivingEntityPatch<?> entitypatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, Phase phase) {
		LivingEntity entity = entitypatch.getOriginal();
		float prevPoseTime = prevState.attacking() ? prevElapsedTime : phase.preDelay;
		float poseTime = state.attacking() ? elapsedTime : phase.contact;
		List<Entity> list = this.getPhaseByTime(elapsedTime).getCollidingEntities(entitypatch, this, prevPoseTime, poseTime, this.getPlaySpeed(entitypatch, this));

		if (!list.isEmpty()) {
			HitEntityList hitEntities = new HitEntityList(entitypatch, list, phase.getProperty(AttackPhaseProperty.HIT_PRIORITY).orElse(HitEntityList.Priority.DISTANCE));
			int maxStrikes = this.getMaxStrikes(entitypatch, phase);

			while (entitypatch.getCurrenltyHurtEntities().size() < maxStrikes && hitEntities.next()) {
				Entity hitten = hitEntities.getEntity();
				LivingEntity trueEntity = this.getTrueEntity(hitten);

				if (trueEntity != null && trueEntity.isAlive() && !entitypatch.getCurrenltyAttackedEntities().contains(trueEntity) && !entitypatch.isTeammate(hitten)) {
					if (hitten instanceof LivingEntity || hitten instanceof PartEntity) {
						if (entity.canSee(hitten)) {
							EpicFightDamageSource source = this.getEpicFightDamageSource(entitypatch, hitten, phase);
							int prevInvulTime = hitten.invulnerableTime;
							hitten.invulnerableTime = 0;

							AttackResult attackResult = entitypatch.attack(source, hitten, phase.hand);
							hitten.invulnerableTime = prevInvulTime;


							if (attackResult.resultType.dealtDamage()) {
								hitten.level.playSound(null, hitten.getX(), hitten.getY(), hitten.getZ(), this.getHitSound(entitypatch, phase), hitten.getSoundSource(), 1.0F, 1.0F);
								this.spawnHitParticle(((ServerWorld)hitten.level), entitypatch, hitten, phase);
							}

							entitypatch.getCurrenltyAttackedEntities().add(trueEntity);


							if (attackResult.resultType.shouldCount()) {
								entitypatch.getCurrenltyHurtEntities().add(trueEntity);
							}

						}
					}
				}
			}
		}
	}

	public LivingEntity getTrueEntity(Entity entity) {
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity;
		} else if (entity instanceof PartEntity<?> partEntity) {
			Entity parentEntity = partEntity.getParent();

			if (parentEntity instanceof LivingEntity livingEntity) {
				return livingEntity;
			}
		}

		return null;
	}

	@Override
	protected EntityState getState(LivingEntityPatch<?> entitypatch, DynamicAnimation animation, float time) {
		if (animation.isLinkAnimation()) {
			EntityState state = super.getState(entitypatch, animation, 0.0F);

			if (time + animation.getPlaySpeed(entitypatch, animation) * EpicFightOptions.A_TICK < animation.getTotalTime()) {
				state.setState(EntityState.ATTACKING, false);
			}

			return state;
		}

		return super.getState(entitypatch, animation, time);
	}

	@Override
	protected TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, DynamicAnimation animation, float time) {
		if (animation.isLinkAnimation()) {
			TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap = super.getStatesMap(entitypatch, animation, 0.0F);

			if (time + animation.getPlaySpeed(entitypatch, animation) * EpicFightOptions.A_TICK < animation.getTotalTime()) {
				stateMap.put((EntityState.StateFactor<?>)EntityState.ATTACKING, Boolean.valueOf(false));
			}

			return stateMap;
		}

		return super.getStatesMap(entitypatch, animation, time);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getState(EntityState.StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, DynamicAnimation animation, float time) {
		if (animation.isLinkAnimation()) {
			if (stateFactor == EntityState.ATTACKING && time + animation.getPlaySpeed(entitypatch, animation) * EpicFightOptions.A_TICK < animation.getTotalTime()) {
				return (T)Boolean.valueOf(false);
			}

			return super.getState(stateFactor, entitypatch, animation, 0.0F);
		}

		return super.getState(stateFactor, entitypatch, animation, time);
	}

	protected int getMaxStrikes(LivingEntityPatch<?> entitypatch, Phase phase) {
		return phase.getProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER).map((valueCorrector) -> valueCorrector.getTotalValue(entitypatch.getMaxStrikes(phase.hand))).orElse(Float.valueOf(entitypatch.getMaxStrikes(phase.hand))).intValue();
	}

	protected SoundEvent getSwingSound(LivingEntityPatch<?> entitypatch, Phase phase) {
		return phase.getProperty(AttackPhaseProperty.SWING_SOUND).orElse(entitypatch.getSwingSound(phase.hand));
	}

	protected SoundEvent getHitSound(LivingEntityPatch<?> entitypatch, Phase phase) {
		return phase.getProperty(AttackPhaseProperty.HIT_SOUND).orElse(entitypatch.getWeaponHitSound(phase.hand));
	}

	public EpicFightDamageSource getEpicFightDamageSource(LivingEntityPatch<?> entitypatch, Entity target, Phase phase) {
		return this.getEpicFightDamageSource(entitypatch.getDamageSource(this, phase.hand), entitypatch, target, phase);
	}

	public EpicFightDamageSource getEpicFightDamageSource(DamageSource originalSource, LivingEntityPatch<?> entitypatch, Entity target, Phase phase) {
		if (phase == null) {
			phase = this.getPhaseByTime(entitypatch.getAnimator().getPlayerFor(this).getElapsedTime());
		}

		EpicFightDamageSource extendedSource;

		if (originalSource instanceof EpicFightDamageSource epicfightDamageSource) {
			extendedSource = epicfightDamageSource;
		} else {
			extendedSource = EpicFightDamageSources.copy(originalSource).setAnimation(this);
		}

		phase.getProperty(AttackPhaseProperty.DAMAGE_MODIFIER).ifPresent((opt) -> {
			extendedSource.setDamageModifier(opt);
		});

		phase.getProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER).ifPresent((opt) -> {
			extendedSource.setArmorNegation(opt.getTotalValue(extendedSource.getArmorNegation()));
		});

		phase.getProperty(AttackPhaseProperty.IMPACT_MODIFIER).ifPresent((opt) -> {
			extendedSource.setImpact(opt.getTotalValue(extendedSource.getImpact()));
		});

		phase.getProperty(AttackPhaseProperty.STUN_TYPE).ifPresent((opt) -> {
			extendedSource.setStunType(opt);
		});

//		phase.getProperty(AttackPhaseProperty.EXTRA_DAMAGE).ifPresent((opt) -> {
//			opt.forEach(extendedSource::addExtraDamage);
//		});

		phase.getProperty(AttackPhaseProperty.SOURCE_LOCATION_PROVIDER).ifPresent((opt) -> {
			extendedSource.setInitialPosition(opt.apply(entitypatch));
		});

		phase.getProperty(AttackPhaseProperty.SOURCE_LOCATION_PROVIDER).ifPresentOrElse((opt) -> {
			extendedSource.setInitialPosition(opt.apply(entitypatch));
		}, () -> {
			extendedSource.setInitialPosition(entitypatch.getOriginal().position());
		});

		return extendedSource;
	}

	protected void spawnHitParticle(ServerWorld world, LivingEntityPatch<?> attacker, Entity hit, Phase phase) {
		Optional<RegistryObject<HitParticleType>> particleOptional = phase.getProperty(AttackPhaseProperty.PARTICLE);
		HitParticleType particle = particleOptional.isPresent() ? particleOptional.get().get() : attacker.getWeaponHitParticle(phase.hand);
		particle.spawnParticleWithArgument(world, null, null, hit, attacker.getOriginal());
	}

	@Override
	public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		if (entitypatch instanceof PlayerPatch<?> playerpatch) {
			Phase phase = this.getPhaseByTime(playerpatch.getAnimator().getPlayerFor(this).getElapsedTime());
			float speedFactor = this.getProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR).orElse(1.0F);
			Optional<Float> property = this.getProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED);
			float correctedSpeed = property.map((value) -> playerpatch.getAttackSpeed(phase.hand) / value).orElse(this.getTotalTime() * playerpatch.getAttackSpeed(phase.hand));
			correctedSpeed = Math.round(correctedSpeed * 1000.0F) / 1000.0F;

			return 1.0F + (correctedSpeed - 1.0F) * speedFactor;
		}

		return 1.0F;
	}

	public <V> AttackAnimation addProperty(AttackAnimationProperty<V> propertyType, V value) {
		this.properties.put(propertyType, value);
		return this;
	}

	public <V> AttackAnimation addProperty(AttackPhaseProperty<V> propertyType, V value) {
		return this.addProperty(propertyType, value, 0);
	}

	public <V> AttackAnimation addProperty(AttackPhaseProperty<V> propertyType, V value, int index) {
		this.phases[index].addProperty(propertyType, value);
		return this;
	}

	public Phase getPhaseByTime(float elapsedTime) {
		Phase currentPhase = null;

		for (Phase phase : this.phases) {
			currentPhase = phase;

			if (phase.end > elapsedTime) {
				break;
			}
		}

		return currentPhase;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderDebugging(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, float playbackTime, float partialTicks) {
		AnimationPlayer animPlayer = entitypatch.getAnimator().getPlayerFor(this);
		float prevElapsedTime = animPlayer.getPrevElapsedTime();
		float elapsedTime = animPlayer.getElapsedTime();
		Phase phase = this.getPhaseByTime(playbackTime);

		for (Pair<Joint, Collider> colliderInfo : phase.colliders) {
			Collider collider = colliderInfo.getSecond();

			if (collider == null) {
				collider = entitypatch.getColliderMatching(phase.hand);
			}

			collider.draw(poseStack, buffer, entitypatch, this, colliderInfo.getFirst(), prevElapsedTime, elapsedTime, partialTicks, this.getPlaySpeed(entitypatch, this));
		}
	}

	public static class JointColliderPair extends Pair<Joint, Collider> {
		public JointColliderPair(Joint first, Collider second) {
			super(first, second);
		}

		public static JointColliderPair of(Joint joint, Collider collider) {
			return new JointColliderPair(joint, collider);
		}
	}


	public static class Phase {
		private final Map<AttackPhaseProperty<?>, Object> properties = Maps.newHashMap();
		public final float start;
		public final float antic;
		public final float preDelay;
		public final float contact;
		public final float recovery;
		public final float end;
		public final Hand hand;
		public JointColliderPair[] colliders;

		//public final Joint first;
		//public final Collider second;

		public final boolean noStateBind;

		public Phase(float start, float antic, float contact, float recovery, float end, Joint joint, Collider collider) {
			this(start, antic, contact, recovery, end, Hand.MAIN_HAND, joint, collider);
		}

		public Phase(float start, float antic, float contact, float recovery, float end, Hand hand, Joint joint, Collider collider) {
			this(start, antic, antic, contact, recovery, end, hand, joint, collider);
		}

		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, Joint joint, Collider collider) {
			this(start, antic, preDelay, contact, recovery, end, Hand.MAIN_HAND, joint, collider);
		}

		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, Hand hand, Joint joint, Collider collider) {
			this(start, antic, preDelay, contact, recovery, end, false, hand, joint, collider);
		}

		public Phase(Hand hand, Joint joint, Collider collider) {
			this(0, 0, 0, 0, 0, 0, true, hand, joint, collider);
		}

		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, boolean noStateBind, Hand hand, Joint joint, Collider collider) {
			this(start, antic, preDelay, contact, recovery, end, noStateBind, hand, JointColliderPair.of(joint, collider));
		}

		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, Hand hand, JointColliderPair... colliders) {
			this(start, antic, preDelay, contact, recovery, end, false, hand, colliders);
		}

		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, boolean noStateBind, Hand hand, JointColliderPair... colliders) {
			if (start > end) {
				throw new IllegalArgumentException("Phase create exception: Start time is bigger than end time");
			}

			this.start = start;
			this.antic = antic;
			this.preDelay = preDelay;
			this.contact = contact;
			this.recovery = recovery;
			this.end = end;
			this.colliders = colliders;
			this.hand = hand;
			this.noStateBind = noStateBind;
		}

		public <V> Phase addProperty(AttackPhaseProperty<V> propertyType, V value) {
			this.properties.put(propertyType, value);
			return this;
		}

		public void addProperties(Set<Map.Entry<AttackPhaseProperty<?>, Object>> set) {
			for(Map.Entry<AttackPhaseProperty<?>, Object> entry : set) {
				this.properties.put(entry.getKey(), entry.getValue());
			}
		}

		@SuppressWarnings("unchecked")
		public <V> Optional<V> getProperty(AttackPhaseProperty<V> propertyType) {
			return (Optional<V>) Optional.ofNullable(this.properties.get(propertyType));
		}

		public List<Entity> getCollidingEntities(LivingEntityPatch<?> entitypatch, AttackAnimation animation, float prevElapsedTime, float elapsedTime, float attackSpeed) {
			Set<Entity> entities = Sets.newHashSet();

			for (Pair<Joint, Collider> colliderInfo : this.colliders) {
				Collider collider = colliderInfo.getSecond();

				if (collider == null) {
					collider = entitypatch.getColliderMatching(this.hand);
				}

				entities.addAll(collider.updateAndSelectCollideEntity(entitypatch, animation, prevElapsedTime, elapsedTime, colliderInfo.getFirst(), attackSpeed));
			}

			return new ArrayList<>(entities);
		}

		public JointColliderPair[] getColliders() {
			return this.colliders;
		}

		public Hand getHand() {
			return this.hand;
		}
	}
}