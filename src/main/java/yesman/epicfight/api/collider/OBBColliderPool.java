package yesman.epicfight.api.collider;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import java.util.ArrayDeque;

public class OBBColliderPool {
    private static final int MAX_POOL_SIZE = 128;
    private static final ArrayDeque<OBBCollider> pool = new ArrayDeque<>(MAX_POOL_SIZE);

    public static OBBCollider acquire(double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
        OBBCollider collider = pool.poll();
        if (collider == null) {
            return new OBBCollider(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
        } else {
            collider.reset(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
            return collider;
        }
    }

    public static void release(OBBCollider collider) {
        if (pool.size() < MAX_POOL_SIZE) {
            pool.offer(collider);
        }
    }
} 