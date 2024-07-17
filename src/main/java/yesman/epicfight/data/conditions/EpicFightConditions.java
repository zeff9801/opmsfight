package yesman.epicfight.data.conditions;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import yesman.epicfight.data.conditions.entity.TargetInPov;
import yesman.epicfight.main.EpicFightMod;

import java.util.function.Supplier;

public class  EpicFightConditions {
	public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(new ResourceLocation(EpicFightMod.MODID, "conditions"), EpicFightMod.MODID);
	public static final Supplier<IForgeRegistry<Supplier<Condition<?>>>> REGISTRY = CONDITIONS.makeRegistry(RegistryBuilder::new);
	
	public static <T extends Condition<?>> Supplier<T> getConditionOrThrow(ResourceLocation key) {
		if (!REGISTRY.get().containsKey(key)) {
			throw new IllegalArgumentException("No condition named " + key);
		}
		
		return getConditionOrNull(key);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Condition<?>> Supplier<T> getConditionOrNull(ResourceLocation key) {
		return (Supplier<T>) REGISTRY.get().getValue(key);
	}
	
	//EntityPatch conditions
	//public static final RegistryObject<Supplier<Condition<?>>> OFFHAND_ITEM_CATEGORY = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "offhand_item_category").getPath(), () -> OffhandItemCategory::new);
	//public static final RegistryObject<Supplier<Condition<?>>> SKILL_ACTIVE = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "skill_active").getPath(), () -> SkillActivated::new);
	
	//Mobpatch conditions
	//public static final RegistryObject<Supplier<Condition<?>>> HEALTH_POINT = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "health").getPath(), () -> HealthPoint::new);
	//public static final RegistryObject<Supplier<Condition<?>>> RANDOM = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "random_chance").getPath(), () -> RandomChance::new);
	//public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_DISTANCE = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "within_distance").getPath(), () -> TargetInDistance::new);
	//public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_EYE_HEIGHT = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "within_eye_height").getPath(), () -> TargetInEyeHeight::new);
	public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_POV = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "within_angle").getPath(), () -> TargetInPov::new);
	public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_POV_HORIZONTAL = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "within_angle_horizontal").getPath(), () -> TargetInPov.TargetInPovHorizontal::new);
	
	//Itemstack conditions
	//public static final RegistryObject<Supplier<Condition<?>>> TAG_VALUE = CONDITIONS.register(new ResourceLocation(EpicFightMod.MODID, "tag_value").getPath(), () -> TagValueCondition::new);
}