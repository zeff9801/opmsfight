package yesman.epicfight.skill.guard;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.DamageSourceHelper;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class GuardSkill extends Skill {
    protected static final UUID EVENT_UUID = UUID.fromString("b422f7a0-f378-11eb-9a03-0242ac130003");

    public static class Builder extends Skill.Builder<GuardSkill> {
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions = Maps.newHashMap();
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> advancedGuardMotions = Maps.newHashMap();
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardBreakMotions = Maps.newHashMap();

        public Builder setCategory(SkillCategory category) {
            this.category = category;
            return this;
        }

        public Builder setActivateType(ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public Builder addGuardMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation> function) {
            this.guardMotions.put(weaponCategory, function);
            return this;
        }

        public Builder addAdvancedGuardMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?> function) {
            this.advancedGuardMotions.put(weaponCategory, function);
            return this;
        }

        public Builder addGuardBreakMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation> function) {
            this.guardBreakMotions.put(weaponCategory, function);
            return this;
        }
    }

    public static GuardSkill.Builder createGuardBuilder() {
        return (new GuardSkill.Builder())
                .setCategory(SkillCategories.GUARD)
                .setActivateType(ActivateType.ONE_SHOT)
                .setResource(Resource.STAMINA)
                .addGuardMotion(WeaponCategories.AXE, (item, player) -> Animations.SWORD_GUARD_HIT)
                .addGuardMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_HIT)
                .addGuardMotion(WeaponCategories.KATANA, (item, player) -> Animations.UCHIGATANA_GUARD_HIT)
                .addGuardMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
                .addGuardMotion(WeaponCategories.SPEAR, (item, player) -> item.getStyle(player) == Styles.TWO_HAND ? Animations.SPEAR_GUARD_HIT : null)
                .addGuardMotion(WeaponCategories.SWORD, (item, player) -> item.getStyle(player) == Styles.ONE_HAND ? Animations.SWORD_GUARD_HIT : Animations.SWORD_DUAL_GUARD_HIT)
                .addGuardMotion(WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
                .addGuardBreakMotion(WeaponCategories.AXE, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_BREAK)
                .addGuardBreakMotion(WeaponCategories.KATANA, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(WeaponCategories.SPEAR, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(WeaponCategories.SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
                .addGuardBreakMotion(WeaponCategories.TACHI, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED);
    }

    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions;
    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> advancedGuardMotions;
    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardBreakMotions;

    protected float penalizer;

    public GuardSkill(GuardSkill.Builder builder) {
        super(builder);
        this.guardMotions = builder.guardMotions;
        this.advancedGuardMotions = builder.advancedGuardMotions;
        this.guardBreakMotions = builder.guardBreakMotions;
    }

    @Override
    public void setParams(CompoundNBT parameters) {
        super.setParams(parameters);
        this.penalizer = parameters.getFloat("penalizer");
    }

    @Override
    public void onInitiate(SkillContainer container) {
        container.getExecuter().getEventListener().addEventListener(EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID, (event) -> {
            CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(Hand.MAIN_HAND);

            if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, BlockType.GUARD) && this.isExecutableState(event.getPlayerPatch())) {
                event.getPlayerPatch().getOriginal().startUsingItem(Hand.MAIN_HAND);
            }
        });

        container.getExecuter().getEventListener().addEventListener(EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID, (event) -> {
            CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(Hand.MAIN_HAND);

            if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, BlockType.GUARD) && this.isExecutableState(event.getPlayerPatch())) {
                event.getPlayerPatch().getOriginal().startUsingItem(Hand.MAIN_HAND);
            }
        });

        container.getExecuter().getEventListener().addEventListener(EventType.SERVER_ITEM_STOP_EVENT, EVENT_UUID, (event) -> {
            ServerPlayerEntity serverplayer = event.getPlayerPatch().getOriginal();
            container.getDataManager().setDataSync(SkillDataKeys.PENALTY_RESTORE_COUNTER.get(), serverplayer.tickCount, serverplayer);
        });

        container.getExecuter().getEventListener().addEventListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
            container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), 0.0F, event.getPlayerPatch().getOriginal());
        });

        container.getExecuter().getEventListener().addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            if (container.getExecuter().getOriginal().isUsingItem() && this.guardMotions.containsKey(container.getExecuter().getHoldingItemCapability(Hand.MAIN_HAND).getWeaponCategory())) {
                ClientPlayerEntity clientPlayer = event.getPlayerPatch().getOriginal();
                clientPlayer.setSprinting(false);
                clientPlayer.sprintTriggerTime = -1;
                Minecraft mc = Minecraft.getInstance();
                ClientEngine.getInstance().controllEngine.setKeyBind(mc.options.keySprint, false);
            }
        });

        container.getExecuter().getEventListener().addEventListener(EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(event.getPlayerPatch().getOriginal().getUsedItemHand());

            if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, BlockType.GUARD) && event.getPlayerPatch().getOriginal().isUsingItem() && this.isExecutableState(event.getPlayerPatch())) {
                DamageSource damageSource = event.getDamageSource();
                boolean isFront = false;
                Vector3d sourceLocation = damageSource.getSourcePosition();

                if (sourceLocation != null) {
                    Vector3d viewVector = event.getPlayerPatch().getOriginal().getViewVector(1.0F);
                    viewVector = viewVector.subtract(0, viewVector.y, 0).normalize();

                    Vector3d toSourceLocation = sourceLocation.subtract(event.getPlayerPatch().getOriginal().position()).normalize();

                    if (toSourceLocation.dot(viewVector) > 0.0D) {
                        isFront = true;
                    }
                }

                if (isFront) {
                    float impact = 0.5F;
                    float knockback = 0.25F;

                    if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamageSource) {
                        if (epicfightDamageSource.is(EpicFightDamageSources.TYPE.GUARD_PUNCTURE)) {
                            return;
                        }

                        impact = epicfightDamageSource.getImpact();
                        knockback += Math.min(impact * 0.1F, 1.0F);
                    }

                    this.guard(container, itemCapability, event, knockback, impact, false);
                }
            }
        }, 1);
    }

    public void guard(SkillContainer container, CapabilityItem itemCapability, HurtEvent.Pre event, float knockback, float impact, boolean advanced) {
        DamageSource damageSource = event.getDamageSource();

        if (this.isBlockableSource(damageSource, advanced)) {
            event.getPlayerPatch().playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
            ServerPlayerEntity serveerPlayer = event.getPlayerPatch().getOriginal();
            EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serveerPlayer.getLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serveerPlayer, damageSource.getDirectEntity());

            if (damageSource.getDirectEntity() instanceof LivingEntity livingEntity) {
                knockback += EnchantmentHelper.getKnockbackBonus(livingEntity) * 0.1F;
            }

            float penalty = container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get()) + this.getPenalizer(itemCapability);
            float consumeAmount = penalty * impact;
            boolean canAfford = event.getPlayerPatch().consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);

            event.getPlayerPatch().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
            container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), penalty, event.getPlayerPatch().getOriginal());

            BlockType blockType = canAfford ? BlockType.GUARD : BlockType.GUARD_BREAK;
            StaticAnimation animation = this.getGuardMotion(event.getPlayerPatch(), itemCapability, blockType);

            if (animation != null) {
                event.getPlayerPatch().playAnimationSynchronized(animation, 0.0F);
            }

            if (blockType == BlockType.GUARD_BREAK) {
                event.getPlayerPatch().playSound(EpicFightSounds.NEUTRALIZE_MOBS, 3.0F, 0.0F, 0.1F);
            }

            this.dealEvent(event.getPlayerPatch(), event, advanced);
        }
    }

    public void dealEvent(PlayerPatch<?> playerpatch, HurtEvent.Pre event, boolean advanced) {
        event.setCanceled(true);
        event.setResult(AttackResult.ResultType.BLOCKED);

        LivingEntityPatch<?> attackerpatch = EpicFightCapabilities.getEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class);

        if (attackerpatch != null) {
            attackerpatch.setLastAttackEntity(playerpatch.getOriginal());
        }

        Entity directEntity = event.getDamageSource().getDirectEntity();
        LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(directEntity, LivingEntityPatch.class);

        if (entitypatch != null) {
            entitypatch.onAttackBlocked(event.getDamageSource(), playerpatch);
        }
    }

    protected float getPenalizer(CapabilityItem itemCapability) {
        return this.penalizer;
    }

    protected Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> getGuradMotionMap(BlockType blockType) {
        switch (blockType) {
            case GUARD_BREAK:
                return this.guardBreakMotions;
            case GUARD:
                return this.guardMotions;
            case ADVANCED_GUARD:
                return this.advancedGuardMotions;
            default:
                throw new IllegalArgumentException("unsupported block type " + blockType);
        }
    }

    protected boolean isHoldingWeaponAvailable(PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
        StaticAnimation anim = itemCapability.getGuardMotion(this, blockType, playerpatch);

        if (anim != null) {
            return true;
        }

        Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions = this.getGuradMotionMap(blockType);

        if (!guardMotions.containsKey(itemCapability.getWeaponCategory())) {
            return false;
        }

        Object motion = guardMotions.get(itemCapability.getWeaponCategory()).apply(itemCapability, playerpatch);

        return motion != null;
    }

    /**
     * Not safe from null pointer exception
     * Must call isAvailableState first to check if it's safe
     *
     * @param metadata 0: guard breaks, 1: normal guards, 2: reinforced guards
     * @return StaticAnimation
     */
    @Nullable
    protected StaticAnimation getGuardMotion(PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
        StaticAnimation animation = itemCapability.getGuardMotion(this, blockType, playerpatch);

        if (animation != null) {
            return animation;
        }

        return (StaticAnimation)this.getGuradMotionMap(blockType).getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null).apply(itemCapability, playerpatch);
    }

    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);

        if (!container.getExecuter().isLogicalClient() && !container.getExecuter().getOriginal().isUsingItem()) {
            float penalty = container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get());

            if (penalty > 0) {
                int hitTick = container.getDataManager().getDataValue(SkillDataKeys.PENALTY_RESTORE_COUNTER.get());

                if (container.getExecuter().getOriginal().tickCount - hitTick > 40) {
                    container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), 0.0F, (ServerPlayerEntity) container.getExecuter().getOriginal());
                }
            }
        } else {
            container.getExecuter().resetActionTick();
        }
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecuter().getEventListener().removeListener(EventType.HURT_EVENT_PRE, EVENT_UUID, 1);
        container.getExecuter().getEventListener().removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.SERVER_ITEM_STOP_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
    }

    @Override
    public boolean isExecutableState(PlayerPatch<?> executer) {
        return !(executer.footsOnGround() || executer.getEntityState().hurt()) && executer.getEntityState().canUseSkill() && executer.isBattleMode();
    }

    protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
        return !damageSource.isBypassInvul()
                && !DamageSourceHelper.is(damageSource,EpicFightDamageSources.TYPE.PARTIAL_DAMAGE)
                && !damageSource.isBypassArmor()
                && !damageSource.isProjectile()
                && !damageSource.isExplosion()
                && !damageSource.isMagic()
                && !damageSource.isFire();
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get()) > 0.0F;
    }

    @Override
    public List<WeaponCategory> getAvailableWeaponCategories() {
        return List.copyOf(this.guardMotions.keySet());
    }

    protected boolean isAdvancedGuard() {
        return false;
    }

    public enum BlockType {
        GUARD_BREAK, GUARD, ADVANCED_GUARD
    }
}