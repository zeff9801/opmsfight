package yesman.epicfight.compat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.client.render.CuriosLayer;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CuriosCompat implements ICompatModule {
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<PatchedRenderersEvent.Modify>addListener((event) -> {
			if (event.get(EntityType.PLAYER) instanceof PatchedLivingEntityRenderer patchedlivingrenderer) {
				//patchedlivingrenderer.addPatchedLayer(CuriosLayer.class, new CuriosLayerRenderer());
			}
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class CuriosLayerRenderer extends ModelRenderLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, CuriosLayer<LivingEntity, EntityModel<LivingEntity>>, AnimatedMesh> {
		private static final List<Function<ItemStack, BipedModel<?>>> CURIO_MODEL_GETTERS = Lists.newArrayList();
		private static final Map<String, Map<ItemStack, BipedModel<?>>> CURIOS_MODELS_BY_SLOTS = Maps.newHashMap();
		
		private static BipedModel<?> getCurioModel(ItemStack itemstack) {
			for (Function<ItemStack, BipedModel<?>> modelGetter : CURIO_MODEL_GETTERS) {
				BipedModel<?> humanoidModel = modelGetter.apply(itemstack);
				
				if (humanoidModel != null) {
					return humanoidModel;
				}
			}
			
			return null;
		}
		
		public CuriosLayerRenderer() {
			super(null);
			
			if (ModList.get().isLoaded("relics")) {
				//CURIO_MODEL_GETTERS.add(RelicsModelProvider::getCuriosModel);
			}
		}
		
		@Override
		protected void renderLayer(LivingEntityPatch<LivingEntity> entitypatch, LivingEntity entityliving, CuriosLayer<LivingEntity, EntityModel<LivingEntity>> vanillaLayer, MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight,
								   OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
			CuriosApi.getCuriosHelper().getCuriosHandler(entityliving).ifPresent((handler) -> {
				handler.getCurios().forEach((id, stacksHandler) -> {
					IDynamicStackHandler stackHandler = stacksHandler.getStacks();
					IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

					//System.out.println(id +" , "+ stackHandler.getSlots());

					for (int i = 0; i < stackHandler.getSlots(); i++) {
						ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
						//boolean cosmetic = true;
						NonNullList<Boolean> renderStates = stacksHandler.getRenders();
						boolean renderable = renderStates.size() > i && renderStates.get(i);

						if (stack.isEmpty() && renderable) {
							stack = stackHandler.getStackInSlot(i);
							//cosmetic = false;
						}

						if (!stack.isEmpty()) {
							//SlotContext slotContext = new SlotContext(id, entityliving, i, cosmetic, renderable);
							ItemStack finalStack = stack;

							Map<ItemStack, BipedModel<?>> models = CURIOS_MODELS_BY_SLOTS.computeIfAbsent(id, (k) -> Maps.newHashMap());

							CURIOS_MODELS_BY_SLOTS.get(id);

							BipedModel<?> modele = getCurioModel(finalStack);

							if (modele != null) {

							}

							/**
							CuriosRendererRegistry.getRenderer(stack.getItem()).ifPresent(renderer -> renderer.render(finalStack, slotContext, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount,
											partialTicks, ageInTicks, netHeadYaw, headPitch));
							**/
						}
					}
				});
			});
		}
	}
}