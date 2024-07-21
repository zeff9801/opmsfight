package yesman.epicfight.gameasset;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.api.animation.types.grappling.GrapplingAttackAnimation;
import yesman.epicfight.api.animation.types.grappling.GrapplingTryAnimation;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.HitEntityList.Priority;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.VectorUtils;
import yesman.epicfight.api.utils.math.*;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.identity.MeteorSlamSkill;
import yesman.epicfight.skill.weaponinnate.SteelWhirlwindSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Animations {
	public static StaticAnimation DUMMY_ANIMATION = new StaticAnimation() {

		AnimationClip animatinoClip = new AnimationClip();

		@Override
		public void loadAnimation(IResourceManager resourceManager) {
		}

		@Override
		public AnimationClip getAnimationClip() {
			return this.animatinoClip;
		}
	};
	public static StaticAnimation BIPED_IDLE;
	public static StaticAnimation BIPED_WALK;
	public static StaticAnimation BIPED_RUN;
	public static StaticAnimation BIPED_SNEAK;
	public static StaticAnimation BIPED_SWIM;
	public static StaticAnimation BIPED_FLOAT;
	public static StaticAnimation BIPED_KNEEL;
	public static StaticAnimation BIPED_FALL;
	public static StaticAnimation BIPED_FLYING;
	public static StaticAnimation BIPED_CREATIVE_IDLE;
	public static StaticAnimation BIPED_CREATIVE_FLYING;
	public static StaticAnimation BIPED_CREATIVE_FLYING_FORWARD;
	public static StaticAnimation BIPED_CREATIVE_FLYING_BACKWARD;
	public static StaticAnimation BIPED_MOUNT;
	public static StaticAnimation BIPED_SIT;
	public static StaticAnimation BIPED_JUMP;
	public static StaticAnimation BIPED_DEATH;
	public static StaticAnimation BIPED_DIG_MAINHAND;
	public static StaticAnimation BIPED_DIG_OFFHAND;
	public static StaticAnimation BIPED_DIG;
	public static StaticAnimation BIPED_RUN_SPEAR;
	public static StaticAnimation BIPED_HOLD_GREATSWORD;
	public static StaticAnimation BIPED_HOLD_UCHIGATANA_SHEATHING;
	public static StaticAnimation BIPED_HOLD_UCHIGATANA;
	public static StaticAnimation BIPED_HOLD_TACHI;
	public static StaticAnimation BIPED_HOLD_LONGSWORD;
	public static StaticAnimation BIPED_HOLD_LIECHTENAUER;
	public static StaticAnimation BIPED_HOLD_SPEAR;
	public static StaticAnimation BIPED_HOLD_DUAL_WEAPON;
	public static StaticAnimation BIPED_HOLD_CROSSBOW;
	public static StaticAnimation BIPED_HOLD_MAP_TWOHAND;
	public static StaticAnimation BIPED_HOLD_MAP_OFFHAND;
	public static StaticAnimation BIPED_HOLD_MAP_MAINHAND;
	public static StaticAnimation BIPED_HOLD_MAP_TWOHAND_MOVE;
	public static StaticAnimation BIPED_HOLD_MAP_OFFHAND_MOVE;
	public static StaticAnimation BIPED_HOLD_MAP_MAINHAND_MOVE;
	public static StaticAnimation BIPED_WALK_GREATSWORD;
	public static StaticAnimation BIPED_WALK_SPEAR;
	public static StaticAnimation BIPED_WALK_UCHIGATANA_SHEATHING;
	public static StaticAnimation BIPED_WALK_UCHIGATANA;
	public static StaticAnimation BIPED_WALK_TWOHAND;
	public static StaticAnimation BIPED_WALK_LONGSWORD;
	public static StaticAnimation BIPED_WALK_LIECHTENAUER;
	public static StaticAnimation BIPED_RUN_GREATSWORD;
	public static StaticAnimation BIPED_RUN_UCHIGATANA;
	public static StaticAnimation BIPED_RUN_UCHIGATANA_SHEATHING;
	public static StaticAnimation BIPED_RUN_DUAL;
	public static StaticAnimation BIPED_RUN_LONGSWORD;
	public static StaticAnimation BIPED_UCHIGATANA_SCRAP;
	public static StaticAnimation BIPED_LIECHTENAUER_READY;
	public static StaticAnimation BIPED_HIT_SHIELD;
	public static StaticAnimation BIPED_CLIMBING;
	public static StaticAnimation BIPED_SLEEPING;
	public static StaticAnimation BIPED_BOW_AIM;
	public static StaticAnimation BIPED_BOW_SHOT;
	public static StaticAnimation BIPED_DRINK;
	public static StaticAnimation BIPED_EAT;
	public static StaticAnimation BIPED_SPYGLASS_USE;
	public static StaticAnimation BIPED_CROSSBOW_AIM;
	public static StaticAnimation BIPED_CROSSBOW_SHOT;
	public static StaticAnimation BIPED_CROSSBOW_RELOAD;
	public static StaticAnimation BIPED_JAVELIN_AIM;
	public static StaticAnimation BIPED_JAVELIN_THROW;
	public static StaticAnimation BIPED_HIT_SHORT;
	public static StaticAnimation BIPED_HIT_LONG;
	public static StaticAnimation BIPED_HIT_ON_MOUNT;
	public static StaticAnimation BIPED_LANDING;
	public static StaticAnimation BIPED_KNOCKDOWN;
	public static StaticAnimation BIPED_BLOCK;
	public static StaticAnimation BIPED_ROLL_FORWARD;
	public static StaticAnimation BIPED_ROLL_BACKWARD;
	public static StaticAnimation BIPED_STEP_FORWARD;
	public static StaticAnimation BIPED_STEP_BACKWARD;
	public static StaticAnimation BIPED_STEP_LEFT;
	public static StaticAnimation BIPED_STEP_RIGHT;
	public static StaticAnimation BIPED_KNOCKDOWN_WAKEUP_LEFT;
	public static StaticAnimation BIPED_KNOCKDOWN_WAKEUP_RIGHT;
	public static StaticAnimation BIPED_DEMOLITION_LEAP_CHARGING;
	public static StaticAnimation BIPED_DEMOLITION_LEAP;
	public static StaticAnimation AXE_AUTO1;
	public static StaticAnimation AXE_AUTO2;
	public static StaticAnimation AXE_DASH;
	public static StaticAnimation AXE_AIRSLASH;
	public static StaticAnimation FIST_AUTO1;
	public static StaticAnimation FIST_AUTO2;
	public static StaticAnimation FIST_AUTO3;
	public static StaticAnimation FIST_DASH;
	public static StaticAnimation FIST_AIR_SLASH;
	public static StaticAnimation SPEAR_ONEHAND_AUTO;
	public static StaticAnimation SPEAR_BLOCK_AUTO;
	public static StaticAnimation SPEAR_ONEHAND_AIR_SLASH;
	public static StaticAnimation SPEAR_TWOHAND_AUTO1;
	public static StaticAnimation SPEAR_TWOHAND_AUTO2;
	public static StaticAnimation SPEAR_TWOHAND_AIR_SLASH;
	public static StaticAnimation SPEAR_DASH;
	public static StaticAnimation SPEAR_MOUNT_ATTACK;
	public static StaticAnimation SPEAR_GUARD;
	public static StaticAnimation SPEAR_GUARD_HIT;
	public static StaticAnimation SWORD_AUTO1;
	public static StaticAnimation SWORD_AUTO2;
	public static StaticAnimation SWORD_AUTO3;
	public static StaticAnimation SWORD_DASH;
	public static StaticAnimation SWORD_AIR_SLASH;
	public static StaticAnimation SWORD_GUARD;
	public static StaticAnimation SWORD_GUARD_HIT;
	public static StaticAnimation SWORD_GUARD_ACTIVE_HIT1;
	public static StaticAnimation SWORD_GUARD_ACTIVE_HIT2;
	public static StaticAnimation SWORD_GUARD_ACTIVE_HIT3;
	public static StaticAnimation LONGSWORD_GUARD_ACTIVE_HIT1;
	public static StaticAnimation LONGSWORD_GUARD_ACTIVE_HIT2;
	public static StaticAnimation SWORD_DUAL_AUTO1;
	public static StaticAnimation SWORD_DUAL_AUTO2;
	public static StaticAnimation SWORD_DUAL_AUTO3;
	public static StaticAnimation SWORD_DUAL_DASH;
	public static StaticAnimation SWORD_DUAL_AIR_SLASH;
	public static StaticAnimation SWORD_DUAL_GUARD;
	public static StaticAnimation SWORD_DUAL_GUARD_HIT;
	public static StaticAnimation BIPED_COMMON_NEUTRALIZED;
	public static StaticAnimation GREATSWORD_GUARD_BREAK;
	public static StaticAnimation METEOR_SLAM;
	public static StaticAnimation REVELATION_ONEHAND;
	public static StaticAnimation REVELATION_TWOHAND;
	public static StaticAnimation LONGSWORD_AUTO1;
	public static StaticAnimation LONGSWORD_AUTO2;
	public static StaticAnimation LONGSWORD_AUTO3;
	public static StaticAnimation LONGSWORD_DASH;
	public static StaticAnimation LONGSWORD_LIECHTENAUER_AUTO1;
	public static StaticAnimation LONGSWORD_LIECHTENAUER_AUTO2;
	public static StaticAnimation LONGSWORD_LIECHTENAUER_AUTO3;
	public static StaticAnimation LONGSWORD_AIR_SLASH;
	public static StaticAnimation LONGSWORD_GUARD;
	public static StaticAnimation LONGSWORD_GUARD_HIT;
	public static StaticAnimation TACHI_AUTO1;
	public static StaticAnimation TACHI_AUTO2;
	public static StaticAnimation TACHI_AUTO3;
	public static StaticAnimation TACHI_DASH;
	public static StaticAnimation TOOL_AUTO1;
	public static StaticAnimation TOOL_AUTO2;
	public static StaticAnimation TOOL_DASH;
	public static StaticAnimation UCHIGATANA_AUTO1;
	public static StaticAnimation UCHIGATANA_AUTO2;
	public static StaticAnimation UCHIGATANA_AUTO3;
	public static StaticAnimation UCHIGATANA_DASH;
	public static StaticAnimation UCHIGATANA_AIR_SLASH;
	public static StaticAnimation UCHIGATANA_SHEATHING_AUTO;
	public static StaticAnimation UCHIGATANA_SHEATHING_DASH;
	public static StaticAnimation UCHIGATANA_SHEATH_AIR_SLASH;
	public static StaticAnimation UCHIGATANA_GUARD;
	public static StaticAnimation UCHIGATANA_GUARD_HIT;
	public static StaticAnimation SWORD_MOUNT_ATTACK;
	public static StaticAnimation GREATSWORD_AUTO1;
	public static StaticAnimation GREATSWORD_AUTO2;
	public static StaticAnimation GREATSWORD_DASH;
	public static StaticAnimation GREATSWORD_AIR_SLASH;
	public static StaticAnimation GREATSWORD_GUARD;
	public static StaticAnimation GREATSWORD_GUARD_HIT;
	public static StaticAnimation DAGGER_AUTO1;
	public static StaticAnimation DAGGER_AUTO2;
	public static StaticAnimation DAGGER_AUTO3;
	public static StaticAnimation DAGGER_DASH;
	public static StaticAnimation DAGGER_AIR_SLASH;
	public static StaticAnimation DAGGER_DUAL_AUTO1;
	public static StaticAnimation DAGGER_DUAL_AUTO2;
	public static StaticAnimation DAGGER_DUAL_AUTO3;
	public static StaticAnimation DAGGER_DUAL_AUTO4;
	public static StaticAnimation DAGGER_DUAL_DASH;
	public static StaticAnimation DAGGER_DUAL_AIR_SLASH;
	public static StaticAnimation TRIDENT_AUTO1;
	public static StaticAnimation TRIDENT_AUTO2;
	public static StaticAnimation TRIDENT_AUTO3;
	public static StaticAnimation THE_GUILLOTINE;
	public static StaticAnimation SWEEPING_EDGE;
	public static StaticAnimation DANCING_EDGE;
	public static StaticAnimation HEARTPIERCER;
	public static StaticAnimation GRASPING_SPIRAL_FIRST;
	public static StaticAnimation GRASPING_SPIRAL_SECOND;
	public static StaticAnimation STEEL_WHIRLWIND_CHARGING;
	public static StaticAnimation STEEL_WHIRLWIND;
	public static StaticAnimation BATTOJUTSU;
	public static StaticAnimation BATTOJUTSU_DASH;
	public static StaticAnimation RUSHING_TEMPO1;
	public static StaticAnimation RUSHING_TEMPO2;
	public static StaticAnimation RUSHING_TEMPO3;
	public static StaticAnimation RELENTLESS_COMBO;
	public static StaticAnimation EVISCERATE_FIRST;
	public static StaticAnimation EVISCERATE_SECOND;
	public static StaticAnimation BLADE_RUSH_COMBO1;
	public static StaticAnimation BLADE_RUSH_COMBO2;
	public static StaticAnimation BLADE_RUSH_COMBO3;
	public static StaticAnimation BLADE_RUSH_HIT;
	public static StaticAnimation BLADE_RUSH_EXECUTE_BIPED;
	public static StaticAnimation BLADE_RUSH_TRY;
	public static StaticAnimation BLADE_RUSH_FAILED;
	public static StaticAnimation WRATHFUL_LIGHTING;
	public static StaticAnimation TSUNAMI;
	public static StaticAnimation TSUNAMI_REINFORCED;
	public static StaticAnimation EVERLASTING_ALLEGIANCE_CALL;
	public static StaticAnimation EVERLASTING_ALLEGIANCE_CATCH;
	public static StaticAnimation SHARP_STAB;
	public static StaticAnimation OFF_ANIMATION_HIGHEST;
	public static StaticAnimation OFF_ANIMATION_MIDDLE;
	public static StaticAnimation OFF_ANIMATION_LOWEST;

	public static StaticAnimation BIPED_STAFF_IDLE;
	public static StaticAnimation BIPED_STAFF_RUN;
	public static StaticAnimation STAFF_AUTO1;
	public static StaticAnimation STAFF_AUTO2;
	public static StaticAnimation STAFF_AUTO3;

	public static StaticAnimation BIPED_PHANTOM_ASCENT_FORWARD;
	public static StaticAnimation BIPED_PHANTOM_ASCENT_BACKWARD;

	@SubscribeEvent
	public static void registerAnimations(AnimationRegistryEvent event) {
		event.getRegistryMap().put(EpicFightMod.MODID, Animations::build);
	}


	private static void build() {
		HumanoidArmature biped = Armatures.BIPED;

		//Map
		BIPED_HOLD_MAP_TWOHAND = new StaticAnimation(true, "biped/living/hold_map_twohand", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION);
		BIPED_HOLD_MAP_OFFHAND = new StaticAnimation(true, "biped/living/hold_map_offhand", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION);
		BIPED_HOLD_MAP_MAINHAND = new StaticAnimation(true, "biped/living/hold_map_mainhand", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION);
		BIPED_HOLD_MAP_TWOHAND_MOVE = new StaticAnimation(true, "biped/living/hold_map_twohand_move", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION);
		BIPED_HOLD_MAP_OFFHAND_MOVE = new StaticAnimation(true, "biped/living/hold_map_offhand_move", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION);
		BIPED_HOLD_MAP_MAINHAND_MOVE = new StaticAnimation(true, "biped/living/hold_map_mainhand_move", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION);

		//Biped Animations
		BIPED_IDLE = new StaticAnimation(true, "biped/living/idle", biped);
		BIPED_WALK = new MovementAnimation(true, "biped/living/walk", biped);
		BIPED_FLYING = new StaticAnimation(true, "biped/living/fly", biped);
		BIPED_CREATIVE_IDLE = new StaticAnimation(true, "biped/living/creative_idle", biped);
		BIPED_CREATIVE_FLYING_FORWARD = new MovementAnimation(EpicFightOptions.GENERAL_ANIMATION_CONVERT_TIME, true, "biped/living/creative_fly_forward", biped, true)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.FLYING_CORRECTION);

		BIPED_CREATIVE_FLYING_BACKWARD = new MovementAnimation(EpicFightOptions.GENERAL_ANIMATION_CONVERT_TIME, true, "biped/living/creative_fly_backward", biped, true)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.FLYING_CORRECTION2);

		BIPED_CREATIVE_FLYING = new SelectiveAnimation((entitypatch) -> {
			Vector3d view = entitypatch.getOriginal().getViewVector(1.0F);
			Vector3d move = entitypatch.getOriginal().getDeltaMovement();

			double dot = view.dot(move);

			return dot < 0.0D ? 1 : 0;
		}, "biped/living/creative_fly", BIPED_CREATIVE_FLYING_FORWARD, BIPED_CREATIVE_FLYING_BACKWARD);
		BIPED_SPYGLASS_USE = new MirrorAnimation(0.15F, true, "biped/living/spyglass", "biped/living/spyglass_mainhand", "biped/living/spyglass_offhand", biped)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
					if (self.isLinkAnimation()) {
						return speed;
					}

					return 0.0F;
				})
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, (self, pose, entitypatch, elapsedTime, partialTicks) -> {
					if (entitypatch.isFirstPerson()) {
						pose.getJointTransformData().clear();
					} else if (!(self.isLinkAnimation())) {
						LivingMotion livingMotion = entitypatch.getCurrentLivingMotion();
						Pose rawPose;

						if (livingMotion == LivingMotions.SWIM || livingMotion == LivingMotions.FLY || livingMotion == LivingMotions.CREATIVE_FLY) {
							rawPose = self.getRawPose(3.3333F);
						} else {
							float xRot = MathHelper.clamp((entitypatch.getOriginal().xRot + 90.0F) * 0.0166666666666667F, 0.0F, 3.0F);
							rawPose = self.getRawPose(xRot);
							float f = 90.0F;
							float ratio = (f - Math.abs(entitypatch.getOriginal().xRot)) / f;
							float yawOffset = entitypatch.getOriginal().getVehicle() != null ? entitypatch.getOriginal().getYHeadRot() : entitypatch.getOriginal().yBodyRot;
							rawPose.getJointTransformData().get("Chest").frontResult(
									JointTransform.getRotation(QuaternionUtils.YP.rotationDegrees(MathHelper.wrapDegrees(entitypatch.getOriginal().getYHeadRot() - yawOffset) * ratio))
									, OpenMatrix4f::mulAsOriginInverse
							);
						}

						pose.getJointTransformData().putAll(rawPose.getJointTransformData());
					}
				})
				.addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
		BIPED_RUN = new MovementAnimation(true, "biped/living/run", biped);
		BIPED_RUN_DUAL = new MovementAnimation(true, "biped/living/run_dual", biped);
		BIPED_SNEAK = new MovementAnimation(true, "biped/living/sneak", biped);
		BIPED_SWIM = new MovementAnimation(true, "biped/living/swim", biped);
		BIPED_FLOAT = new StaticAnimation(true, "biped/living/float", biped);
		BIPED_KNEEL = new StaticAnimation(true, "biped/living/kneel", biped);
		BIPED_FALL = new StaticAnimation(true, "biped/living/fall", biped);
		BIPED_MOUNT = new StaticAnimation(true, "biped/living/mount", biped);
		BIPED_SIT = new StaticAnimation(true, "biped/living/sit", biped);
		BIPED_DIG_MAINHAND = new StaticAnimation(0.11F, true, "biped/living/dig_mainhand", biped);
		BIPED_DIG_OFFHAND = new StaticAnimation(0.11F, true, "biped/living/dig_offhand", biped);
		BIPED_DIG = new SelectiveAnimation((entitypatch) -> entitypatch.getOriginal().swingingArm == Hand.OFF_HAND ? 1 : 0, "biped/living/dig", BIPED_DIG_MAINHAND, BIPED_DIG_OFFHAND);
		BIPED_BOW_AIM = new AimAnimation(false, "biped/combat/bow_aim_mid", "biped/combat/bow_aim_up", "biped/combat/bow_aim_down", "biped/combat/bow_aim_lying", biped);
		BIPED_BOW_SHOT = new ReboundAnimation(0.04F, false, "biped/combat/bow_shot_mid", "biped/combat/bow_shot_up", "biped/combat/bow_shot_down", "biped/combat/bow_shot_lying", biped);
		BIPED_DRINK = new MirrorAnimation(0.35F, true, "biped/living/drink", "biped/living/drink_mainhand", "biped/living/drink_offhand", biped).addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
		BIPED_EAT = new MirrorAnimation(0.35F, true, "biped/living/eat", "biped/living/eat_mainhand", "biped/living/eat_offhand", biped).addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
		BIPED_JUMP = new StaticAnimation(0.083F, false, "biped/living/jump", biped);
		BIPED_RUN_SPEAR = new MovementAnimation(true, "biped/living/run_spear", biped);
		BIPED_BLOCK = new MirrorAnimation(0.25F, true, "biped/living/shield", "biped/living/shield_mainhand", "biped/living/shield_offhand", biped);

		BIPED_HOLD_GREATSWORD = new StaticAnimation(true, "biped/living/hold_greatsword", biped);
		BIPED_HOLD_UCHIGATANA_SHEATHING = new StaticAnimation(true, "biped/living/hold_uchigatana_sheath", biped);
		BIPED_HOLD_UCHIGATANA = new StaticAnimation(true, "biped/living/hold_uchigatana", biped);
		BIPED_HOLD_TACHI = new StaticAnimation(true, "biped/living/hold_tachi", biped);
		BIPED_HOLD_LONGSWORD = new StaticAnimation(true, "biped/living/hold_longsword", biped);
		BIPED_HOLD_SPEAR = new StaticAnimation(true, "biped/living/hold_spear", biped);
		BIPED_HOLD_DUAL_WEAPON = new StaticAnimation(true, "biped/living/hold_dual", biped);
		BIPED_HOLD_LIECHTENAUER = new StaticAnimation(true, "biped/living/hold_liechtenauer", biped);

		BIPED_WALK_GREATSWORD = new MovementAnimation(true, "biped/living/walk_greatsword", biped);
		BIPED_WALK_SPEAR = new MovementAnimation(true, "biped/living/walk_spear", biped);
		BIPED_WALK_UCHIGATANA_SHEATHING = new MovementAnimation(true, "biped/living/walk_uchigatana_sheath", biped);
		BIPED_WALK_UCHIGATANA = new MovementAnimation(true, "biped/living/walk_uchigatana", biped);
		BIPED_WALK_TWOHAND = new MovementAnimation(true, "biped/living/walk_twohand", biped);
		BIPED_WALK_LONGSWORD = new MovementAnimation(true, "biped/living/walk_longsword", biped);
		BIPED_WALK_LIECHTENAUER = new MovementAnimation(true, "biped/living/walk_liechtenauer", biped);

		BIPED_RUN_GREATSWORD = new MovementAnimation(true, "biped/living/run_greatsword", biped);
		BIPED_RUN_UCHIGATANA = new MovementAnimation(true, "biped/living/run_uchigatana", biped);
		BIPED_RUN_UCHIGATANA_SHEATHING = new MovementAnimation(true, "biped/living/run_uchigatana_sheath", biped);
		BIPED_RUN_DUAL = new MovementAnimation(true, "biped/living/run_dual", biped);

		BIPED_RUN_LONGSWORD = new MovementAnimation(true, "biped/living/run_longsword", biped);

		BIPED_UCHIGATANA_SCRAP = new StaticAnimation(0.05F, false, "biped/living/uchigatana_scrap", biped)
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.15F, ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT).params(EpicFightSounds.SWORD_IN));
		BIPED_LIECHTENAUER_READY = new StaticAnimation(0.1F, false, "biped/living/liechtenauer_ready", biped);

		BIPED_HIT_SHIELD = new MirrorAnimation(0.05F, false, "biped/combat/hit_shield", "biped/combat/hit_shield_mainhand", "biped/combat/hit_shield_offhand", biped);

		BIPED_CLIMBING = new MovementAnimation(0.16F, true, "biped/living/climb", biped)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
		BIPED_SLEEPING = new StaticAnimation(0.16F, true, "biped/living/sleep", biped);

		BIPED_JAVELIN_AIM = new AimAnimation(false, "biped/combat/javelin_aim_mid", "biped/combat/javelin_aim_up", "biped/combat/javelin_aim_down", "biped/combat/javelin_aim_lying", biped);
		BIPED_JAVELIN_THROW = new ReboundAnimation(0.08F, false, "biped/combat/javelin_throw_mid", "biped/combat/javelin_throw_up", "biped/combat/javelin_throw_down", "biped/combat/javelin_throw_lying", biped);

		OFF_ANIMATION_HIGHEST = new OffAnimation("common/off_highest");
		OFF_ANIMATION_MIDDLE = new OffAnimation("common/off_middle");
		OFF_ANIMATION_LOWEST = new OffAnimation("common/off_lowest");

		SPEAR_GUARD = new StaticAnimation(true, "biped/skill/guard_spear", biped);
		SWORD_GUARD = new StaticAnimation(true, "biped/skill/guard_sword", biped);
		SWORD_DUAL_GUARD = new StaticAnimation(true, "biped/skill/guard_dualsword", biped);
		GREATSWORD_GUARD = new StaticAnimation(0.25F, true, "biped/skill/guard_greatsword", biped);
		UCHIGATANA_GUARD = new StaticAnimation(0.25F, true, "biped/skill/guard_uchigatana", biped);
		LONGSWORD_GUARD = new StaticAnimation(0.25F, true, "biped/skill/guard_longsword", biped);

		STEEL_WHIRLWIND_CHARGING = new StaticAnimation(0.15F, false, "biped/skill/steel_whirlwind_charging", biped)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CHARGING);

		/**
		 * Main Frame Animations
		 **/
		BIPED_ROLL_FORWARD = new DodgeAnimation(0.1F, "biped/skill/roll_forward", 0.6F, 0.8F, biped)
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.0F, ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER).params(EpicFightSounds.ROLL));
		BIPED_ROLL_BACKWARD = new DodgeAnimation(0.1F, "biped/skill/roll_backward", 0.6F, 0.8F, biped)
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.0F, ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER).params(EpicFightSounds.ROLL));

		BIPED_STEP_FORWARD = new DodgeAnimation(0.1F, 0.35F, "biped/skill/step_forward", 0.6F, 1.65F, biped)
				.addState(EntityState.LOCKON_ROTATE, true)
				.newTimePair(0.0F, 0.2F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);
		BIPED_STEP_BACKWARD = new DodgeAnimation(0.1F, 0.35F, "biped/skill/step_backward", 0.6F, 1.65F, biped)
				.addState(EntityState.LOCKON_ROTATE, true)
				.newTimePair(0.0F, 0.2F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);
		BIPED_STEP_LEFT = new DodgeAnimation(0.1F, 0.35F, "biped/skill/step_left", 0.6F, 1.65F, biped)
				.addState(EntityState.LOCKON_ROTATE, true)
				.newTimePair(0.0F, 0.2F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);
		BIPED_STEP_RIGHT = new DodgeAnimation(0.1F, 0.35F, "biped/skill/step_right", 0.6F, 1.65F, biped)
				.addState(EntityState.LOCKON_ROTATE, true)
				.newTimePair(0.0F, 0.2F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);

		BIPED_KNOCKDOWN_WAKEUP_LEFT = new DodgeAnimation(0.1F, "biped/skill/knockdown_wakeup_left", 0.8F, 0.6F, biped);
		BIPED_KNOCKDOWN_WAKEUP_RIGHT = new DodgeAnimation(0.1F, "biped/skill/knockdown_wakeup_right", 0.8F, 0.6F, biped);

		BIPED_DEMOLITION_LEAP_CHARGING = new ActionAnimation(0.15F, "biped/skill/demolition_leap_charge", biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CHARGING)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, (self, pose, entitypatch, time, partialTicks) -> {
					if (!self.isStaticAnimation()) {
						return;
					}

					float xRot = MathHelper.clamp(entitypatch.getCameraXRot(), -60.0F, 50.0F);
					float yRot = MathHelper.clamp(MathHelper.wrapDegrees(entitypatch.getCameraYRot() - entitypatch.getOriginal().yRot), -60.0F, 60.0F);

					JointTransform chest = pose.getOrDefaultTransform("Chest");
					chest.frontResult(JointTransform.getRotation(QuaternionUtils.YP.rotationDegrees(yRot)), OpenMatrix4f::mulAsOriginInverse);

					JointTransform head = pose.getOrDefaultTransform("Head");
					MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(xRot), head.rotation(), head.rotation());
				})
				.newTimePair(0.0F, Float.MAX_VALUE)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, true)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, true);
		BIPED_PHANTOM_ASCENT_FORWARD = new ActionAnimation(0.05F, 0.7F, "biped/skill/phantom_ascent_forward", biped)
				.addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
				.newTimePair(0.0F, 0.5F)
				.addStateRemoveOld(EntityState.INACTION, true)
				.addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
					Vector3d pos = entitypatch.getOriginal().position();

					entitypatch.playSound(EpicFightSounds.ROLL, 0, 0);
					entitypatch.getOriginal().level.addAlwaysVisibleParticle(EpicFightParticles.AIR_BURST.get(), pos.x, pos.y + entitypatch.getOriginal().getBbHeight() * 0.5D, pos.z, 0, -1, 2);
				}, AnimationEvent.Side.CLIENT));
		BIPED_PHANTOM_ASCENT_BACKWARD = new ActionAnimation(0.05F, 0.7F, "biped/skill/phantom_ascent_backward", biped)
				.addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
				.newTimePair(0.0F, 0.5F)
				.addStateRemoveOld(EntityState.INACTION, true)
				.addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
					Vector3d pos = entitypatch.getOriginal().position();

					entitypatch.playSound(EpicFightSounds.ROLL, 0, 0);
					entitypatch.getOriginal().level.addAlwaysVisibleParticle(EpicFightParticles.AIR_BURST.get(), pos.x, pos.y + entitypatch.getOriginal().getBbHeight() * 0.5D, pos.z, 0, -1, 2);
				}, AnimationEvent.Side.CLIENT));
		BIPED_DEMOLITION_LEAP = new ActionAnimation(0.05F, 0.45F, "biped/skill/demolition_leap", biped);
		BIPED_PHANTOM_ASCENT_FORWARD = new ActionAnimation(0.05F, 0.7F, "biped/skill/phantom_ascent_forward", biped)
				.addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
				.newTimePair(0.0F, 0.5F)
				.addStateRemoveOld(EntityState.INACTION, true)
				.addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
					Vector3d pos = entitypatch.getOriginal().position();

					entitypatch.playSound(EpicFightSounds.ROLL, 0, 0);
					entitypatch.getOriginal().level.addAlwaysVisibleParticle(EpicFightParticles.AIR_BURST.get(), pos.x, pos.y + entitypatch.getOriginal().getBbHeight() * 0.5D, pos.z, 0, -1, 2);
				}, AnimationEvent.Side.CLIENT));
		BIPED_PHANTOM_ASCENT_BACKWARD = new ActionAnimation(0.05F, 0.7F, "biped/skill/phantom_ascent_backward", biped)
				.addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
				.newTimePair(0.0F, 0.5F)
				.addStateRemoveOld(EntityState.INACTION, true)
				.addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
					Vector3d pos = entitypatch.getOriginal().position();

					entitypatch.playSound(EpicFightSounds.ROLL, 0, 0);
					entitypatch.getOriginal().level.addAlwaysVisibleParticle(EpicFightParticles.AIR_BURST.get(), pos.x, pos.y + entitypatch.getOriginal().getBbHeight() * 0.5D, pos.z, 0, -1, 2);
				}, AnimationEvent.Side.CLIENT));

		FIST_AUTO1 = new BasicAttackAnimation(0.08F, 0.0F, 0.11F, 0.16F, Hand.OFF_HAND, null, biped.toolL, "biped/combat/fist_auto1", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT);
		FIST_AUTO2 = new BasicAttackAnimation(0.08F, 0.0F, 0.11F, 0.16F, null, biped.toolR, "biped/combat/fist_auto2", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT);
		FIST_AUTO3 = new BasicAttackAnimation(0.08F, 0.05F, 0.16F, 0.5F, Hand.OFF_HAND, null, biped.toolL, "biped/combat/fist_auto3", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT);
		FIST_DASH = new DashAttackAnimation(0.06F, 0.05F, 0.15F, 0.3F, 0.7F, null, biped.shoulderR, "biped/combat/fist_dash", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE);
		SWORD_AUTO1 = new BasicAttackAnimation(0.1F, 0.0F, 0.1F, 0.4F, null, biped.toolR, "biped/combat/sword_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);
		SWORD_AUTO2 = new BasicAttackAnimation(0.1F, 0.05F, 0.15F, 0.4F, null, biped.toolR, "biped/combat/sword_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);
		SWORD_AUTO3 = new BasicAttackAnimation(0.1F, 0.05F, 0.15F, 0.6F, null, biped.toolR, "biped/combat/sword_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);
		SWORD_DASH = new DashAttackAnimation(0.1F, 0.1F, 0.1F, 0.2F, 0.65F, null, biped.toolR, "biped/combat/sword_dash", biped, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);
		GREATSWORD_AUTO1 = new BasicAttackAnimation(0.25F, 0.15F, 0.25F, 0.65F, null, biped.toolR, "biped/combat/greatsword_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F);
		GREATSWORD_AUTO2 = new BasicAttackAnimation(0.1F, 0.5F, 0.65F, 1.5F, null, biped.toolR, "biped/combat/greatsword_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F);
		GREATSWORD_DASH = new DashAttackAnimation(0.2F, 0.2F, 0.35F, 0.6F, 1.2F, null, biped.toolR, "biped/combat/greatsword_dash", biped, false)
				.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageSources.TYPE.FINISHER))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.4F, Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.CLIENT).params(new Vec3f(0.0F, -0.24F, -2.0F), Armatures.BIPED.toolR, 1.1D, 0.55F));

		SPEAR_ONEHAND_AUTO = new BasicAttackAnimation(0.1F, 0.35F, 0.45F, 0.75F, null, biped.toolR, "biped/combat/spear_onehand_auto", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		SPEAR_TWOHAND_AUTO1 = new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.45F, null, biped.toolR, "biped/combat/spear_twohand_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		SPEAR_TWOHAND_AUTO2 = new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.7F, null, biped.toolR, "biped/combat/spear_twohand_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		SPEAR_DASH = new DashAttackAnimation(0.1F, 0.25F, 0.3F, 0.4F, 0.8F, null, biped.toolR, "biped/combat/spear_dash", biped, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		TOOL_AUTO1 = new BasicAttackAnimation(0.13F, 0.05F, 0.15F, 0.3F, null, biped.toolR, "biped/combat/tool_auto1", biped)
				.setResourceLocation("biped/combat/sword_auto1");
		TOOL_AUTO2 = new BasicAttackAnimation(0.13F, 0.05F, 0.15F, 0.4F, null, biped.toolR, "biped/combat/sword_auto4", biped);
		TOOL_DASH = new DashAttackAnimation(0.16F, 0.08F, 0.15F, 0.25F, 0.58F, null, biped.toolR, "biped/combat/tool_dash", biped, true)
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1));
		AXE_DASH = new DashAttackAnimation(0.25F, 0.08F, 0.4F, 0.46F, 0.9F, null, biped.toolR, "biped/combat/axe_dash", biped, true);
		SWORD_DUAL_AUTO1 = new BasicAttackAnimation(0.08F, 0.1F, 0.2F, 0.3F, null, biped.toolR, "biped/combat/sword_dual_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.newTimePair(0.0F, 0.2F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false);
		SWORD_DUAL_AUTO2 = new BasicAttackAnimation(0.1F, 0.1F, 0.2F, 0.3F, Hand.OFF_HAND, null, biped.toolL, "biped/combat/sword_dual_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.newTimePair(0.0F, 0.2F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false);
		SWORD_DUAL_AUTO3 = new BasicAttackAnimation(0.1F, "biped/combat/sword_dual_auto3", biped,
				new Phase(0.0F, 0.25F, 0.25F, 0.35F, 0.6F, Float.MAX_VALUE, Hand.MAIN_HAND, AttackAnimation.JointColliderPair.of(biped.toolR, null), AttackAnimation.JointColliderPair.of(biped.toolL, null)))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);
		SWORD_DUAL_DASH = new DashAttackAnimation(0.16F, "biped/combat/sword_dual_dash", biped,
				new Phase(0.0F, 0.05F, 0.05F, 0.3F, 0.75F, Float.MAX_VALUE, Hand.MAIN_HAND, AttackAnimation.JointColliderPair.of(biped.toolR, null), AttackAnimation.JointColliderPair.of(biped.toolL, null)))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null);
		UCHIGATANA_AUTO1 = new BasicAttackAnimation(0.05F, 0.15F, 0.25F, 0.3F, null, biped.toolR, "biped/combat/uchigatana_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		UCHIGATANA_AUTO2 = new BasicAttackAnimation(0.05F, 0.2F, 0.3F, 0.3F, null, biped.toolR, "biped/combat/uchigatana_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		UCHIGATANA_AUTO3 = new BasicAttackAnimation(0.1F, 0.15F, 0.25F, 0.5F, null, biped.toolR, "biped/combat/uchigatana_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		UCHIGATANA_DASH = new DashAttackAnimation(0.1F, 0.05F, 0.05F, 0.15F, 0.6F, null, biped.toolR, "biped/combat/uchigatana_dash", biped, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		UCHIGATANA_SHEATHING_AUTO = new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.65F, ColliderPreset.BATTOJUTSU, biped.rootJoint, "biped/combat/uchigatana_sheath_auto", biped)
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP);
		UCHIGATANA_SHEATHING_DASH = new DashAttackAnimation(0.05F, 0.05F, 0.2F, 0.35F, 0.65F, ColliderPreset.BATTOJUTSU_DASH, biped.rootJoint, "biped/combat/uchigatana_sheath_dash", biped)
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP);

		AXE_AUTO1 = new BasicAttackAnimation(0.16F, 0.05F, 0.15F, 0.7F, null, biped.toolR, "biped/combat/axe_auto1", biped);
		AXE_AUTO2 = new BasicAttackAnimation(0.16F, 0.05F, 0.15F, 0.85F, null, biped.toolR, "biped/combat/axe_auto2", biped);

		LONGSWORD_AUTO1 = new BasicAttackAnimation(0.1F, 0.25F, 0.35F, 0.5F, null, biped.toolR, "biped/combat/longsword_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		LONGSWORD_AUTO2 = new BasicAttackAnimation(0.15F, 0.2F, 0.3F, 0.45F, null, biped.toolR, "biped/combat/longsword_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		LONGSWORD_AUTO3 = new BasicAttackAnimation(0.05F, 0.2F, 0.3F, 1.0F, null, biped.toolR, "biped/combat/longsword_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		LONGSWORD_DASH = new DashAttackAnimation(0.1F, 0.1F, 0.25F, 0.4F, 0.75F, null, biped.toolR, "biped/combat/longsword_dash", biped, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);

		LONGSWORD_LIECHTENAUER_AUTO1 = new BasicAttackAnimation(0.1F, 0.15F, 0.25F, 0.5F, null, biped.toolR, "biped/combat/longsword_liechtenauer_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		LONGSWORD_LIECHTENAUER_AUTO2 = new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.5F, null, biped.toolR, "biped/combat/longsword_liechtenauer_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		LONGSWORD_LIECHTENAUER_AUTO3 = new BasicAttackAnimation(0.25F, 0.1F, 0.2F, 0.7F, null, biped.toolR, "biped/combat/longsword_liechtenauer_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);

		TACHI_AUTO1 = new BasicAttackAnimation(0.1F, 0.35F, 0.4F, 0.5F, null, biped.toolR, "biped/combat/tachi_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 3);
		TACHI_AUTO2 = new BasicAttackAnimation(0.15F, 0.2F, 0.3F, 0.5F, null, biped.toolR, "biped/combat/tachi_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		TACHI_AUTO3 = new BasicAttackAnimation(0.15F, 0.2F, 0.3F, 0.85F, null, biped.toolR, "biped/combat/tachi_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		TACHI_DASH = new DashAttackAnimation(0.1F, 0.3F, 0.3F, 0.4F, 1.0F, null, biped.toolR, "biped/combat/tachi_dash", biped, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);
		DAGGER_AUTO1 = new BasicAttackAnimation(0.05F, 0.05F, 0.15F, 0.25F, null, biped.toolR, "biped/combat/dagger_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_AUTO2 = new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.25F, null, biped.toolR, "biped/combat/dagger_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_AUTO3 = new BasicAttackAnimation(0.05F, 0.2F, 0.25F, 0.4F, null, biped.toolR, "biped/combat/dagger_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_DASH = new DashAttackAnimation(0.05F, 0.1F, 0.2F, 0.25F, 0.6F, null, biped.toolR, "biped/combat/dagger_dash", biped, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
				.newTimePair(0.0F, 0.4F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
				.newConditionalTimePair((entitypatch) -> (entitypatch.isLastAttackSuccess() ? 1 : 0), 0.4F, 0.6F)
				.addConditionalState(0, EntityState.CAN_BASIC_ATTACK, false)
				.addConditionalState(1, EntityState.CAN_BASIC_ATTACK, true);
		DAGGER_DUAL_AUTO1 = new BasicAttackAnimation(0.05F, 0.1F, 0.2F, 0.25F, null, biped.toolR, "biped/combat/dagger_dual_auto1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_DUAL_AUTO2 = new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.16F, Hand.OFF_HAND, null, biped.toolL, "biped/combat/dagger_dual_auto2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_DUAL_AUTO3 = new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.2F, null, biped.toolR, "biped/combat/dagger_dual_auto3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_DUAL_AUTO4 = new BasicAttackAnimation(0.15F, "biped/combat/dagger_dual_auto4", biped,
				new Phase(0.0F, 0.1F, 0.1F, 0.2F, 0.2F, 0.2F, Hand.OFF_HAND, biped.toolL, null)
				, new Phase(0.2F, 0.2F, 0.3F, 0.6F, 0.6F, biped.toolR, null))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);

		DAGGER_DUAL_DASH = new DashAttackAnimation(0.1F, "biped/combat/dagger_dual_dash", biped,
				new Phase(0.0F, 0.1F, 0.2F, 0.3F, 0.65F, Float.MAX_VALUE, Hand.MAIN_HAND, AttackAnimation.JointColliderPair.of(biped.toolR, null), AttackAnimation.JointColliderPair.of(biped.toolL, null)))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true);

		TRIDENT_AUTO1 = new BasicAttackAnimation(0.3F, 0.05F, 0.16F, 0.45F, null, biped.toolR, "biped/combat/trident_auto1", biped);
		TRIDENT_AUTO2 = new BasicAttackAnimation(0.05F, 0.25F, 0.36F, 0.55F, null, biped.toolR, "biped/combat/trident_auto2", biped);
		TRIDENT_AUTO3 = new BasicAttackAnimation(0.2F, 0.3F, 0.46F, 0.9F, null, biped.toolR, "biped/combat/trident_auto3", biped);

		SWORD_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, null, biped.toolR, "biped/combat/sword_airslash", biped);
		SWORD_DUAL_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, ColliderPreset.DUAL_SWORD_AIR_SLASH, biped.torso, "biped/combat/sword_dual_airslash", biped);
		UCHIGATANA_AIR_SLASH = new AirSlashAnimation(0.1F, 0.05F, 0.16F, 0.3F, null, biped.toolR, "biped/combat/uchigatana_airslash", biped);
		UCHIGATANA_SHEATH_AIR_SLASH = new AirSlashAnimation(0.1F, 0.1F, 0.16F, 0.3F, null, biped.toolR, "biped/combat/uchigatana_sheath_airslash", biped)
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(2))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		SPEAR_ONEHAND_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, null, biped.toolR, "biped/combat/spear_onehand_airslash", biped);
		SPEAR_TWOHAND_AIR_SLASH = new AirSlashAnimation(0.1F, 0.25F, 0.36F, 0.6F, null, biped.toolR, "biped/combat/spear_twohand_airslash", biped)
				.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageSources.TYPE.FINISHER));
		LONGSWORD_AIR_SLASH = new AirSlashAnimation(0.1F, 0.3F, 0.41F, 0.5F, null, biped.toolR, "biped/combat/longsword_airslash", biped);
		GREATSWORD_AIR_SLASH = new AirSlashAnimation(0.1F, 0.5F, 0.55F, 0.71F, 0.75F, false, null, biped.toolR, "biped/combat/greatsword_airslash", biped)
				.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageSources.TYPE.FINISHER));
		FIST_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, null, biped.toolR, "biped/combat/fist_airslash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 4.0F);
		DAGGER_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.45F, null, biped.toolR, "biped/combat/dagger_airslash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_DUAL_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, ColliderPreset.DUAL_DAGGER_AIR_SLASH, biped.torso, "biped/combat/dagger_dual_airslash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
				.setResourceLocation("biped/combat/sword_dual_airslash");
		AXE_AIRSLASH = new AirSlashAnimation(0.1F, 0.3F, 0.4F, 0.65F, null, biped.toolR, "biped/combat/axe_airslash", biped);

		SWORD_MOUNT_ATTACK = new MountAttackAnimation(0.16F, 0.1F, 0.2F, 0.25F, 0.7F, null, biped.toolR, "biped/combat/sword_mount_attack", biped);
		SPEAR_MOUNT_ATTACK = new MountAttackAnimation(0.16F, 0.38F, 0.38F, 0.45F, 0.8F, null, biped.toolR, "biped/combat/spear_mount_attack", biped)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);

		SWORD_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_sword_hit", biped);
		SWORD_GUARD_ACTIVE_HIT1 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_sword_hit_active1", biped);
		SWORD_GUARD_ACTIVE_HIT2 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_sword_hit_active2", biped);
		SWORD_GUARD_ACTIVE_HIT3 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_sword_hit_active3", biped);

		LONGSWORD_GUARD_ACTIVE_HIT1 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_longsword_hit_active1", biped);
		LONGSWORD_GUARD_ACTIVE_HIT2 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_longsword_hit_active2", biped);

		SWORD_DUAL_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_dualsword_hit", biped);
		BIPED_COMMON_NEUTRALIZED = new LongHitAnimation(0.05F, "biped/skill/guard_break1", biped);
		GREATSWORD_GUARD_BREAK = new LongHitAnimation(0.05F, "biped/skill/guard_break2", biped);

		LONGSWORD_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_longsword_hit", biped);
		SPEAR_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_spear_hit", biped);
		GREATSWORD_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_greatsword_hit", biped);
		UCHIGATANA_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_uchigatana_hit", biped);

		METEOR_SLAM = new AttackAnimation(0.05F, 0.0F, 0.2F, 0.3F, 1.0F, ColliderPreset.GREATSWORD, biped.toolR, "biped/skill/greatsword_slam", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.NO_SOUND)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
				.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, (self, entitypatch, transformSheet) -> {
					RayTraceResult hitResult = entitypatch.getOriginal().pick(50.0D, 1.0F, false);
					Vector3d to = hitResult.getLocation();


					Vector3d from = entitypatch.getOriginal().position();
					Vector3d correction = to.subtract(from).normalize().scale(2.0D);

					TransformSheet correctedCoord = self.getCoord().getCorrectedModelCoord(entitypatch, from, to.add(correction), 0, 2);
					transformSheet.readFrom(correctedCoord);
				})
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
					if (0.2F > elapsedTime) {
						if (entitypatch instanceof PlayerPatch<?> playerpatch) {
							SkillContainer skill = playerpatch.getSkill(EpicFightSkills.METEOR_STRIKE);

							if (skill != null) {
								return (float)Math.sqrt(7.0F / MeteorSlamSkill.getFallDistance(skill));
							}
						}
					}

					return 1.0F;
				})
				.addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
					entitypatch.playSound(EpicFightSounds.ENTITY_MOVE, 1.0F, 0.0F, 0.0F);
				}, AnimationEvent.Side.CLIENT));
//				.addEvents(AnimationEvent.TimeStampedEvent.create(0.25F, Animations.ReusableSources.FRACTURE_METEOR_STRIKE, AnimationEvent.Side.SERVER)
//						.params(new Vec3f(0.0F, -0.2F, -1.8F), Armatures.BIPED.toolR, 0.3F));

		REVELATION_ONEHAND = new AttackAnimation(0.05F, 0.0F, 0.05F, 0.1F, 0.35F, ColliderPreset.FIST, biped.legR, "biped/skill/revelation_normal", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT)
				.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageSources.TYPE.COUNTER))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
				.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(0.5F))
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(0.0F))
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(2.0F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_LOCROT_TARGET)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_LOCROT_TARGET)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE);

		REVELATION_TWOHAND = new AttackAnimation(0.1F, 0.0F, 0.05F, 0.1F, 0.35F, ColliderPreset.FIST_FIXED, biped.rootJoint, "biped/skill/revelation_twohand", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT)
				.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageSources.TYPE.COUNTER))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
				.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(0.5F))
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(0.0F))
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(2.0F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_LOCROT_TARGET)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_LOCROT_TARGET)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE);

		BIPED_HIT_SHORT = new HitAnimation(0.05F, "biped/combat/hit_short", biped);
		BIPED_HIT_LONG = new LongHitAnimation(0.08F, "biped/combat/hit_long", biped);
		BIPED_HIT_ON_MOUNT = new LongHitAnimation(0.08F, "biped/combat/hit_on_mount", biped);
		BIPED_LANDING = new LongHitAnimation(0.03F, "biped/living/landing", biped);
		BIPED_KNOCKDOWN = new KnockdownAnimation(0.08F, "biped/combat/knockdown", biped);
		BIPED_DEATH = new LongHitAnimation(0.16F, "biped/living/death", biped);

		SWEEPING_EDGE = new AttackAnimation(0.1F, 0.0F, 0.15F, 0.3F, 0.8F, null, biped.toolR, "biped/skill/sweeping_edge", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 1)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);

		DANCING_EDGE = new AttackAnimation(0.1F, "biped/skill/dancing_edge", biped,
				new Phase(0.0F, 0.25F, 0.4F, 0.4F, 0.4F, biped.toolR, null), new Phase(0.4F, 0.4F, 0.5F, 0.55F, 0.6F, Hand.OFF_HAND, biped.toolL, null),
				new Phase(0.6F, 0.6F, 0.7F, 1.15F, Float.MAX_VALUE, biped.toolR, null))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true);

		THE_GUILLOTINE = new AttackAnimation(0.15F, 0.2F, 0.7F, 0.75F, 1.1F, null, biped.toolR, "biped/skill/the_guillotine", biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE);

		HEARTPIERCER = new AttackAnimation(0.11F, "biped/skill/heartpiercer", biped,
				new Phase(0.0F, 0.3F, 0.36F, 0.5F, 0.5F, biped.toolR, null), new Phase(0.5F, 0.5F, 0.56F, 0.75F, 0.75F, biped.toolR, null),
				new Phase(0.75F, 0.75F, 0.81F, 1.05F, Float.MAX_VALUE, biped.toolR, null))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);

		GRASPING_SPIRAL_FIRST = new AttackAnimation(0.1F, 0.25F, 0.3F, 0.4F, 0.8F, null, biped.toolR, "biped/skill/grasping_spire_first", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
				.setResourceLocation("biped/combat/spear_dash")
				.addEvents(StaticAnimationProperty.ON_END_EVENTS,
						AnimationEvent.create((entitypatch, animation, params) -> {
							List<LivingEntity> hitEnemies = entitypatch.getCurrenltyHurtEntities();
							Vector3d vec = entitypatch.getOriginal().position().add(Vector3d.directionFromRotation(new Vector2f(0.0F, entitypatch.getOriginal().yRot)));
							AttackAnimation attackAnimation = (AttackAnimation)animation;

							for (LivingEntity e : hitEnemies) {
								if (e.isAlive()) {
									LivingEntityPatch<?> targetpatch = EpicFightCapabilities.getEntityPatch(e, LivingEntityPatch.class);

									if (targetpatch != null) {
										DamageSource dmgSource = attackAnimation.getEpicFightDamageSource(entitypatch, e, attackAnimation.phases[0]);

										if (!targetpatch.tryHurt(dmgSource, 0).resultType.dealtDamage()) {
											continue;
										}
									}

									Vector3d toAttacker = e.position().subtract(vec).multiply(0.3F, 0.3F, 0.3F);
									Vector3d addVector = vec.add(toAttacker);
									e.setPos(addVector.x, addVector.y, addVector.z);
								}
							}
						}, AnimationEvent.Side.SERVER))
				.addEvents(
						AnimationEvent.TimeStampedEvent.create(0.75F, (entitypatch, animation, params) -> {
							if (entitypatch.isLastAttackSuccess()) {
								entitypatch.playAnimationSynchronized(GRASPING_SPIRAL_SECOND, 0.0F);
							}
						}, AnimationEvent.Side.SERVER)
				);

		GRASPING_SPIRAL_SECOND = new AttackAnimation(0.1F, 0.0F, 0.5F, 0.6F, 0.95F, null, biped.toolR, "biped/skill/grasping_spire_second", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);

		STEEL_WHIRLWIND = new AttackAnimation(0.15F, "biped/skill/steel_whirlwind", biped,
				new Phase(0.0F, 0.0F, 0.0F, 0.2F, 0.45F, 0.45F, biped.toolR, null), new Phase(0.45F, 0.45F, 0.45F, 0.65F, 1.0F, 1.0F, biped.toolR, null),
				new Phase(1.0F, 1.0F, 1.0F, 1.2F, 2.55F, Float.MAX_VALUE, biped.toolR, null))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 4)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, (animation, entitypatch, transformSheet) -> {
					if (entitypatch instanceof PlayerPatch<?> playerpatch) {
						int chargingPower = SteelWhirlwindSkill.getChargingPower(playerpatch.getSkill(SkillSlots.WEAPON_INNATE));
						transformSheet.readFrom(animation.getCoord().copyAll().extendsZCoord(0.6666F + chargingPower / 5.0F, 0, 2));
					} else {
						MoveCoordFunctions.RAW_COORD.set(animation, entitypatch, transformSheet);
					}
				})
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
					if (elapsedTime < 1.05F) {
						if (entitypatch instanceof PlayerPatch<?> playerpatch) {
							int chargingPower = SteelWhirlwindSkill.getChargingPower(playerpatch.getSkill(SkillSlots.WEAPON_INNATE));
							return 0.6666F + chargingPower / 20.0F;
						}
					}

					return 1.0F;
				})
				.newTimePair(0.0F, 2.55F)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);

		BATTOJUTSU = new AttackAnimation(0.15F, 0.0F, 0.75F, 0.8F, 1.2F, ColliderPreset.BATTOJUTSU, biped.rootJoint, "biped/skill/battojutsu", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.05F, ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER).params(EpicFightSounds.SWORD_IN));

		BATTOJUTSU_DASH = new AttackAnimation(0.15F, 0.43F, 0.7F, 0.8F, 1.4F, ColliderPreset.BATTOJUTSU_DASH, biped.rootJoint, "biped/skill/battojutsu_dash", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.addEvents(
						AnimationEvent.TimeStampedEvent.create(0.05F, ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER).params(EpicFightSounds.SWORD_IN),
						AnimationEvent.TimeStampedEvent.create(0.7F, (entitypatch, animation, params) -> {
							Entity entity = entitypatch.getOriginal();
							entity.level.addParticle(EpicFightParticles.ENTITY_AFTER_IMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);
							Random random = entitypatch.getOriginal().getRandom();
							double x = entity.getX() + (random.nextDouble() - random.nextDouble()) * 2.0D;
							double y = entity.getY();
							double z = entity.getZ() + (random.nextDouble() - random.nextDouble()) * 2.0D;
							entity.level.addParticle(ParticleTypes.EXPLOSION, x, y, z, random.nextDouble() * 0.005D, 0.0D, 0.0D);
						}, AnimationEvent.Side.CLIENT)
				);

		RUSHING_TEMPO1 = new AttackAnimation(0.05F, 0.0F, 0.15F, 0.25F, 0.6F, null, biped.toolR, "biped/skill/rushing_tempo1", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 2)
				.addProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER, false)
				.newTimePair(0.0F, 0.25F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false);
		RUSHING_TEMPO2 = new AttackAnimation(0.05F, 0.0F, 0.15F, 0.25F, 0.6F, null, biped.toolR, "biped/skill/rushing_tempo2", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 2)
				.addProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER, false)
				.newTimePair(0.0F, 0.25F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false);
		RUSHING_TEMPO3 = new AttackAnimation(0.05F, 0.0F, 0.2F, 0.25F, 0.6F, null, biped.toolR, "biped/skill/rushing_tempo3", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 2)
				.addProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER, false)
				.newTimePair(0.0F, 0.25F)
				.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false);

		RELENTLESS_COMBO = new AttackAnimation(0.05F, "biped/skill/relentless_combo", biped,
				new Phase(0.0F, 0.016F, 0.066F, 0.133F, 0.133F, Hand.OFF_HAND, biped.rootJoint, ColliderPreset.FIST_FIXED), new Phase(0.133F, 0.133F, 0.183F, 0.25F, 0.25F, biped.rootJoint, ColliderPreset.FIST_FIXED),
				new Phase(0.25F, 0.25F, 0.3F, 0.366F, 0.366F, Hand.OFF_HAND, biped.rootJoint, ColliderPreset.FIST_FIXED), new Phase(0.366F, 0.366F, 0.416F, 0.483F, 0.483F, biped.rootJoint, ColliderPreset.FIST_FIXED),
				new Phase(0.483F, 0.483F, 0.533F, 0.6F, 0.6F, Hand.OFF_HAND, biped.rootJoint, ColliderPreset.FIST_FIXED), new Phase(0.6F, 0.6F, 0.65F, 0.716F, 0.716F, biped.rootJoint, ColliderPreset.FIST_FIXED),
				new Phase(0.716F, 0.716F, 0.766F, 0.833F, 0.833F, Hand.OFF_HAND, biped.rootJoint, ColliderPreset.FIST_FIXED), new Phase(0.833F, 0.833F, 0.883F, 1.1F, 1.1F, biped.rootJoint, ColliderPreset.FIST_FIXED))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 4.0F);

		EVISCERATE_FIRST = new AttackAnimation(0.08F, 0.0F, 0.05F, 0.15F, 0.45F, null, biped.toolR, "biped/skill/eviscerate_first", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_LOCROT_TARGET)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_LOCROT_TARGET);

		EVISCERATE_SECOND = new AttackAnimation(0.15F, 0.0F, 0.04F, 0.05F, 0.4F, null, biped.toolR, "biped/skill/eviscerate_second", biped)
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.EVISCERATE)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);

		BLADE_RUSH_COMBO1 = new AttackAnimation(0.1F, 0.0F, 0.2F, 0.25F, 0.85F, ColliderPreset.BIPED_BODY_COLLIDER, biped.rootJoint, "biped/skill/blade_rush_combo1", biped)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.35F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.25F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_DEST_LOCATION_BEGIN)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_DEST_LOCATION)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.newTimePair(0.0F, 0.65F)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);

		BLADE_RUSH_COMBO2 = new AttackAnimation(0.1F, 0.0F, 0.2F, 0.25F, 0.85F, ColliderPreset.BIPED_BODY_COLLIDER, biped.rootJoint, "biped/skill/blade_rush_combo2", biped)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.35F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.25F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_DEST_LOCATION_BEGIN)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_DEST_LOCATION)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.newTimePair(0.0F, 0.65F)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);

		BLADE_RUSH_COMBO3 = new AttackAnimation(0.1F, 0.0F, 0.25F, 0.35F, 0.85F, ColliderPreset.BIPED_BODY_COLLIDER, biped.rootJoint, "biped/skill/blade_rush_combo3", biped)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.35F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.25F))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_DEST_LOCATION_BEGIN)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_DEST_LOCATION)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.newTimePair(0.0F, 0.6F)
				.addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false);

		BLADE_RUSH_HIT = new LongHitAnimation(0.1F, "biped/interact/blade_rush_hit", biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.IS_DEATH_ANIMATION, true);

		BLADE_RUSH_EXECUTE_BIPED = new GrapplingAttackAnimation(0.5F, 1.5F, "biped/skill/blade_rush_execute", biped)
				.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageSources.TYPE.EXECUTION))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.5F))
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.95F))
				.addEvents(
						AnimationEvent.TimeStampedEvent.create(0.1F, (entitypatch, animation, params) -> {
							LivingEntity grapplingTarget = entitypatch.getGrapplingTarget();

							if (grapplingTarget != null) {
								entitypatch.playSound(EpicFightSounds.BLADE_HIT, 0.0F, 0.0F);
							}
						}, AnimationEvent.Side.CLIENT),
						AnimationEvent.TimeStampedEvent.create(0.3F, (entitypatch, animation, params) -> {
							LivingEntity grapplingTarget = entitypatch.getGrapplingTarget();

							if (grapplingTarget != null) {
								entitypatch.playSound(EpicFightSounds.BLADE_HIT, 0.0F, 0.0F);
							}
						}, AnimationEvent.Side.CLIENT)
				);
		BLADE_RUSH_FAILED = new ActionAnimation(0.0F, 0.85F, "biped/skill/blade_rush_failed", biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.0F));

		BLADE_RUSH_TRY = new GrapplingTryAnimation(0.1F, 0.0F, 0.4F, 0.4F, 0.45F, ColliderPreset.BIPED_BODY_COLLIDER, biped.rootJoint, "biped/skill/blade_rush_try", BLADE_RUSH_HIT, BLADE_RUSH_EXECUTE_BIPED, BLADE_RUSH_FAILED, biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.15F, 0.35F))
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, null);

		WRATHFUL_LIGHTING = new AttackAnimation(0.15F, "biped/skill/wrathful_lighting", biped
				,new Phase(0.0F, 0.0F, 0.3F, 0.36F, 1.0F, Float.MAX_VALUE, biped.toolR, null)
				,new Phase(Hand.MAIN_HAND, biped.rootJoint, null))
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.35F, ReusableSources.SUMMON_THUNDER, AnimationEvent.Side.SERVER));

		TSUNAMI = new AttackAnimation(0.2F, 0.2F, 0.35F, 1.0F, 1.8F, ColliderPreset.BIPED_BODY_COLLIDER, biped.rootJoint, "biped/skill/tsunami", biped)
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(10))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.2F, 1.1F))
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.addEvents(StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH))
				.addEvents(StaticAnimationProperty.EVENTS, AnimationEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH).params(EntitySize.scalable(0.6F, 1.0F)))
				.addEvents(AnimationEvent.TimePeriodEvent.create(0.35F, 1.0F, (entitypatch, animation, params) -> {
					Vector3d pos = entitypatch.getOriginal().position();

					for (int x = -1; x <= 1; x += 2) {
						for (int z = -1; z <= 1; z += 2) {
							Vector3d rand = new Vector3d(Math.random() * x, Math.random(), Math.random() * z).normalize().scale(2.0D);
							entitypatch.getOriginal().level.addParticle(EpicFightParticles.TSUNAMI_SPLASH.get(), pos.x + rand.x, pos.y + rand.y - 1.0D, pos.z + rand.z, rand.x * 0.1D, rand.y * 0.1D, rand.z * 0.1D);
						}
					}
				}, AnimationEvent.Side.CLIENT))
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.35F, (entitypatch, animation, params) -> {
					entitypatch.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 0, 0);
				}, AnimationEvent.Side.CLIENT), AnimationEvent.TimeStampedEvent.create(0.35F, (entitypatch, animation, params) -> {
					entitypatch.setAirborneState(true);
				}, AnimationEvent.Side.SERVER));

		TSUNAMI_REINFORCED = new AttackAnimation(0.2F, 0.2F, 0.35F, 0.65F, 1.3F, ColliderPreset.BIPED_BODY_COLLIDER, biped.rootJoint, "biped/skill/tsunami_reinforced", biped)
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(10))
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD_WITH_X_ROT)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.RAW_COORD_WITH_X_ROT)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.15F, 0.85F))
				.addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
				.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.ROOT_X_MODIFIER)
				.addEvents(StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH))
				.addEvents(StaticAnimationProperty.EVENTS, AnimationEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH).params(EntitySize.scalable(0.6F, 1.0F)))
				.addEvents(AnimationEvent.TimePeriodEvent.create(0.35F, 1.0F, (entitypatch, animation, params) -> {
					Vector3d pos = entitypatch.getOriginal().position();

					for (int x = -1; x <= 1; x += 2) {
						for (int z = -1; z <= 1; z += 2) {
							Vector3d rand = new Vector3d(Math.random() * x, Math.random(), Math.random() * z).normalize().scale(2.0D);
							entitypatch.getOriginal().level.addParticle(EpicFightParticles.TSUNAMI_SPLASH.get(), pos.x + rand.x, pos.y + rand.y - 1.0D, pos.z + rand.z, rand.x * 0.1D, rand.y * 0.1D, rand.z * 0.1D);
						}
					}
				}, AnimationEvent.Side.CLIENT))
				.addEvents(AnimationEvent.TimeStampedEvent.create(0.35F, (entitypatch, animation, params) -> {
					entitypatch.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 0, 0);
				}, AnimationEvent.Side.CLIENT), AnimationEvent.TimeStampedEvent.create(0.35F, (entitypatch, animation, params) -> {
					entitypatch.setAirborneState(true);
				}, AnimationEvent.Side.SERVER));

		EVERLASTING_ALLEGIANCE_CALL = new ActionAnimation(0.1F, 0.55F, "biped/skill/everlasting_allegiance_call", biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
		EVERLASTING_ALLEGIANCE_CATCH = new ActionAnimation(0.05F, 0.8F, "biped/skill/everlasting_allegiance_catch", biped)
				.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);

		SHARP_STAB = new AttackAnimation(0.15F, 0.05F, 0.1F, 0.15F, 0.7F, ColliderPreset.LONGSWORD, biped.toolR, "biped/skill/sharp_stab", biped);
	}

	private static class ReuseableEvents {
		private static final Consumer<LivingEntityPatch<?>> KATANA_IN = (entitypatch) -> entitypatch.playSound(EpicFightSounds.SWORD_IN, 0, 0);
	}

	public static class ReusableSources {
		public static final AnimationEvent.AnimationEventConsumer RESIZE_BOUNDING_BOX = (entitypatch, animation, params) -> {
			if (params != null) {
				entitypatch.resetSize((EntitySize) params[0]);
			}
		};

		public static final AnimationEvent.AnimationEventConsumer RESTORE_BOUNDING_BOX = (entitypatch, animation, params) -> {
			entitypatch.getOriginal().refreshDimensions();
		};

		public static final AnimationEvent.AnimationEventConsumer FRACTURE_GROUND_SIMPLE = (entitypatch, animation, params) -> {
			Vector3d position = entitypatch.getOriginal().position();
			OpenMatrix4f modelTransform = entitypatch.getArmature().getBindedTransformFor(animation.getPoseByTime(entitypatch, (float)params[3], 1.0F), (Joint)params[1])
					.mulFront(
							OpenMatrix4f.createTranslation((float)position.x, (float)position.y, (float)position.z)
									.mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
											.mulBack(entitypatch.getModelMatrix(1.0F))));

			World level = entitypatch.getOriginal().level;
			Vector3d weaponEdge = OpenMatrix4f.transform(modelTransform, ((Vec3f)params[0]).toDoubleVector());
			Vector3d slamStartPos;
			RayTraceResult hitResult = level.clip(new RayTraceContext(position.add(0.0D, 0.1D, 0.0D), weaponEdge, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entitypatch.getOriginal()));

			if (hitResult.getType() == RayTraceResult.Type.BLOCK) {
				BlockRayTraceResult blockHitResult = (BlockRayTraceResult) hitResult;
				Direction direction = blockHitResult.getDirection();
				BlockPos collidePos = blockHitResult.getBlockPos().offset(direction.getStepX(), direction.getStepY(), direction.getStepZ());

				//TODO IMPLEMENT FRACTURE BLOCK
//				if (!LevelUtil.canTransferShockWave(level, collidePos, level.getBlockState(collidePos))) {
//					collidePos = collidePos.below();
//				}

				slamStartPos = new Vector3d(collidePos.getX(), collidePos.getY(), collidePos.getZ());
			} else {
				slamStartPos = weaponEdge.subtract(0.0D, 1.0D, 0.0D);
			}
			//TODO Needs FractureBlock implemented
//			LevelUtil.circleSlamFracture(entitypatch.getOriginal(), level, slamStartPos, (double)params[2], false, false);
		};

//		public static final AnimationEvent.AnimationEventConsumer FRACTURE_METEOR_STRIKE = (entitypatch, animation, params) -> {
//			if (entitypatch instanceof PlayerPatch<?> playerpatch) {
//				SkillContainer skill = playerpatch.getSkill(EpicFightSkills.METEOR_STRIKE);
//
//				if (skill != null) {
//					double slamPower = (float)(Math.log(MeteorSlamSkill.getFallDistance(skill) * entitypatch.getOriginal().getAttributeValue(EpicFightAttributes.IMPACT.get())));
//					FRACTURE_GROUND_SIMPLE.fire(entitypatch, animation, params[0], params[1], slamPower, params[2]);
//				}
//			}
//		};

		public static final AnimationEvent.AnimationEventConsumer SUMMON_THUNDER = (entitypatch, animation, params) -> {
			if (entitypatch.isLogicalClient()) {
				return;
			}

			if (animation instanceof AttackAnimation attackAnimation) {
				Phase phase = attackAnimation.phases[1];

				int i = (int)phase.getProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER).orElse(ValueModifier.setter(3)).getTotalValue(0);
				float damage = phase.getProperty(AttackPhaseProperty.DAMAGE_MODIFIER).orElse(ValueModifier.setter(8.0F)).getTotalValue(0);

				LivingEntity original = entitypatch.getOriginal();
				ServerWorld level = (ServerWorld)original.level;
				float total = damage + ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create().get(original, original.getItemInHand(Hand.MAIN_HAND), null, damage);

				List<Entity> list = level.getEntities(original, original.getBoundingBox().inflate(10.0D, 4.0D, 10.0D), (e) -> {
					return !(e.distanceToSqr(original) > 100.0D) && !e.isAlliedTo(original) && entitypatch.getOriginal().canSee(e);
				});

				list = HitEntityList.Priority.HOSTILITY.sort(entitypatch, list);
				int count = 0;

				while (count < i && count < list.size()) {
					Entity e = list.get(count++);
					BlockPos blockpos = e.blockPosition();
					LightningBoltEntity lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
					lightningbolt.setVisualOnly(true);
					lightningbolt.moveTo(Vector3d.atBottomCenterOf(blockpos));
					lightningbolt.setDamage(0.0F);
					lightningbolt.setCause(entitypatch instanceof ServerPlayerPatch serverPlayerPatch ? serverPlayerPatch.getOriginal() : null);

					DamageSource dmgSource = DamageSource.LIGHTNING_BOLT;
					EpicFightDamageSource damageSource = attackAnimation.getEpicFightDamageSource(dmgSource, entitypatch, e, phase).setHurtItem(entitypatch.getOriginal().getItemInHand(Hand.MAIN_HAND));
					e.hurt(damageSource, total);
					e.thunderHit(level, lightningbolt);

					level.addFreshEntity(lightningbolt);
				}

				if (count > 0) {
					if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && level.random.nextFloat() < 0.08F && level.getThunderLevel(1.0F) < 1.0F) {
//						level.setWeatherParameters(0, MathUtils.randomBetweenInclusive(level.random, 12000, 180000), true, true);
					}

					original.playSound(SoundEvents.TRIDENT_THUNDER, 5.0F, 1.0F);
				}
			}
		};

		public static final AnimationEvent.AnimationEventConsumer PLAY_SOUND = (entitypatch, animation, params) -> entitypatch.playSound((SoundEvent)params[0], 0, 0);

		public static final AnimationProperty.PoseModifier COMBO_ATTACK_DIRECTION_MODIFIER = (self, pose, entitypatch, time, partialTicks) -> {
			if (!self.isStaticAnimation() || entitypatch instanceof PlayerPatch<?> playerpatch && playerpatch.isFirstPerson()) {
				return;
			}

			float pitch = entitypatch.getAttackDirectionPitch();
			JointTransform chest = pose.getOrDefaultTransform("Chest");
			chest.frontResult(JointTransform.getRotation(QuaternionUtils.XP.rotationDegrees(-pitch)), OpenMatrix4f::mulAsOriginInverse);

			if (entitypatch instanceof PlayerPatch) {
				float xRot = MathUtils.lerpBetween(entitypatch.getOriginal().xRotO, entitypatch.getOriginal().xRot, partialTicks);
				OpenMatrix4f toOriginalRotation = entitypatch.getArmature().getBindedTransformFor(pose, entitypatch.getArmature().searchJointByName("Head")).removeScale().removeTranslation().invert();
				Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
				OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(-(pitch + xRot), xAxis);

				pose.getOrDefaultTransform("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
			}
		};

		public static final AnimationProperty.PoseModifier ROOT_X_MODIFIER = (self, pose, entitypatch, time, partialTicks) -> {
			float pitch = -entitypatch.getOriginal().xRot;
			JointTransform chest = pose.getOrDefaultTransform("Root");
			chest.frontResult(JointTransform.getRotation(QuaternionUtils.XP.rotationDegrees(-pitch)), OpenMatrix4f::mulAsOriginInverse);
		};

		public static final AnimationProperty.PoseModifier FLYING_CORRECTION = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
			Vector3d vec3d = entitypatch.getOriginal().getViewVector(partialTicks);
			Vector3d vec3d1 = entitypatch.getOriginal().getDeltaMovement();
			double d0 = VectorUtils.horizontalDistanceSqr(vec3d1);
			double d1 = VectorUtils.horizontalDistanceSqr(vec3d);

			if (d0 > 0.0D && d1 > 0.0D) {
				JointTransform root = pose.getOrDefaultTransform("Root");
				JointTransform head = pose.getOrDefaultTransform("Head");
				double d2 = (vec3d1.x * vec3d.x + vec3d1.z * vec3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
				double d3 = vec3d1.x * vec3d.z - vec3d1.z * vec3d.x;
				float zRot = MathHelper.clamp((float)(Math.signum(d3) * Math.acos(d2)), -1.0F, 1.0F);

				root.frontResult(JointTransform.getRotation(QuaternionUtils.ZP.rotation(zRot)), OpenMatrix4f::mulAsOriginInverse);

				float xRot = (float) MathUtils.getXRotOfVector(vec3d1) * 2.0F;

				MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(xRot), root.rotation(), root.rotation());
				MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(-xRot), head.rotation(), head.rotation());
			}
		};

		public static final AnimationProperty.PoseModifier FLYING_CORRECTION2 = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
			Vector3d vec3d = entitypatch.getOriginal().getViewVector(partialTicks);
			Vector3d vec3d1 = entitypatch.getOriginal().getDeltaMovement();
			double d0 = VectorUtils.horizontalDistanceSqr(vec3d1);
			double d1 = VectorUtils.horizontalDistanceSqr(vec3d);

			if (d0 > 0.0D && d1 > 0.0D) {
				JointTransform root = pose.getOrDefaultTransform("Root");
				JointTransform head = pose.getOrDefaultTransform("Head");
				float xRot = (float) MathUtils.getXRotOfVector(vec3d1) * 2.0F;
				MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(-xRot), root.rotation(), root.rotation());
				MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(xRot), head.rotation(), head.rotation());
			}
		};

		public static final AnimationProperty.PoseModifier MAP_ARMS_CORRECTION = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
			float xRot = 50.0F - (entitypatch.getOriginal().xRotO + (entitypatch.getOriginal().xRot - entitypatch.getOriginal().xRotO) * partialTicks);
			xRot = MathHelper.clamp(xRot, 0.0F, 50.0F);

			JointTransform shoulderL = pose.getOrDefaultTransform("Shoulder_L");
			JointTransform shoulderR = pose.getOrDefaultTransform("Shoulder_R");

			float trans = xRot / 500.0F;

			shoulderL.jointLocal(JointTransform.getTranslation(new Vec3f(0.0F, trans, -trans)), OpenMatrix4f::mul);
			shoulderR.jointLocal(JointTransform.getTranslation(new Vec3f(0.0F, trans, -trans)), OpenMatrix4f::mul);
			shoulderL.frontResult(JointTransform.getRotation(QuaternionUtils.XP.rotationDegrees(xRot)), OpenMatrix4f::mulAsOriginInverse);
			shoulderR.frontResult(JointTransform.getRotation(QuaternionUtils.XP.rotationDegrees(xRot)), OpenMatrix4f::mulAsOriginInverse);
		};

		public static final AnimationProperty.PlaybackSpeedModifier CONSTANT_ONE = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.0F;

		public static final AnimationProperty.PlaybackSpeedModifier CHARGING = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
			if (self.isLinkAnimation()) {
				return 1.0F;
			} else {
				return (float)-Math.pow((self.getTotalTime() - elapsedTime) / self.getTotalTime() - 1.0F, 2) + 1.0F;
			}
		};
	}
}