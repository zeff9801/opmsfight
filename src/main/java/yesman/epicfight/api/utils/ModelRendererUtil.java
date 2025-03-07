package yesman.epicfight.api.utils;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModelRendererUtil {

    /**
     * Handles reseting a part animation specific data
     */
    public static void resertPartAnimation(ModelRenderer part) {
        part.x = 0;
        part.y = 0;
        part.z = 0;
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;
    }

    //public static getInitialPose(ModelRenderer part) {
 //       return PartPose.offset()
 //   }

    @OnlyIn(Dist.CLIENT)
    public static class PartPose {
        public static final PartPose ZERO = offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        public final float x;
        public final float y;
        public final float z;
        public final float xRot;
        public final float yRot;
        public final float zRot;

        private PartPose(float x, float y, float z, float xRot, float yRot, float zRot) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }

        public static PartPose offset(float x, float y, float z) {
            return offsetAndRotation(x, y, z, 0.0F, 0.0F, 0.0F);
        }

        public static PartPose rotation(float xRot, float yRot, float zRot) {
            return offsetAndRotation(0.0F, 0.0F, 0.0F, xRot, yRot, zRot);
        }

        public static PartPose offsetAndRotation(float x, float y, float z, float xRot, float yRot, float zRot) {
            return new PartPose(x, y, z, xRot, yRot, zRot);
        }
    }

}
