package yesman.epicfight.client.particle;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.MeshProvider;
import yesman.epicfight.api.client.model.RawMesh;

@OnlyIn(Dist.CLIENT)
public abstract class TexturedCustomModelParticle extends CustomModelParticle<RawMesh> {
	protected final ResourceLocation texture;

	public TexturedCustomModelParticle(ClientWorld level, double x, double y, double z, double xd, double yd, double zd, MeshProvider<RawMesh> particleMesh, ResourceLocation texture) {
		super(level, x, y, z, xd, yd, zd, particleMesh);
		this.texture = texture;
	}

	@Override
	public void prepareDraw(MatrixStack poseStack, float partialTicks) {
		//RenderSystem.setShaderTexture(0, this.texture);
		Minecraft mc = Minecraft.getInstance();
		mc.textureManager.bind(this.texture);
	}
}