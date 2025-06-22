package yesman.epicfight.skill.identity;

import com.google.common.collect.Maps;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.DamageSourceHelper;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class MeteorSlamSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("03181ad0-e750-11ed-a05b-0242ac120003");

    public static class Builder extends Skill.Builder<MeteorSlamSkill> {
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation>> slamMotions = Maps.newHashMap();

        public Builder addSlamMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation> function) {
            this.slamMotions.put(weaponCategory, function);
            return this;
        }

        @Override
        public Builder setCategory(SkillCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public Builder setActivateType(ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        @Override
        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }
    }

    public static float getFallDistance(SkillContainer skillContainer) {
        return skillContainer.getDataManager().getDataValue(SkillDataKeys.FALL_DISTANCE.get());
    }

    public static MeteorSlamSkill.Builder createMeteorSlamBuilder() {
        return (new MeteorSlamSkill.Builder())
                .setCategory(SkillCategories.IDENTITY)
                .setResource(Resource.NONE)
                .addSlamMotion(WeaponCategories.SPEAR, (item, player) -> Animations.METEOR_SLAM)
                .addSlamMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.METEOR_SLAM)
                .addSlamMotion(WeaponCategories.TACHI, (item, player) -> Animations.METEOR_SLAM)
                .addSlamMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.METEOR_SLAM);
    }

    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation>> slamMotions;
    private final double minDistance = 6.0D;

    public MeteorSlamSkill(Builder builder) {
        super(builder);

        this.slamMotions = builder.slamMotions;
    }

    @Override
    public void onInitiate(SkillContainer container) {
        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event) -> {
            if (container.getExecuter() instanceof ServerPlayerPatch serverPlayerPatch) {
                Skill skill = event.getSkillContainer().getSkill();

                if (skill.getCategory() != SkillCategories.BASIC_ATTACK && skill.getCategory() != SkillCategories.AIR_ATTACK) {
                    return;
                }

                if (container.getExecuter().getOriginal().isOnGround() || container.getExecuter().getOriginal().xRot < 40.0F) {
                    return;
                }

                CapabilityItem holdingItem = container.getExecuter().getHoldingItemCapability(Hand.MAIN_HAND);

                if (!this.slamMotions.containsKey(holdingItem.getWeaponCategory())) {
                    return;
                }

                StaticAnimation slamAnimation = this.slamMotions.get(holdingItem.getWeaponCategory()).apply(holdingItem, container.getExecuter());

                if (slamAnimation == null) {
                    return;
                }

                Vector3d vec3 = container.getExecuter().getOriginal().getEyePosition(1.0F);
                Vector3d vec31 = container.getExecuter().getOriginal().getViewVector(1.0F);
                Vector3d vec32 = vec3.add(vec31.x * 50.0D, vec31.y * 50.0D, vec31.z * 50.0D);
                RayTraceResult hitResult = container.getExecuter().getOriginal().level.clip(new RayTraceContext(vec3, vec32, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, container.getExecuter().getOriginal()));

                if (hitResult.getType() != RayTraceResult.Type.MISS) {
                    Vector3d to = hitResult.getLocation();
                    Vector3d from = container.getExecuter().getOriginal().position();
                    double distance = to.distanceTo(from);

                    if (distance > this.minDistance) {
                        container.getExecuter().playAnimationSynchronized(slamAnimation, 0.0F);
                        container.getDataManager().setDataSync(SkillDataKeys.FALL_DISTANCE.get(), (float)distance, serverPlayerPatch.getOriginal());
                        container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), true);
                        event.setCanceled(true);
                    }
                }
            }
        });

        listener.addEventListener(EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            if (DamageSourceHelper.is(event.getDamageSource(), EpicFightDamageSources.TYPE.FALL_DAMAGE) && container.getDataManager().getDataValue(SkillDataKeys.PROTECT_NEXT_FALL.get())) {
                float stamina = container.getExecuter().getStamina();
                float damage = event.getAmount();
                event.setAmount(damage - stamina);
                event.setCanceled(true);
                container.getExecuter().setStamina(stamina - damage);
                container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
            }
        });

        listener.addEventListener(EventType.FALL_EVENT, EVENT_UUID, (event) -> {
            if (LevelUtil.calculateLivingEntityFallDamage(event.getForgeEvent().getEntityLiving(), event.getForgeEvent().getDamageMultiplier(), event.getForgeEvent().getDistance()) == 0) {
                container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecuter().getEventListener().removeListener(EventType.FALL_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.HURT_EVENT_PRE, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.SKILL_EXECUTE_EVENT, EVENT_UUID);
    }

    @Override
    public List<WeaponCategory> getAvailableWeaponCategories() {
        return List.copyOf(this.slamMotions.keySet());
    }
}