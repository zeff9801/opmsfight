package yesman.epicfight.skill.guard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.DamageSourceHelper;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

import java.util.List;

public class ImpactGuardSkill extends GuardSkill {
    public static GuardSkill.Builder createEnergizingGuardBuilder() {
        return GuardSkill.createGuardBuilder()
                .addAdvancedGuardMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
                .addAdvancedGuardMotion(WeaponCategories.SPEAR, (item, player) -> item.getStyle(player) == Styles.TWO_HAND ? Animations.SPEAR_GUARD_HIT : null)
                .addAdvancedGuardMotion(WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
                .addAdvancedGuardMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_HIT);
    }

    protected float superiorPenalizer;
    protected float damageReducer;

    public ImpactGuardSkill(GuardSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void setParams(CompoundNBT parameters) {
        super.setParams(parameters);
        this.superiorPenalizer = parameters.getFloat("superior_penalizer");
        this.damageReducer = parameters.getFloat("damage_reducer");
    }

    @Override
    public void guard(SkillContainer container, CapabilityItem itemCapapbility, HurtEvent.Pre event, float knockback, float impact, boolean advanced) {
        boolean canUse = this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapapbility, BlockType.ADVANCED_GUARD);

        if (event.getDamageSource().isExplosion()) {
            impact = event.getAmount();
        }

        super.guard(container, itemCapapbility, event, knockback, impact, canUse);
    }

    @Override
    public void dealEvent(PlayerPatch<?> playerpatch, HurtEvent.Pre event, boolean advanced) {
        boolean isSpecialSource = isAdvancedBlockableDamageSource(event.getDamageSource());
        event.setAmount(isSpecialSource ? event.getAmount() * this.damageReducer * 0.01F : 0.0F);
        event.setResult(isSpecialSource ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.BLOCKED);

        LivingEntityPatch<?> attackerpatch = EpicFightCapabilities.getEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class);

        if (attackerpatch != null) {
            attackerpatch.setLastAttackEntity(playerpatch.getOriginal());
        }

        if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamageSource) {
            epicfightDamageSource.setStunType(StunType.NONE);
        }

        event.setCanceled(true);
        Entity directEntity = event.getDamageSource().getDirectEntity();
        LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(directEntity, LivingEntityPatch.class);

        if (advanced) {
            LivingEntity original = playerpatch.getOriginal();
            EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(((ServerWorld)original.level), null, null, original, directEntity);
        }

        if (entitypatch != null) {
            entitypatch.onAttackBlocked(event.getDamageSource(), playerpatch);
        }
    }

    @Override
    protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
        return !damageSource.isBypassInvul()
                && !DamageSourceHelper.is(damageSource, EpicFightDamageSources.TYPE.PARTIAL_DAMAGE)
                && (!damageSource.isBypassArmor()
                && !damageSource.isProjectile()
                && !damageSource.isExplosion()
                && !damageSource.isMagic()
                && !damageSource.isFire()
                || advanced);
    }

    @Override
    public float getPenalizer(CapabilityItem itemCap) {
        return this.advancedGuardMotions.containsKey(itemCap.getWeaponCategory()) ? this.superiorPenalizer : this.penalizer;
    }

    private static boolean isAdvancedBlockableDamageSource(DamageSource damageSource) {
        return damageSource.isExplosion()
                || damageSource.isMagic()
                || damageSource.isFire()
                || damageSource.isProjectile()
                || damageSource.isBypassArmor();
    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.GUARD;
    }

    @Override
    protected boolean isAdvancedGuard() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Object> getTooltipArgsOfScreen(List<Object> list) {
        list.add(String.format("%.1f", 100.0F - this.damageReducer));
        return list;
    }

    @Override
    public List<WeaponCategory> getAvailableWeaponCategories() {
        return List.copyOf(this.advancedGuardMotions.keySet());
    }
}