package yesman.epicfight.client.particle;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.client.model.ItemSkin;
import yesman.epicfight.api.client.model.ItemSkins;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class AnimationTrailParticle extends AbstractTrailParticle<LivingEntityPatch<?>> {
    private final Joint joint;
    private final StaticAnimation animation;
    private final List<TrailEdge> invisibleTrailEdges;
    private final IAnimatedSprite sprites;

    protected AnimationTrailParticle(ClientWorld level, LivingEntityPatch<?> entitypatch, Joint joint, StaticAnimation animation, TrailInfo trailInfo, IAnimatedSprite sprite) {
        super(level, entitypatch, trailInfo);
        this.joint = joint;
        this.animation = animation;
        this.invisibleTrailEdges = Lists.newLinkedList();
        this.sprites = sprite;
        this.setSpriteFromAge(sprite);

        Pose prevPose = this.owner.getAnimator().getPose(0.0F);
        Pose middlePose = this.owner.getAnimator().getPose(0.5F);
        Pose currentPose = this.owner.getAnimator().getPose(1.0F);
        Vector3d posOld = this.owner.getOriginal().getPosition(0.0F);
        Vector3d posMid = this.owner.getOriginal().getPosition(0.5F);
        Vector3d posCur = this.owner.getOriginal().getPosition(1.0F);

        OpenMatrix4f prvmodelTf = OpenMatrix4f.createTranslation((float)posOld.x, (float)posOld.y, (float)posOld.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(this.owner.getModelMatrix(0.0F)));
        OpenMatrix4f middleModelTf = OpenMatrix4f.createTranslation((float)posMid.x, (float)posMid.y, (float)posMid.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(this.owner.getModelMatrix(0.5F)));
        OpenMatrix4f curModelTf = OpenMatrix4f.createTranslation((float)posCur.x, (float)posCur.y, (float)posCur.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(this.owner.getModelMatrix(1.0F)));

        OpenMatrix4f prevJointTf = this.owner.getArmature().getBindedTransformFor(prevPose, this.joint).mulFront(prvmodelTf);
        OpenMatrix4f middleJointTf = this.owner.getArmature().getBindedTransformFor(middlePose, this.joint).mulFront(middleModelTf);
        OpenMatrix4f currentJointTf = this.owner.getArmature().getBindedTransformFor(currentPose, this.joint).mulFront(curModelTf);

        Vector3d prevStartPos = OpenMatrix4f.transform(prevJointTf, trailInfo.start);
        Vector3d prevEndPos = OpenMatrix4f.transform(prevJointTf, trailInfo.end);
        Vector3d middleStartPos = OpenMatrix4f.transform(middleJointTf, trailInfo.start);
        Vector3d middleEndPos = OpenMatrix4f.transform(middleJointTf, trailInfo.end);
        Vector3d currentStartPos = OpenMatrix4f.transform(currentJointTf, trailInfo.start);
        Vector3d currentEndPos = OpenMatrix4f.transform(currentJointTf, trailInfo.end);

        this.invisibleTrailEdges.add(new TrailEdge(prevStartPos, prevEndPos, this.trailInfo.trailLifetime));
        this.invisibleTrailEdges.add(new TrailEdge(middleStartPos, middleEndPos, this.trailInfo.trailLifetime));
        this.invisibleTrailEdges.add(new TrailEdge(currentStartPos, currentEndPos, this.trailInfo.trailLifetime));
    }

    @Override
    protected boolean canContinue() {
        AnimationPlayer animPlayer = this.owner.getAnimator().getPlayerFor(this.animation);
        return this.owner.getOriginal().isAlive() && this.animation == animPlayer.getAnimation().getRealAnimation() && animPlayer.getElapsedTime() <= this.trailInfo.endTime;
    }

    @Override
    protected void createNextCurve() {
        AnimationPlayer animPlayer = this.owner.getAnimator().getPlayerFor(this.animation);
        boolean isTrailInvisible = animPlayer.getAnimation().isLinkAnimation() || animPlayer.getElapsedTime() <= this.trailInfo.startTime;
        boolean isFirstTrail = this.trailEdges.isEmpty();
        boolean needCorrection = (!isTrailInvisible && isFirstTrail);

        if (needCorrection) {
            float startCorrection = Math.max((this.trailInfo.startTime - animPlayer.getPrevElapsedTime()) / (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()), 0.0F);
            this.startEdgeCorrection = this.trailInfo.interpolateCount * 2 * startCorrection;
        }

        TrailInfo trailInfo = this.trailInfo;
        Pose prevPose = this.owner.getAnimator().getPose(0.0F);
        Pose currentPose = this.owner.getAnimator().getPose(1.0F);
        Pose middlePose = this.owner.getAnimator().getPose(0.5F);

        Vector3d posOld = this.owner.getOriginal().getPosition(0.0F);
        Vector3d posCur = this.owner.getOriginal().getPosition(1.0F);
        Vector3d posMid = MathUtils.lerpVector(posOld, posCur, 0.5F);

        OpenMatrix4f prevModelMatrix = this.owner.getModelMatrix(0.0F);
        OpenMatrix4f curModelMatrix = this.owner.getModelMatrix(1.0F);
        JointTransform lastTransform = JointTransform.fromMatrix(curModelMatrix);
        JointTransform currentTransform = JointTransform.fromMatrix(curModelMatrix);

        OpenMatrix4f prvmodelTf = OpenMatrix4f
                .createTranslation((float)posOld.x, (float)posOld.y, (float)posOld.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(prevModelMatrix));
        OpenMatrix4f middleModelTf = OpenMatrix4f
                .createTranslation((float)posMid.x, (float)posMid.y, (float)posMid.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(JointTransform.interpolate(lastTransform, currentTransform, 0.5F).toMatrix()));
        OpenMatrix4f curModelTf = OpenMatrix4f
                .createTranslation((float)posCur.x, (float)posCur.y, (float)posCur.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS)
                        .mulBack(curModelMatrix));

        OpenMatrix4f prevJointTf = this.owner.getArmature().getBindedTransformFor(prevPose, this.joint).mulFront(prvmodelTf);
        OpenMatrix4f middleJointTf = this.owner.getArmature().getBindedTransformFor(middlePose, this.joint).mulFront(middleModelTf);
        OpenMatrix4f currentJointTf = this.owner.getArmature().getBindedTransformFor(currentPose, this.joint).mulFront(curModelTf);
        Vector3d prevStartPos = OpenMatrix4f.transform(prevJointTf, trailInfo.start);
        Vector3d prevEndPos = OpenMatrix4f.transform(prevJointTf, trailInfo.end);
        Vector3d middleStartPos = OpenMatrix4f.transform(middleJointTf, trailInfo.start);
        Vector3d middleEndPos = OpenMatrix4f.transform(middleJointTf, trailInfo.end);
        Vector3d currentStartPos = OpenMatrix4f.transform(currentJointTf, trailInfo.start);
        Vector3d currentEndPos = OpenMatrix4f.transform(currentJointTf, trailInfo.end);

        List<Vector3d> finalStartPositions;
        List<Vector3d> finalEndPositions;
        boolean visibleTrail;

        if (isTrailInvisible) {
            finalStartPositions = Lists.newArrayList();
            finalEndPositions = Lists.newArrayList();
            finalStartPositions.add(prevStartPos);
            finalStartPositions.add(middleStartPos);
            finalEndPositions.add(prevEndPos);
            finalEndPositions.add(middleEndPos);

            this.invisibleTrailEdges.clear();
            visibleTrail = false;
        } else {
            List<Vector3d> startPosList = Lists.newArrayList();
            List<Vector3d> endPosList = Lists.newArrayList();
            TrailEdge edge1;
            TrailEdge edge2;

            if (isFirstTrail) {
                int lastIdx = this.invisibleTrailEdges.size() - 1;
                edge1 = this.invisibleTrailEdges.get(lastIdx);
                edge2 = new TrailEdge(prevStartPos, prevEndPos, -1);
            } else {
                edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount / 2 + 1));
                edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
                edge2.lifetime++;
            }

            startPosList.add(edge1.start);
            endPosList.add(edge1.end);
            startPosList.add(edge2.start);
            endPosList.add(edge2.end);
            startPosList.add(middleStartPos);
            endPosList.add(middleEndPos);
            startPosList.add(currentStartPos);
            endPosList.add(currentEndPos);

            finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount);
            finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount);

            if (!isFirstTrail) {
                finalStartPositions.remove(0);
                finalEndPositions.remove(0);
            }

            visibleTrail = true;
        }

        this.makeTrailEdges(finalStartPositions, finalEndPositions, visibleTrail ? this.trailEdges : this.invisibleTrailEdges);
    }

    /**
     * A trimmed provider adapted from 1.21 that understands animation/joint encoded in the particle parameters.
     */
    @OnlyIn(Dist.CLIENT)
    public static class Provider implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;

        public Provider(IAnimatedSprite sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            long eid = Double.doubleToRawLongBits(x);
            long animid = Double.doubleToRawLongBits(z);
            long jointId = Double.doubleToRawLongBits(xSpeed);
            long index = Double.doubleToRawLongBits(ySpeed);

            Entity entity = level.getEntity((int)eid);
            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

            if (entitypatch == null) {
                EpicFightMod.LOGGER.debug("[Trail] Missing entity patch for eid {}", eid);
                return null;
            }

            StaticAnimation animation = AnimationManager.getInstance().byId((int)animid);

            if (animation == null) {
                EpicFightMod.LOGGER.debug("[Trail] Missing animation id {}", animid);
                return null;
            }

            Optional<List<TrailInfo>> trailInfos = animation.getProperty(ClientAnimationProperties.TRAIL_EFFECT);

            if (trailInfos.isPresent()) {
                TrailInfo base = trailInfos.get().get((int)index);
                TrailInfo result = base.copy().build();

                if (result.hand != null) {
                    ItemStack stack = entitypatch.getOriginal().getItemInHand(result.hand);
                    ItemSkin itemSkin = ItemSkins.getItemSkin(stack.getItem());
                    if (itemSkin != null) {
                        itemSkin.trailInfo.copy().build(result);
                    }
                }

                if (!result.playable()) {
                    EpicFightMod.LOGGER.debug("[Trail] Trail info not playable for animation {} index {}", animid, index);
                    return null;
                }

                Armature armature = animation.getArmature();
                Joint joint = armature.searchJointById((int)jointId);
                AnimationTrailParticle particle = new AnimationTrailParticle(level, entitypatch, joint, animation, result, this.sprites);
                return particle;
            }

            EpicFightMod.LOGGER.debug("[Trail] No trail effects for animation {}", animid);
            return null;
        }
    }
}
