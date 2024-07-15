package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ibm.icu.impl.Pair;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.KatanaPassive;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class WeaponTypeReloadListener extends JsonReloadListener {

	public static final Function<Item, CapabilityItem.Builder> AXE = (item) -> (CapabilityItem.Builder) WeaponCapability.builder()
            .category(WeaponCategories.AXE)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .collider(ColliderPreset.TOOLS)
            .newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .innateSkill(Styles.ONE_HAND, EpicFightSkills.GUILLOTINE_AXE)
            .livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD);
	
	public static final Function<Item, CapabilityItem.Builder> HOE = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.HOE)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .collider(ColliderPreset.TOOLS).newStyleCombo(Styles.ONE_HAND, Animations.TOOL_AUTO1, Animations.TOOL_AUTO2, Animations.TOOL_DASH, Animations.SWORD_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

	public static final Function<Item, CapabilityItem.Builder> PICKAXE = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.PICKAXE)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .collider(ColliderPreset.TOOLS)
            .newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

	public static final Function<Item, CapabilityItem.Builder> SHOVEL = (item) -> WeaponCapability.builder()
			.category(WeaponCategories.SHOVEL)
			.collider(ColliderPreset.TOOLS)
			.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

	public static final Function<Item, CapabilityItem.Builder> SWORD = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.SWORD)
            .styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategories.SWORD ? Styles.TWO_HAND : Styles.ONE_HAND)
            .collider(ColliderPreset.SWORD)
            .newStyleCombo(Styles.ONE_HAND, Animations.SWORD_AUTO1, Animations.SWORD_AUTO2, Animations.SWORD_AUTO3, Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH)
            .newStyleCombo(Styles.TWO_HAND, Animations.SWORD_DUAL_AUTO1, Animations.SWORD_DUAL_AUTO2, Animations.SWORD_DUAL_AUTO3, Animations.SWORD_DUAL_DASH, Animations.SWORD_DUAL_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .innateSkill(Styles.ONE_HAND, EpicFightSkills.SWEEPING_EDGE)
            .innateSkill(Styles.TWO_HAND, EpicFightSkills.DANCING_EDGE)
            .livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SWORD_DUAL_GUARD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_DUAL_WEAPON)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_DUAL_WEAPON)
            .weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).getWeaponCategory() == WeaponCategories.SWORD);

	public static final Function<Item, CapabilityItem.Builder> SPEAR = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.SPEAR)
            .styleProvider((playerpatch) -> (playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategories.SHIELD) ? Styles.ONE_HAND : Styles.TWO_HAND)
            .collider(ColliderPreset.SPEAR)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.ONE_HAND, Animations.SPEAR_ONEHAND_AUTO, Animations.SPEAR_DASH, Animations.SPEAR_ONEHAND_AIR_SLASH)
            .newStyleCombo(Styles.TWO_HAND, Animations.SPEAR_TWOHAND_AUTO1, Animations.SPEAR_TWOHAND_AUTO2, Animations.SPEAR_DASH, Animations.SPEAR_TWOHAND_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SPEAR_MOUNT_ATTACK)
            .innateSkill(Styles.ONE_HAND, EpicFightSkills.HEARTPIERCER)
            .innateSkill(Styles.TWO_HAND, EpicFightSkills.GRASPING_SPIRE)
            .livingMotionModifier(Styles.ONE_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_STAFF_IDLE)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_SPEAR)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_SPEAR)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_STAFF_RUN)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_SPEAR)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SPEAR_GUARD);

	public static final Function<Item, CapabilityItem.Builder> GREATSWORD = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.GREATSWORD)
            .styleProvider((playerpatch) -> Styles.TWO_HAND)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.TWO_HAND, Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2, Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH)
            .innateSkill(Styles.TWO_HAND, EpicFightSkills.STEEL_WHIRLWIND)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.JUMP, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLY, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_GREATSWORD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.GREATSWORD_GUARD);

	public static final Function<Item, CapabilityItem.Builder> KATANA = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.UCHIGATANA)
            .styleProvider((entitypatch) -> {
                if (entitypatch instanceof PlayerPatch<?> playerpatch) {
                    if (playerpatch.getSkill(SkillCategories.WEAPON_PASSIVE).getDataManager().hasData(KatanaPassive.SHEATH) &&
                            playerpatch.getSkill(SkillCategories.WEAPON_PASSIVE).getDataManager().getDataValue(KatanaPassive.SHEATH)) {
                        return Styles.SHEATH;
                    }
                }
                return Styles.TWO_HAND;
            })
            .passiveSkill(EpicFightSkills.KATANA_PASSIVE)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.SHEATH, Animations.UCHIGATANA_SHEATHING_AUTO, Animations.UCHIGATANA_SHEATHING_DASH, Animations.UCHIGATANA_SHEATH_AIR_SLASH)
            .newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .innateSkill(Styles.SHEATH,  EpicFightSkills.FATAL_DRAW)
            .innateSkill(Styles.TWO_HAND, EpicFightSkills.FATAL_DRAW)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_WALK_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.KNEEL, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.CHASE, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.SNEAK, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.SHEATH, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.UCHIGATANA_GUARD);
	public static final Function<Item, CapabilityItem.Builder> TACHI = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.TACHI)
            .styleProvider((playerpatch) -> Styles.TWO_HAND)
            .collider(ColliderPreset.TACHI)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.TWO_HAND, Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3, Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .innateSkill(Styles.TWO_HAND, EpicFightSkills.RUSHING_TEMPO)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_IDLE)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_SNEAK)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_SWIM)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_FLOAT)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_FALL)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);

	public static final Function<Item, CapabilityItem.Builder> LONGSWORD = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.LONGSWORD)
            .styleProvider((entitypatch) -> {
                if (entitypatch instanceof PlayerPatch<?>) {
                    if (((PlayerPatch<?>)entitypatch).getSkill(SkillCategories.WEAPON_INNATE).getRemainDuration() > 0) {
                        return Styles.LIECHTENAUER;
                    }
                }
                return Styles.TWO_HAND;
            })
            .hitSound(EpicFightSounds.BLADE_HIT)
            .collider(ColliderPreset.LONGSWORD)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.ONE_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
            .newStyleCombo(Styles.TWO_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
            .newStyleCombo(Styles.OCHS, Animations.LONGSWORD_LIECHTENAUER_AUTO1, Animations.LONGSWORD_LIECHTENAUER_AUTO2, Animations.LONGSWORD_LIECHTENAUER_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
            .innateSkill(Styles.ONE_HAND,  EpicFightSkills.SHARP_STAB)
            .innateSkill(Styles.TWO_HAND,  EpicFightSkills.LIECHTENAUER)
            .innateSkill(Styles.OCHS, EpicFightSkills.LIECHTENAUER)
            .livingMotionModifier(Styles.COMMON, LivingMotions.IDLE, Animations.BIPED_HOLD_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.CHASE, Animations.BIPED_WALK_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.SNEAK, Animations.BIPED_SNEAK)
            .livingMotionModifier(Styles.COMMON, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
            .livingMotionModifier(Styles.COMMON, LivingMotions.JUMP, Animations.BIPED_JUMP)
            .livingMotionModifier(Styles.COMMON, LivingMotions.SWIM, Animations.BIPED_SWIM)
            .livingMotionModifier(Styles.COMMON, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .livingMotionModifier(Styles.OCHS, LivingMotions.IDLE, Animations.BIPED_HOLD_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.WALK, Animations.BIPED_WALK_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.CHASE, Animations.BIPED_WALK_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
            .livingMotionModifier(Styles.OCHS, LivingMotions.SNEAK, Animations.BIPED_SNEAK)
            .livingMotionModifier(Styles.OCHS, LivingMotions.KNEEL, Animations.BIPED_KNEEL)
            .livingMotionModifier(Styles.OCHS, LivingMotions.JUMP, Animations.BIPED_JUMP)
            .livingMotionModifier(Styles.OCHS, LivingMotions.SWIM, Animations.BIPED_SWIM)
            .livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .livingMotionModifier(Styles.OCHS, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);

	public static final Function<Item, CapabilityItem.Builder> DAGGER = (item) -> WeaponCapability.builder()
					.category(WeaponCategories.DAGGER)
					.styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategories.DAGGER ? Styles.TWO_HAND : Styles.ONE_HAND)
					.hitSound(EpicFightSounds.BLADE_HIT)
					.swingSound(EpicFightSounds.WHOOSH_SMALL)
					.collider(ColliderPreset.DAGGER)
					.weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).weaponCategory == WeaponCategories.DAGGER)
					.newStyleCombo(Styles.ONE_HAND, Animations.DAGGER_AUTO1, Animations.DAGGER_AUTO2, Animations.DAGGER_AUTO3, Animations.DAGGER_DASH, Animations.DAGGER_AIR_SLASH)
					.newStyleCombo(Styles.TWO_HAND, Animations.DAGGER_DUAL_AUTO1, Animations.DAGGER_DUAL_AUTO2, Animations.DAGGER_DUAL_AUTO3, Animations.DAGGER_DUAL_AUTO4, Animations.DAGGER_DUAL_DASH, Animations.DAGGER_DUAL_AIR_SLASH)
					.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
			.innateSkill(Styles.ONE_HAND, EpicFightSkills.EVISCERATE)
			.innateSkill(Styles.TWO_HAND, EpicFightSkills.BLADE_RUSH)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_DUAL_WEAPON);

	public static final Function<Item, CapabilityItem.Builder> FIST = (item) -> WeaponCapability.builder()
			.newStyleCombo(Styles.ONE_HAND, Animations.FIST_AUTO1, Animations.FIST_AUTO2, Animations.FIST_AUTO3, Animations.FIST_DASH, Animations.FIST_AIR_SLASH)
			.innateSkill(Styles.ONE_HAND, EpicFightSkills.RELENTLESS_COMBO)
			.category(WeaponCategories.FIST)
			.constructor(GloveCapability::new);

	public static final Function<Item, CapabilityItem.Builder> BOW =  (item) -> RangedWeaponCapability.builder()
			.addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
			.addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK)
			.addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_BOW_AIM)
			.addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT)
			.constructor(BowCapability::new);

	public static final Function<Item, CapabilityItem.Builder> CROSSBOW =  (item) -> RangedWeaponCapability.builder()
			.addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.KNEEL, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.RUN, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.SNEAK, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.SWIM, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.FLOAT, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.FALL, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.RELOAD, Animations.BIPED_CROSSBOW_RELOAD)
			.addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_CROSSBOW_AIM)
			.addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_CROSSBOW_SHOT)
			.constructor(CrossbowCapability::new);

	public static final Function<Item, CapabilityItem.Builder> TRIDENT = (item) -> RangedWeaponCapability.builder()
			.addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
			.addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK)
			.addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_JAVELIN_AIM)
			.addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_JAVELIN_THROW)
			.constructor(TridentCapability::new)
			.category(WeaponCategories.TRIDENT);

	public static final Function<Item, CapabilityItem.Builder> SHIELD = (item) -> CapabilityItem.builder()
			.constructor(ShieldCapability::new)
			.category(WeaponCategories.SHIELD);


	public static void registerDefaultWeaponTypes() {

		Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry = Maps.newHashMap();
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "axe"), WeaponTypeReloadListener.AXE);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "fist"), WeaponTypeReloadListener.FIST);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "hoe"), WeaponTypeReloadListener.HOE);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "pickaxe"), WeaponTypeReloadListener.PICKAXE);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "shovel"), WeaponTypeReloadListener.SHOVEL);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "sword"), WeaponTypeReloadListener.SWORD);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "spear"), WeaponTypeReloadListener.SPEAR);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "greatsword"), WeaponTypeReloadListener.GREATSWORD);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "katana"), WeaponTypeReloadListener.KATANA); //uchigatana
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "tachi"), WeaponTypeReloadListener.TACHI);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "longsword"), WeaponTypeReloadListener.LONGSWORD);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "dagger"), WeaponTypeReloadListener.DAGGER);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "bow"), WeaponTypeReloadListener.BOW);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "crossbow"), WeaponTypeReloadListener.CROSSBOW);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "trident"), WeaponTypeReloadListener.TRIDENT);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "shield"), WeaponTypeReloadListener.SHIELD);

		WeaponCapabilityPresetRegistryEvent weaponCapabilityPresetRegistryEvent = new WeaponCapabilityPresetRegistryEvent(typeEntry);
		ModLoader.get().postEvent(weaponCapabilityPresetRegistryEvent);
		PRESETS.putAll(weaponCapabilityPresetRegistryEvent.getTypeEntry());
	}

	public static Function<Item, CapabilityItem.Builder> get(String typeName) {
		ResourceLocation rl = new ResourceLocation(typeName);
		return PRESETS.get(rl);
	}

	public static final String DIRECTORY = "capabilities/weapons/types";

	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> PRESETS = Maps.newHashMap();
	private static final Map<ResourceLocation, CompoundNBT> TAGMAP = Maps.newHashMap();

	public WeaponTypeReloadListener() {
		super(GSON, DIRECTORY);
	}
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> packEntry, IResourceManager resourceManager, IProfiler profilerFiller) {
		clear();

		for (Map.Entry<ResourceLocation, JsonElement> entry : packEntry.entrySet()) {
			CompoundNBT nbt = null;

			try {
				nbt = JsonToNBT.parseTag(entry.getValue().toString());
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}

			try {
				WeaponCapability.Builder builder = deserializeWeaponCapabilityBuilder(nbt);

				PRESETS.put(entry.getKey(), (itemstack) -> builder);
				TAGMAP.put(entry.getKey(), nbt);
			} catch (Exception e) {
				EpicFightMod.LOGGER.warn("Error while deserializing weapon type datapack: " + entry.getKey());
				e.printStackTrace();
			}
		}
	}
	public static Function<Item, CapabilityItem.Builder> getOrThrow(String typeName) {
		ResourceLocation rl = new ResourceLocation(typeName);

		if (!PRESETS.containsKey(rl)) {
			throw new IllegalArgumentException("Can't find weapon type: " + rl);
		}

		return PRESETS.get(rl);
	}

	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSync packet) {
		if (packet.getType() == SPDatapackSync.Type.WEAPON_TYPE) {
			PRESETS.clear();
			registerDefaultWeaponTypes();

			for (CompoundNBT tag : packet.getTags()) {
				PRESETS.put(new ResourceLocation(tag.getString("registry_name")), (itemstack) -> deserializeWeaponCapabilityBuilder(tag));
			}

			ItemCapabilityReloadListener.weaponTypeProcessedCheck();
		}
	}

	public static WeaponCapability.Builder deserializeWeaponCapabilityBuilder(CompoundNBT tag) {
		WeaponCapability.Builder builder = WeaponCapability.builder();

		if (!tag.contains("category") || StringUtil.isNullOrEmpty(tag.getString("category"))) {
			throw new IllegalArgumentException("Define weapon category.");
		}

		builder.category(WeaponCategory.ENUM_MANAGER.getOrThrow(tag.getString("category")));
		builder.collider(ColliderPreset.deserializeSimpleCollider(tag.getCompound("collider")));
		builder.canBePlacedOffhand(tag.contains("usable_in_offhand") ? tag.getBoolean("usable_in_offhand") : true);

		if (tag.contains("hit_particle")) {
			ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(tag.getString("hit_particle")));

			if (particleType == null) {
				EpicFightMod.LOGGER.warn("Can't find a particle type " + tag.getString("hit_particle"));
			} else if (!(particleType instanceof HitParticleType)) {
				EpicFightMod.LOGGER.warn(tag.getString("hit_particle") + " is not a hit particle type");
			} else {
				builder.hitParticle((HitParticleType)particleType);
			}
		}

		if (tag.contains("swing_sound")) {
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("swing_sound")));

			if (sound == null) {
				EpicFightMod.LOGGER.warn("Can't find a swing sound " + tag.getString("swing_sound"));
			} else {
				builder.swingSound(sound);
			}
		}

		if (tag.contains("hit_sound")) {
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("hit_sound")));

			if (sound == null) {
				EpicFightMod.LOGGER.warn("Can't find a hit sound " + tag.getString("hit_sound"));
			} else {
				builder.hitSound(sound);
			}
		}

		CompoundNBT combosTag = tag.getCompound("combos");

		for (String key : combosTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(key);
			ListNBT comboAnimations = combosTag.getList(key, Constants.NBT.TAG_STRING);
			StaticAnimation[] animArray = new StaticAnimation[comboAnimations.size()];

			for (int i = 0; i < comboAnimations.size(); i++) {
				animArray[i] = AnimationManager.getInstance().byKeyOrThrow(comboAnimations.getString(i));
			}

			builder.newStyleCombo(style, animArray);
		}

		CompoundNBT innateSkillsTag = tag.getCompound("innate_skills");

		for (String key : innateSkillsTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(key);

			//builder.innateSkill(style, (itemstack) -> SkillManager.getSkill(innateSkillsTag.getString(key)));
		}

		CompoundNBT livingmotionModifierTag = tag.getCompound("livingmotion_modifier");

		for (String sStyle : livingmotionModifierTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(sStyle);
			CompoundNBT styleAnimationTag = livingmotionModifierTag.getCompound(sStyle);

			for (String sLivingmotion : styleAnimationTag.getAllKeys()) {
				LivingMotion livingmotion = LivingMotion.ENUM_MANAGER.getOrThrow(sLivingmotion);
				StaticAnimation animation = AnimationManager.getInstance().byKeyOrThrow(styleAnimationTag.getString(sLivingmotion));

				builder.livingMotionModifier(style, livingmotion, animation);
			}
		}

		CompoundNBT stylesTag = tag.getCompound("styles");
		final List<Pair<Predicate<LivingEntityPatch<?>>, Style>> conditions = Lists.newArrayList();
		final Style defaultStyle = Style.ENUM_MANAGER.getOrThrow(stylesTag.getString("default"));

		/*for (INBT caseTag : stylesTag.getList("cases",  Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT caseCompTag = (CompoundNBT)caseTag;
			List<EntityPatchCondition> conditionList = Lists.newArrayList();

			for (INBT offhandTag : caseCompTag.getList("conditions", Constants.NBT.TAG_COMPOUND)) {
				CompoundNBT offhandCompound = (CompoundNBT)offhandTag;
				Supplier<EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(new ResourceLocation(offhandCompound.getString("predicate")));
				EntityPatchCondition condition = conditionProvider.get();
				condition.read(offhandCompound);
				conditionList.add(condition);
			}

			conditions.add(Pair.of((entitypatch) -> {
				for (EntityPatchCondition condition : conditionList) {
					if (!condition.predicate(entitypatch)) {
						return false;
					}
				}

				return true;
			}, Style.ENUM_MANAGER.getOrThrow(caseCompTag.getString("style"))));
		}

		builder.styleProvider((entitypatch) -> {
			for (Pair<Predicate<LivingEntityPatch<?>>, Style> entry : conditions) {
				if (entry.getFirst().test(entitypatch)) {
					return entry.getSecond();
				}
			}

			return defaultStyle;
		});*/

		/*if (tag.contains("offhand_item_compatible_predicate")) {
			ListNBT offhandValidatorList = tag.getList("offhand_item_compatible_predicate", Constants.NBT.TAG_COMPOUND);
			List<EntityPatchCondition> conditionList = Lists.newArrayList();

			for (INBT offhandTag : offhandValidatorList) {
				CompoundNBT offhandCompound = (CompoundNBT)offhandTag;
				Supplier<EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(new ResourceLocation(offhandCompound.getString("predicate")));
				EntityPatchCondition condition = conditionProvider.get();
				condition.read(offhandCompound);
				conditionList.add(condition);
			}

			builder.weaponCombinationPredicator((entitypatch) -> {
				for (EntityPatchCondition condition : conditionList) {
					if (!condition.predicate(entitypatch)) {
						return false;
					}
				}

				return true;
			});
		}*/

		return builder;
	}

	public static int getTagCount() {
		return TAGMAP.size();
	}
	public static void clear() {
		PRESETS.clear();
		WeaponTypeReloadListener.registerDefaultWeaponTypes();
	}
}