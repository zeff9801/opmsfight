
package yesman.epicfight.skill;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import yesman.epicfight.main.EpicFightMod;

//public class SkillDataKeys {
//    private static final Supplier<RegistryBuilder<SkillDataManager.SkillDataKey<?>>> BUILDER = () -> new RegistryBuilder<SkillDataManager.SkillDataKey<?>>().addCallback(SkillDataKey.getRegistryCallback());
//
//    public static final DeferredRegister<SkillDataManager.SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(new ResourceLocation(EpicFightMod.MODID, "skill_data_keys"), EpicFightMod.MODID);
//    public static final Supplier<IForgeRegistry<SkillDataManager.SkillDataKey<?>>> REGISTRY = DATA_KEYS.makeRegistry(BUILDER);
//
//    public static final RegistryObject<SkillDataKey<Integer>> COMBO_COUNTER = DATA_KEYS.register("combo_counter", () -> SkillDataKey.createIntKey(0, false, BasicAttack.class, BladeRushSkill.class));
//    public static final RegistryObject<SkillDataKey<Boolean>> SHEATH = DATA_KEYS.register("sheath", () -> SkillDataKey.createBooleanKey(false, false, BattojutsuPassive.class, BattojutsuSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> PENALTY_RESTORE_COUNTER = DATA_KEYS.register("penalty_restore_counter", () -> SkillDataKey.createIntKey(0, false, GuardSkill.class));
//    public static final RegistryObject<SkillDataKey<Float>> PENALTY = DATA_KEYS.register("penalty", () -> SkillDataKey.createFloatKey(0.0F, false, GuardSkill.class, ImpactGuardSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> LAST_ACTIVE = DATA_KEYS.register("last_active", () -> SkillDataKey.createIntKey(0, false, ParryingSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> PARRY_MOTION_COUNTER = DATA_KEYS.register("parry_motion_counter", () -> SkillDataKey.createIntKey(0, false, ParryingSkill.class));
//    public static final RegistryObject<SkillDataKey<Float>> FALL_DISTANCE = DATA_KEYS.register("fall_distance", () -> SkillDataKey.createFloatKey(0.0F, false, MeteorSlamSkill.class));
//    public static final RegistryObject<SkillDataKey<Boolean>> PROTECT_NEXT_FALL = DATA_KEYS.register("slam_protect_next_fall", () -> SkillDataKey.createBooleanKey(false, false, MeteorSlamSkill.class, DemolitionLeapSkill.class, PhantomAscentSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> STACKS = DATA_KEYS.register("stacks", () -> SkillDataKey.createIntKey(0, false, RevelationSkill.class));
//    public static final RegistryObject<SkillDataKey<Boolean>> JUMP_KEY_PRESSED_LAST_TICK = DATA_KEYS.register("jump_key_pressed_last_tick", () -> SkillDataKey.createBooleanKey(false, false, PhantomAscentSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> JUMP_COUNT = DATA_KEYS.register("jump_count", () -> SkillDataKey.createIntKey(0, false, PhantomAscentSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> THROWN_TRIDENT_ENTITY_ID = DATA_KEYS.register("thrown_trident_entity_id", () -> SkillDataKey.createIntKey(-1, false, EverlastingAllegiance.class));
//    public static final RegistryObject<SkillDataKey<Integer>> LAST_HIT_COUNT = DATA_KEYS.register("last_hit_count", () -> SkillDataKey.createIntKey(0, false, GraspingSpireSkill.class));
//    public static final RegistryObject<SkillDataKey<Integer>> CHARGING_POWER = DATA_KEYS.register("charging_power", () -> SkillDataKey.createIntKey(0, true, SteelWhirlwindSkill.class));
//}
