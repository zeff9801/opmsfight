package yesman.epicfight.client.renderer.patched.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.client.forgeevent.AnimatedArmorTextureEvent;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.MeshProvider;
import yesman.epicfight.api.client.model.armor.CustomModelBakery;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.JsonModelLoader;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class WearableItemLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends BipedModel<E>, AM extends HumanoidMesh> extends ModelRenderLayer<E, T, M, BipedArmorLayer<E, M, M>, AM> {
	private static final Map<ResourceLocation, AnimatedMesh> ARMOR_MODELS = Maps.newHashMap();
	private static final Map<String, ResourceLocation> EPICFIGHT_OVERRIDING_TEXTURES = Maps.newHashMap();


	public static void clearModels() {
		ARMOR_MODELS.values().forEach(AnimatedMesh::destroy);
		ARMOR_MODELS.clear();
		EPICFIGHT_OVERRIDING_TEXTURES.clear();
	}

	public static void putModel(ResourceLocation rl, AnimatedMesh animatedMesh) {
		if (ARMOR_MODELS.containsKey(rl)) {
			AnimatedMesh oldModel = ARMOR_MODELS.get(rl);

			if (oldModel != animatedMesh) {
				ARMOR_MODELS.get(rl).destroy();
			}
		}

		ARMOR_MODELS.put(rl, animatedMesh);
	}
	
	final boolean firstPersonModel;

	public WearableItemLayer(MeshProvider<AM> meshProvider, boolean firstPersonModel) {
		super(meshProvider);

		this.firstPersonModel = firstPersonModel;
	}
	private void renderArmor(MatrixStack matStack, IRenderTypeBuffer multiBufferSource, int packedLightIn, boolean hasEffect, AnimatedMesh model, Armature armature, float r, float g, float b, ResourceLocation armorTexture, OpenMatrix4f[] poses) {
		model.draw(matStack, multiBufferSource, RenderType.armorCutoutNoCull(armorTexture), packedLightIn, r, g, b, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void renderLayer(T entitypatch, E entityliving, BipedArmorLayer<E, M, M> vanillaLayer, MatrixStack poseStack, IRenderTypeBuffer buf, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			if (slot.getType() != EquipmentSlotType.Group.ARMOR) {
				continue;
			}

			boolean firstPersonChest = false;

			if (entitypatch.isFirstPerson() && this.firstPersonModel) {
				if (slot != EquipmentSlotType.CHEST) {
					continue;
				} else {
					firstPersonChest = true;
				}
			}

			if (slot == EquipmentSlotType.HEAD && this.firstPersonModel) {
				continue;
			}

			ItemStack itemstack = entityliving.getItemBySlot(slot);
			Item item = itemstack.getItem();

			if (item instanceof ArmorItem armorItem) {
				if (slot != armorItem.getEquipmentSlot(itemstack)) {
					return;
				}

				poseStack.pushPose();
				float head = 0.0F;

				if (slot == EquipmentSlotType.HEAD) {
					poseStack.translate(0.0D, head * 0.055D, 0.0D);
				}

				M defaultModel = vanillaLayer.getArmorModel(slot);
				Model armorModel = ForgeHooksClient.getArmorModel(entityliving, itemstack, slot, defaultModel);
				AnimatedMesh armorMesh = this.getArmorModel(vanillaLayer, defaultModel, armorModel, entityliving, armorItem, itemstack, slot);

				if (armorMesh == null) {
					poseStack.popPose();
					return;
				}

				if (armorModel instanceof BipedModel humanoidModel) {
					boolean shouldSit = entityliving.isPassenger() && (entityliving.getVehicle() != null && entityliving.getVehicle().shouldRiderSit());
					float f8 = 0.0F;
					float f5 = 0.0F;

					if (!shouldSit && entityliving.isAlive()) {
						f8 = entityliving.animationSpeed = (partialTicks);
						f5 = entityliving.animationPosition = (partialTicks);

						if (entityliving.isBaby()) {
							f5 *= 3.0F;
						}

						if (f8 > 1.0F) {
							f8 = 1.0F;
						}
					}

					humanoidModel.setupAnim(entityliving, f8, f5, bob, yRot, xRot);
					//humanoidModel.head.loadPose(humanoidModel.head.getInitialPose());
					//humanoidModel.hat.loadPose(humanoidModel.hat.getInitialPose());
					//humanoidModel.body.loadPose(humanoidModel.body.getInitialPose());
					//humanoidModel.leftArm.loadPose(humanoidModel.leftArm.getInitialPose());
					//humanoidModel.rightArm.loadPose(humanoidModel.rightArm.getInitialPose());
					//humanoidModel.leftLeg.loadPose(humanoidModel.leftLeg.getInitialPose());
					//humanoidModel.rightLeg.loadPose(humanoidModel.rightLeg.getInitialPose());
				}

				armorMesh.initialize();

				if (firstPersonChest) {
					armorMesh.getAllParts().forEach(part -> part.setHidden(true));

					if (armorMesh.hasPart("leftArm")) {
						armorMesh.getPart("leftArm").setHidden(false);
					}

					if (armorMesh.hasPart("rightArm")) {
						armorMesh.getPart("rightArm").setHidden(false);
					}
				}

				/*if (armorItem instanceof IDyeableArmorItem dyeableItem) {
					int i = dyeableItem.getColor(itemstack);
					float r = (float) (i >> 16 & 255) / 255.0F;
					float g = (float) (i >> 8 & 255) / 255.0F;
					float b = (float) (i & 255) / 255.0F;

					this.renderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), r, g, b, this.getArmorTexture(itemstack, entityliving, armorMesh, slot, null, defaultModel), poses);
					this.renderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, this.getArmorTexture(itemstack, entityliving, armorMesh, slot, "overlay", defaultModel), poses);
				} else {
					this.renderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, this.getArmorTexture(itemstack, entityliving, armorMesh, slot, null, defaultModel), poses);
				}

				ArmorTrim.getTrim(entityliving.level().registryAccess(), itemstack).ifPresent((armorTrim) -> {
					this.renderTrim(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), armorItem.getMaterial(), armorTrim, slot, poses);
				});

				if (itemstack.hasFoil()) {
					this.renderGlint(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), poses);
				}*/

				poseStack.popPose();
			}
		}
	}

	private AnimatedMesh getArmorModel(BipedArmorLayer<E, M, M> originalRenderer, M originalModel, Model forgeHooksArmorModel, E entityliving, ArmorItem armorItem, ItemStack itemstack, EquipmentSlotType slot) {
		ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(armorItem);

		if (ARMOR_MODELS.containsKey(registryName) && !ClientEngine.getInstance().renderEngine.shouldRenderVanillaModel()) {
			return ARMOR_MODELS.get(registryName);
		} else {
			IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			ResourceLocation rl = new ResourceLocation(ForgeRegistries.ITEMS.getKey(armorItem).getNamespace(), "animmodels/armor/" + ForgeRegistries.ITEMS.getKey(armorItem).getPath() + ".json");
			AnimatedMesh animatedMesh = null;

			if (resourceManager.hasResource(rl)){
				JsonModelLoader modelLoader = new JsonModelLoader(resourceManager, rl);
				animatedMesh = modelLoader.loadAnimatedMesh(AnimatedMesh::new);
			} else {
				Iterable<ItemStack> armorItems = entityliving.getArmorSlots();
				ItemStack head = entityliving.getItemBySlot(EquipmentSlotType.HEAD);
				ItemStack chest = entityliving.getItemBySlot(EquipmentSlotType.CHEST);
				ItemStack legs = entityliving.getItemBySlot(EquipmentSlotType.LEGS);
				ItemStack feet = entityliving.getItemBySlot(EquipmentSlotType.FEET);

				if (armorItems instanceof List<ItemStack> armorItemList) {
					armorItemList.set(0, ItemStack.EMPTY);
					armorItemList.set(1, ItemStack.EMPTY);
					armorItemList.set(2, ItemStack.EMPTY);
					armorItemList.set(3, ItemStack.EMPTY);
					armorItemList.set(slot.getIndex(), itemstack);
				}

				MatrixStack ps = new MatrixStack();
				ps.translate(0, 0, 10000);

				boolean falsu = false;

				if (forgeHooksArmorModel instanceof BipedModel<?> humanoidModel && falsu) {
					//Setup default visibility
					switch (slot) {
						case FEET -> {
							humanoidModel.rightLeg.visible = true;
							humanoidModel.leftLeg.visible = true;
						}
						case LEGS -> {
							humanoidModel.body.visible = true;
							humanoidModel.rightLeg.visible = true;
							humanoidModel.leftLeg.visible = true;
						}
						case CHEST -> {
							humanoidModel.body.visible = true;
							humanoidModel.rightArm.visible = true;
							humanoidModel.leftArm.visible = true;
						}
						case HEAD -> {
							humanoidModel.head.visible = true;
							humanoidModel.hat.visible = true;
						}
						default -> {}
					}
				}

				//Render armor to get the visibility of each part
				originalRenderer.render(ps, Minecraft.getInstance().renderBuffers().bufferSource(), 0, entityliving, 0, 0, 0, 0, 0, 0);

				if (armorItems instanceof List<ItemStack> armorItemList) {
					armorItemList.set(0, feet);
					armorItemList.set(1, legs);
					armorItemList.set(2, chest);
					armorItemList.set(3, head);
				}

				animatedMesh = CustomModelBakery.bakeArmor(entityliving, itemstack, armorItem, slot, originalModel, forgeHooksArmorModel, originalRenderer.getParentModel(), this.mesh.get());
			}

			putModel(registryName, animatedMesh);

			return animatedMesh;
		}
	}

	private ResourceLocation getArmorTexture(ItemStack itemstack, LivingEntity entity, AnimatedMesh armorMesh, EquipmentSlotType slot, String type, M originalModel) {
		ArmorItem item = (ArmorItem) itemstack.getItem();
		String texture = item.getMaterial().getName();
		String domain = "minecraft";
		int idx = texture.indexOf(':');

		if (idx != -1) {
			domain = texture.substring(0, idx);
			texture = texture.substring(idx + 1);
		}

		String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (innerModel(slot) ? 2 : 1), type == null ? "" : String.format("_%s", type));
		s1 = ForgeHooksClient.getArmorTexture(entity, itemstack, s1, slot, type);
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

		AnimatedArmorTextureEvent animatedArmorTextureEvent = new AnimatedArmorTextureEvent(entity, itemstack, slot, originalModel);
		MinecraftForge.EVENT_BUS.post(animatedArmorTextureEvent);
		ResourceLocation extensionTexturePath = animatedArmorTextureEvent.getResultLocation();

		if (armorMesh.getRenderProperty() != null && armorMesh.getRenderProperty().getCustomTexturePath() != null) {
			s1 = armorMesh.getRenderProperty().getCustomTexturePath();
			extensionTexturePath = null;
		}

		if (extensionTexturePath != null) {
			return extensionTexturePath;
		}

		ResourceLocation resourcelocation = BipedArmorLayer.ARMOR_LOCATION_CACHE.get(s1);

		if (resourcelocation == null) {
			resourcelocation = new ResourceLocation(s1);
			BipedArmorLayer.ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
		}

		return resourcelocation;
	}

	private static boolean innerModel(EquipmentSlotType slot) {
		return slot == EquipmentSlotType.LEGS;
	}
}