package yesman.epicfight.mixin;

import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CapabilityProvider.class)
public interface MixinCapabilityProviderInvoker {

    @Invoker(value = "reviveCaps", remap = false)
    default void invokeReviveCaps() {

    }

    @Invoker(value = "invalidateCaps", remap = false)
    default void invokeInvalidateCaps() {

    }

}
