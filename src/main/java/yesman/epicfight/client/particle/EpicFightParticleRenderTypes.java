package yesman.epicfight.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings({"deprecation"})
@OnlyIn(Dist.CLIENT)
public class EpicFightParticleRenderTypes {
	public static final IParticleRenderType BLEND_LIGHTMAP_PARTICLE = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderSystem.depthMask(false);
			textureManager.bind(AtlasTexture.LOCATION_PARTICLES);

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.lightTexture().turnOnLightLayer();

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
		}

		public void end(Tessellator tessellator) {
			tessellator.end();

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.lightTexture().turnOffLightLayer();
		}

		@Override
		public String toString() {
			return "BLEND_LIGHTMAP_PARTICLE";
		}
	};

	public static final IParticleRenderType PARTICLE_MODEL_NO_NORMAL = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.disableCull();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableBlend();
			RenderSystem.depthMask(true);

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.overlayTexture().setupOverlayColor();
			mc.gameRenderer.lightTexture().turnOnLightLayer();

			bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
		}

		public void end(Tessellator tessellator) {
			tessellator.end();

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.overlayTexture().teardownOverlayColor();
			mc.gameRenderer.lightTexture().turnOffLightLayer();
		}

		public String toString() {
			return "PARTICLE_MODEL_NO_NORMAL";
		}
	};

	public static final IParticleRenderType LIGHTNING = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.disableCull();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			RenderSystem.colorMask(true, true, true, true);
			RenderSystem.depthMask(false);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		}

		public void end(Tessellator tessellator) {
			tessellator.end();

			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();
		}

		public String toString() {
			return "LIGHTNING";
		}
	};

	public static final IParticleRenderType TRAIL = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.disableCull();

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.lightTexture().turnOnLightLayer();

			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
		}

		public void end(Tessellator tessellator) {
			tessellator.end();

			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.lightTexture().turnOffLightLayer();
		}

		@Override
		public String toString() {
			return "EPICFIGHT:TRAIL";
		}
	};

	public static final IParticleRenderType TRANSLUCENT_GLOWING = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.disableCull();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);

			bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		}

		public void end(Tessellator tessellator) {
			tessellator.end();

			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();
		}

		@Override
		public String toString() {
			return "EPICFIGHT:TRANSLUCENT_GLOWING";
		}
	};

	public static final IParticleRenderType TRANSLUCENT = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.disableCull();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableDepthTest();

			RenderSystem.disableTexture();
			RenderHelper.turnBackOn();

			bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR_LIGHTMAP);
		}

		public void end(Tessellator tessellator) {
			tessellator.getBuilder().sortQuads(0.0F, 0.0F, 0.0F);
			tessellator.end();
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();
			RenderHelper.turnOff();
		}

		@Override
		public String toString() {
			return "EPICFIGHT:TRANSLUCENT";
		}
	};
}
