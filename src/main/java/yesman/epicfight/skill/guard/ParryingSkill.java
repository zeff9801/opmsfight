package yesman.epicfight.skill.guard;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import javax.annotation.Nullable;
import java.util.List;

public class ParryingSkill extends GuardSkill {
    private static final int PARRY_WINDOW = 8;

    public static GuardSkill.Builder createActiveGuardBuilder() {
        return GuardSkill.createGuardBuilder()
                .addAdvancedGuardMotion(WeaponCategories.SWORD, (itemCap, playerpatch) -> itemCap.getStyle(playerpatch) == Styles.ONE_HAND ?
                        new StaticAnimation[] { Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2 } :
                        new StaticAnimation[] { Animations.SWORD_GUARD_ACTIVE_HIT2, Animations.SWORD_GUARD_ACTIVE_HIT3 })
                .addAdvancedGuardMotion(WeaponCategories.LONGSWORD, (itemCap, playerpatch) ->
                        new StaticAnimation[] { Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2 })
                .addAdvancedGuardMotion(WeaponCategories.KATANA, (itemCap, playerpatch) ->
                        new StaticAnimation[] { Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2 })
                .addAdvancedGuardMotion(WeaponCategories.TACHI, (itemCap, playerpatch) ->
                        new StaticAnimation[] { Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2 });
    }

    public ParryingSkill(GuardSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecuter().getEventListener().addEventListener(EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID, (event) -> {
            CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(Hand.MAIN_HAND);

            if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, BlockType.GUARD) && this.isExecutableState(event.getPlayerPatch())) {
                event.getPlayerPatch().getOriginal().startUsingItem(Hand.MAIN_HAND);
            }

            int lastActive = container.getDataManager().getDataValue(SkillDataKeys.LAST_ACTIVE.get());

            if (event.getPlayerPatch().getOriginal().tickCount - lastActive > PARRY_WINDOW * 2) {
                container.getDataManager().setData(SkillDataKeys.LAST_ACTIVE.get(), event.getPlayerPatch().getOriginal().tickCount);
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
    }

    @Override
    public void guard(SkillContainer container, CapabilityItem itemCapability, HurtEvent.Pre event, float knockback, float impact, boolean advanced) {
        if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, BlockType.ADVANCED_GUARD)) {
            DamageSource damageSource = event.getDamageSource();

            if (this.isBlockableSource(damageSource, true)) {
                ServerPlayerEntity playerentity = event.getPlayerPatch().getOriginal();
                boolean successParrying = playerentity.tickCount - container.getDataManager().getDataValue(SkillDataKeys.LAST_ACTIVE.get()) < PARRY_WINDOW;
                float penalty = container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get());
                event.getPlayerPatch().playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(((ServerWorld)playerentity.level), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, playerentity, damageSource.getDirectEntity());

                if (successParrying) {
                    event.setParried(true);
                    penalty = 0.1F;
                    knockback *= 0.4F;

                    // Solution by Cyber2049(github): Fix continuous parry
                    container.getDataManager().setData(SkillDataKeys.LAST_ACTIVE.get(), 0);
                } else {
                    penalty += this.getPenalizer(itemCapability);
                    container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), penalty, playerentity);
                }

                if (damageSource.getDirectEntity() instanceof LivingEntity livingentity) {
                    knockback += EnchantmentHelper.getKnockbackBonus(livingentity) * 0.1F;
                }

                event.getPlayerPatch().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                float consumeAmount = penalty * impact;
                boolean canAfford = event.getPlayerPatch().consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);

                BlockType blockType = successParrying ? BlockType.ADVANCED_GUARD : (canAfford ? BlockType.GUARD : BlockType.GUARD_BREAK);
                StaticAnimation animation = this.getGuardMotion(event.getPlayerPatch(), itemCapability, blockType);

                if (animation != null) {
                    event.getPlayerPatch().playAnimationSynchronized(animation, 0);
                }

                if (blockType == BlockType.GUARD_BREAK) {
                    event.getPlayerPatch().playSound(EpicFightSounds.NEUTRALIZE_MOBS, 3.0F, 0.0F, 0.1F);
                }

                this.dealEvent(event.getPlayerPatch(), event, advanced);

                return;
            }
        }

        super.guard(container, itemCapability, event, knockback, impact, false);
    }

    @Override
    protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
        return (damageSource.isProjectile() && advanced) || super.isBlockableSource(damageSource, false);
    }

    @Nullable
    protected StaticAnimation getGuardMotion(PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
        StaticAnimation animation = itemCapability.getGuardMotion(this, blockType, playerpatch);

        if (animation != null) {
            return animation;
        }

        if (blockType == BlockType.ADVANCED_GUARD) {
            StaticAnimation[] motions = (StaticAnimation[])this.getGuradMotionMap(blockType).getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null).apply(itemCapability, playerpatch);

            if (motions != null) {
                SkillDataManager dataManager = playerpatch.getSkill(this).getDataManager();
                int motionCounter = dataManager.getDataValue(SkillDataKeys.PARRY_MOTION_COUNTER.get());
                dataManager.setDataF(SkillDataKeys.PARRY_MOTION_COUNTER.get(), (v) -> v + 1);
                motionCounter %= motions.length;

                return motions[motionCounter];
            }
        }

        return super.getGuardMotion(playerpatch, itemCapability, blockType);
    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.GUARD;
    }

    @Override
    protected boolean isAdvancedGuard() {
        return true;
    }

    @Override
    public List<WeaponCategory> getAvailableWeaponCategories() {
        return List.copyOf(this.advancedGuardMotions.keySet());
    }
}