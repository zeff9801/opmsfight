package yesman.epicfight.skill.weaponinnate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.UUID;

public class LiechtenauerSkill extends WeaponInnateSkill {
    private static final UUID EVENT_UUID = UUID.fromString("244c57c0-a837-11eb-bcbc-0242ac130002");
    private int returnDuration;

    public LiechtenauerSkill(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void setParams(CompoundNBT parameters) {
        super.setParams(parameters);
        this.returnDuration = parameters.getInt("return_duration");
    }

    @Override
    public void onInitiate(SkillContainer container) {
        container.getExecuter().getEventListener().addEventListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
            if (container.isActivated() && !container.isDisabled()) {
                if (event.getAttackDamage() > event.getTarget().getHealth()) {
                    this.setDurationSynchronize(event.getPlayerPatch(), Math.min(this.maxDuration, container.getRemainDuration() + this.returnDuration));
                }
            }
        });

        container.getExecuter().getEventListener().addEventListener(EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            int phaseLevel = event.getPlayerPatch().getEntityState().getLevel();

            if (event.getAmount() > 0.0F && container.isActivated() && !container.isDisabled() && phaseLevel > 0 && phaseLevel < 3 &&
                    this.canExecute(event.getPlayerPatch()) && isBlockableSource(event.getDamageSource())) {
                DamageSource damageSource = event.getDamageSource();
                boolean isFront = false;
                Vector3d sourceLocation = damageSource.getSourcePosition();

                if (sourceLocation != null) {
                    Vector3d viewVector = event.getPlayerPatch().getOriginal().getViewVector(1.0F);
                    Vector3d toSourceLocation = sourceLocation.subtract(event.getPlayerPatch().getOriginal().position()).normalize();

                    if (toSourceLocation.dot(viewVector) > 0.0D) {
                        isFront = true;
                    }
                }

                if (isFront) {
                    event.getPlayerPatch().playSound(EpicFightSounds.CLASH, -0.05F, 0.1F);
                    ServerPlayerEntity playerentity = event.getPlayerPatch().getOriginal();
                    EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(playerentity.getLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, playerentity, damageSource.getDirectEntity());

                    float knockback = 0.25F;

                    if (damageSource instanceof EpicFightDamageSource epicfightSource) {
                        knockback += Math.min(epicfightSource.getImpact() * 0.1F, 1.0F);
                    }

                    if (damageSource.getDirectEntity() instanceof LivingEntity livingentity) {
                        knockback += EnchantmentHelper.getKnockbackBonus(livingentity) * 0.1F;
                    }

                    LivingEntityPatch<?> attackerpatch = EpicFightCapabilities.getEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class);

                    if (attackerpatch != null) {
                        attackerpatch.setLastAttackEntity(event.getPlayerPatch().getOriginal());
                    }

                    event.getPlayerPatch().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                    event.setCanceled(true);
                    event.setResult(AttackResult.ResultType.BLOCKED);
                }
            }
        }, 0);

        container.getExecuter().getEventListener().addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            SkillContainer skillContainer = event.getPlayerPatch().getSkill(this);

            if (skillContainer.isActivated()) {
                ClientPlayerEntity clientPlayer = event.getPlayerPatch().getOriginal();
                clientPlayer.setSprinting(false);
                clientPlayer.sprintTriggerTime = -1;
                Minecraft mc = Minecraft.getInstance();
                ClientEngine.getInstance().controllEngine.setKeyBind(mc.options.keySprint, false);
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecuter().getEventListener().removeListener(EventType.HURT_EVENT_PRE, EVENT_UUID, 0);
        container.getExecuter().getEventListener().removeListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
        executer.playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F);

        if (executer.getSkill(this).isActivated()) {
            this.cancelOnServer(executer, args);
        } else {
            super.executeOnServer(executer, args);
            executer.getSkill(this).activate();
            executer.modifyLivingMotionByCurrentItem();
            executer.playAnimationSynchronized(Animations.BIPED_LIECHTENAUER_READY, 0.0F);
        }
    }

    @Override
    public void cancelOnServer(ServerPlayerPatch executer, PacketBuffer args) {
        executer.getSkill(this).deactivate();
        super.cancelOnServer(executer, args);
        executer.modifyLivingMotionByCurrentItem();
    }

    @Override
    public void executeOnClient(LocalPlayerPatch executer, PacketBuffer args) {
        super.executeOnClient(executer, args);
        executer.getSkill(this).activate();
    }

    @Override
    public void cancelOnClient(LocalPlayerPatch executer, PacketBuffer args) {
        super.cancelOnClient(executer, args);
        executer.getSkill(this).deactivate();
    }

    @Override
    public boolean canExecute(PlayerPatch<?> executer) {
        if (executer.isLogicalClient()) {
            return super.canExecute(executer);
        } else {
            ItemStack itemstack = executer.getOriginal().getMainHandItem();

            return EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(executer, itemstack) == this && executer.getOriginal().getVehicle() == null;
        }
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        return this;
    }

    private static boolean isBlockableSource(DamageSource damageSource) {
        return !damageSource.isBypassInvul() && !damageSource.isExplosion();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<ITextComponent> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
//        List<Component> list = Lists.newArrayList();
//        List<Object> tooltipArgs = Lists.newArrayList();
//        String traslatableText = this.getTranslationKey();
//
//        tooltipArgs.add(this.maxDuration / 20);
//        tooltipArgs.add(this.returnDuration / 20);
//
//        list.add(Component.translatable(traslatableText).withStyle(ChatFormatting.WHITE).append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
//        list.add(Component.translatable(traslatableText + ".tooltip", tooltipArgs.toArray(new Object[0])).withStyle(ChatFormatting.DARK_GRAY));
//
//        return list;

        return null;
    }

    public enum Stance {
        VOM_TAG, PFLUG, OCHS
    }
}