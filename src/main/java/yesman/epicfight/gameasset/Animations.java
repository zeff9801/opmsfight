package yesman.epicfight.gameasset;

import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.api.animation.types.StaticAnimation.Event;
import yesman.epicfight.api.animation.types.StaticAnimation.Event.Side;
import yesman.epicfight.api.client.animation.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.model.ClientModels;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.HitEntityList.Priority;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class Animations {
	public static StaticAnimation DUMMY_ANIMATION = new StaticAnimation();
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
	public static StaticAnimation LETHAL_SLICING_TWICE;
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
	public static StaticAnimation BIPED_HOLD_KATANA_SHEATHING;
	public static StaticAnimation BIPED_HOLD_KATANA;
	public static StaticAnimation BIPED_WALK_UNSHEATHING;
	public static StaticAnimation BIPED_RUN_UNSHEATHING;
	public static StaticAnimation BIPED_KATANA_SCRAP;
	public static StaticAnimation COMMON_GUARD_BREAK;
	public static StaticAnimation KATANA_AUTO1;
	public static StaticAnimation KATANA_AUTO2;
	public static StaticAnimation KATANA_AUTO3;
	public static StaticAnimation KATANA_AIR_SLASH;
	public static StaticAnimation KATANA_SHEATHING_AUTO;
	public static StaticAnimation KATANA_SHEATHING_DASH;
	public static StaticAnimation KATANA_SHEATH_AIR_SLASH;
	public static StaticAnimation KATANA_GUARD;
	public static StaticAnimation KATANA_GUARD_HIT;
	public static StaticAnimation GUILLOTINE_AXE;
	public static StaticAnimation SPEAR_THRUST;
	public static StaticAnimation SPEAR_SLASH;
	public static StaticAnimation GIANT_WHIRLWIND;
	public static StaticAnimation FATAL_DRAW;
	public static StaticAnimation FATAL_DRAW_DASH;
	public static StaticAnimation LETHAL_SLICING;
	public static StaticAnimation LETHAL_SLICING_ONCE;
	public static StaticAnimation BLADE_RUSH_FIRST;
	public static StaticAnimation BLADE_RUSH_SECOND;
	public static StaticAnimation BLADE_RUSH_THIRD;
	public static StaticAnimation BLADE_RUSH_FINISHER;

	public static void registerAnimations(AnimationRegistryEvent event) {
		event.getRegistryMap().put(EpicFightMod.MODID, Animations::build);
	}

	private static void build() {
		Models<?> models = FMLEnvironment.dist == Dist.CLIENT ? ClientModels.LOGICAL_CLIENT : Models.LOGICAL_SERVER;
		Model biped = models.biped;

		BIPED_IDLE = new StaticAnimation(true, "biped/living/idle", biped);
		BIPED_WALK = new MovementAnimation(true, "biped/living/walk", biped);
		BIPED_FLYING = new StaticAnimation(true, "biped/living/fly", biped);
		BIPED_CREATIVE_IDLE = new StaticAnimation(true, "biped/living/creative_idle", biped);
		BIPED_CREATIVE_FLYING = new MovementAnimation(true, "biped/living/creative_fly", biped);
		BIPED_CREATIVE_FLYING_FORWARD = new MovementAnimation(true, "biped/living/creative_fly", biped);
		BIPED_CREATIVE_FLYING_BACKWARD = new MovementAnimation(true, "biped/living/creative_fly_backward", biped); //TODO need to switch to backwards
		BIPED_HOLD_CROSSBOW = new StaticAnimation(true, "biped/living/hold_crossbow", biped);
		BIPED_RUN = new MovementAnimation(true, "biped/living/run", biped);
		BIPED_SNEAK = new MovementAnimation(true, "biped/living/sneak", biped);
		BIPED_SWIM = new MovementAnimation(true, "biped/living/swim", biped);
		BIPED_FLOAT = new StaticAnimation(true, "biped/living/float", biped);
		BIPED_KNEEL = new StaticAnimation(true, "biped/living/kneel", biped);
		BIPED_FALL = new StaticAnimation(false, "biped/living/fall", biped);
		BIPED_MOUNT = new StaticAnimation(true, "biped/living/mount", biped);
		BIPED_DIG = new StaticAnimation(0.11F, true, "biped/living/dig", biped);
		BIPED_BOW_AIM = new AimAnimation(false, "biped/combat/bow_aim_mid", "biped/combat/bow_aim_up", "biped/combat/bow_aim_down", "biped/combat/bow_aim_lying", biped);
		BIPED_BOW_SHOT = new ReboundAnimation(0.04F, false, "biped/combat/bow_shot_mid", "biped/combat/bow_shot_up", "biped/combat/bow_shot_down", "biped/combat/bow_shot_lying", biped);
		BIPED_CROSSBOW_AIM = new AimAnimation(false, "biped/combat/crossbow_aim_mid", "biped/combat/crossbow_aim_up", "biped/combat/crossbow_aim_down", "biped/combat/crossbow_aim_lying", biped);
		BIPED_CROSSBOW_SHOT = new ReboundAnimation(false, "biped/combat/crossbow_shot_mid", "biped/combat/crossbow_shot_up", "biped/combat/crossbow_shot_down", "biped/combat/crossbow_shot_lying", biped);
		BIPED_CROSSBOW_RELOAD = new StaticAnimation(false, "biped/combat/crossbow_reload", biped);
		BIPED_JUMP = new StaticAnimation(0.083F, false, "biped/living/jump", biped);
		BIPED_RUN_SPEAR = new MovementAnimation(true, "biped/living/run_holding_weapon", biped);
		BIPED_BLOCK = new MirrorAnimation(0.25F, true, "biped/living/shield", "biped/living/shield_mirror", biped);
		BIPED_HOLD_GREATSWORD = new StaticAnimation(true, "biped/living/hold_greatsword", biped);
		BIPED_WALK_GREATSWORD = new MovementAnimation(true, "biped/living/walk_greatsword", biped);
		BIPED_RUN_GREATSWORD = new MovementAnimation(true, "biped/living/run_greatsword", biped);
		BIPED_HOLD_KATANA_SHEATHING = new StaticAnimation(true, "biped/living/hold_katana_sheath", biped);
		BIPED_HOLD_KATANA = new StaticAnimation(true, "biped/living/hold_katana", biped);
		BIPED_WALK_UNSHEATHING = new MovementAnimation(true, "biped/living/walk_unsheath", biped);
		BIPED_WALK_TWOHAND = new MovementAnimation(true, "biped/living/walk_twohand", biped);
		BIPED_RUN_UNSHEATHING = new MovementAnimation(true, "biped/living/run_katana", biped);
		BIPED_HOLD_DUAL_WEAPON = new StaticAnimation(true, "biped/living/hold_dual", biped);
		BIPED_RUN_DUAL = new MovementAnimation(true, "biped/living/run_dual", biped);

		BIPED_KATANA_SCRAP = new StaticAnimation(false, "biped/living/katana_scrap", biped)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[] {Event.create(0.15F, ReuseableEvents.KATANA_IN, Event.Side.CLIENT)});

		BIPED_HOLD_TACHI = new StaticAnimation(true, "biped/living/hold_tachi", biped);

		BIPED_HOLD_LONGSWORD = new StaticAnimation(true, "biped/living/hold_longsword", biped);
		BIPED_HOLD_SPEAR = new StaticAnimation(true, "biped/living/hold_spear", biped);
		BIPED_CLIMBING = new MovementAnimation(0.16F, true, "biped/living/climb", biped)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);
		BIPED_SLEEPING = new StaticAnimation(0.16F, true, "biped/living/sleep", biped);

		BIPED_JAVELIN_AIM = new AimAnimation(false, "biped/combat/javelin_aim_mid", "biped/combat/javelin_aim_up", "biped/combat/javelin_aim_down", "biped/combat/javelin_aim_lying", biped);
		BIPED_JAVELIN_THROW = new ReboundAnimation(0.08F, false, "biped/combat/javelin_throw_mid", "biped/combat/javelin_throw_up", "biped/combat/javelin_throw_down", "biped/combat/javelin_throw_lying", biped);

		OFF_ANIMATION_HIGHEST = new OffAnimation("off_highest");
		OFF_ANIMATION_MIDDLE = new OffAnimation("off_middle");


		SPEAR_GUARD = new StaticAnimation(true, "biped/skill/guard_spear", biped);
		SWORD_GUARD = new StaticAnimation(true, "biped/skill/guard_sword", biped);
		SWORD_DUAL_GUARD = new StaticAnimation(true, "biped/skill/guard_dualsword", biped);
		GREATSWORD_GUARD = new StaticAnimation(0.25F, true, "biped/skill/guard_greatsword", biped);
		KATANA_GUARD = new StaticAnimation(0.25F, true, "biped/skill/guard_katana", biped);
		LONGSWORD_GUARD = new StaticAnimation(0.25F, true, "biped/skill/guard_longsword", biped);

		BIPED_ROLL_FORWARD = new DodgeAnimation(0.1F, "biped/skill/roll_forward", 0.6F, 0.8F, biped);
		BIPED_ROLL_BACKWARD = new DodgeAnimation(0.1F, "biped/skill/roll_backward", 0.6F, 0.8F, biped);
		BIPED_STEP_FORWARD = new DodgeAnimation(0.05F, "biped/skill/step_forward", 0.6F, 1.65F, biped);
		BIPED_STEP_BACKWARD = new DodgeAnimation(0.05F, "biped/skill/step_backward", 0.6F, 1.65F, biped);
		BIPED_STEP_LEFT = new DodgeAnimation(0.05F, "biped/skill/step_left", 0.6F, 1.65F, biped);
		BIPED_STEP_RIGHT = new DodgeAnimation(0.05F, "biped/skill/step_right", 0.6F, 1.65F, biped);

		BIPED_KNOCKDOWN_WAKEUP_LEFT = new DodgeAnimation(0.1F, "biped/skill/knockdown_wakeup_left", 0.8F, 0.6F, biped);
		BIPED_KNOCKDOWN_WAKEUP_RIGHT = new DodgeAnimation(0.1F, "biped/skill/knockdown_wakeup_right", 0.8F, 0.6F, biped);

		FIST_AUTO1 = new BasicAttackAnimation(0.08F, 0.0F, 0.11F, 0.16F, Hand.OFF_HAND, null, "Tool_L", "biped/combat/fist_auto1", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT);
		FIST_AUTO2 = new BasicAttackAnimation(0.08F, 0.0F, 0.11F, 0.16F, null, "Tool_R", "biped/combat/fist_auto2", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT);
		FIST_AUTO3 = new BasicAttackAnimation(0.08F, 0.05F, 0.16F, 0.5F, Hand.OFF_HAND, null, "Tool_L", "biped/combat/fist_auto3", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT);
		FIST_DASH = new DashAttackAnimation(0.06F, 0.05F, 0.15F, 0.3F, 0.7F, null, "Shoulder_R", "biped/combat/fist_dash", biped)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);;
		SWORD_AUTO1 = new BasicAttackAnimation(0.13F, 0.0F, 0.11F, 0.3F, null, "Tool_R", "biped/combat/sword_auto1", biped);
		SWORD_AUTO2 = new BasicAttackAnimation(0.13F, 0.0F, 0.11F, 0.3F, null, "Tool_R", "biped/combat/sword_auto2", biped);
		SWORD_AUTO3 = new BasicAttackAnimation(0.13F, 0.0F, 0.11F, 0.6F, null, "Tool_R", "biped/combat/sword_auto3", biped);
		SWORD_DASH = new DashAttackAnimation(0.12F, 0.1F, 0.25F, 0.4F, 0.65F, null, "Tool_R", "biped/combat/sword_dash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);
		GREATSWORD_AUTO1 = new BasicAttackAnimation(0.2F, 0.4F, 0.6F, 0.8F, null, "Tool_R", "biped/combat/greatsword_auto1", biped);
		GREATSWORD_AUTO2 = new BasicAttackAnimation(0.2F, 0.4F, 0.6F, 0.8F, null, "Tool_R", "biped/combat/greatsword_auto2", biped);
		GREATSWORD_DASH = new DashAttackAnimation(0.11F, 0.4F, 0.65F, 0.8F, 1.2F, null, "Tool_R", "biped/combat/greatsword_dash", false, biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackPhaseProperty.FINISHER, true);
		SPEAR_ONEHAND_AUTO = new BasicAttackAnimation(0.16F, 0.1F, 0.2F, 0.45F, null, "Tool_R", "biped/combat/spear_onehand_auto", biped);
		SPEAR_TWOHAND_AUTO1 = new BasicAttackAnimation(0.25F, 0.05F, 0.15F, 0.45F, null, "Tool_R", "biped/combat/spear_twohand_auto1", biped);
		SPEAR_TWOHAND_AUTO2 = new BasicAttackAnimation(0.25F, 0.05F, 0.15F, 0.45F, null, "Tool_R", "biped/combat/spear_twohand_auto2", biped);
		SPEAR_DASH = new DashAttackAnimation(0.16F, 0.05F, 0.2F, 0.3F, 0.7F, null, "Tool_R", "biped/combat/spear_dash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		TOOL_AUTO1 = new BasicAttackAnimation(0.13F, 0.05F, 0.15F, 0.3F, null, "Tool_R", String.valueOf(SWORD_AUTO1.getId()), biped);
		TOOL_AUTO2 = new BasicAttackAnimation(0.13F, 0.05F, 0.15F, 0.4F, null, "Tool_R", "biped/combat/sword_auto4", biped);
		TOOL_DASH = new DashAttackAnimation(0.16F, 0.08F, 0.15F, 0.25F, 0.58F, null, "Tool_R", "biped/combat/tool_dash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1));
		AXE_DASH = new DashAttackAnimation(0.25F, 0.08F, 0.4F, 0.46F, 0.9F, null, "Tool_R", "biped/combat/axe_dash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		SWORD_DUAL_AUTO1 = new BasicAttackAnimation(0.16F, 0.0F, 0.11F, 0.2F, null, "Tool_R", "biped/combat/sword_dual_auto1", biped);
		SWORD_DUAL_AUTO2 = new BasicAttackAnimation(0.13F, 0.0F, 0.11F, 0.15F, Hand.OFF_HAND, null, "Tool_L", "biped/combat/sword_dual_auto2", biped);
		SWORD_DUAL_AUTO3 = new BasicAttackAnimation(0.18F, 0.0F, 0.25F, 0.35F, 0.6F, ColliderPreset.DUAL_SWORD, "Torso", "biped/combat/sword_dual_auto3", biped);
		SWORD_DUAL_DASH = new DashAttackAnimation(0.16F, 0.05F, 0.05F, 0.3F, 0.75F, ColliderPreset.DUAL_SWORD_DASH, "Root", "biped/combat/sword_dual_dash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		KATANA_AUTO1 = new BasicAttackAnimation(0.06F, 0.05F, 0.16F, 0.2F, null, "Tool_R", "biped/combat/katana_auto1", biped);
		KATANA_AUTO2 = new BasicAttackAnimation(0.16F, 0.0F, 0.11F, 0.2F, null, "Tool_R", "biped/combat/katana_auto2", biped);
		KATANA_AUTO3 = new BasicAttackAnimation(0.06F, 0.1F, 0.21F, 0.59F, null, "Tool_R", "biped/combat/katana_auto3", biped);
		KATANA_SHEATHING_AUTO = new BasicAttackAnimation(0.06F, 0.0F, 0.06F, 0.65F, ColliderPreset.FATAL_DRAW, "Root", "biped/combat/katana_sheath_auto", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION, ValueModifier.adder(30.0F))
				.addProperty(AttackPhaseProperty.DAMAGE, ValueModifier.multiplier(2.0F))
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(2))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP);
		KATANA_SHEATHING_DASH = new DashAttackAnimation(0.06F, 0.05F, 0.05F, 0.11F, 0.65F, null, "Tool_R", "biped/combat/katana_sheath_dash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION, ValueModifier.adder(30.0F))
				.addProperty(AttackPhaseProperty.DAMAGE, ValueModifier.multiplier(2.0F))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP);
		AXE_AUTO1 = new BasicAttackAnimation(0.16F, 0.05F, 0.16F, 0.7F, null, "Tool_R", "biped/combat/axe_auto1", biped);
		AXE_AUTO2 = new BasicAttackAnimation(0.16F, 0.05F, 0.16F, 0.85F, null, "Tool_R", "biped/combat/axe_auto2", biped);
		LONGSWORD_AUTO1 = new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.45F, null, "Tool_R", "biped/combat/longsword_auto1", biped);
		LONGSWORD_AUTO2 = new BasicAttackAnimation(0.15F, 0.1F, 0.21F, 0.45F, null, "Tool_R", "biped/combat/longsword_auto2", biped);
		LONGSWORD_AUTO3 = new BasicAttackAnimation(0.15F, 0.05F, 0.16F, 0.8F, null, "Tool_R", "biped/combat/longsword_auto3", biped);
		LONGSWORD_DASH = new DashAttackAnimation(0.15F, 0.1F, 0.3F, 0.5F, 0.7F, null, "Tool_R", "biped/combat/longsword_dash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		TACHI_DASH = new DashAttackAnimation(0.15F, 0.1F, 0.2F, 0.45F, 0.7F, null, "Tool_R", "biped/combat/tachi_dash", false, biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		DAGGER_AUTO1 = new BasicAttackAnimation(0.08F, 0.05F, 0.15F, 0.2F, null, "Tool_R", "biped/combat/dagger_auto1", biped);
		DAGGER_AUTO2 = new BasicAttackAnimation(0.08F, 0.0F, 0.1F, 0.2F, null, "Tool_R", "biped/combat/dagger_auto2", biped);
		DAGGER_AUTO3 = new BasicAttackAnimation(0.08F, 0.15F, 0.26F, 0.5F, null, "Tool_R", "biped/combat/dagger_auto3", biped);
		DAGGER_DUAL_AUTO1 = new BasicAttackAnimation(0.08F, 0.05F, 0.16F, 0.25F, null, "Tool_R", "biped/combat/dagger_dual_auto1", biped);
		DAGGER_DUAL_AUTO2 = new BasicAttackAnimation(0.08F, 0.0F, 0.11F, 0.16F, Hand.OFF_HAND, null, "Tool_L", "biped/combat/dagger_dual_auto2", biped);
		DAGGER_DUAL_AUTO3 = new BasicAttackAnimation(0.08F, 0.0F, 0.11F, 0.2F, null, "Tool_R", "biped/combat/dagger_dual_auto3", biped);
		DAGGER_DUAL_AUTO4 = new BasicAttackAnimation(0.13F, 0.1F, 0.21F, 0.4F, ColliderPreset.DUAL_DAGGER_DASH, "Root", "biped/combat/dagger_dual_auto4", biped);
		DAGGER_DUAL_DASH = new DashAttackAnimation(0.1F, 0.1F, 0.25F, 0.3F, 0.65F, ColliderPreset.DUAL_DAGGER_DASH, "Root", "biped/combat/dagger_dual_dash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		DAGGER_DASH = new DashAttackAnimation(0.05F, 0.1F, 0.2F, 0.25F, 0.6F, null,"Tool_R", "biped/combat/dagger_dash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true);
		//.newTimePair(0.0F, 0.4F)
				//.addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
				//.newConditionalTimePair((entitypatch) -> (entitypatch.isLastAttackSuccess() ? 1 : 0), 0.4F, 0.6F)
				//.addConditionalState(0, EntityState.CAN_BASIC_ATTACK, false)
				//.addConditionalState(1, EntityState.CAN_BASIC_ATTACK, true);

		SWORD_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, null, "Tool_R", "biped/combat/sword_airslash", biped);
		SWORD_DUAL_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, ColliderPreset.DUAL_SWORD_AIR_SLASH, "Torso", "biped/combat/sword_dual_airslash", biped);
		KATANA_AIR_SLASH = new AirSlashAnimation(0.1F, 0.05F, 0.16F, 0.3F, null, "Tool_R", "biped/combat/katana_airslash", biped);
		KATANA_SHEATH_AIR_SLASH = new AirSlashAnimation(0.1F, 0.1F, 0.16F, 0.3F, null, "Tool_R", "biped/combat/katana_sheath_airslash", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackPhaseProperty.ARMOR_NEGATION, ValueModifier.adder(30.0F))
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(2))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		SPEAR_ONEHAND_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, null, "Tool_R", "biped/combat/spear_onehand_airslash", biped);
		SPEAR_TWOHAND_AIR_SLASH = new AirSlashAnimation(0.1F, 0.25F, 0.36F, 0.6F, null, "Tool_R", "biped/combat/spear_twohand_airslash", biped)
				.addProperty(AttackPhaseProperty.FINISHER, true);
		LONGSWORD_AIR_SLASH = new AirSlashAnimation(0.1F, 0.3F, 0.41F, 0.5F, null, "Tool_R", "biped/combat/longsword_airslash", biped);
		GREATSWORD_AIR_SLASH = new AirSlashAnimation(0.1F, 0.5F, 0.55F, 0.71F, 0.75F, false, null, "Tool_R", "biped/combat/greatsword_airslash", biped)
				.addProperty(AttackPhaseProperty.FINISHER, true);
		FIST_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, null, "Tool_R", "biped/combat/fist_airslash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 4.0F);
		DAGGER_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.45F, null, "Tool_R", "biped/combat/dagger_airslash", biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);
		DAGGER_DUAL_AIR_SLASH = new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, ColliderPreset.DUAL_DAGGER_AIR_SLASH, "Torso", String.valueOf(SWORD_DUAL_AIR_SLASH.getId()), biped)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F);
		AXE_AIRSLASH = new AirSlashAnimation(0.1F, 0.3F, 0.4F, 0.65F, null, "Tool_R", "biped/combat/axe_airslash", biped);

		SWORD_MOUNT_ATTACK = new MountAttackAnimation(0.16F, 0.1F, 0.2F, 0.25F, 0.7F, null, "Tool_R", "biped/combat/sword_mount_attack", biped);
		SPEAR_MOUNT_ATTACK = new MountAttackAnimation(0.16F, 0.38F, 0.38F, 0.45F, 0.8F, null, "Tool_R", "biped/combat/spear_mount_attack", biped)
				.addProperty(AttackAnimationProperty.ROTATE_X, true);


		SWORD_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_sword_hit", biped);
		SWORD_GUARD_ACTIVE_HIT1 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_sword_hit_active1", biped);
		SWORD_GUARD_ACTIVE_HIT2 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_sword_hit_active2", biped);
		SWORD_GUARD_ACTIVE_HIT3 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_sword_hit_active3", biped);

		LONGSWORD_GUARD_ACTIVE_HIT1 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_longsword_hit_active1", biped);
		LONGSWORD_GUARD_ACTIVE_HIT2 = new GuardAnimation(0.05F, 0.2F, "biped/skill/guard_longsword_hit_active2", biped);

		SWORD_DUAL_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_dualsword_hit", biped);
		COMMON_GUARD_BREAK = new LongHitAnimation(0.05F, "biped/skill/guard_break1", biped);
		GREATSWORD_GUARD_BREAK = new LongHitAnimation(0.05F, "biped/skill/guard_break2", biped);

		LONGSWORD_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_longsword_hit", biped);
		SPEAR_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_spear_hit", biped);
		GREATSWORD_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_greatsword_hit", biped);
		KATANA_GUARD_HIT = new GuardAnimation(0.05F, "biped/skill/guard_katana_hit", biped);

		BIPED_HIT_SHORT = new HitAnimation(0.05F, "biped/combat/hit_short", biped);
		BIPED_HIT_LONG = new LongHitAnimation(0.08F, "biped/combat/hit_long", biped);
		BIPED_HIT_ON_MOUNT = new LongHitAnimation(0.08F, "biped/combat/hit_on_mount", biped);
		BIPED_LANDING = new LongHitAnimation(0.08F, "biped/living/landing", biped);
		BIPED_KNOCKDOWN = new KnockdownAnimation(0.08F, 2.1F, "biped/combat/knockdown", biped);
		BIPED_DEATH = new LongHitAnimation(0.16F, "biped/living/death", biped);

		SWEEPING_EDGE = new SpecialAttackAnimation(0.16F, 0.1F, 0.35F, 0.46F, 0.79F, null, "Tool_R", "biped/skill/sweeping_edge", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.ROTATE_X, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AttackAnimationProperty.COLLIDER_ADDER, 1);

		DANCING_EDGE = new SpecialAttackAnimation(0.25F, "biped/skill/dancing_edge", biped,
				new Phase(0.0F, 0.2F, 0.31F, 0.4F, 0.4F, "Tool_R", null), new Phase(0.4F, 0.5F, 0.61F, 0.65F, 0.65F, Hand.OFF_HAND, "Tool_L", null),
				new Phase(0.65F, 0.75F, 0.85F, 1.15F, Float.MAX_VALUE, "Tool_R", null))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
				.addProperty(AnimationProperty.MoveCoordFunctions.MOVE_VERTICAL, true);

		GUILLOTINE_AXE = new SpecialAttackAnimation(0.08F, 0.2F, 0.5F, 0.65F, 1.0F, null, "Tool_R", "biped/skill/guillotine_axe", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AnimationProperty.MoveCoordFunctions.MOVE_VERTICAL, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);

		SPEAR_THRUST = new SpecialAttackAnimation(0.11F, "biped/skill/spear_thrust", biped,
				new Phase(0.0F, 0.3F, 0.36F, 0.5F, 0.5F, "Tool_R", null), new Phase(0.5F, 0.5F, 0.56F, 0.75F, 0.75F, "Tool_R", null),
				new Phase(0.75F, 0.75F, 0.81F, 1.05F, Float.MAX_VALUE, "Tool_R", null))
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.ROTATE_X, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);

		SPEAR_SLASH = new SpecialAttackAnimation(0.1F, "biped/skill/spear_slash", biped,
				new Phase(0.0F, 0.2F, 0.41F, 0.5F, 0.5F, "Tool_R", null), new Phase(0.5F, 0.5F, 0.75F, 0.95F, 1.25F, Float.MAX_VALUE, "Tool_R", null))
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.ROTATE_X, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F);

		GIANT_WHIRLWIND = new SpecialAttackAnimation(0.41F, "biped/skill/giant_whirlwind", biped,
				new Phase(0.0F, 0.3F, 0.35F, 0.55F, 0.9F, 0.9F, "Tool_R", null), new Phase(0.9F, 0.95F, 1.05F, 1.2F, 1.5F, 1.5F, "Tool_R", null),
				new Phase(1.5F, 1.65F, 1.75F, 1.95F, 2.5F, Float.MAX_VALUE, "Tool_R", null))
				.addProperty(AttackAnimationProperty.ROTATE_X, true)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F);

		FATAL_DRAW = new SpecialAttackAnimation(0.15F, 0.0F, 0.7F, 0.81F, 1.0F, ColliderPreset.FATAL_DRAW, "Root", "biped/skill/fatal_draw", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[] {Event.create(0.05F, ReuseableEvents.KATANA_IN, Event.Side.SERVER)});

		FATAL_DRAW_DASH = new SpecialAttackAnimation(0.15F, 0.43F, 0.85F, 0.851F, 1.4F, ColliderPreset.FATAL_DRAW_DASH, "Root", "biped/skill/fatal_draw_dash", biped)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[] {Event.create(0.05F, ReuseableEvents.KATANA_IN, Event.Side.SERVER)})
				.addProperty(StaticAnimationProperty.EVENTS, new Event[] {Event.create(0.85F, (entitypatch) -> {
					Entity entity = entitypatch.getOriginal();
					entitypatch.getOriginal().level.addParticle(EpicFightParticles.ENTITY_AFTER_IMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);
				}, Side.CLIENT)});

		LETHAL_SLICING = new SpecialAttackAnimation(0.15F, 0.0F, 0.0F, 0.11F, 0.38F, ColliderPreset.FIST_FIXED, "Root", "biped/skill/lethal_slicing_start", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);;

		LETHAL_SLICING_ONCE = new SpecialAttackAnimation(0.016F, 0.0F, 0.0F, 0.1F, 0.6F, ColliderPreset.FATAL_DRAW, "Root", "biped/skill/lethal_slicing_once", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);

		LETHAL_SLICING_TWICE = new SpecialAttackAnimation(0.016F, "biped/skill/lethal_slicing_twice", biped,
				new Phase(0.0F, 0.0F, 0.1F, 0.15F, 0.15F, "Root", ColliderPreset.FATAL_DRAW), new Phase(0.15F, 0.15F, 0.25F, 0.6F, Float.MAX_VALUE, "Root", ColliderPreset.FATAL_DRAW))
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F);

		RELENTLESS_COMBO = new SpecialAttackAnimation(0.05F, "biped/skill/relentless_combo", biped,
				new Phase(0.0F, 0.016F, 0.066F, 0.133F, 0.133F, Hand.OFF_HAND, "Root", ColliderPreset.FIST_FIXED), new Phase(0.133F, 0.133F, 0.183F, 0.25F, 0.25F, "Root", ColliderPreset.FIST_FIXED),
				new Phase(0.25F, 0.25F, 0.3F, 0.366F, 0.366F, Hand.OFF_HAND, "Root", ColliderPreset.FIST_FIXED), new Phase(0.366F, 0.366F, 0.416F, 0.483F, 0.483F, "Root", ColliderPreset.FIST_FIXED),
				new Phase(0.483F, 0.483F, 0.533F, 0.6F, 0.6F, Hand.OFF_HAND, "Root", ColliderPreset.FIST_FIXED), new Phase(0.6F, 0.6F, 0.65F, 0.716F, 0.716F, "Root", ColliderPreset.FIST_FIXED),
				new Phase(0.716F, 0.716F, 0.766F, 0.833F, 0.833F, Hand.OFF_HAND, "Root", ColliderPreset.FIST_FIXED), new Phase(0.833F, 0.833F, 0.883F, 1.1F, 1.1F, "Root", ColliderPreset.FIST_FIXED))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 4.0F);

		EVISCERATE_FIRST = new SpecialAttackAnimation(0.08F, 0.05F, 0.05F, 0.15F, 0.45F, null, "Tool_R", "biped/skill/eviscerate_first", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);

		EVISCERATE_SECOND = new SpecialAttackAnimation(0.15F, 0.0F, 0.0F, 0.0F, 0.4F, null, "Tool_R", "biped/skill/eviscerate_second", biped)
				.addProperty(AttackAnimationProperty.LOCK_ROTATION, true)
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.EVISCERATE)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE)
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F);

		BLADE_RUSH_FIRST = new SpecialAttackAnimation(0.1F, 0.0F, 0.0F, 0.06F, 0.3F, ColliderPreset.BLADE_RUSH, "Root", "biped/skill/blade_rush_first", biped)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);;
		BLADE_RUSH_SECOND = new SpecialAttackAnimation(0.1F, 0.0F, 0.0F, 0.06F, 0.3F, ColliderPreset.BLADE_RUSH, "Root", "biped/skill/blade_rush_second", biped)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);;
		BLADE_RUSH_THIRD = new SpecialAttackAnimation(0.1F, 0.0F, 0.0F, 0.06F, 0.3F, ColliderPreset.BLADE_RUSH, "Root", "biped/skill/blade_rush_third", biped)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);;
		BLADE_RUSH_FINISHER = new SpecialAttackAnimation(0.15F, 0.0F, 0.1F, 0.16F, 0.65F, ColliderPreset.BLADE_RUSH, "Root", "biped/skill/blade_rush_finisher", biped)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(AttackPhaseProperty.HIT_PRIORITY, Priority.TARGET)
				.addProperty(StaticAnimationProperty.PLAY_SPEED, 1.0F);;
	}

	private static class ReuseableEvents {
		private static final Consumer<LivingEntityPatch<?>> KATANA_IN = (entitypatch) -> entitypatch.playSound(EpicFightSounds.SWORD_IN, 0, 0);
	}

	@OnlyIn(Dist.CLIENT)
	public static void buildClient() {
		OFF_ANIMATION_HIGHEST.addProperty(ClientAnimationProperties.PRIORITY, Layer.Priority.HIGHEST);
		OFF_ANIMATION_MIDDLE.addProperty(ClientAnimationProperties.PRIORITY, Layer.Priority.MIDDLE);
	}
}