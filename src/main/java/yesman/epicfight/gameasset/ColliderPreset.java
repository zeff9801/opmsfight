package yesman.epicfight.gameasset;

import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;

public class ColliderPreset {
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
	public static final Collider SWORD = new MultiOBBCollider(3, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.35D);
	public static final Collider LONGSWORD = new MultiOBBCollider(3, 0.4D, 0.4D, 0.8D, 0D, 0D, -0.75D);
	public static final Collider SPEAR = new MultiOBBCollider(3, 0.6D, 0.6D, 1.0D, 0D, 0D, -1.0D);
	public static final Collider TOOLS = new MultiOBBCollider(3, 0.4D, 0.4D, 0.55D, 0D, 0.0D, -0.25D);
	public static final Collider FIST_FIXED = new OBBCollider(0.4D, 0.4D, 0.5D, 0D, 1.0D, -0.85D);
	public static final Collider DUAL_SWORD_AIR_SLASH = new OBBCollider(0.8D, 0.4D, 1.0D, 0D, 0.5D, -0.5D);
	public static final Collider DUAL_DAGGER_AIR_SLASH = new OBBCollider(0.8D, 0.4D, 0.75D, 0D, 0.5D, -0.5D);
    /**
	public static void update() {
		Collider newCOllider = new OBBCollider(0.7D, 0.7D, 3.5D, 0D, 1.0D, -3.5D);
		((AttackAnimation)Animations.FATAL_DRAW_DASH).changeCollider(newCOllider, 0);
	}**/
	
	
}