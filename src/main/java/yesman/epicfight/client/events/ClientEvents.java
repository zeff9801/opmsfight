package yesman.epicfight.client.events;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.UseAction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyPressedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseClickedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseReleasedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.RightClickItemEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
	private static final Pair<ResourceLocation, ResourceLocation> OFFHAND_TEXTURE = Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
	private static Minecraft minecraft = Minecraft.getInstance();
	
	@SubscribeEvent
	public static void mouseClickEvent(MouseClickedEvent.Pre event) {
		if (event.getGui() instanceof ContainerScreen) {
			Slot slot = ((ContainerScreen<?>)event.getGui()).getSlotUnderMouse();
			
			if (slot != null) {
				CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(minecraft.player.inventory.getCarried());
				
				if (!cap.canBePlacedOffhand()) {
					if (slot.getNoItemIcon() != null && slot.getNoItemIcon().equals(OFFHAND_TEXTURE)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void mouseReleaseEvent(MouseReleasedEvent.Pre event) {
		if (event.getGui() instanceof ContainerScreen) {
			Slot slot = ((ContainerScreen<?>)event.getGui()).getSlotUnderMouse();
			
			if (slot != null) {
				CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(minecraft.player.inventory.getCarried());
				
				if (!cap.canBePlacedOffhand()) {
					if (slot.getNoItemIcon() != null && slot.getNoItemIcon().equals(OFFHAND_TEXTURE)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void presssKeyInGui(KeyboardKeyPressedEvent.Pre event) {
		CapabilityItem itemCapability = CapabilityItem.EMPTY;
		
		if (event.getKeyCode() == minecraft.options.keySwapOffhand.getKey().getValue()) {
			if (event.getGui() instanceof ContainerScreen) {
				Slot slot = ((ContainerScreen<?>)event.getGui()).getSlotUnderMouse();
				
				if (slot != null && slot.hasItem()) {
					itemCapability = EpicFightCapabilities.getItemStackCapability(slot.getItem());
					
					if (!itemCapability.canBePlacedOffhand()) {
						event.setCanceled(true);
					}
				}
			}
		} else if (event.getKeyCode() >= 49 && event.getKeyCode() <= 57) {
			if (event.getGui() instanceof ContainerScreen) {
				Slot slot = ((ContainerScreen<?>)event.getGui()).getSlotUnderMouse();
				
				if (slot != null && slot.getNoItemIcon() != null && slot.getNoItemIcon().equals(OFFHAND_TEXTURE)) {
					itemCapability = EpicFightCapabilities.getItemStackCapability(minecraft.player.inventory.getItem(event.getKeyCode() - 49));
					
					if (!itemCapability.canBePlacedOffhand()) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void rightClickItemClient(RightClickItem event) {
		if (event.getSide() == LogicalSide.CLIENT) {
			LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LocalPlayerPatch.class);
			
			if (playerpatch != null && playerpatch.getOriginal().getOffhandItem().getUseAnimation() == UseAction.NONE) {
				boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.CLIENT_ITEM_USE_EVENT, new RightClickItemEvent<>(playerpatch));
				
				if (playerpatch.getEntityState().movementLocked()) {
					canceled = true;
				}
				
				event.setCanceled(canceled);
			}
		}
	}

	@SubscribeEvent
	public static void clientLoggingInEvent(ClientPlayerNetworkEvent.LoggedInEvent event) {
		LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(event.getPlayer(), LocalPlayerPatch.class);

		if (playerpatch != null) {
			ClientEngine.getInstance().controllEngine.setPlayerPatch(playerpatch);
		}
	}
	
	@SubscribeEvent
	public static void clientRespawnEvent(ClientPlayerNetworkEvent.RespawnEvent event) {
		LocalPlayerPatch oldCap = EpicFightCapabilities.getEntityPatch(event.getOldPlayer(), LocalPlayerPatch.class);
		
		if (oldCap != null) {
			LocalPlayerPatch newCap = EpicFightCapabilities.getEntityPatch(event.getNewPlayer(), LocalPlayerPatch.class);

			if (newCap != null) {
				//			newCap.copySkillsFrom(oldCap); //TODO Uncomment this if skills do not get copied over after player respawned
				newCap.onRespawnLocalPlayer(event);
				newCap.toMode(oldCap.getPlayerMode(), false);
				ClientEngine.getInstance().controllEngine.setPlayerPatch(newCap);
			}
		}
	}
	
	@SubscribeEvent
	public static void clientLogoutEvent(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		if (event.getPlayer() != null) {
			ItemCapabilityReloadListener.reset();
			ItemCapabilityProvider.clear();
			EntityPatchProvider.clear();
			WeaponTypeReloadListener.clear();

			ClientEngine.getInstance().renderEngine.zoomOut(0);
			ClientEngine.getInstance().renderEngine.registerRenderer();
		}
	}


}