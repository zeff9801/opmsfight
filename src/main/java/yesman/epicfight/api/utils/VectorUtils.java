package yesman.epicfight.api.utils;

import net.minecraft.util.math.vector.Vector3d;

public class VectorUtils {

    public static double horizontalDistance(Vector3d in) {
        return Math.sqrt(in.x * in.x + in.z * in.z);
    }

    public static double horizontalDistanceSqr(Vector3d in) {
        return in.x * in.x + in.z * in.z;
    }
}
