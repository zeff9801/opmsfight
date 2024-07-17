package yesman.epicfight.world.capabilities.entitypatch;


import com.google.common.collect.Multimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributeSupplier;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public abstract class HurtableEntityPatch<T extends LivingEntity> extends EntityPatch<T> {
    private boolean stunReductionDecreases;
    protected float stunTimeReductionDefault;
    protected float stunTimeReduction;
    protected boolean cancelKnockback;

    @Override
    public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
        super.onJoinWorld(entityIn, event);
        this.original.getAttributes().supplier = new EpicFightAttributeSupplier(this.original.getAttributes().supplier);
    }

    @Override
    protected void serverTick(LivingEvent.LivingUpdateEvent event) {
        this.cancelKnockback = false;

        if (this.stunReductionDecreases) {
            float stunArmor = this.getStunArmor();

            this.stunTimeReduction -= 0.1F * (1.1F - this.stunTimeReduction * this.stunTimeReduction) * (1.0F - stunArmor / (7.5F + stunArmor));

            if (this.stunTimeReduction < 0.0F) {
                this.stunReductionDecreases = false;
                this.stunTimeReduction = 0.0F;
            }
        } else {
            if (this.stunTimeReduction < this.stunTimeReductionDefault) {
                this.stunTimeReduction += 0.02F * (1.1F - this.stunTimeReduction * this.stunTimeReduction);

                if (this.stunTimeReduction > this.stunTimeReductionDefault) {
                    this.stunTimeReduction = this.stunTimeReductionDefault;
                }
            }
        }
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTicks) {
        return null;
    }

    public abstract boolean applyStun(StunType stunType, float stunTime);

    public float getWeight() {
        return (float)this.original.getAttributeValue(Attributes.MAX_HEALTH) * 2.0F;
    }

    public float getStunShield() {
        return 0.0F;
    }

    public void setStunShield(float value) {
    }

    public void setStunReductionOnHit(StunType stunType) {
        this.stunReductionDecreases = true;

        if (stunType != StunType.NONE) {
            this.stunTimeReduction += Math.max((1.0F - this.stunTimeReduction) * 0.8F, 0.5F);
            this.stunTimeReduction = Math.min(1.0F, this.stunTimeReduction);
            this.stunReductionDecreases = true;
        }
    }

    public float getStunReduction() {
        return this.stunTimeReduction;
    }

    public void setDefaultStunReduction(EquipmentSlotType equipmentslot, ItemStack from, ItemStack to) {
        Multimap<Attribute, AttributeModifier> modifiersToAdd = to.getAttributeModifiers(equipmentslot); //TODO Normally this should return a MultiMap with just the STUN_ARMOR modifiers, but its not possible on this Guava version
        Multimap<Attribute, AttributeModifier> modifiersToRemove = from.getAttributeModifiers(equipmentslot);

        //TODO Might cause unexpected behaviours because it removes/adds all the modifiers not just the one we want
        original.getAttributes().addTransientAttributeModifiers(modifiersToAdd);
        original.getAttributes().removeAttributeModifiers(modifiersToRemove);

        double stunArmor = this.original.getAttributeValue(EpicFightAttributes.STUN_ARMOR.get());

        this.stunReductionDecreases = stunArmor < this.getStunArmor();
        this.stunTimeReductionDefault = (float) (stunArmor / (stunArmor + 7.5F));
    }

    public float getStunArmor() {
        ModifiableAttributeInstance stunArmor = this.original.getAttribute(EpicFightAttributes.STUN_ARMOR.get());
        return (float)(stunArmor == null ? 0.0F : stunArmor.getValue());
    }

    public EntityState getEntityState() {
        return EntityState.DEFAULT_STATE;
    }

    public boolean shouldCancelKnockback() {
        return this.cancelKnockback;
    }

    public abstract boolean isStunned();

    public void knockBackEntity(Vector3d sourceLocation, float power) {
        double d1 = sourceLocation.x() - this.original.getX();
        double d0;

        for (d0 = sourceLocation.z() - this.original.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
            d1 = (Math.random() - Math.random()) * 0.01D;
        }

        power *= 1.0D - this.original.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

        if (power > 0.0D) {
            this.original.hasImpulse = true;
            Vector3d vec3 = this.original.getDeltaMovement();
            Vector3d vec31 = (new Vector3d(d1, 0.0D, d0)).normalize().scale(power);
            this.original.setDeltaMovement(vec3.x / 2.0D - vec31.x, this.original.isOnGround() ? Math.min(0.4D, vec3.y / 2.0D) : vec3.y, vec3.z / 2.0D - vec31.z);
        }
    }

    public void playSound(SoundEvent sound, float pitchModifierMin, float pitchModifierMax) {
        this.playSound(sound, 1.0F, pitchModifierMin, pitchModifierMax);
    }

    public void playSound(SoundEvent sound, float volume, float pitchModifierMin, float pitchModifierMax) {
        if (sound == null) {
            return;
        }

        float pitch = (this.original.getRandom().nextFloat() * 2.0F - 1.0F) * (pitchModifierMax - pitchModifierMin);

        if (!this.isLogicalClient()) {
            this.original.level.playSound(null, this.original.getX(), this.original.getY(), this.original.getZ(), sound, this.original.getSoundSource(), volume, 1.0F + pitch);
        } else {
            this.original.level.playLocalSound(this.original.getX(), this.original.getY(), this.original.getZ(), sound, this.original.getSoundSource(), volume, 1.0F + pitch, false);
        }
    }

    @Override
    public boolean overrideRender() {
        return false;
    }
}