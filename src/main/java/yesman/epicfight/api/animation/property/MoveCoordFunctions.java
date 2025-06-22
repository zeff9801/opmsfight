package yesman.epicfight.api.animation.property;

import com.joml.Quaternionf;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.utils.VectorUtils;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MoveCoordFunctions {
    private static final Vec3f VEC3F_HOLDER = new Vec3f();
    private static final Vec4f VEC4F_HOLDER_1 = new Vec4f();
    private static final Vec4f VEC4F_HOLDER_2 = new Vec4f();
    private static final TransformSheet TRANSFORM_SHEET_HOLDER = new TransformSheet();
    private static final Vec3f VEC3F_HOLDER_2 = new Vec3f();
    private static final Vec3f VEC3F_HOLDER_3 = new Vec3f();

    @FunctionalInterface
    public interface MoveCoordSetter {
        void set(DynamicAnimation animation, LivingEntityPatch<?> entitypatch, TransformSheet transformSheet);
    }

    @FunctionalInterface
    public interface MoveCoordGetter {
        Vec3f get(DynamicAnimation animation, LivingEntityPatch<?> entitypatch, TransformSheet transformSheet);
    }

    public static final MoveCoordGetter DIFF_FROM_PREV_COORD = (animation, entitypatch, coord) -> {
        LivingEntity livingentity = entitypatch.getOriginal();
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
        JointTransform jt = coord.getInterpolatedTransform(player.getElapsedTime());
        JointTransform prevJt = coord.getInterpolatedTransform(player.getPrevElapsedTime());
        VEC4F_HOLDER_1.set(jt.translation());
        VEC4F_HOLDER_2.set(prevJt.translation());
        OpenMatrix4f rotationTransform = entitypatch.getModelMatrix(1.0F).removeTranslation();
        OpenMatrix4f localTransform = entitypatch.getArmature().searchJointByName("Root").getLocalTrasnform().removeTranslation();
        rotationTransform.mulBack(localTransform);
        VEC4F_HOLDER_1.transform(rotationTransform);
        VEC4F_HOLDER_2.transform(rotationTransform);

        boolean hasNoGravity = entitypatch.getOriginal().isNoGravity();
        boolean moveVertical = animation.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL).orElse(false) || animation.getProperty(AnimationProperty.ActionAnimationProperty.COORD).isPresent();
        float dx = VEC4F_HOLDER_2.x - VEC4F_HOLDER_1.x;
        float dy = (moveVertical || hasNoGravity) ? VEC4F_HOLDER_1.y - VEC4F_HOLDER_2.y : 0.0F;
        float dz = VEC4F_HOLDER_2.z - VEC4F_HOLDER_1.z;
        dx = Math.abs(dx) > 0.0001F ? dx : 0.0F;
        dz = Math.abs(dz) > 0.0001F ? dz : 0.0F;


        BlockPos blockpos = new BlockPos(livingentity.getX(), livingentity.getBoundingBox().minY - 1.0D, livingentity.getZ());
        BlockState blockState = livingentity.level.getBlockState(blockpos);
        ModifiableAttributeInstance movementSpeed = livingentity.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean soulboost = blockState.is(BlockTags.SOUL_SPEED_BLOCKS) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, livingentity) > 0;
        float speedFactor = (float)(soulboost ? 1.0D : livingentity.level.getBlockState(blockpos).getBlock().getSpeedFactor());
        float moveMultiplier = (float)(animation.getProperty(AnimationProperty.ActionAnimationProperty.AFFECT_SPEED).orElse(false) ? (movementSpeed.getValue() / movementSpeed.getBaseValue()) : 1.0F);

        VEC3F_HOLDER.set(dx * moveMultiplier * speedFactor, dy, dz * moveMultiplier * speedFactor);
        
        return VEC3F_HOLDER;
    };

    public static final MoveCoordGetter WORLD_COORD = (animation, entitypatch, coord) -> {
        LivingEntity livingentity = entitypatch.getOriginal();
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
        JointTransform jt = coord.getInterpolatedTransform(player.getElapsedTime());

        VEC3F_HOLDER.set(jt.translation());
        VEC3F_HOLDER.sub(Vec3f.fromDoubleVector(livingentity.position()));
        return VEC3F_HOLDER;
    };

    public static final MoveCoordGetter ATTACHED = (animation, entitypatch, coord) -> {
        LivingEntity target = entitypatch.getGrapplingTarget();

        if (target == null) {
            return DIFF_FROM_PREV_COORD.get(animation, entitypatch, coord);
        }

        TransformSheet rootCoord = animation.getCoord();
        LivingEntity livingentity = entitypatch.getOriginal();
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
        Vec3f model = rootCoord.getInterpolatedTransform(player.getElapsedTime()).translation();
        OpenMatrix4f.transform3v(OpenMatrix4f.createRotatorDeg(-target.yRot, Vec3f.Y_AXIS), model, VEC3F_HOLDER);
        VEC3F_HOLDER.add(Vec3f.fromDoubleVector(target.position()));
        livingentity.yRot=(MathHelper.wrapDegrees(target.yRot + 180.0F));
        VEC3F_HOLDER.sub(Vec3f.fromDoubleVector(livingentity.position()));
        
        return VEC3F_HOLDER;
    };

    public static final MoveCoordSetter TRACE_DEST_LOCATION_BEGIN = (self, entitypatch, transformSheet) -> {
        LivingEntity attackTarget = entitypatch.getTarget();
        TRANSFORM_SHEET_HOLDER.readFrom(self.getCoord());
        Keyframe[] rootKeyframes = TRANSFORM_SHEET_HOLDER.getKeyframes();

        if (attackTarget != null && attackTarget.isAlive()) {
            Vector3d start = entitypatch.getOriginal().position();
            Vector3d toTarget = attackTarget.position().subtract(start);
            VEC3F_HOLDER_2.set(rootKeyframes[rootKeyframes.length - 1].transform().translation());
            VEC3F_HOLDER_2.multiply(-1.0F, 1.0F, -1.0F);
            float yRot = (float)MathUtils.getYRotOfVector(toTarget);

            VEC3F_HOLDER_2.rotate(-yRot, Vec3f.Y_AXIS);

            Vector3d dst = attackTarget.position().add(VEC3F_HOLDER_2.x, VEC3F_HOLDER_2.y, VEC3F_HOLDER_2.z);
            float clampedXRot = MathUtils.rotlerp(entitypatch.getOriginal().xRot, (float)MathUtils.getXRotOfVector(toTarget), 20.0F);
            float clampedYRot = MathUtils.rotlerp(entitypatch.getOriginal().yRot, yRot, entitypatch.getYRotLimit());
            TransformSheet newTransform = TRANSFORM_SHEET_HOLDER.getCorrectedWorldCoord(entitypatch, start, dst, -clampedXRot, clampedYRot, 0, rootKeyframes.length);

            transformSheet.readFrom(newTransform);
        } else {
            TRANSFORM_SHEET_HOLDER.transform((jt) -> {
                VEC3F_HOLDER_2.set(self.getCoord().getKeyframes()[0].transform().translation());
                jt.translation().sub(VEC3F_HOLDER_2);

                LivingEntity original = entitypatch.getOriginal();
                Vector3d pos = original.position();

                jt.translation().rotate(-original.yRot, Vec3f.Y_AXIS);
                jt.translation().multiply(-1.0F, 1.0F, -1.0F);
                VEC3F_HOLDER_3.set((float)pos.x, (float)pos.y, (float)pos.z);
                jt.translation().add(VEC3F_HOLDER_3);
            });

            transformSheet.readFrom(TRANSFORM_SHEET_HOLDER);
        }
    };

    public static final MoveCoordSetter TRACE_DEST_LOCATION = (self, entitypatch, transformSheet) -> {
        LivingEntity attackTarget = entitypatch.getTarget();

        if (attackTarget != null && attackTarget.isAlive()) {
            TRANSFORM_SHEET_HOLDER.readFrom(self.getCoord());
            Keyframe[] rootKeyframes = TRANSFORM_SHEET_HOLDER.getKeyframes();
            Vector3d start = entitypatch.getArmature().getActionAnimationCoord().getKeyframes()[0].transform().translation().toDoubleVector();
            Vector3d toTarget = attackTarget.position().subtract(start);
            VEC3F_HOLDER_2.set(rootKeyframes[rootKeyframes.length - 1].transform().translation());
            VEC3F_HOLDER_2.multiply(1.0F, 1.0F, -1.0F);
            float yRot = (float)MathUtils.getYRotOfVector(toTarget);

            VEC3F_HOLDER_2.rotate(-yRot, Vec3f.Y_AXIS);

            Vector3d dst = attackTarget.position().add(VEC3F_HOLDER_2.toDoubleVector());
            float clampedXRot = (float)MathUtils.getXRotOfVector(toTarget);
            float clampedYRot = MathUtils.rotlerp(entitypatch.getOriginal().yRot, yRot, entitypatch.getYRotLimit());
            TransformSheet newTransform = TRANSFORM_SHEET_HOLDER.getCorrectedWorldCoord(entitypatch, start, dst, -clampedXRot, clampedYRot, 0, rootKeyframes.length);

            entitypatch.getOriginal().yRot = clampedYRot;
            transformSheet.readFrom(newTransform);
        }
    };

    public static final MoveCoordSetter TRACE_LOC_TARGET = (self, entitypatch, transformSheet) -> {
        LivingEntity attackTarget = entitypatch.getTarget();

        if (attackTarget != null && !self.getRealAnimation().getProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE).orElse(false)) {
            TRANSFORM_SHEET_HOLDER.readFrom(self.getCoord());
            Keyframe[] keyframes = TRANSFORM_SHEET_HOLDER.getKeyframes();
            int startFrame = 0;
            int endFrame = keyframes.length - 1;
            Vec3f keyLast = keyframes[endFrame].transform().translation();
            Vector3d pos = entitypatch.getOriginal().position();
            Vector3d targetpos = attackTarget.position();
            Vector3d toTarget = targetpos.subtract(pos);
            Vector3d viewVec = entitypatch.getOriginal().getViewVector(1.0F);
            float horizontalDistance = Math.max((float) VectorUtils.horizontalDistance(toTarget) - (attackTarget.getBbWidth() + entitypatch.getOriginal().getBbWidth()) * 0.75F, 0.0F);
            VEC3F_HOLDER_2.set(keyLast.x, 0.0F, -horizontalDistance);
            float scale = Math.min(VEC3F_HOLDER_2.length() / keyLast.length(), 2.0F);

            if (scale > 1.0F) {
                float dot = (float)toTarget.normalize().dot(viewVec.normalize());
                scale = Math.max(scale * dot, 1.0F);
            }

            for (int i = startFrame; i <= endFrame; i++) {
                Vec3f translation = keyframes[i].transform().translation();

                if (translation.z < 0.0F) {
                    translation.z *= scale;
                }
            }

            transformSheet.readFrom(TRANSFORM_SHEET_HOLDER);
        } else {
            transformSheet.readFrom(self.getCoord());
        }
    };

    public static final MoveCoordSetter TRACE_LOCROT_TARGET = (self, entitypatch, transformSheet) -> {
        LivingEntity attackTarget = entitypatch.getTarget();

        if (attackTarget != null) {
            TRANSFORM_SHEET_HOLDER.readFrom(self.getCoord());
            Keyframe[] keyframes = TRANSFORM_SHEET_HOLDER.getKeyframes();
            int startFrame = 0;
            int endFrame = keyframes.length - 1;
            Vec3f keyLast = keyframes[endFrame].transform().translation();
            Vector3d pos = entitypatch.getOriginal().position();
            Vector3d targetpos = attackTarget.position();
            Vector3d toTarget = targetpos.subtract(pos);
            float horizontalDistance = Math.max((float)VectorUtils.horizontalDistance(toTarget) - (attackTarget.getBbWidth() + entitypatch.getOriginal().getBbWidth()) * 0.75F, 0.0F);
            VEC3F_HOLDER_2.set(keyLast.x, 0.0F, -horizontalDistance);
            float scale = Math.min(VEC3F_HOLDER_2.length() / keyLast.length(), 2.0F);
            float yRot = (float)MathUtils.getYRotOfVector(toTarget);
            float clampedYRot = MathUtils.rotlerp(entitypatch.getYRot(), yRot, entitypatch.getYRotLimit());
            entitypatch.setYRot(clampedYRot);

            for (int i = startFrame; i <= endFrame; i++) {
                Vec3f translation = keyframes[i].transform().translation();

                if (translation.z < 0.0F) {
                    translation.z *= scale;
                }
            }

            transformSheet.readFrom(TRANSFORM_SHEET_HOLDER);
        } else {
            transformSheet.readFrom(self.getCoord());
        }
    };

    public static final MoveCoordSetter RAW_COORD = (self, entitypatch, transformSheet) -> {
        transformSheet.readFrom(self.getCoord());
    };

    public static final MoveCoordSetter RAW_COORD_WITH_X_ROT = (self, entitypatch, transformSheet) -> {
        float xRot = entitypatch.getOriginal().xRot;
        TRANSFORM_SHEET_HOLDER.readFrom(self.getCoord());

        for (Keyframe kf : TRANSFORM_SHEET_HOLDER.getKeyframes()) {
            kf.transform().translation().rotate(-xRot, Vec3f.X_AXIS);
        }

        transformSheet.readFrom(TRANSFORM_SHEET_HOLDER);
    };

    public static final MoveCoordSetter VEX_TRACE = (self, entitypatch, transformSheet) -> {
        TRANSFORM_SHEET_HOLDER.readFrom(self.getCoord());
        Keyframe[] keyframes = TRANSFORM_SHEET_HOLDER.getKeyframes();
        int startFrame = 0;
        int endFrame = 6;
        Vector3d pos = entitypatch.getOriginal().position();
        Vector3d targetpos = entitypatch.getTarget().position();
        float verticalDistance = (float) (targetpos.y - pos.y);
        VEC3F_HOLDER_2.set(0.0F, -verticalDistance, (float)VectorUtils.horizontalDistance(targetpos.subtract(pos)));
        VEC3F_HOLDER_3.set(0.0F, 0.0F, 1.0F);
        Quaternionf rotator = Vec3f.getRotatorBetween(VEC3F_HOLDER_2, VEC3F_HOLDER_3);

        for (int i = startFrame; i <= endFrame; i++) {
            keyframes[i].transform().rotation().mul(rotator);
        }

        transformSheet.readFrom(TRANSFORM_SHEET_HOLDER);
    };
}