package yesman.epicfight.world.capabilities.item;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.item.Item;
import net.minecraft.item.TieredItem;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.KatanaPassive;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class WeaponCapabilityPresets {
	public static final Function<Item, CapabilityItem.Builder> AXE = (item) -> {
		CapabilityItem.Builder builder = WeaponCapability.builder()
				.category(WeaponCategories.AXE)
				.hitSound(EpicFightSounds.BLADE_HIT)
				.collider(ColliderPreset.TOOLS)
				.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
				.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
				.specialAttack(Styles.ONE_HAND, EpicFightSkills.GUILLOTINE_AXE)
				.livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD);

		if (item instanceof TieredItem) {
			int harvestLevel = ((TieredItem)item).getTier().getLevel();

			if (harvestLevel != 0) {
				builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(10.0D * harvestLevel)));
			}

			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.7D + 0.3D * harvestLevel)));
		}

		return builder;
	};
	public static final Function<Item, CapabilityItem.Builder> HOE = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
				.category(WeaponCategories.HOE)
				.hitSound(EpicFightSounds.BLADE_HIT)
				.collider(ColliderPreset.TOOLS).newStyleCombo(Styles.ONE_HAND, Animations.TOOL_AUTO1, Animations.TOOL_AUTO2, Animations.TOOL_DASH, Animations.SWORD_AIR_SLASH)
				.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

		if (item instanceof TieredItem) {
			int harvestLevel = ((TieredItem)item).getTier().getLevel();
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(-0.4D + 0.1D * harvestLevel)));
		}

		return builder;
	};
	public static final Function<Item, CapabilityItem.Builder> PICKAXE = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
				.category(WeaponCategories.PICKAXE)
				.hitSound(EpicFightSounds.BLADE_HIT)
				.collider(ColliderPreset.TOOLS)
				.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
				.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

		if (item instanceof TieredItem) {
			int harvestLevel = ((TieredItem)item).getTier().getLevel();

			if (harvestLevel != 0) {
				builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(6.0D * harvestLevel)));
			}

			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.4D + 0.1D * harvestLevel)));
		}

		return builder;
	};
	public static final Function<Item, CapabilityItem.Builder> SHOVEL = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
				.category(WeaponCategories.SHOVEL)
				.collider(ColliderPreset.TOOLS)
				.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
				.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

		if (item instanceof TieredItem) {
			int harvestLevel = ((TieredItem)item).getTier().getLevel();
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.8D + 0.4D * harvestLevel)));
		}

		return builder;
	};
	public static final Function<Item, CapabilityItem.Builder> SWORD = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
				.category(WeaponCategories.SWORD)
				.styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategories.SWORD ? Styles.TWO_HAND : Styles.ONE_HAND)
				.collider(ColliderPreset.SWORD)
				.hitSound(EpicFightSounds.BLADE_HIT)
				.newStyleCombo(Styles.ONE_HAND, Animations.SWORD_AUTO1, Animations.SWORD_AUTO2, Animations.SWORD_AUTO3, Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH)
				.newStyleCombo(Styles.TWO_HAND, Animations.SWORD_DUAL_AUTO1, Animations.SWORD_DUAL_AUTO2, Animations.SWORD_DUAL_AUTO3, Animations.SWORD_DUAL_DASH, Animations.SWORD_DUAL_AIR_SLASH)
				.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
				.specialAttack(Styles.ONE_HAND, EpicFightSkills.SWEEPING_EDGE)
				.specialAttack(Styles.TWO_HAND, EpicFightSkills.DANCING_EDGE)
				.livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SWORD_DUAL_GUARD)
				.weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).weaponCategory == WeaponCategories.SWORD);

		if (item instanceof TieredItem) {
			int harvestLevel = ((TieredItem)item).getTier().getLevel();
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.5D + 0.2D * harvestLevel)));
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, Pair.of(EpicFightAttributes.MAX_STRIKES.get(), EpicFightAttributes.getMaxStrikesModifier(1)));
		}

		return builder;
	};
	public static final Function<Item, CapabilityItem.Builder> SPEAR = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
				.category(WeaponCategories.SPEAR)
				.styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategories.SHIELD ? Styles.ONE_HAND : Styles.TWO_HAND)
				.collider(ColliderPreset.SPEAR)
				.hitSound(EpicFightSounds.BLADE_HIT)
				.canBePlacedOffhand(false)
				.newStyleCombo(Styles.ONE_HAND, Animations.SPEAR_ONEHAND_AUTO, Animations.SPEAR_DASH, Animations.SPEAR_ONEHAND_AIR_SLASH)
				.newStyleCombo(Styles.TWO_HAND, Animations.SPEAR_TWOHAND_AUTO1, Animations.SPEAR_TWOHAND_AUTO2, Animations.SPEAR_DASH, Animations.SPEAR_TWOHAND_AIR_SLASH)
				.newStyleCombo(Styles.MOUNT, Animations.SPEAR_MOUNT_ATTACK)
				.specialAttack(Styles.ONE_HAND, EpicFightSkills.HEARTPIERCER)
				.specialAttack(Styles.TWO_HAND, EpicFightSkills.SLAUGHTER_STANCE)
				.livingMotionModifier(Styles.ONE_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_SPEAR)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_SPEAR)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_SPEAR)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_SPEAR)
				.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SPEAR_GUARD);

		return builder;
	};
	public static final Function<Item, CapabilityItem.Builder> GREATSWORD = (item) -> WeaponCapability.builder()
            .category(WeaponCategories.GREATSWORD)
            .styleProvider((playerpatch) -> Styles.TWO_HAND)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG)
            .hitSound(EpicFightSounds.BLADE_HIT)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.TWO_HAND, Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2, Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH)
            .specialAttack(Styles.TWO_HAND, EpicFightSkills.STEEL_WHIRLWIND)
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
            .category(WeaponCategories.KATANA)
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
            .collider(ColliderPreset.KATANA)
            .canBePlacedOffhand(false)
            .newStyleCombo(Styles.SHEATH, Animations.UCHIGATANA_SHEATHING_AUTO, Animations.UCHIGATANA_SHEATHING_DASH, Animations.UCHIGATANA_SHEATH_AIR_SLASH)
            .newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
            .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
            .specialAttack(Styles.SHEATH,  EpicFightSkills.FATAL_DRAW)
            .specialAttack(Styles.TWO_HAND, EpicFightSkills.FATAL_DRAW)
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
            .specialAttack(Styles.TWO_HAND, EpicFightSkills.RUSHING_TEMPO)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_TACHI)
            .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_TACHI)
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
            .specialAttack(Styles.ONE_HAND,  EpicFightSkills.SHARP_STAB)
            .specialAttack(Styles.TWO_HAND,  EpicFightSkills.LIECHTENAUER)
            .specialAttack(Styles.OCHS, EpicFightSkills.LIECHTENAUER)
            .livingMotionModifier(Styles.COMMON, LivingMotions.IDLE, Animations.BIPED_HOLD_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.CHASE, Animations.BIPED_WALK_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.SNEAK, Animations.BIPED_HOLD_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.KNEEL, Animations.BIPED_HOLD_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.JUMP, Animations.BIPED_HOLD_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.SWIM, Animations.BIPED_HOLD_LONGSWORD)
            .livingMotionModifier(Styles.COMMON, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .livingMotionModifier(Styles.OCHS, LivingMotions.IDLE, Animations.BIPED_HOLD_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.WALK, Animations.BIPED_WALK_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.CHASE, Animations.BIPED_WALK_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.RUN, Animations.BIPED_HOLD_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.SNEAK, Animations.BIPED_HOLD_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.KNEEL, Animations.BIPED_HOLD_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.JUMP, Animations.BIPED_HOLD_LIECHTENAUER)
            .livingMotionModifier(Styles.OCHS, LivingMotions.SWIM, Animations.BIPED_HOLD_LIECHTENAUER)
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
			.specialAttack(Styles.ONE_HAND, EpicFightSkills.EVISCERATE)
			.specialAttack(Styles.TWO_HAND, EpicFightSkills.BLADE_RUSH)
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
			.specialAttack(Styles.ONE_HAND, EpicFightSkills.RELENTLESS_COMBO)
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

	private static final Map<String, Function<Item, CapabilityItem.Builder>> PRESETS = Maps.newHashMap();

	public static void register() {
		Map<String, Function<Item, CapabilityItem.Builder>> typeEntry = Maps.newHashMap();
		typeEntry.put("axe", AXE);
		typeEntry.put("fist", FIST);
		typeEntry.put("hoe", HOE);
		typeEntry.put("pickaxe", PICKAXE);
		typeEntry.put("shovel", SHOVEL);
		typeEntry.put("sword", SWORD);
		typeEntry.put("spear", SPEAR);
		typeEntry.put("greatsword", GREATSWORD);
		typeEntry.put("katana", KATANA);
		typeEntry.put("tachi", TACHI);
		typeEntry.put("longsword", LONGSWORD);
		typeEntry.put("dagger", DAGGER);
		typeEntry.put("bow", BOW);
		typeEntry.put("crossbow", CROSSBOW);
		typeEntry.put("trident", TRIDENT);
		typeEntry.put("shield", SHIELD);

		WeaponCapabilityPresetRegistryEvent weaponCapabilityPresetRegistryEvent = new WeaponCapabilityPresetRegistryEvent(typeEntry);
		ModLoader.get().postEvent(weaponCapabilityPresetRegistryEvent);
		PRESETS.putAll(weaponCapabilityPresetRegistryEvent.getTypeEntry());
	}

	public static Function<Item, CapabilityItem.Builder> get(String typeName) {
		return PRESETS.get(typeName);
	}
}