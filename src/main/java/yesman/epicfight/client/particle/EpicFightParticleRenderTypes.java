package yesman.epicfight.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import yesman.epicfight.api.utils.math.MathUtils;

import java.util.function.Function;

@SuppressWarnings({"deprecation"})
@OnlyIn(Dist.CLIENT)
public class EpicFightParticleRenderTypes {
	public static final IParticleRenderType BLEND_LIGHTMAP_PARTICLE = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderSystem.depthMask(false);
			//RenderSystem.setShader(GameRenderer::getParticleShader);
			//RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			textureManager.bind(AtlasTexture.LOCATION_PARTICLES);

			Minecraft mc = Minecraft.getInstance();
	        mc.gameRenderer.lightTexture().turnOnLightLayer();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);

		}
		
		public void end(Tessellator tesselator) {
			tesselator.end();
			
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
			//RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);

			Minecraft mc = Minecraft.getInstance();
			mc.gameRenderer.overlayTexture().setupOverlayColor();
	        mc.gameRenderer.lightTexture().turnOnLightLayer();

			bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
		}

		public void end(Tessellator tesselator) {
			//tesselator.getBuilder().sortQuads(VertexSorting.DISTANCE_TO_ORIGIN);
			tesselator.end();

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
			//RenderSystem.setShader(GameRenderer::getRendertypeLightningShader);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		}

		public void end(Tessellator tesselator) {
			tesselator.end();
			
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();
		}

		public String toString() {
			return "LIGHTING";
		}
	};

	public static final Function<ResourceLocation, IParticleRenderType> TRAIL_PROVIDER = MathUtils.memoize((texturePath) -> {
		return new IParticleRenderType() {
			public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
				RenderSystem.enableBlend();
				RenderSystem.disableCull();

				Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
			    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				RenderSystem.enableDepthTest();
				RenderSystem.depthMask(true);
		        //RenderSystem.setShader(GameRenderer::getParticleShader);
		       // RenderSystem.setShaderTexture(0, texturePath);

		        Minecraft mc = Minecraft.getInstance();
		        mc.gameRenderer.lightTexture().turnOnLightLayer();

				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);

			}
			
			public void end(Tessellator tesselator) {
				//tesselator.getBuilder().sortQuads(VertexSorting.DISTANCE_TO_ORIGIN);
				tesselator.getBuilder().sortQuads(0.0F, 0.0F, 0.0F);
				tesselator.end();
				
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
	});
	
	public static final IParticleRenderType TRANSLUCENT_GLOWING = new IParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.disableCull();
		    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
	       // RenderSystem.setShader(GameRenderer::getPositionColorShader);

			bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		}

		public void end(Tessellator tesselator) {
			//tesselator.getBuilder().setQuadSorting(VertexSorting.DISTANCE_TO_ORIGIN);
			tesselator.getBuilder().sortQuads(0.0F, 0.0F, 0.0F);
			tesselator.end();
			
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
	        //RenderSystem.setShader(GameRenderer::getPositionColorLightmapShader);
	        
	        Minecraft mc = Minecraft.getInstance();
	        mc.gameRenderer.lightTexture().turnOnLightLayer();

			bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR_LIGHTMAP);
		}

		public void end(Tessellator tesselator) {
			//tesselator.getBuilder().sortQuads(VertexSorting.DISTANCE_TO_ORIGIN);
			tesselator.getBuilder().sortQuads(0.0F, 0.0F, 0.0F);
			tesselator.end();
			//RenderSystem.enableTexture(); Don't think i need this

			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();
			//RenderHelper.turnOff(); Don't think i need this

			Minecraft mc = Minecraft.getInstance();
	        mc.gameRenderer.lightTexture().turnOffLightLayer();
		}

		@Override
		public String toString() {
			return "EPICFIGHT:TRANSLUCENT";
		}
	};
}