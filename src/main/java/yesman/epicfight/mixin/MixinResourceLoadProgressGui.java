
package yesman.epicfight.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.events.RegisterClientReloadListenersEvent;

@Mixin(ResourceLoadProgressGui.class)
public class MixinResourceLoadProgressGui {

    /**
     * Very bootleg inject target, but injected code into Minecraft.class constructor is overwritten by another mod
     */
    @Inject(method = "registerTextures", at = @At("HEAD"))
    private static void inject_register(CallbackInfo ci) {
        ModLoader.get().postEvent(new RegisterClientReloadListenersEvent((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()));
    }

}