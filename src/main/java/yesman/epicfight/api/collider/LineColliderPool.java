package yesman.epicfight.api.collider;

import java.util.ArrayDeque;

public class LineColliderPool {
    private static final int MAX_POOL_SIZE = 128;
    private static final ArrayDeque<LineCollider> pool = new ArrayDeque<>(MAX_POOL_SIZE);

    public static LineCollider acquire(double posX, double posY, double posZ, double vecX, double vecY, double vecZ) {
        LineCollider collider = pool.poll();
        if (collider == null) {
            return new LineCollider(posX, posY, posZ, vecX, vecY, vecZ);
        } else {
            collider.reset(posX, posY, posZ, vecX, vecY, vecZ);
            return collider;
        }
    }

    public static void release(LineCollider collider) {
        if (pool.size() < MAX_POOL_SIZE) {
            pool.offer(collider);
        }
    }
} 