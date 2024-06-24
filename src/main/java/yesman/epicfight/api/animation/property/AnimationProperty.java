package yesman.epicfight.api.animation.property;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.RegistryObject;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.MoveCoordFunctions.MoveCoordGetter;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.ExtendedDamageSource.StunType;
import yesman.epicfight.api.utils.HitEntityList.Priority;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ExtraDamageType;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Function;


public abstract class AnimationProperty<T> {
	public static class StaticAnimationProperty<T> extends AnimationProperty<T> {
		/**
		 * You can put the various events in animation. Must be registered in order of time.
		 */
		public static final StaticAnimationProperty<StaticAnimation.Event[]> EVENTS = new StaticAnimationProperty<StaticAnimation.Event[]> ();

		/**
		 * You can set the fixed play speed of the animation.
		 */
		public static final StaticAnimationProperty<Float> PLAY_SPEED = new StaticAnimationProperty<Float> ();


		public static final StaticAnimationProperty<PlaybackTimeModifier> ELAPSED_TIME_MODIFIER = new StaticAnimationProperty<PlaybackTimeModifier> ();

	}
	
	public static class MoveCoordFunctions<T> extends AnimationProperty<T> {
		/**
		 * This property will set the entity's delta movement to (0, 0, 0) on beginning of the animation if true.
		 */
		public static final MoveCoordFunctions<Boolean> STOP_MOVEMENT = new MoveCoordFunctions<Boolean>();
		
		/**
		 * This property will move entity's coord of y-axis according to animation's coord if true.
		 */
		public static final MoveCoordFunctions<Boolean> MOVE_VERTICAL = new MoveCoordFunctions<Boolean>();

		/**
		 * Coord of action animation
		 */
		public static final MoveCoordFunctions<TransformSheet> COORD = new MoveCoordFunctions<TransformSheet>();
		
		/**
		 * This property determines whether to move the entity in link animation or not.
		 */
		public static final MoveCoordFunctions<Boolean> MOVE_ON_LINK = new MoveCoordFunctions<Boolean>();
		
		/**
		 * You can specify the coord movement time in action animation. Must be registered in order of time.
		 */
		public static final MoveCoordFunctions<ActionAnimation.ActionTime[]> MOVE_TIME = new MoveCoordFunctions<ActionAnimation.ActionTime[]>();
		
		/**
		 * Set the dynamic coordinates of action animation.
		 */
		public static final MoveCoordFunctions<MoveCoordSetter> COORD_SET_BEGIN = new MoveCoordFunctions<MoveCoordSetter>();
		
		/**
		 * Set the dynamic coordinates of action animation.
		 */
		public static final MoveCoordFunctions<MoveCoordSetter> COORD_SET_TICK = new MoveCoordFunctions<MoveCoordSetter>();
		
		/**
		 * This property determines if the speed effect will increase the move distance.
		 */
		public static final MoveCoordFunctions<Boolean> AFFECT_SPEED = new MoveCoordFunctions<Boolean>();
		
		/**
		 * This property determines if the movement can be canceled by {@link LivingEntityPatch#shouldBlockMoving()}.
		 */
		public static final MoveCoordFunctions<Boolean> CANCELABLE_MOVE = new MoveCoordFunctions<Boolean>();
	}
	public static class ActionAnimationProperty<T> extends AnimationProperty<T> {
		/**
		 * This property will set the entity's delta movement to (0, 0, 0) on beginning of the animation if true.
		 */
		public static final ActionAnimationProperty<Boolean> STOP_MOVEMENT = new ActionAnimationProperty<Boolean> ();

		/**
		 * This property will move entity's coord also as y-axis if true.
		 * Don't recommend using this property because it's old system. Use the coord joint instead.
		 */
		public static final ActionAnimationProperty<Boolean> MOVE_VERTICAL = new ActionAnimationProperty<Boolean> ();

		/**
		 * This property determines the time of entity not affected by gravity.
		 */
		public static final ActionAnimationProperty<TimePairList> NO_GRAVITY_TIME = new ActionAnimationProperty<TimePairList> ();

		/**
		 * Coord of action animation
		 */
		public static final ActionAnimationProperty<TransformSheet> COORD = new ActionAnimationProperty<TransformSheet> ();

		/**
		 * This property determines whether to move the entity in link animation or not.
		 */
		public static final ActionAnimationProperty<Boolean> MOVE_ON_LINK = new ActionAnimationProperty<Boolean> ();

		/**
		 * You can specify the coord movement time in action animation. Must be registered in order of time.
		 */
		public static final ActionAnimationProperty<TimePairList> MOVE_TIME = new ActionAnimationProperty<TimePairList> ();

		/**
		 * Set the dynamic coordinates of action animation.
		 */
		public static final ActionAnimationProperty<MoveCoordSetter> COORD_SET_BEGIN = new ActionAnimationProperty<MoveCoordSetter> ();

		/**
		 * Set the dynamic coordinates of action animation.
		 */
		public static final ActionAnimationProperty<MoveCoordSetter> COORD_SET_TICK = new ActionAnimationProperty<MoveCoordSetter> ();

		/**
		 * Set the coordinates of action animation.
		 */
		public static final ActionAnimationProperty<MoveCoordGetter> COORD_GET = new ActionAnimationProperty<MoveCoordGetter> ();

		/**
		 * This property determines if the speed effect will increase the move distance.
		 */
		public static final ActionAnimationProperty<Boolean> AFFECT_SPEED = new ActionAnimationProperty<Boolean> ();

		/**
		 * This property determines if the movement can be canceled by {@link LivingEntityPatch#shouldBlockMoving()}.
		 */
		public static final ActionAnimationProperty<Boolean> CANCELABLE_MOVE = new ActionAnimationProperty<Boolean> ();

		/**
		 * Death animations won't be played if this value is true
		 */
		public static final ActionAnimationProperty<Boolean> IS_DEATH_ANIMATION = new ActionAnimationProperty<Boolean> ();

		/**
		 * This property determines the update time of {@link ActionAnimationProperty#COORD_SET_TICK}
		 */
		public static final ActionAnimationProperty<TimePairList> COORD_UPDATE_TIME = new ActionAnimationProperty<TimePairList> ();

		/**
		 * This property determines if it reset the player baic attack combo counter or not {@link BasicAttack}
		 */
		public static final ActionAnimationProperty<Boolean> RESET_PLAYER_COMBO_COUNTER = new ActionAnimationProperty<Boolean> ();
	}

	@FunctionalInterface
	public interface MoveCoordSetter {
		public void set(DynamicAnimation self, LivingEntityPatch<?> entitypatch, TransformSheet transformSheet);
	}
	
	public static class AttackAnimationProperty<T> extends AnimationProperty<T> {
		/**
		 * This property determines if the player's camera is fixed during the attacking phase.
		 */
		public static final AttackAnimationProperty<Boolean> LOCK_ROTATION = new AttackAnimationProperty<Boolean> ();
		
		/**
		 * This property determines the animation can be rotated vertically based on the player's view.
		 */
		public static final AttackAnimationProperty<Boolean> ROTATE_X = new AttackAnimationProperty<Boolean> ();
		
		/**
		 * This property determines if the animation has a fixed amount of move distance not depending on the distance between attacker and target entity
		 */
		public static final AttackAnimationProperty<Boolean> FIXED_MOVE_DISTANCE = new AttackAnimationProperty<Boolean> ();
		
		/**
		 * This property determines how much the play speed affect by entity's attack speed.
		 */
		public static final AttackAnimationProperty<Float> ATTACK_SPEED_FACTOR = new AttackAnimationProperty<Float> ();
		
		/**
		 * This property determines the basis of the speed factor. Without this value, the basis is the total animation time.
		 */
		public static final AttackAnimationProperty<Float> BASIS_ATTACK_SPEED = new AttackAnimationProperty<Float> ();

		/**
		 * This property adds colliders when detecting hit entity by @MultiCollider.
		 */
		public static final AttackAnimationProperty<Integer> EXTRA_COLLIDERS = new AttackAnimationProperty<Integer> ();

		public static final MoveCoordSetter RAW_COORD = (self, entitypatch, transformSheet) -> {
			transformSheet.readFrom(self.getCoord().copyAll());
		};
	}


	public static class AttackPhaseProperty<T> extends AnimationProperty<T> {
		public static final AttackPhaseProperty<ValueModifier> MAX_STRIKES_MODIFIER = new AttackPhaseProperty<ValueModifier> ();
		public static final AttackPhaseProperty<ValueModifier> DAMAGE = new AttackPhaseProperty<ValueModifier> ();
		public static final AttackPhaseProperty<ExtraDamageType> EXTRA_DAMAGE = new AttackPhaseProperty<ExtraDamageType> ();
		public static final AttackPhaseProperty<ValueModifier> ARMOR_NEGATION = new AttackPhaseProperty<ValueModifier> ();
		public static final AttackPhaseProperty<ValueModifier> IMPACT = new AttackPhaseProperty<ValueModifier> ();
		public static final AttackPhaseProperty<StunType> STUN_TYPE = new AttackPhaseProperty<StunType> ();
		public static final AttackPhaseProperty<SoundEvent> SWING_SOUND = new AttackPhaseProperty<SoundEvent> ();
		public static final AttackPhaseProperty<SoundEvent> HIT_SOUND = new AttackPhaseProperty<SoundEvent> ();
		public static final AttackPhaseProperty<RegistryObject<HitParticleType>> PARTICLE = new AttackPhaseProperty<RegistryObject<HitParticleType>> ();
		public static final AttackPhaseProperty<Priority> HIT_PRIORITY = new AttackPhaseProperty<Priority> ();
		public static final AttackPhaseProperty<Boolean> FINISHER = new AttackPhaseProperty<Boolean> ();
		public static final AttackPhaseProperty<Function<LivingEntityPatch<?>, Vector3d>> SOURCE_LOCATION_PROVIDER = new AttackPhaseProperty<Function<LivingEntityPatch<?>, Vector3d>> ();
	}
	@FunctionalInterface
	public interface PlaybackTimeModifier {
		Pair<Float, Float> modify(DynamicAnimation self, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime);
	}
}