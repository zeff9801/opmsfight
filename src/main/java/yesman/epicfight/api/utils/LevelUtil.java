package yesman.epicfight.api.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.utils.math.Vec2i;

public class LevelUtil {

    /**
     * {@link LivingEntity#calculateFallDamage} Calculates the fall damage instead of the vanilla method because it's protected
     */
    public static int calculateLivingEntityFallDamage(LivingEntity livingEntity, float distance, float modifier) {
        if (livingEntity.getType().is(EntityTypeTags.RAIDERS)) {//TODO should be FALL_DAMAGE_IMMUNE
            return 0;
        } else {
            EffectInstance mobeffectinstance = livingEntity.getEffect(Effects.JUMP);
            float f = mobeffectinstance == null ? 0.0F : (float) (mobeffectinstance.getAmplifier() + 1);
            return MathHelper.ceil((distance - 3.0F - f) * modifier);
        }
    }


    private static boolean isBlockOverlapLine(Vec2i vec2, Vector3d from, Vector3d to) {
        return isLinesCross(vec2.x, vec2.y, vec2.x + 1, vec2.y, from.x, from.z, to.x, to.z)
                || isLinesCross(vec2.x, vec2.y, vec2.x, vec2.y + 1, from.x, from.z, to.x, to.z)
                || isLinesCross(vec2.x + 1, vec2.y, vec2.x + 1, vec2.y + 1, from.x, from.z, to.x, to.z)
                || isLinesCross(vec2.x, vec2.y + 1, vec2.x + 1, vec2.y + 1, from.x, from.z, to.x, to.z);
    }

    private static boolean isLinesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double v1 = (x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1);
        double u = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / v1;
        double v = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / v1;

        return 0 < u && u < 1 && 0 < v && v < 1;
    }

    //TODO Implement FractureBlock
//    public static boolean canTransferShockWave(World level, BlockPos blockPos, BlockState blockState) {
//        return Block.isFaceFull(blockState.getCollisionShape(level, blockPos, ISelectionContext.empty()), Direction.DOWN) || (blockState instanceof FractureBlockState);
//    }

}
