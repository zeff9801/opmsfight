package yesman.epicfight.api.utils.math;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class AABBUtil
{
    public static AxisAlignedBB ofSize(Vector3d p_165883_, double p_165884_, double p_165885_, double p_165886_) {
        return new AxisAlignedBB(p_165883_.x - p_165884_ / 2.0, p_165883_.y - p_165885_ / 2.0, p_165883_.z - p_165886_ / 2.0, p_165883_.x + p_165884_ / 2.0, p_165883_.y + p_165885_ / 2.0, p_165883_.z + p_165886_ / 2.0);
    }
}
