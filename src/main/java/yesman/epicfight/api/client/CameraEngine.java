package yesman.epicfight.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.config.ConfigManager;

@OnlyIn(Dist.CLIENT)
public class CameraEngine {
    private static CameraEngine instance;
    private int cameraShakeTime = 0;
    private float cameraShakeStrength = 0.0F;
    private float frequency = 0.0F;

    public static CameraEngine getInstance() {
        return instance;
    }

    public CameraEngine() {
        instance = this;
    }

    public void shakeCamera(int time, float strength, float frequency) {
        if (strength > this.cameraShakeStrength) {
            this.cameraShakeStrength = strength;
            this.cameraShakeTime = time;
            this.frequency = frequency;
        }
    }

    public void shakeCamera(int time, float strength) {
        this.shakeCamera(time, strength, 3.0F);
    }

    public void reset() {
        this.cameraShakeStrength = 0.0F;
        this.frequency = 0.0F;
    }

    @EventBusSubscriber(
            modid = "impactful",
            value = {Dist.CLIENT}
    )
    public static class Events {
        public Events() {
        }

        @SubscribeEvent
        public static void cameraSetupEvent(CameraSetup event) {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                if (CameraEngine.instance.cameraShakeTime > 0) {
                    CameraEngine.instance.cameraShakeTime--;
                    float delta = Minecraft.getInstance().getFrameTime();
                    float ticksExistedDelta = (float)player.tickCount + delta;
                    float k = CameraEngine.instance.cameraShakeStrength / 4.0F * ConfigManager.SCREEN_SHAKE_AMPLITUDE_MULTIPLY.get().floatValue();
                    float f = CameraEngine.instance.frequency;
                    if (!(Boolean) ConfigManager.DISABLE_SCREEN_SHAKE.get() && !Minecraft.getInstance().isPaused()) {
                        event.setPitch((float)((double)event.getPitch() + (double)k * Math.cos((double)(ticksExistedDelta * f + 2.0F))));
                        event.setYaw((float)((double)event.getYaw() + (double)k * Math.cos((double)(ticksExistedDelta * f + 1.0F))));
                        event.setRoll((float)((double)event.getRoll() + (double)k * Math.cos((double)(ticksExistedDelta * f))));
                    }
                } else if (CameraEngine.instance.cameraShakeStrength != 0.0F) {
                    CameraEngine.instance.reset();
                }
            }
        }
    }
}
