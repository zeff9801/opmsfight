package yesman.epicfight.events;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.provider.ProviderEntity;
import yesman.epicfight.world.capabilities.provider.ProviderItem;
import yesman.epicfight.world.capabilities.provider.ProviderSkill;

@Mod.EventBusSubscriber(modid=EpicFightMod.MODID)
public class CapabilityEvent {

	@SubscribeEvent
	public static void attachItemCapability(AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject() != null) {
			ProviderItem prov = new ProviderItem(event.getObject());

			if (prov.hasCapability()) {
				event.addCapability(new ResourceLocation(EpicFightMod.MODID, "item_cap"), prov);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
		EntityPatch oldEntitypatch = EpicFightCapabilities.getEntityPatch(event.getObject(), EntityPatch.class);

		if (oldEntitypatch == null) {
			ProviderEntity prov = new ProviderEntity(event.getObject());

			if (prov.hasCapability()) {
				EntityPatch entitypatch = prov.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

				entitypatch.onConstructed(event.getObject());
				event.addCapability(new ResourceLocation(EpicFightMod.MODID, "entity_cap"), prov);

				if (entitypatch instanceof PlayerPatch<?> playerpatch) {
					if (event.getObject().getCapability(EpicFightCapabilities.CAPABILITY_SKILL).orElse(null) == null) {

						if (playerpatch != null) {
							ProviderSkill skillProvider = new ProviderSkill(playerpatch);
							event.addCapability(new ResourceLocation(EpicFightMod.MODID, "skill_cap"), skillProvider);
						}
					}
				}
			}
		}
	}
}