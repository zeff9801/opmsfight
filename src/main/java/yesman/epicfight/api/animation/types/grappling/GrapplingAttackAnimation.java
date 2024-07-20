package yesman.epicfight.api.animation.types.grappling;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public class GrapplingAttackAnimation extends AttackAnimation {
    public GrapplingAttackAnimation(float contact, float recovery, String path, Armature armature) {
        this(contact, recovery, Hand.MAIN_HAND, path, armature);
    }

    public GrapplingAttackAnimation(float contact, float recovery, Hand hand, String path, Armature armature) {
        super(0.0F, 0.0F, contact, contact, recovery, hand, null, armature.rootJoint, path, armature);

        this.addProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F);
        this.addProperty(ActionAnimationProperty.MOVE_ON_LINK, false);
        this.addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD);
        this.addProperty(ActionAnimationProperty.COORD_SET_TICK, null);
        this.addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.ATTACHED);
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        if (entitypatch.shouldMoveOnCurrentSide(this)) {
            Keyframe[] grapplingAnimCoord = entitypatch.getArmature().getActionAnimationCoord().getKeyframes();
            Vec3f translation = grapplingAnimCoord[grapplingAnimCoord.length - 1].transform().translation();
            entitypatch.getOriginal().setDeltaMovement(0.0D, 0.0D, 0.0D);

            Vector3d doubleVector = translation.toDoubleVector();
            entitypatch.getOriginal().setPos(doubleVector.x, doubleVector.y, doubleVector.z);
        }

        super.begin(entitypatch);
    }

    @Override
    public void end(LivingEntityPatch<?> entitypatch, DynamicAnimation nextAnimation, boolean isEnd) {
        super.end(entitypatch, nextAnimation, isEnd);
        entitypatch.setGrapplingTarget(null);
    }

    @Override
    protected void attackTick(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
        AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
        float elapsedTime = player.getElapsedTime();
        float prevElapsedTime = player.getPrevElapsedTime();
        EntityState state = this.getState(entitypatch, animation, elapsedTime);
        EntityState prevState = this.getState(entitypatch, animation, prevElapsedTime);
        Phase phase = this.getPhaseByTime(elapsedTime);

        if (prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2)) {
            LivingEntity grapplingTarget = entitypatch.getGrapplingTarget();

            if (grapplingTarget != null) {
                EpicFightDamageSource source = this.getEpicFightDamageSource(entitypatch, grapplingTarget, phase);
                source.setInitialPosition(null);
                int prevInvulTime = grapplingTarget.invulnerableTime;
                grapplingTarget.invulnerableTime = 0;
                entitypatch.attack(source, grapplingTarget, Hand.MAIN_HAND);
                grapplingTarget.invulnerableTime = prevInvulTime;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDebugging(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
    }
}