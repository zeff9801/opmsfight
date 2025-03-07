package yesman.epicfight.api.utils.math;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class AABBUtil
{
    public static AxisAlignedBB ofSize(Vector3d center, double width, double height, double depth) {
        return new AxisAlignedBB(
                center.x - width / 2.0,
                center.y - height / 2.0,
                center.z - depth / 2.0,
                center.x + width / 2.0,
                center.y + height / 2.0,
                center.z + depth / 2.0
        );
    }
}
