package yesman.epicfight.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = PlayerEntity.class)
public abstract class MixinPlayer {
    @Redirect(at = @At( value = "INVOKE",
            target = "Lnet/minecraft/util/CombatTracker;recordDamage(Lnet/minecraft/util/DamageSource;FF)V"),
            method = "actuallyHurt(Lnet/minecraft/util/DamageSource;F)V")
    private void epicfight_recordDamage(CombatTracker self, DamageSource damagesource, float health, float damage) {
        LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(damagesource.getEntity(), LivingEntityPatch.class);

        if (entitypatch != null) {
            entitypatch.setLastAttackEntity(self.getMob());
        }

        self.recordDamage(damagesource, health, damage);
    }
}