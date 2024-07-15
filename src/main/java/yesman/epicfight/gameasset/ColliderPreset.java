package yesman.epicfight.gameasset;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.main.EpicFightMod;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ColliderPreset implements IFutureReloadListener {

	private static final BiMap<ResourceLocation, Collider> PRESETS = HashBiMap.create();

	public static Collider registerCollider(ResourceLocation rl, Collider collider) {
		if (PRESETS.containsKey(rl)) {
			throw new IllegalStateException("Collider named " + rl + " already registered.");
		}

		PRESETS.put(rl, collider);

		return collider;
	}

	public static Set<Map.Entry<ResourceLocation, Collider>> entries() {
		return Collections.unmodifiableSet(PRESETS.entrySet());
	}

	public static ResourceLocation getKey(Collider collider) {
		return PRESETS.inverse().get(collider);
	}

	public static Collider get(ResourceLocation rl) {
		return PRESETS.get(rl);
	}

	public static final Collider DAGGER = registerCollider(new ResourceLocation(EpicFightMod.MODID, "dagger"),
			new MultiOBBCollider(3, 0.4D, 0.4D, 0.6D, 0.0D, 0.0D, -0.1D));
	public static final Collider DUAL_DAGGER_DASH = registerCollider(new ResourceLocation(EpicFightMod.MODID, "dual_dagger_dash"),
			new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D));
	public static final Collider BIPED_BODY_COLLIDER = registerCollider(new ResourceLocation(EpicFightMod.MODID, "biped_body_collider"),
			new MultiOBBCollider(
			new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D),
			new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D)
	));
	public static final Collider BLADE_RUSH = new OBBCollider(0.8D, 0.5D, 1.9D, 0.0D, 1.0D, -1.2D);//

	public static final Collider DUAL_SWORD = registerCollider(new ResourceLocation(EpicFightMod.MODID, "dual_sword"),
			new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 0.5D, -1.0D));
	public static final Collider DUAL_SWORD_DASH = registerCollider(new ResourceLocation(EpicFightMod.MODID, "dual_sword_dash"),
			new OBBCollider(0.8D, 0.5D, 1.0D, 0D, 1.0D, -1.0D));
	public static final Collider FATAL_DRAW = new OBBCollider(1.75D, 0.25D, 1.35D, 0D, 1.0D, -1.0D);//
	public static final Collider FATAL_DRAW_DASH = new OBBCollider(0.7D, 0.7D, 4.0D, 0D, 1.0D, -4.0D);//
	public static final Collider FIST = registerCollider(new ResourceLocation(EpicFightMod.MODID, "fist"),
			new MultiOBBCollider(3, 0.4D, 0.4D, 0.4D, 0D, 0D, 0D));
	public static final Collider GREATSWORD = registerCollider(new ResourceLocation(EpicFightMod.MODID, "greatsword"),
			new MultiOBBCollider(3, 0.5D, 0.8D, 1.0D, 0D, 0D, -1.0D));
	public static final Collider HEAD = registerCollider(new ResourceLocation(EpicFightMod.MODID, "head"),
			new OBBCollider(0.4D, 0.4D, 0.4D, 0D, 0D, -0.3D));
	public static final Collider TACHI = registerCollider(new ResourceLocation(EpicFightMod.MODID, "tachi"),
			new MultiOBBCollider(3, 0.4D, 0.4D, 0.95D, 0D, 0D, -0.95D));
	public static final Collider UCHIGATANA = registerCollider(new ResourceLocation(EpicFightMod.MODID, "uchigatana"),
			new MultiOBBCollider(5, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.7D));
	public static final Collider SWORD = registerCollider(new ResourceLocation(EpicFightMod.MODID, "sword"),
			new MultiOBBCollider(3, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.35D));
	public static final Collider LONGSWORD = registerCollider(new ResourceLocation(EpicFightMod.MODID, "longsword"),
			new MultiOBBCollider(3, 0.4D, 0.4D, 0.8D, 0D, 0D, -0.75D));
	public static final Collider SPEAR = registerCollider(new ResourceLocation(EpicFightMod.MODID, "spear"),
			new MultiOBBCollider(3, 0.6D, 0.6D, 1.0D, 0D, 0D, -1.0D));
	public static final Collider TOOLS = registerCollider(new ResourceLocation(EpicFightMod.MODID, "tools"),
			new MultiOBBCollider(3, 0.4D, 0.4D, 0.55D, 0D, 0.0D, -0.25D));
	public static final Collider FIST_FIXED = registerCollider(new ResourceLocation(EpicFightMod.MODID, "fist_fixed"),
			new OBBCollider(0.4D, 0.4D, 0.5D, 0D, 1.25D, -0.85D));
	public static final Collider DUAL_SWORD_AIR_SLASH = registerCollider(new ResourceLocation(EpicFightMod.MODID, "dual_sword_air_slash"),
			new OBBCollider(0.8D, 0.4D, 1.0D, 0D, 0.5D, -0.5D));
	public static final Collider DUAL_DAGGER_AIR_SLASH = registerCollider(new ResourceLocation(EpicFightMod.MODID, "dual_dagger_air_slash"),
			new OBBCollider(0.8D, 0.4D, 0.75D, 0D, 0.5D, -0.5D));
	public static final Collider BATTOJUTSU = registerCollider(new ResourceLocation(EpicFightMod.MODID, "battojutsu"),
			new OBBCollider(3.0D, 0.4D, 1.5D, 0.0D, 1.2D, -1.0D));
	public static final Collider BATTOJUTSU_DASH = registerCollider(new ResourceLocation(EpicFightMod.MODID, "battojutsu_dash"), new MultiOBBCollider(
			new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
			new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
			new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
			new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
			new OBBCollider(1.5D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D)
	));

	public static Collider deserializeSimpleCollider(CompoundNBT tag) throws IllegalArgumentException {
		int number = tag.getInt("number");

		if (number < 1) {
			throw new IllegalArgumentException("Datapack deserialization error: the number of colliders must bigger than 0!");
		}

		ListNBT sizeVector = tag.getList("size", 6);
		ListNBT centerVector = tag.getList("center", 6);

		if (sizeVector.size() != 3) {
			throw new IllegalArgumentException("The size list tag must consist of three double elements.");
		}

		if (centerVector.size() != 3) {
			throw new IllegalArgumentException("The center list tag must consist of three double elements.");
		}

		double sizeX = sizeVector.getDouble(0);
		double sizeY = sizeVector.getDouble(1);
		double sizeZ = sizeVector.getDouble(2);

		double centerX = centerVector.getDouble(0);
		double centerY = centerVector.getDouble(1);
		double centerZ = centerVector.getDouble(2);

		if (sizeX < 0.0D || sizeY < 0.0D || sizeZ < 0.0D || (sizeX == 0.0D && sizeY == 0.0D && sizeZ == 0.0D)) {
			throw new IllegalArgumentException("Datapack deserialization error: the size of the collider must be non-negative value!");
		}

		if (number == 1) {
			return new OBBCollider(sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
		} else {
			return new MultiOBBCollider(number, sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
		}
	}

	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.runAsync(() -> {
			//Collider newCOllider = new OBBCollider(0.4D, 0.4D, 0.5D, 0D, 1.25D, -0.85D)
			//((AttackAnimation)Animations.FATAL_DRAW_DASH).changeCollider(newCOllider, 0);
		}, gameExecutor).thenCompose(stage::wait);
	}
}