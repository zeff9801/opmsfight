package yesman.epicfight.mixin.accessor;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingRenderer.class)
public interface LivingRendererAccessor<T extends LivingEntity> {
    @Invoker("getRenderType")
    RenderType invokeGetRenderType(T entity, boolean isVisible, boolean isVisibleToPlayer, boolean isGlowing);
} 