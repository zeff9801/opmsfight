package yesman.epicfight.world.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Collections;
import java.util.List;

public class DodgeLeft extends LivingEntity {//TODO has to be registered first etc
	private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
	private LivingEntityPatch<?> entitypatch;
	
	public DodgeLeft(EntityType<? extends LivingEntity> type, World level) {
		super(type, level);
	}
	
	public DodgeLeft(LivingEntityPatch<?> entitypatch) {
		this(EpicFightEntities.DODGE_LEFT.get(), entitypatch.getOriginal().level);
		
		this.entitypatch = entitypatch;
		Vector3d pos = entitypatch.getOriginal().position();
		double x = pos.x;
		double y = pos.y;
		double z = pos.z;
		
		this.setPos(x, y, z);
		this.setBoundingBox(entitypatch.getOriginal().getBoundingBox().expandTowards(1.0D, 0.0D, 1.0D));
		
		if (this.level.isClientSide()) {
			this.remove();
		}
	}
	
	@Override
	public void tick() {
		if (this.tickCount > 5) {
			this.remove();
		}
	}
	
	@Override
	public boolean hurt(DamageSource damageSource, float amount) {
		if (this.level.isClientSide()) {
			return false;
		}
		
		if (!DodgeAnimation.DODGEABLE_SOURCE_VALIDATOR.apply(damageSource).dealtDamage()) {
			this.entitypatch.onDodgeSuccess(damageSource);
		}
		
		this.remove();
		return false;
	}
	
	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return EMPTY_LIST;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlotType p_21127_) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlotType p_21036_, ItemStack p_21037_) {
		
	}

	@Override
	public HandSide getMainArm() {
		return HandSide.RIGHT;
	}
}