package yesman.epicfight.events;

import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

public class RegisterClientReloadListenersEvent extends Event implements IModBusEvent {

    private final IReloadableResourceManager resourceManager;

    public RegisterClientReloadListenersEvent(IReloadableResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    /**
     * Registers the given reload listener to the client-side resource manager.
     *
     * @param reloadListener the reload listener
     */
    public void registerReloadListener(IFutureReloadListener reloadListener)
    {
        resourceManager.registerReloadListener(reloadListener);
    }

}
