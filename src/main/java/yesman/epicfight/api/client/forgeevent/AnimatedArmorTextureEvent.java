package yesman.epicfight.api.client.forgeevent;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class AnimatedArmorTextureEvent extends Event {
	private final LivingEntity livingentity;
    private final ItemStack itemstack;
    private final EquipmentSlotType equipmentSlot;
    private final BipedModel<?> originalModel;
    private ResourceLocation resultLocation;

	public AnimatedArmorTextureEvent(LivingEntity livingentity, ItemStack itemstack, EquipmentSlotType equipmentSlot, BipedModel<?> originalModel) {
		this.livingentity = livingentity;
        this.itemstack = itemstack;
        this.equipmentSlot = equipmentSlot;
        this.originalModel = originalModel;
	}

	public ResourceLocation getResultLocation() {
		return this.resultLocation;
	}

	public void setResultLocation(ResourceLocation resultLocation) {
		if (this.resultLocation != null) {
			EpicFightMod.LOGGER.debug("AnimatedArmorTextureEvent: You've overriden the existing texutre location " + this.resultLocation);
		}

		this.resultLocation = resultLocation;
	}

	public LivingEntity getLivingEntity() {
		return this.livingentity;
	}

	public ItemStack getItemstack() {
		return this.itemstack;
	}

	public EquipmentSlotType getEquipmentSlot() {
		return this.equipmentSlot;
	}

	public BipedModel<?> getOriginalModel() {
		return this.originalModel;
	}
}