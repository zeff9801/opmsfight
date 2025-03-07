package yesman.epicfight.compat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;

public class FirstPersonCompat implements ICompatModule {
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		/*FirstPersonAPI.getActivationHandlers().add(new ActivationHandler() {
			public boolean preventFirstperson() {
				PlayerPatch<?> playerpatch = ClientEngine.getInstance().getPlayerPatch();
				
				if (playerpatch != null && playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.BATTLE && EpicFightMod.CLIENT_CONFIGS.firstPersonModel.getValue()) {
					return true;
				}
				
				return false;
			}
		});*/
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
}