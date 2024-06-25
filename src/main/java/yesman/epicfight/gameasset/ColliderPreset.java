package yesman.epicfight.gameasset;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ColliderPreset {

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

	public static final Collider DAGGER = new MultiOBBCollider(3, 0.4D, 0.4D, 0.6D, 0.0D, 0.0D, -0.1D);
	public static final Collider DUAL_DAGGER_DASH = new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D);

	public static final Collider BLADE_RUSH = new OBBCollider(0.8D, 0.5D, 1.9D, 0.0D, 1.0D, -1.2D);
	public static final Collider DUAL_SWORD = new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 0.5D, -1.0D);
	public static final Collider DUAL_SWORD_DASH = new OBBCollider(0.8D, 0.5D, 1.0D, 0D, 1.0D, -1.0D);
	public static final Collider FATAL_DRAW = new OBBCollider(1.75D, 0.25D, 1.35D, 0D, 1.0D, -1.0D);
	public static final Collider FATAL_DRAW_DASH = new OBBCollider(0.7D, 0.7D, 4.0D, 0D, 1.0D, -4.0D);

	public static final Collider FIST = new MultiOBBCollider(3, 0.4D, 0.4D, 0.4D, 0D, 0D, 0D);
	public static final Collider GREATSWORD = new MultiOBBCollider(3, 0.5D, 0.8D, 1.0D, 0D, 0D, -1.0D);
	public static final Collider TACHI = new MultiOBBCollider(3, 0.4D, 0.4D, 0.95D, 0D, 0D, -0.95D);
	public static final Collider KATANA = new MultiOBBCollider(3, 0.4D, 0.4D, 1.0D, 0D, 0D, -0.5D);
	public static final Collider UCHIGATANA = new MultiOBBCollider(5, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.7D);
	public static final Collider SWORD = new MultiOBBCollider(3, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.35D);
	public static final Collider LONGSWORD = new MultiOBBCollider(3, 0.4D, 0.4D, 0.8D, 0D, 0D, -0.75D);
	public static final Collider SPEAR = new MultiOBBCollider(3, 0.6D, 0.6D, 1.0D, 0D, 0D, -1.0D);
	public static final Collider TOOLS = new MultiOBBCollider(3, 0.4D, 0.4D, 0.55D, 0D, 0.0D, -0.25D);
	public static final Collider FIST_FIXED = new OBBCollider(0.4D, 0.4D, 0.5D, 0D, 1.0D, -0.85D);
	public static final Collider DUAL_SWORD_AIR_SLASH = new OBBCollider(0.8D, 0.4D, 1.0D, 0D, 0.5D, -0.5D);
	public static final Collider DUAL_DAGGER_AIR_SLASH = new OBBCollider(0.8D, 0.4D, 0.75D, 0D, 0.5D, -0.5D);
	public static final Collider BATTOJUTSU = new OBBCollider(2.5D, 0.25D, 1.5D, 0D, 1.0D, -1.0D);
	public static final Collider BATTOJUTSU_DASH = new OBBCollider(1D, 1D, 1D, 0D, 1.0D, -1.0D);

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

	/**
	public static void update() {
		Collider newCOllider = new OBBCollider(0.7D, 0.7D, 3.5D, 0D, 1.0D, -3.5D);
		((AttackAnimation)Animations.FATAL_DRAW_DASH).changeCollider(newCOllider, 0);
	}**/
	

}