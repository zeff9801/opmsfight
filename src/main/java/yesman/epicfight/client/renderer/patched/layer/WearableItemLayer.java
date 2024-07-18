package yesman.epicfight.client.renderer.patched.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.armor.CustomModelBakery;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.JsonModelLoader;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class WearableItemLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends BipedModel<E>, AM extends HumanoidMesh> extends PatchedLayer<E, T, M, BipedArmorLayer<E, M, M>, AM> {


	private static final Map<ResourceLocation, AnimatedMesh> ARMOR_MODELS = new HashMap<>();
	private static final Map<String, ResourceLocation> EPICFIGHT_OVERRIDING_TEXTURES = Maps.newHashMap();
	
	public static void clear() {
		ARMOR_MODELS.clear();
		EPICFIGHT_OVERRIDING_TEXTURES.clear();
	}
	
	final boolean firstPersonModel;

	public WearableItemLayer(AM mesh, boolean doNotRenderHelment) {
        super(mesh);
        this.firstPersonModel = doNotRenderHelment;
	}
	
	private void renderArmor(MatrixStack matStack, IRenderTypeBuffer multiBufferSource, int packedLightIn, boolean hasEffect, AnimatedMesh model, Armature armature, float r, float g, float b, ResourceLocation armorTexture, OpenMatrix4f[] poses) {
		IVertexBuilder vertexConsumer = EpicFightRenderTypes.getArmorVertexBuilder(multiBufferSource, EpicFightRenderTypes.animatedArmor(armorTexture, model.getRenderProperty().isTransparent()), hasEffect);
		model.drawModelWithPose(matStack, vertexConsumer, packedLightIn, r, g, b, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}

	@Override
	public void renderLayer(T entitypatch, E entityliving, BipedArmorLayer<E, M, M> vanillaLayer, MatrixStack poseStack, IRenderTypeBuffer buf, int packedLightIn,
							OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			if (slot.getType() != EquipmentSlotType.Group.ARMOR) {
				continue;
			}

			boolean chestPart = false;

			if (entitypatch.isFirstPerson() && this.firstPersonModel) {
				if (slot != EquipmentSlotType.CHEST) {
					continue;
				} else {
					chestPart = true;
				}
			}

			if (slot == EquipmentSlotType.HEAD && this.firstPersonModel) {
				continue;
			}

			ItemStack stack = entityliving.getItemBySlot(slot);
			Item item = stack.getItem();

			if (item instanceof ArmorItem armorItem) {
				if (slot != item.getEquipmentSlot(stack)) {
					return;
				}

				poseStack.pushPose();
				float head = 0.0F;

				if (slot == EquipmentSlotType.HEAD) {
					poseStack.translate(0.0D, head * 0.055D, 0.0D);
				}

				boolean debuggingMode = ClientEngine.getInstance().isArmorModelDebuggingMode();

				if (debuggingMode) {
					poseStack.pushPose();
					poseStack.scale(-1.0F, -1.0F, 1.0F);
					poseStack.translate(1.0D, -1.501D, 0.0D);
					vanillaLayer.render(poseStack, buf, packedLightIn, entityliving, partialTicks, head, packedLightIn, bob, yRot, xRot);
					poseStack.popPose();
				}

				M defaultModel = vanillaLayer.getArmorModel(slot);
				AnimatedMesh armorMesh = this.getArmorModel(vanillaLayer, defaultModel, entityliving, armorItem, stack, slot, debuggingMode);

				if (armorMesh == null) {
					poseStack.popPose();
					return;
				}

				armorMesh.initialize();

				if (chestPart) {
					if (armorMesh.hasPart("torso")) {
						armorMesh.getPart("torso").hidden = true;
					}
				}

				boolean hasEffect = stack.hasFoil();

				/*if (armorItem instanceof IDyeableArmorItem  dyeableItem) {
					int i = dyeableItem.getColor(stack);
					float r = (float) (i >> 16 & 255) / 255.0F;
					float g = (float) (i >> 8 & 255) / 255.0F;
					float b = (float) (i & 255) / 255.0F;

					this.renderArmor(poseStack, buf, packedLightIn, hasEffect, armorMesh, entitypatch.getArmature(), r, g, b, this.getArmorTexture(stack, entityliving, armorMesh, slot, null, defaultModel), poses);
					this.renderArmor(poseStack, buf, packedLightIn, hasEffect, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, this.getArmorTexture(stack, entityliving, armorMesh, slot, "overlay", defaultModel), poses);
				} else {
					this.renderArmor(poseStack, buf, packedLightIn, hasEffect, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, this.getArmorTexture(stack, entityliving, armorMesh, slot, null, defaultModel), poses);
				}

				ArmorTrim.getTrim(entityliving.level.registryAccess(), stack).ifPresent((armorTrim) -> {
					this.renderTrim(poseStack, buf, packedLightIn, hasEffect, armorMesh, entitypatch.getArmature(), armorItem.getMaterial(), armorTrim, poses);
				});*/


				poseStack.popPose();
			}
		}
	}

	private AnimatedMesh getArmorModel(BipedArmorLayer<E, M, M> originalRenderer, M originalModel, E entityliving, ArmorItem armorItem, ItemStack stack, EquipmentSlotType slot, boolean armorDebugging) {
		ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(armorItem);

		if (ARMOR_MODELS.containsKey(registryName) && !armorDebugging) {
			return ARMOR_MODELS.get(registryName);
		} else {
			IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			ResourceLocation rl = new ResourceLocation(ForgeRegistries.ITEMS.getKey(armorItem).getNamespace(), "animmodels/armor/" + ForgeRegistries.ITEMS.getKey(armorItem).getPath() + ".json");
			AnimatedMesh model = null;

			try {
				IResource resource = resourceManager.getResource(rl);
				JsonModelLoader modelLoader = new JsonModelLoader(resourceManager, rl);
				model = modelLoader.loadAnimatedMesh(AnimatedMesh::new);
			} catch (FileNotFoundException e) {
				// Resource not found, handle the case where the custom model should be used
				Model customModel = ForgeHooksClient.getArmorModel(entityliving, stack, slot, originalModel);

				if (customModel == originalModel || !(customModel instanceof BipedModel<?> humanoidModel)) {
					model = this.mesh.getHumanoidArmorModel(slot);
				} else {
					model = CustomModelBakery.bake(humanoidModel, armorItem, slot, armorDebugging);
				}
			} catch (IOException e) {
				// Handle other I/O exceptions
				throw new RuntimeException(e);
			}


            ARMOR_MODELS.put(registryName, model);

			return model;
		}
	}
	
	private ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		ArmorItem item = (ArmorItem) stack.getItem();
		String texture = item.getMaterial().getName();
		String domain = "minecraft";
		int idx = texture.indexOf(':');
		
		if (idx != -1) {
			domain = texture.substring(0, idx);
			texture = texture.substring(idx + 1);
		}
		
		String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (slot == EquipmentSlotType.LEGS ? 2 : 1), type == null ? "" : String.format("_%s", type));
		s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
		int idx2 = s1.lastIndexOf('/');
		String s2 = String.format("%s/epicfight/%s", s1.substring(0, idx2), s1.substring(idx2 + 1));
		ResourceLocation resourcelocation2 = EPICFIGHT_OVERRIDING_TEXTURES.get(s2);
		
		if (resourcelocation2 != null) {
			return resourcelocation2;
		} else if (!EPICFIGHT_OVERRIDING_TEXTURES.containsKey(s2)) {
			resourcelocation2 = new ResourceLocation(s2);
			IResourceManager rm = Minecraft.getInstance().getResourceManager();
			
			if (rm.hasResource(resourcelocation2)) {
				EPICFIGHT_OVERRIDING_TEXTURES.put(s2, resourcelocation2);
				return resourcelocation2;
			} else {
				EPICFIGHT_OVERRIDING_TEXTURES.put(s2, null);
			}
		}
		
		ResourceLocation resourcelocation = BipedArmorLayer.ARMOR_LOCATION_CACHE.get(s1);
		
		if (resourcelocation == null) {
			resourcelocation = new ResourceLocation(s1);
			BipedArmorLayer.ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
		}
		
		return resourcelocation;
	}
}