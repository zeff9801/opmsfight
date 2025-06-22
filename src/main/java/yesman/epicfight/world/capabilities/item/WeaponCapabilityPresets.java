package yesman.epicfight.world.capabilities.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.TieredItem;
import net.minecraft.util.Hand;
import net.minecraft.item.UseAction;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.ZoomInType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.Styles;

import java.util.function.Function;

@SuppressWarnings("deprecation") // getLevel
public class WeaponCapabilityPresets {
    public static final Function<Item, CapabilityItem.Builder> AXE = (item) -> {
        CapabilityItem.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.AXE)
                .hitSound(EpicFightSounds.BLADE_HIT)
                .collider(ColliderPreset.TOOLS)
                .newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                .innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.GUILLOTINE_AXE)
                .livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD);

        if (item instanceof TieredItem tieredItem) {
            int harvestLevel = tieredItem.getTier().getLevel();

            if (harvestLevel != 0) {
                builder.addStyleAttibutes(Styles.COMMON, Pair.of(EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(10.0D * harvestLevel)));
            }

            builder.addStyleAttibutes(Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.7D + 0.3D * harvestLevel)));
        }

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder> HOE = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.HOE)
                .hitSound(EpicFightSounds.BLADE_HIT)
                .collider(ColliderPreset.TOOLS).newStyleCombo(Styles.ONE_HAND, Animations.TOOL_AUTO1, Animations.TOOL_AUTO2, Animations.TOOL_DASH, Animations.SWORD_AIR_SLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

        if (item instanceof TieredItem tieredItem) {
            int harvestLevel = tieredItem.getTier().getLevel();
            builder.addStyleAttibutes(Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(-0.4D + 0.1D * harvestLevel)));
        }

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder> PICKAXE = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.PICKAXE)
                .hitSound(EpicFightSounds.BLADE_HIT)
                .collider(ColliderPreset.TOOLS)
                .newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

        if (item instanceof TieredItem tieredItem) {
            int harvestLevel = tieredItem.getTier().getLevel();

            if (harvestLevel != 0) {
                builder.addStyleAttibutes(Styles.COMMON, Pair.of(EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(6.0D * harvestLevel)));
            }

            builder.addStyleAttibutes(Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.4D + 0.1D * harvestLevel)));
        }

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder> SHOVEL = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.SHOVEL)
                .collider(ColliderPreset.TOOLS)
                .newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);

        if (item instanceof TieredItem tieredItem) {
            int harvestLevel = tieredItem.getTier().getLevel();
            builder.addStyleAttibutes(Styles.COMMON, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(0.8D + 0.4D * harvestLevel)));
        }

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder> SWORD = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.SWORD)
                .styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategory.SWORD ? Styles.TWO_HAND : Styles.ONE_HAND)
                .collider(ColliderPreset.SWORD)
                .newStyleCombo(Styles.ONE_HAND, Animations.SWORD_AUTO1, Animations.SWORD_AUTO2, Animations.SWORD_AUTO3, Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH)
                .newStyleCombo(Styles.TWO_HAND, Animations.SWORD_DUAL_AUTO1, Animations.SWORD_DUAL_AUTO2, Animations.SWORD_DUAL_AUTO3, Animations.SWORD_DUAL_DASH, Animations.SWORD_DUAL_AIR_SLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                .innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.SWEEPING_EDGE)
                .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.DANCING_EDGE)
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
                .weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).getWeaponCategory() == WeaponCategory.SWORD);

        if (item instanceof TieredItem tieredItem) {
            builder.hitSound(tieredItem.getTier() == ItemTier.WOOD ? EpicFightSounds.BLUNT_HIT : EpicFightSounds.BLADE_HIT);
            builder.hitParticle(tieredItem.getTier() == ItemTier.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
        }

        return builder;
    };
    public static final Function<Item, CapabilityItem.Builder> SPEAR = (item) ->
            WeaponCapability.builder()
                    .category(WeaponCategory.SPEAR)
                    .styleProvider((playerpatch) -> (playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategory.SHIELD) ?
                            Styles.ONE_HAND : Styles.TWO_HAND)
                    .collider(ColliderPreset.SPEAR)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .canBePlacedOffhand(false)
                    .newStyleCombo(Styles.ONE_HAND, Animations.SPEAR_ONEHAND_AUTO, Animations.SPEAR_DASH, Animations.SPEAR_ONEHAND_AIR_SLASH)
                    .newStyleCombo(Styles.TWO_HAND, Animations.SPEAR_TWOHAND_AUTO1, Animations.SPEAR_TWOHAND_AUTO2, Animations.SPEAR_DASH, Animations.SPEAR_TWOHAND_AIR_SLASH)
                    .newStyleCombo(Styles.MOUNT, Animations.SPEAR_MOUNT_ATTACK)
                    .innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.HEARTPIERCER)
                    .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.GRASPING_SPIRE)
                    .livingMotionModifier(Styles.ONE_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_SPEAR)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_SPEAR)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_SPEAR)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_SPEAR)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SPEAR_GUARD);

    public static final Function<Item, CapabilityItem.Builder> GREATSWORD = (item) ->
            WeaponCapability.builder()
                    .category(WeaponCategory.GREATSWORD)
                    .styleProvider((playerpatch) -> Styles.TWO_HAND)
                    .collider(ColliderPreset.GREATSWORD)
                    .swingSound(EpicFightSounds.WHOOSH_BIG)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .canBePlacedOffhand(false)
                    .newStyleCombo(Styles.TWO_HAND, Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2, Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH)
                    .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.STEEL_WHIRLWIND)
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

    public static final Function<Item, CapabilityItem.Builder> KATANA = (item) ->
            WeaponCapability.builder()
                    .category(WeaponCategory.KATANA)
                    .styleProvider((entitypatch) -> {
                        if (entitypatch instanceof PlayerPatch<?> playerpatch && (playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(SkillDataKeys.SHEATH.get()) &&
                                playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().getDataValue(SkillDataKeys.SHEATH.get()))) {
                            return Styles.SHEATH;
                        }
                        return Styles.TWO_HAND;
                    })
                    .passiveSkill(EpicFightSkills.BATTOJUTSU_PASSIVE)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.KATANA)
                    .canBePlacedOffhand(false)
                    .newStyleCombo(Styles.SHEATH, Animations.UCHIGATANA_SHEATHING_AUTO, Animations.UCHIGATANA_SHEATHING_DASH, Animations.UCHIGATANA_SHEATH_AIR_SLASH)
                    .newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
                    .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                    .innateSkill(Styles.SHEATH, (itemstack) -> EpicFightSkills.BATTOJUTSU)
                    .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.BATTOJUTSU)
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

    public static final Function<Item, CapabilityItem.Builder> TACHI = (item) ->
            WeaponCapability.builder()
                    .category(WeaponCategory.TACHI)
                    .styleProvider((playerpatch) -> Styles.TWO_HAND)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.TACHI)
                    .canBePlacedOffhand(false)
                    .newStyleCombo(Styles.TWO_HAND, Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3, Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH)
                    .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                    .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.LIECHTENAUER)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA)
                    .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);

    public static final Function<Item, CapabilityItem.Builder> LONGSWORD = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.LONGSWORD)
                .styleProvider((playerpatch) -> Styles.TWO_HAND)
                .hitSound(EpicFightSounds.BLADE_HIT)
                .collider(ColliderPreset.LONGSWORD)
                .canBePlacedOffhand(false)
                .newStyleCombo(Styles.TWO_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
                .newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
                .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.EVISCERATE)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.JUMP, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLY, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_GREATSWORD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);

        if (item instanceof TieredItem tieredItem) {
            builder.addStyleAttibutes(Styles.TWO_HAND, Pair.of(EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(5.0D * tieredItem.getTier().getLevel())));
        }

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder> DAGGER = (item) -> {
        WeaponCapability.Builder builder = WeaponCapability.builder()
                .category(WeaponCategory.DAGGER)
                .styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategory.DAGGER ? Styles.TWO_HAND : Styles.ONE_HAND)
                .hitSound(EpicFightSounds.BLADE_HIT)
                .collider(ColliderPreset.DAGGER)
                .newStyleCombo(Styles.ONE_HAND, Animations.DAGGER_AUTO1, Animations.DAGGER_DASH, Animations.DAGGER_AIR_SLASH)
                .newStyleCombo(Styles.TWO_HAND, Animations.DAGGER_DUAL_AUTO1, Animations.DAGGER_DUAL_DASH, Animations.DAGGER_DUAL_AIR_SLASH)
                .innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.SHARP_STAB)
                .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.RELENTLESS_COMBO)
                .livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
                .livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SWORD_DUAL_GUARD)
                .weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).getWeaponCategory() == WeaponCategory.DAGGER);

        if (item instanceof TieredItem tieredItem) {
            builder.addStyleAttibutes(Styles.ONE_HAND, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(-0.2D + (double)tieredItem.getTier().getLevel() * 0.05D)));
            builder.addStyleAttibutes(Styles.TWO_HAND, Pair.of(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(-0.4D + (double)tieredItem.getTier().getLevel() * 0.1D)));
        }

        return builder;
    };

    public static final Function<Item, CapabilityItem.Builder> FIST = (item) -> WeaponCapability.builder()
            .category(WeaponCategory.FIST)
            .styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategory.FIST ? Styles.TWO_HAND : Styles.ONE_HAND);

    public static final Function<Item, CapabilityItem.Builder> BOW =  (item) -> RangedWeaponCapability.builder()
            .styleProvider((playerpatch) -> Styles.RANGED)
            .canBePlacedOffhand(false)
            .zoomInType(ZoomInType.USE_TICK);

    public static final Function<Item, CapabilityItem.Builder> CROSSBOW =  (item) -> RangedWeaponCapability.builder()
            .styleProvider((playerpatch) -> Styles.RANGED)
            .canBePlacedOffhand(false)
            .useAction((playerpatch) -> {
                if (playerpatch.getOriginal().isUsingItem()) {
                    return playerpatch.getOriginal().getUseItem().getUseAnimation();
                }

                return CrossbowItem.isCharged(playerpatch.getOriginal().getMainHandItem()) ? UseAction.BOW : UseAction.CROSSBOW;
            })
            .zoomInType(ZoomInType.CUSTOM);

    public static final Function<Item, CapabilityItem.Builder> TRIDENT = (item) -> RangedWeaponCapability.builder()
            .styleProvider((playerpatch) -> (playerpatch.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == WeaponCategory.SHIELD) ?
                    Styles.ONE_HAND : Styles.TWO_HAND)
            .canBePlacedOffhand(false)
            .innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.TSUNAMI);

    public static final Function<Item, CapabilityItem.Builder> SHIELD = (item) -> WeaponCapability.builder()
            .category(WeaponCategory.SHIELD)
            .styleProvider((playerpatch) -> Styles.ONE_HAND);
}