package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

import java.util.List;
import java.util.UUID;

public class ArmorCapability extends CapabilityItem {
	protected static final UUID[] ARMOR_MODIFIERS_BY_SLOTS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
	protected final double weight;
	protected final double stunArmor;
	private final EquipmentSlotType equipmentSlot;

	protected ArmorCapability(CapabilityItem.Builder builder) {
		super(builder);

		ArmorCapability.Builder armorBuilder = (ArmorCapability.Builder)builder;

		this.equipmentSlot = armorBuilder.equipmentSlot;
		this.weight = armorBuilder.weight;
		this.stunArmor = armorBuilder.stunArmor;
	}

	@Override
	public void modifyItemTooltip(ItemStack stack, List<ITextComponent> itemTooltip, LivingEntityPatch<?> entitypatch) {
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot, LivingEntityPatch<?> entitypatch) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();

		if (equipmentSlot == this.equipmentSlot) {
			map.put(EpicFightAttributes.WEIGHT.get(), new AttributeModifier(ARMOR_MODIFIERS_BY_SLOTS[equipmentSlot.getIndex()], "Armor modifier", this.weight, AttributeModifier.Operation.ADDITION));
			map.put(EpicFightAttributes.STUN_ARMOR.get(), new AttributeModifier(ARMOR_MODIFIERS_BY_SLOTS[equipmentSlot.getIndex()], "Armor modifier", this.stunArmor, AttributeModifier.Operation.ADDITION));
		}

		return map;
	}

	public static ArmorCapability.Builder builder() {
		return new ArmorCapability.Builder();
	}

	public static class Builder extends CapabilityItem.Builder {
		EquipmentSlotType equipmentSlot;
		double weight;
		double stunArmor;

		protected Builder() {
			this.constructor = ArmorCapability::new;
			this.weight = -1.0D;
			this.stunArmor = -1.0D;
		}

		public Builder item(Item item) {
			if (item instanceof ArmorItem armorItem) {
				IArmorMaterial armorMaterial = armorItem.getMaterial();
				this.equipmentSlot = armorItem.getSlot();

				if (this.weight < 0.0D) {
					this.weight = armorMaterial.getDefenseForSlot(this.equipmentSlot) * 2.5F;
				}

				if (this.stunArmor < 0.0D) {
					this.stunArmor = armorMaterial.getDefenseForSlot(this.equipmentSlot) * 0.375F;
				}
			}

			return this;
		}

		public Builder weight(double weight) {
			this.weight = weight;
			return this;
		}

		public Builder stunArmor(double stunArmor) {
			this.stunArmor = stunArmor;
			return this;
		}
	}
}