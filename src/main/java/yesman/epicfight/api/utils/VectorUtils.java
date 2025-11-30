package yesman.epicfight.api.utils;

import net.minecraft.util.math.vector.Vector3d;

public class VectorUtils {

    public static double horizontalDistance(Vector3d in) {
        return Math.sqrt(in.x * in.x + in.z * in.z);
    }

    public static double horizontalDistanceSqr(Vector3d in) {
        return in.x * in.x + in.z * in.z;
    }

    /**
     * Linearly interpolates between two Vector3d values.
     * Equivalent to how getDeltaMovementLerped would work for 1.16.5.
     * @param start The previous delta movement
     * @param end The current delta movement
     * @param partialTick The partial tick (0.0-1.0)
     * @return The lerped Vector3d
     */
    public static Vector3d lerp(Vector3d start, Vector3d end, float partialTick) {
        return new Vector3d(
            start.x + (end.x - start.x) * partialTick,
            start.y + (end.y - start.y) * partialTick,
            start.z + (end.z - start.z) * partialTick
        );
    }
}
