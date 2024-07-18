package yesman.epicfight.gameasset;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityType;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.forgeevent.ModelBuildEvent;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.JsonModelLoader;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.entity.EpicFightEntities;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Armatures {
	public static final Armatures INSTANCE = new Armatures();
	
	@FunctionalInterface
	public interface ArmatureContructor<T extends Armature> {
		T invoke(String name, int jointNumber, Joint joint, Map<String, Joint> jointMap);
	}
	
	private static final BiMap<ResourceLocation, Armature> ARMATURES = HashBiMap.create();
	private static final Map<EntityType<?>, Function<EntityPatch<?>, Armature>> ENTITY_TYPE_ARMATURE = Maps.newHashMap();
	
	public static HumanoidArmature BIPED;
	public static HumanoidArmature SKELETON;

	
	public static void build(IResourceManager resourceManager) {
		ARMATURES.clear();
		ModelBuildEvent.ArmatureBuild event = new ModelBuildEvent.ArmatureBuild(resourceManager, ARMATURES);

		BIPED = event.get(EpicFightMod.MODID, "entity/biped", HumanoidArmature::new);
//		CREEPER = event.get(EpicFightMod.MODID, "entity/creeper", CreeperArmature::new);
//		ENDERMAN = event.get(EpicFightMod.MODID, "entity/enderman", EndermanArmature::new);
		SKELETON = event.get(EpicFightMod.MODID, "entity/skeleton", HumanoidArmature::new);
//		SPIDER = event.get(EpicFightMod.MODID, "entity/spider", SpiderArmature::new);
//		IRON_GOLEM = event.get(EpicFightMod.MODID, "entity/iron_golem", IronGolemArmature::new);
//		RAVAGER = event.get(EpicFightMod.MODID, "entity/ravager", RavagerArmature::new);
//		VEX = event.get(EpicFightMod.MODID, "entity/vex", VexArmature::new);
//		PIGLIN = event.get(EpicFightMod.MODID, "entity/piglin", PiglinArmature::new);
//		HOGLIN = event.get(EpicFightMod.MODID, "entity/hoglin", HoglinArmature::new);
//		DRAGON = event.get(EpicFightMod.MODID, "entity/dragon", DragonArmature::new);
//		WITHER = event.get(EpicFightMod.MODID, "entity/wither", WitherArmature::new);

//		registerEntityTypeArmature(EntityType.CAVE_SPIDER, SPIDER);
//		registerEntityTypeArmature(EntityType.CREEPER, CREEPER);
		registerEntityTypeArmature(EntityType.DROWNED, BIPED);
//		registerEntityTypeArmature(EntityType.ENDERMAN, ENDERMAN);
		registerEntityTypeArmature(EntityType.EVOKER, BIPED);
//		registerEntityTypeArmature(EntityType.HOGLIN, HOGLIN);
		registerEntityTypeArmature(EntityType.HUSK, BIPED);
//		registerEntityTypeArmature(EntityType.IRON_GOLEM, IRON_GOLEM);
//		registerEntityTypeArmature(EntityType.PIGLIN_BRUTE, PIGLIN);
//		registerEntityTypeArmature(EntityType.PIGLIN, PIGLIN);
		registerEntityTypeArmature(EntityType.PILLAGER, BIPED);
//		registerEntityTypeArmature(EntityType.RAVAGER, RAVAGER);
		registerEntityTypeArmature(EntityType.SKELETON, SKELETON);
//		registerEntityTypeArmature(EntityType.SPIDER, SPIDER);
		registerEntityTypeArmature(EntityType.STRAY, SKELETON);
//		registerEntityTypeArmature(EntityType.VEX, VEX);
		registerEntityTypeArmature(EntityType.VINDICATOR, BIPED);
		registerEntityTypeArmature(EntityType.WITCH, BIPED);
		registerEntityTypeArmature(EntityType.WITHER_SKELETON, SKELETON);
//		registerEntityTypeArmature(EntityType.ZOGLIN, HOGLIN);
		registerEntityTypeArmature(EntityType.ZOMBIE, BIPED);
		registerEntityTypeArmature(EntityType.ZOMBIE_VILLAGER, BIPED);
//		registerEntityTypeArmature(EntityType.ZOMBIFIED_PIGLIN, PIGLIN);
		registerEntityTypeArmature(EntityType.PLAYER, BIPED);
//		registerEntityTypeArmature(EntityType.ENDER_DRAGON, DRAGON);
//		registerEntityTypeArmature(EntityType.WITHER, WITHER);
		registerEntityTypeArmature(EpicFightEntities.WITHER_SKELETON_MINION.get(), SKELETON);
//		registerEntityTypeArmature(EpicFightEntities.WITHER_GHOST_CLONE.get(), WITHER);

		ModLoader.get().postEvent(event);
	}
	
	public static void registerEntityTypeArmature(EntityType<?> entityType, Armature armature) {
		ENTITY_TYPE_ARMATURE.put(entityType, (entitypatch) -> armature.deepCopy());
	}
	
	//For preset
	public static void registerEntityTypeArmature(EntityType<?> entityType, String presetName) {
		EntityType<?> presetEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(presetName));
		ENTITY_TYPE_ARMATURE.put(entityType, ENTITY_TYPE_ARMATURE.get(presetEntityType));
	}
	
	public static void registerEntityTypeArmature(EntityType<?> entityType, Function<EntityPatch<?>, Armature> armatureGetFunction) {
		ENTITY_TYPE_ARMATURE.put(entityType, armatureGetFunction);
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends Armature> A getArmatureFor(EntityPatch<?> entitypatch) {
		return (A)ENTITY_TYPE_ARMATURE.get(entitypatch.getOriginal().getType()).apply(entitypatch).deepCopy();
	}
	
	public static ResourceLocation getKey(Armature armature) {
		return ARMATURES.inverse().get(armature);
	}
	
	public static Armature getArmatureOrNull(ResourceLocation rl) {
		return ARMATURES.get(rl);
	}
	
	public static void addArmature(ResourceLocation rl, Armature armature) {
		ARMATURES.put(rl, armature);
	}
	
	public static Function<EntityPatch<?>, Armature> getRegistry(EntityType<?> entityType) {
		return ENTITY_TYPE_ARMATURE.get(entityType);
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends Armature> A getOrCreateArmature(IResourceManager rm, ResourceLocation rl, ArmatureContructor<A> constructor) {
		return (A) ARMATURES.computeIfAbsent(rl, (key) -> {
			JsonModelLoader jsonModelLoader = new JsonModelLoader(rm, wrapLocation(rl));
			return jsonModelLoader.loadArmature(constructor);
		});
	}
	
	public static Set<Map.Entry<ResourceLocation, Armature>> entries() {
		return ARMATURES.entrySet();
	}
	
	public static ResourceLocation wrapLocation(ResourceLocation rl) {
		return rl.getPath().matches("animmodels/.*\\.json") ? rl : new ResourceLocation(rl.getNamespace(), "animmodels/" + rl.getPath() + ".json");
	}
}