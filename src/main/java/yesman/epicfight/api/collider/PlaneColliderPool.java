package yesman.epicfight.api.collider;

import java.util.ArrayDeque;

public class PlaneColliderPool {
    private static final int MAX_POOL_SIZE = 64;
    private static final ArrayDeque<PlaneCollider> pool = new ArrayDeque<>(MAX_POOL_SIZE);

    public static PlaneCollider acquire(double x, double y, double z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
        PlaneCollider collider = pool.poll();
        if (collider == null) {
            return new PlaneCollider(x, y, z, aX, aY, aZ, bX, bY, bZ);
        } else {
            collider.reset(x, y, z, aX, aY, aZ, bX, bY, bZ);
            return collider;
        }
    }

    public static void release(PlaneCollider collider) {
        if (pool.size() < MAX_POOL_SIZE) {
            pool.offer(collider);
        }
    }
} 