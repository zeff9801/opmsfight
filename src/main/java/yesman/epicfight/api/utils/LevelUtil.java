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

    /**
     * Slam code
     */
    private static final Vector3d IMPACT_DIRECTION = new Vector3d(0.0D, -1.0D, 0.0D);

//    public static void spreadShockwave(Level level, Vec3 center, Vec3 direction, double length, int edgeX, int edgeZ, List<Entity> entityBeingHit) {
//        Vec3 edgeOfShockwave = center.add(direction.normalize().scale((float)length));
//        int xFrom = (int)Math.min(Math.floor(center.x), edgeX);
//        int xTo = (int)Math.max(Math.floor(center.x), edgeX);
//        int zFrom = (int)Math.min(Math.floor(center.z), edgeZ);
//        int zTo = (int)Math.max(Math.floor(center.z), edgeZ);
//        List<Vec2i> affectedBlocks = Lists.newArrayList();
//        List<Entity> entitiesInArea = level.isClientSide ? null : level.getEntities(null, new AABB(xFrom, center.y - length, zFrom, xTo, center.y + length, zTo));
//
//        double bounceExponentCoef = Math.min(1.0D / (length * length), 0.1D);
//
//        for (int k = zFrom; k <= zTo; k++) {
//            for (int l = xFrom; l <= xTo; l++) {
//                Vec2i blockCoord = new Vec2i(l, k);
//
//                if (isBlockOverlapLine(blockCoord, center, edgeOfShockwave)) {
//                    affectedBlocks.add(blockCoord);
//                }
//            }
//        }
//
//        affectedBlocks.sort((v1, v2) -> {
//            double v1DistSqr = Math.pow(v1.x - center.x, 2) + Math.pow(v1.y - center.z, 2);
//            double v2DistSqr = Math.pow(v2.x - center.x, 2) + Math.pow(v2.y - center.z, 2);
//
//            if (v1DistSqr > v2DistSqr) {
//                return 1;
//            } else if (v1DistSqr == v2DistSqr) {
//                return 0;
//            } else {
//                return -1;
//            }
//        });
//
//        double y = center.y;
//
//        for (Vec2i block : affectedBlocks) {
//            BlockPos bp = new BlockPos.MutableBlockPos(block.x, y, block.y);
//            BlockState bs = level.getBlockState(bp);
//            BlockPos aboveBp = bp.above();
//            BlockState aboveState = level.getBlockState(aboveBp);
//
//            if (canTransferShockWave(level, aboveBp, aboveState)) {
//                BlockPos aboveTwoBp = aboveBp.above();
//                BlockState aboveTwoState = level.getBlockState(aboveTwoBp);
//
//                if (!canTransferShockWave(level, aboveTwoBp, aboveTwoState)) {
//                    y++;
//                    bp = aboveBp;
//                    bs = aboveState;
//                } else {
//                    break;
//                }
//            } else {
//                if (!level.isClientSide && aboveState.getCollisionShape(level, aboveBp, CollisionContext.empty()).isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
//                    level.destroyBlock(aboveBp, level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS));
//                }
//            }
//
//            if (!canTransferShockWave(level, bp, bs)) {
//                BlockPos belowBp = bp.below();
//                BlockState belowState = level.getBlockState(belowBp);
//
//                if (canTransferShockWave(level, belowBp, belowState)) {
//                    y--;
//                    bp = belowBp;
//                    bs = belowState;
//                } else {
//                    break;
//                }
//            }
//
//            Vec3 blockCenter = new Vec3(bp.getX() + 0.5D, bp.getY(), bp.getZ() + 0.5D);
//            Vec3 centerToBlock = blockCenter.subtract(center);
//            double distance = centerToBlock.horizontalDistance();
//
//            if (length < distance) {
//                continue;
//            }
//
//            if (level.isClientSide) {
//                if (!canTransferShockWave(level, bp, bs) || bs instanceof FractureBlockState) {
//                    continue;
//                }
//
//                Vec3 rotAxis = IMPACT_DIRECTION.cross(centerToBlock).normalize();
//                Vector3f axis = new Vector3f((float)rotAxis.x, (float)rotAxis.y, (float)rotAxis.z);
//                Vector3f translator = new Vector3f(0, Math.max(0.0F, (float)(distance / length) - 0.5F) * 0.5F, 0);
//                Quaternionf rotator = QuaternionUtils.rotationDegrees(axis, (float)(distance / length) * 15.0F + level.random.nextFloat() * 10.0F - 5.0F);
//
//                rotator.mul(QuaternionUtils.XP.rotationDegrees(level.random.nextFloat() * 15.0F - 7.5F));
//                rotator.mul(QuaternionUtils.YP.rotationDegrees(level.random.nextFloat() * 40.0F - 20.0F));
//                rotator.mul(QuaternionUtils.ZP.rotationDegrees(level.random.nextFloat() * 15.0F - 7.5F));
//                int lifeTime = 30 + level.random.nextInt((int)length * 80);
//                double bouncing = Math.pow(distance, 2) * bounceExponentCoef;
//
//                FractureBlockState fractureBlockState = FractureBlock.getDefaultFractureBlockState(null);
//                fractureBlockState.setFractureInfo(bp, bs, translator, rotator, bouncing, lifeTime);
//
//                level.setBlock(bp, fractureBlockState, 0);
//
//                createParticle(level, bp, bs);
//            } else {
//                for (Entity entity : entitiesInArea) {
//                    boolean inSameY = bp.getY() + 1 >= entity.getY() && bp.getY() <= entity.getY();
//
//                    if (bp.getX() == entity.getBlockX() && inSameY && bp.getZ() == entity.getBlockZ()) {
//                        if (!entityBeingHit.contains(entity)) {
//                            entityBeingHit.add(entity);
//                        }
//                    }
//                }
//            }
//        }
//    }

//    @OnlyIn(Dist.CLIENT)
//    public static void createParticle(Level level, BlockPos bp, BlockState bs) {
//        for (int i = 0; i < 4; i += level.getRandom().nextInt(4)) {
//            double x = bp.getX() + (i % 2);
//            double z = bp.getZ() + 1 - (i % 2);
//
//            TerrainParticle blockParticle = new TerrainParticle((ClientLevel)level, x, bp.getY() + 1, z, 0, 0, 0, bs, bp);
//            blockParticle.setParticleSpeed((Math.random() - 0.5D) * 0.3D, Math.random() * 0.5D, (Math.random() - 0.5D) * 0.3D);
//            blockParticle.setLifetime(10 + new Random().nextInt(60));
//
//            Minecraft mc = Minecraft.getInstance();
//            mc.particleEngine.add(blockParticle);
//        }
//    }

//    public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius) {
//        return circleSlamFracture(caster, level, center, radius, false, false, true);
//    }
//
//    public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius, boolean hurtEntities) {
//        return circleSlamFracture(caster, level, center, radius, false, false, hurtEntities);
//    }
//
//    public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius, boolean noSound, boolean noParticle) {
//        return circleSlamFracture(caster, level, center, radius, noSound, noParticle, true);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public static boolean circleSlamFracture(@Nullable LivingEntity caster, ClientLevel level, Vec3 center, double radius, boolean noSound, boolean noParticle) {
//        return circleSlamFracture(caster, level, center, radius, noSound, noParticle, true);
//    }
//
//    public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius, boolean noSound, boolean noParticle, boolean hurtEntities) {
//        Vec3 closestEdge = new Vec3(Math.round(center.x), Math.floor(center.y), Math.round(center.z));
//        Vec3 centerOfBlock = new Vec3(Math.floor(center.x) + 0.5D, Math.floor(center.y), Math.floor(center.z) + 0.5D);
//
//        if (closestEdge.distanceToSqr(center) < centerOfBlock.distanceToSqr(center)) {
//            center = closestEdge;
//        } else {
//            center = centerOfBlock;
//        }
//
//        BlockPos blockPos = new BlockPos.MutableBlockPos(center.x, center.y, center.z);
//        BlockState originBlockState = level.getBlockState(blockPos);
//
//        if (!canTransferShockWave(level, blockPos, originBlockState)) {
//            return false;
//        }
//
//        radius = Math.max(0.5F, radius);
//
//        if (!level.isClientSide) {
//            EpicFightNetworkManager.sendToAllPlayerTrackingThisChunkWithSelf(new SPFracture(center, radius, noSound, noParticle), level.getChunkAt(blockPos));
//        }
//
//        int xFrom = (int)Math.floor(center.x - radius);
//        int xTo = (int)Math.ceil(center.x + radius);
//        int zFrom = (int)Math.floor(center.z - radius);
//        int zTo = (int)Math.ceil(center.z + radius);
//        List<Entity> entityBeingHit = Lists.newArrayList();
//
//        for (int i = zFrom; i <= zTo; i++) {
//            for (int j = xFrom; j <= xTo; j += (i == zFrom || i == zTo) ? 1 : xTo - xFrom) {
//                Vec3 direction = new Vec3(j - center.x + 0.1D, 0.0D, i - center.z);
//                spreadShockwave(level, center, direction, radius, j, i, entityBeingHit);
//            }
//        }
//
//        if (!level.isClientSide && hurtEntities) {
//            for (Entity entity : entityBeingHit) {
//                if (!entity.is(caster)) {
//                    double damageInflict = 1.0D - ((entity.position().distanceTo(center) - radius * 0.5D) / radius);
//                    float damage = (float)(radius * 2.0D * Math.min(damageInflict, 1.0D));
//                    EpicFightDamageSources damageSources = EpicFightDamageSources.of(entity.level());
//                    entity.hurt(damageSources.shockwave(caster)
//                                    .setAnimation(Animations.DUMMY_ANIMATION)
//                                    .setInitialPosition(center)
//                                    .addRuntimeTag(EpicFightDamageType.FINISHER)
//                                    .setStunType(StunType.KNOCKDOWN)
//                                    .addRuntimeTag(DamageTypes.EXPLOSION)
//                            ,damage);
//                }
//            }
//        } else {
//            boolean smallSlam = (radius < 1.5D);
//
//            if (!noSound) {
//                level.playLocalSound(center.x, center.y, center.z, smallSlam ? EpicFightSounds.GROUND_SLAM_SMALL.get() : EpicFightSounds.GROUND_SLAM.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
//            }
//
//            if (!smallSlam && !noParticle) {
//                level.addParticle(EpicFightParticles.GROUND_SLAM.get(), center.x, center.y, center.z, 1.0D, radius * 10.0D, 0.5D);
//            }
//        }
//
//        return true;
//    }

    //TODO Implement FractureBlock
//    public static boolean canTransferShockWave(World level, BlockPos blockPos, BlockState blockState) {
//        return Block.isFaceFull(blockState.getCollisionShape(level, blockPos, ISelectionContext.empty()), Direction.DOWN) || (blockState instanceof FractureBlockState);
//    }

    private static boolean isBlockOverlapLine(Vec2i vec2, Vector3d from, Vector3d to) {
        return isLinesCross(vec2.x, vec2.y, vec2.x + 1, vec2.y, from.x, from.z, to.x, to.z)
                || isLinesCross(vec2.x, vec2.y, vec2.x, vec2.y + 1, from.x, from.z, to.x, to.z)
                || isLinesCross(vec2.x + 1, vec2.y, vec2.x + 1, vec2.y + 1, from.x, from.z, to.x, to.z)
                || isLinesCross(vec2.x, vec2.y + 1, vec2.x + 1, vec2.y + 1, from.x, from.z, to.x, to.z);
    }

    private static boolean isLinesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double u = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1));
        double v = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1));

        return 0 < u && u < 1 && 0 < v && v < 1;
    }

}
