package yesman.epicfight.client.renderer.patched.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.EntityUtils;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class PatchedLivingEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LivingRenderer<E, M>, AM extends AnimatedMesh> extends PatchedEntityRenderer<E, T, R, AM> {

	protected Map<Class<?>, PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>, AM>> patchedLayers = Maps.newHashMap();

	private static final double SHIFT_TRANSLATION = 0.15D;
	private static final float MAX_HEAD_ROTATION = 85.0F;
	private static final float MAX_ROTATION_DIFF = 2500.0F;
	private static final List<LayerRenderer<?, ?>> LAYER_LIST_HOLDER = new ArrayList<>();

	@Override
	public void render(E entityIn, T entitypatch, R renderer, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLight, float partialTicks) {
		super.render(entityIn, entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);
		
		Minecraft mc = Minecraft.getInstance();
		boolean isVisible = this.isVisible(entityIn, entitypatch);
		boolean isVisibleToPlayer = !isVisible && !entityIn.isInvisibleTo(mc.player);
		boolean isGlowing = mc.shouldEntityAppearGlowing(entityIn);
		RenderType renderType = this.getRenderType(entityIn, entitypatch, renderer, isVisible, isVisibleToPlayer, isGlowing);
		Armature armature = entitypatch.getArmature();
		poseStack.pushPose();
		this.mulPoseStack(poseStack, armature, entityIn, entitypatch, partialTicks);
		OpenMatrix4f[] poseMatrices = this.getPoseMatrices(entitypatch, armature, partialTicks);
		
		if (renderType != null) {
			this.prepareVanillaModel(entityIn, renderer.getModel(), renderer, partialTicks);

			AM mesh = this.getMesh(entitypatch);
			this.prepareModel(mesh, entityIn, entitypatch, renderer);

			IVertexBuilder builder = buffer.getBuffer(renderType);
			mesh.drawModelWithPose(poseStack, builder, packedLight, 1.0F, 1.0F, 1.0F, isVisibleToPlayer ? 0.15F : 1.0F, this.getOverlayCoord(entityIn, entitypatch, partialTicks), armature, poseMatrices);
		}
		
		if (!entityIn.isSpectator()) {
			this.renderLayer(renderer, entitypatch, entityIn, poseMatrices, buffer, poseStack, packedLight, partialTicks);
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

	// can't transform the access modifier of getBob method because of overriding
	public float getVanillaRendererBob(E entity, LivingRenderer<E, M> renderer, float partialTicks) {
		return entity.tickCount + partialTicks;
	}

	protected void prepareVanillaModel(E entityIn, M model, LivingRenderer<E, M> renderer, float partialTicks) {
		if (entityIn == null || model == null || renderer == null) {
			return;
		}

		boolean shouldSit = entityIn.isPassenger() && (entityIn.getVehicle() != null && entityIn.getVehicle().shouldRiderSit());
		model.riding = shouldSit;
		model.young = entityIn.isBaby();
		float bodyRot = MathHelper.rotLerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
		float headRot = MathHelper.rotLerp(partialTicks, entityIn.yHeadRotO, entityIn.yHeadRot);
		float headBodyRotDiff = headRot - bodyRot;

		if (shouldSit && entityIn.getVehicle() instanceof LivingEntity livingEntity) {
			bodyRot = MathHelper.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
			headBodyRotDiff = headRot - bodyRot;
			float wrappedDiff = MathHelper.wrapDegrees(headBodyRotDiff);
			if (wrappedDiff < -MAX_HEAD_ROTATION) {
				wrappedDiff = -MAX_HEAD_ROTATION;
			}
			if (wrappedDiff >= MAX_HEAD_ROTATION) {
				wrappedDiff = MAX_HEAD_ROTATION;
			}
			bodyRot = headRot - wrappedDiff;
			if (wrappedDiff * wrappedDiff > MAX_ROTATION_DIFF) {
				bodyRot += wrappedDiff * 0.2F;
			}
			headBodyRotDiff = headRot - bodyRot;
		}

		float xRot = MathHelper.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);
		if (EntityUtils.isEntityUpsideDown(entityIn)) {
			xRot *= -1.0F;
			headBodyRotDiff *= -1.0F;
		}

		float bob = this.getVanillaRendererBob(entityIn, renderer, partialTicks);
		float animationPosition = 0.0F;
		float animationSpeed = 0.0F;

		if (!shouldSit && entityIn.isAlive()) {
			//TODO If weird shit happens with walk animations, this might be the reason
			animationPosition = entityIn.animationPosition * partialTicks;
			animationSpeed = entityIn.animationPosition - entityIn.animationSpeed * (1.0F - partialTicks);
			if (entityIn.isBaby()) {
				animationSpeed *= 3.0F;
			}
			if (animationPosition > 1.0F) {
				animationPosition = 1.0F;
			}
		}

		model.prepareMobModel(entityIn, animationSpeed, animationPosition, partialTicks);
		model.setupAnim(entityIn, animationSpeed, animationPosition, bob, headBodyRotDiff, xRot);
	}

	protected void prepareModel(AM mesh, E entity, T entitypatch, R renderer) {
		mesh.initialize();
	}
	
	protected void renderLayer(LivingRenderer<E, M> renderer, T entitypatch, E entityIn, OpenMatrix4f[] poses, IRenderTypeBuffer buffer, MatrixStack poseStack, int packedLightIn, float partialTicks) {
		@SuppressWarnings("unchecked")
		List<LayerRenderer<E, M>> layers = (List<LayerRenderer<E, M>>)(List<?>)LAYER_LIST_HOLDER;
		layers.clear();
		layers.addAll(renderer.layers);
		Iterator<LayerRenderer<E, M>> iter = layers.iterator();


		float f = MathUtils.lerpBetween(entityIn.yBodyRotO, entityIn.yBodyRot, partialTicks);
        float f1 = MathUtils.lerpBetween(entityIn.yHeadRotO, entityIn.yHeadRot, partialTicks);
        float f2 = f1 - f;
		float f7 = entityIn.getViewXRot(partialTicks);
		float bob = this.getVanillaRendererBob(entityIn, renderer, partialTicks);

		while (iter.hasNext()) {
			LayerRenderer<E, M> layer = iter.next();
			Class<?> rendererClass = layer.getClass();

			if (rendererClass.isAnonymousClass()) {
				rendererClass = rendererClass.getSuperclass();
			}

			this.patchedLayers.computeIfPresent(rendererClass, (key, val) -> {
				val.renderLayer(0, entitypatch, entityIn, layer, poseStack, buffer, packedLightIn, poses, bob, f2, f7, partialTicks);
				iter.remove();
				return val;
			});
		}

		OpenMatrix4f modelMatrix = new OpenMatrix4f().mulFront(poses[entitypatch.getArmature().getRootJoint().getId()]);
		OpenMatrix4f transpose = OpenMatrix4f.transpose(modelMatrix, null);

		//Very bootleg but i need Head Layers to render separately from the other layers
		layers.forEach((layer) -> {
			if (layer instanceof HeadLayer) layer.render(poseStack, buffer, packedLightIn, entityIn, entityIn.animationPosition, entityIn.animationSpeed, partialTicks, entityIn.tickCount, f2, f7);
		});

		poseStack.pushPose();
		MathUtils.translateStack(poseStack, modelMatrix);
		MathUtils.rotateStack(poseStack, transpose);
		poseStack.translate(0.0D, this.getLayerCorrection(), 0.0D);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		
		layers.forEach((layer) -> {
			if (!(layer instanceof HeadLayer)) layer.render(poseStack, buffer, packedLightIn, entityIn, entityIn.animationPosition, entityIn.animationSpeed, partialTicks, entityIn.tickCount, f2, f7);
		});
		
		poseStack.popPose();
		layers.clear();
	}


	//TODO Should Port to newer version, but requires porting RenderTypes
	public RenderType getRenderType(E entityIn, T entitypatch, R renderer, boolean isVisible, boolean isVisibleToPlayer, boolean isGlowing) {
		ResourceLocation resourcelocation = this.getEntityTexture(entitypatch, renderer);

		if (isVisibleToPlayer) {
			return EpicFightRenderTypes.itemEntityTranslucentCull(resourcelocation);
		} else if (isVisible) {
			return EpicFightRenderTypes.animatedModel(resourcelocation);
		} else {
			return isGlowing ? RenderType.outline(resourcelocation) : null;
		}
	}

	protected boolean isVisible(E entityIn, T entitypatch) {
		return !entityIn.isInvisible();
	}
	
	protected int getOverlayCoord(E entity, T entitypatch, float partialTicks) {
		return OverlayTexture.pack(0, OverlayTexture.v(entity.hurtTime > 5));
	}


	@Override
	public void mulPoseStack(MatrixStack poseStack, Armature armature, E entityIn, T entitypatch, float partialTicks) {
		super.mulPoseStack(poseStack, armature, entityIn, entitypatch, partialTicks);
		if (entityIn.isShiftKeyDown()) {
			poseStack.translate(0.0D, SHIFT_TRANSLATION, 0.0D);
		}
	}

	public void addPatchedLayer(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends LayerRenderer<E, M>, AM> patchedLayer) {
		this.patchedLayers.put(originalLayerClass, patchedLayer);
	}

	protected double getLayerCorrection() {
		return 1.15D;
	}
}
