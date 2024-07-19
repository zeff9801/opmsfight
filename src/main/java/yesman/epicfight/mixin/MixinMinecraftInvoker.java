package yesman.epicfight.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MixinMinecraftInvoker {

    @Invoker(value = "startAttack")
    void invokeStartAttack();

}
