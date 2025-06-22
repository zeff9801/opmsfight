package yesman.epicfight.client.renderer.patched.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resources.IResource;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.GsonHelper;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.EntityUtils;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.renderer.patched.layer.RenderOriginalModelLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.mixin.accessor.LivingRendererAccessor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class PatchedLivingEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LivingRenderer<E, M>, AM extends AnimatedMesh> extends PatchedEntityRenderer<E, T, R, AM> implements yesman.epicfight.client.renderer.LayerRenderer<E, T, M> {
	protected final Map<Class<?>, PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>>> patchedLayers = Maps.newHashMap();
	protected final List<PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>>> customLayers = Lists.newArrayList();


	public PatchedLivingEntityRenderer(EntityType<?> entityType) {
		ResourceLocation type = EntityType.getKey(entityType);
		String path = "animated_layers/" + type.getPath();
		List<Pair<ResourceLocation, JsonElement>> layers = Lists.newArrayList();

		SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, IResource> resources = null;
        try {
            resources = (Map<ResourceLocation, IResource>) resourceManager.getResources(new ResourceLocation(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<ResourceLocation, IResource> entry : resources.entrySet()) {
			Reader reader = null;

			try {
				reader = new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8);
				JsonElement jsonelement = GsonHelper.fromJson(new GsonBuilder().create(), reader, JsonElement.class);
				layers.add(Pair.of(entry.getKey(), jsonelement));
			} catch (IllegalArgumentException | JsonParseException jsonparseexception) {
				EpicFightMod.LOGGER.error("Failed to parse layer file {} for {}", entry.getKey(), type);
				jsonparseexception.printStackTrace();
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					// Handle exception
				}
			}
		}

		LayerUtil.addLayer(this, entityType, layers);
	}
	@SuppressWarnings("unchecked")
	public PatchedLivingEntityRenderer<E, T, M, R, AM> initLayerLast(EntityType<?> entityType) {
		List<LayerRenderer<E, M>> vanillaLayers = null;
		Minecraft minecraft = Minecraft.getInstance();

		if (entityType == EntityType.PLAYER) {
			if (minecraft.getEntityRenderDispatcher().renderers.get("default") instanceof LivingRenderer livingentityrenderer) {
				vanillaLayers = livingentityrenderer.layers;
			}
		} else {
			if (minecraft.getEntityRenderDispatcher().renderers.get(entityType) instanceof LivingRenderer livingentityrenderer) {
				vanillaLayers = livingentityrenderer.layers;
			}
		}

		if (vanillaLayers != null) {
			for (LayerRenderer<E, M> layer : vanillaLayers) {
				Class<?> layerClass = layer.getClass();

				if (layerClass.isAnonymousClass()) {
					layerClass = layer.getClass().getSuperclass();
				}

				if (this.patchedLayers.containsKey(layerClass)) {
					continue;
				}

				this.addPatchedLayer(layerClass, new RenderOriginalModelLayer<>("Root", new Vec3f(0.0F, this.getDefaultLayerHeightCorrection(), 0.0F), new Vec3f(0.0F, 0.0F, 0.0F)));
			}
		}

		return this;
	}

	@Override
	public void render(E entity, T entitypatch, R renderer, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight, float partialTicks) {
		super.render(entity, entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);

		Minecraft mc = Minecraft.getInstance();

		boolean isVisible = this.isVisible(entity, entitypatch);
		boolean isVisibleToPlayer = !isVisible && !entity.isInvisibleTo(mc.player);
		boolean isGlowing = mc.shouldEntityAppearGlowing(entity);
		@SuppressWarnings("unchecked")
		RenderType renderType = ((LivingRendererAccessor<E>)renderer).invokeGetRenderType(entity, isVisible, isVisibleToPlayer, isGlowing);

		Armature armature = entitypatch.getArmature();
		poseStack.pushPose();
		this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
		this.prepareVanillaModel(entity, renderer.getModel(), renderer, partialTicks);
		OpenMatrix4f[] poseMatrices = this.getPoseMatrices(entitypatch, armature, partialTicks, false);

		if (renderType != null) {
			AM mesh = this.getMeshProvider(entitypatch).get();
			this.prepareModel(mesh, entity, entitypatch, renderer);

			PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);

			if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
				mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, isVisibleToPlayer ? 0.15F : 1.0F, this.getOverlayCoord(entity, entitypatch, partialTicks), armature, poseMatrices);
			}
		}

		if (!entity.isSpectator()) {
			this.LayerRenderer(renderer, entitypatch, entity, poseMatrices, buffer, poseStack, packedLight, partialTicks);
		}

		if (renderType != null) {
			if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				for (Layer layer : entitypatch.getClientAnimator().getAllLayers()) {
					AnimationPlayer animPlayer = layer.animationPlayer;
					float playTime = animPlayer.getPrevElapsedTime() + (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()) * partialTicks;
					animPlayer.getAnimation().renderDebugging(poseStack, buffer, entitypatch, playTime, partialTicks);
				}
			}
		}

		poseStack.popPose();
	}

	protected void prepareVanillaModel(E entity, M model, LivingRenderer<E, M> renderer, float partialTicks) {
		boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
		model.riding = shouldSit;
		model.young = entity.isBaby();
		float f = MathHelper.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
		float f1 = MathHelper.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
		float f2 = f1 - f;

		if (shouldSit && entity.getVehicle() instanceof LivingEntity livingentity) {
			f = MathHelper.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
			f2 = f1 - f;
			float f3 = MathHelper.wrapDegrees(f2);
			if (f3 < -85.0F) {
				f3 = -85.0F;
			}

			if (f3 >= 85.0F) {
				f3 = 85.0F;
			}

			f = f1 - f3;
			if (f3 * f3 > 2500.0F) {
				f += f3 * 0.2F;
			}

			f2 = f1 - f;
		}

		float f6 = MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot);

		if (EntityUtils.isEntityUpsideDown(entity)) {
			f6 *= -1.0F;
			f2 *= -1.0F;
		}

		float f7 = entity.getViewXRot(partialTicks);
		float f8 = 0.0F;
		float f5 = 0.0F;

		if (!shouldSit && entity.isAlive()) {
			f8 = entity.animationSpeed;
			f5 = entity.animationPosition - entity.animationSpeed * (1.0F - partialTicks);
			if (entity.isBaby()) {
				f5 *= 3.0F;
			}

			if (f8 > 1.0F) {
				f8 = 1.0F;
			}
		}

		model.prepareMobModel(entity, f5, f8, partialTicks);
		model.setupAnim(entity, f5, f8, f7, f2, f6);
	}

	protected void prepareModel(AM mesh, E entity, T entitypatch, R renderer) {
		mesh.initialize();
	}

	protected void LayerRenderer(LivingRenderer<E, M> renderer, T entitypatch, E entity, OpenMatrix4f[] poses, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight, float partialTicks) {
		float f = MathUtils.lerpBetween(entity.yBodyRotO, entity.yBodyRot, partialTicks);
		float f1 = MathUtils.lerpBetween(entity.yHeadRotO, entity.yHeadRot, partialTicks);
		float f2 = f1 - f;
		float f7 = entity.getViewXRot(partialTicks);
		float bob = this.getVanillaRendererBob(entity, renderer, partialTicks);

		for (LayerRenderer<E, M> layer : renderer.layers) {
			Class<?> layerClass = layer.getClass();

			if (layerClass.isAnonymousClass()) {
				layerClass = layerClass.getSuperclass();
			}

			if (this.patchedLayers.containsKey(layerClass)) {
				this.patchedLayers.get(layerClass).renderLayer(entity, entitypatch, layer, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
			}
		}

		for (PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>> patchedLayer : this.customLayers) {
			patchedLayer.renderLayer(entity, entitypatch, null, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
		}
	}

	protected int getOverlayCoord(E entity, T entitypatch, float partialTicks) {
		return OverlayTexture.pack(0, OverlayTexture.v(entity.hurtTime > 5));
	}

	// can't transform the access modifier of getBob method because of overriding
	public float getVanillaRendererBob(E entity, LivingRenderer<E, M> renderer, float partialTicks) {
		return entity.tickCount + partialTicks;
	}

	@Override
	public void mulPoseStack(MatrixStack poseStack, Armature armature, E entityIn, T entitypatch, float partialTicks) {
		super.mulPoseStack(poseStack, armature, entityIn, entitypatch, partialTicks);

		if (entityIn.isCrouching()) {
			poseStack.translate(0.0D, 0.15D, 0.0D);
		}
	}

	protected boolean isVisible(E entityIn, T entitypatch) {
		return !entityIn.isInvisible();
	}
	@Override
	public void addPatchedLayer(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>> patchedLayer) {
		this.patchedLayers.putIfAbsent(originalLayerClass, patchedLayer);
	}

	/**
	 * Use this method in {@link PatchedRenderersEvent.Modify}}
	 */
	public void addPatchedLayerAlways(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>> patchedLayer) {
		this.patchedLayers.put(originalLayerClass, patchedLayer);
	}

	@Override
	public void addCustomLayer(PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>> patchedLayer) {
		this.customLayers.add(patchedLayer);
	}

	protected float getDefaultLayerHeightCorrection() {
		return 1.15F;
	}
}