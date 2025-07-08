package yesman.epicfight.gameasset;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.JsonModelLoader;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Armatures implements IFutureReloadListener {
	public static final Armatures INSTANCE = new Armatures();
	private static IResourceManager resourceManager = null;


	@FunctionalInterface
	public interface ArmatureContructor<T extends Armature> {
		T invoke(String name, int jointNumber, Joint joint, Map<String, Joint> jointMap);
	}
	
	private static final Map<ResourceLocation, ArmatureAccessor<? extends Armature>> ACCESSORS = Maps.newHashMap();
	private static final Map<ArmatureAccessor<? extends Armature>, Armature> ARMATURES = Maps.newHashMap();
	private static final Map<EntityType<?>, AssetAccessor<? extends Armature>> ENTITY_TYPE_ARMATURE_MAPPER = Maps.newHashMap();
	
	public static final ArmatureAccessor<HumanoidArmature> BIPED = ArmatureAccessor.create(EpicFightMod.MODID, "entity/biped", HumanoidArmature::new);

	public static void registerEntityTypes() {
		registerEntityTypeArmature(EntityType.PLAYER, BIPED);
	}
	
	public static void reload(IResourceManager resourceManager) {
		Armatures.resourceManager = resourceManager;
		ACCESSORS.entrySet().removeIf(entry -> !entry.getValue().inRegistry);
		ARMATURES.clear();
	}
	
	public static void registerEntityTypeArmature(EntityType<?> entityType, AssetAccessor<? extends Armature> armatureAccessor) {
		ENTITY_TYPE_ARMATURE_MAPPER.put(entityType, armatureAccessor);
	}
	
	//For presets
	public static void registerEntityTypeArmatureByPreset(EntityType<?> entityType, String presetName) {
		String[] split = presetName.split(":", 2);
		EntityType<?> presetEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(split[0], split[1]));
		ENTITY_TYPE_ARMATURE_MAPPER.put(entityType, ENTITY_TYPE_ARMATURE_MAPPER.get(presetEntityType));
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends Armature> A getArmatureFor(EntityPatch<?> entitypatch) {
		return (A)ENTITY_TYPE_ARMATURE_MAPPER.get(entitypatch.getOriginal().getType()).get().deepCopy();
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <A extends Armature> AssetAccessor<A> get(ResourceLocation id) {
		return (AssetAccessor<A>) ACCESSORS.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends Armature> AssetAccessor<A> getOrCreate(ResourceLocation id, ArmatureContructor<A> armatureConstructor) {
		return ACCESSORS.containsKey(id) ? (AssetAccessor<A>)ACCESSORS.get(id) : ArmatureAccessor.create(id, armatureConstructor, false);
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends Armature> Set<Pair<ResourceLocation, AssetAccessor<A>>> entry() {
		Set<Pair<ResourceLocation, AssetAccessor<A>>> newset = Sets.newHashSet();
		for (AssetAccessor<? extends Armature> accessor : ACCESSORS.values()) {
			try {
				AssetAccessor<A> casted = (AssetAccessor<A>)accessor;
				newset.add(Pair.of(casted.registryName(), casted));
			} catch(ClassCastException e) {
			}
		}
		return newset;
	}
	
	public static ResourceLocation wrapLocation(ResourceLocation rl) {
		return rl.getPath().matches("animmodels/.*\\.json") ? rl : new ResourceLocation(rl.getNamespace(), "animmodels/" + rl.getPath() + ".json");
	}
	
	@Override
	public CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.runAsync(() -> {
			reload(resourceManager);
		}, gameExecutor).thenCompose(stage::wait);
	}
	
	public static record ArmatureAccessor<A extends Armature> (ResourceLocation registryName, ArmatureContructor<A> armatureConstructor, boolean inRegistry) implements AssetAccessor<A> {
		public static <A extends Armature> ArmatureAccessor<A> create(String namespaceId, String path, ArmatureContructor<A> armatureConstructor) {
			return create(new ResourceLocation(namespaceId, path), armatureConstructor, true);
		}
		
		private static <A extends Armature> ArmatureAccessor<A> create(ResourceLocation id, ArmatureContructor<A> armatureConstructor, boolean inRegistry) {
			ArmatureAccessor<A> accessor = new ArmatureAccessor<A> (id, armatureConstructor, inRegistry);
			ACCESSORS.put(id, accessor);
			return accessor;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public A get() {
			if (ARMATURES.get(this) == null) {
				JsonModelLoader jsonModelLoader = new JsonModelLoader(resourceManager, wrapLocation(this.registryName()));
				ARMATURES.put(this, jsonModelLoader.loadArmature(this.armatureConstructor));
			}
			return (A)ARMATURES.get(this);
		}
		
		public String toString() {
			return this.registryName.toString();
		}
		
		public int hashCode() {
			return this.registryName.hashCode();
		}
		
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof ArmatureAccessor armatureAccessor) {
				return this.registryName.equals(armatureAccessor.registryName());
			} else if (obj instanceof ResourceLocation rl) {
				return this.registryName.equals(rl);
			} else if (obj instanceof String name) {
				return this.registryName.toString().equals(name);
			} else {
				return false;
			}
		}
	}
}